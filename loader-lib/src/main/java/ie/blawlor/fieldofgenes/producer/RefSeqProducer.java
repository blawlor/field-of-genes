package ie.blawlor.fieldofgenes.producer;

import net.sf.jfasta.FASTAElement;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RefSeqProducer {

    private final String id;
    private final String urlRoot;
    private final String fileTemplate;
    private final String entryNumber;
    private final KafkaProducer<String, String> kafkaProducer;
    private static final int BUFFER_SIZE = 4096;
    private static final String DOWNLOADS_DIR = "downloads";
    public static final String DATABASES_ROOT_DIR = "databases";
    private static final int MAX_RECORD_SIZE = 100000;

    public RefSeqProducer(String dbDescription, KafkaProducer<String, String> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        final JSONObject jsonObject = new JSONObject(dbDescription);
        id = jsonObject.getString("id");
        urlRoot = jsonObject.getString("url-root");
        fileTemplate = jsonObject.getString("file-template");
        entryNumber = jsonObject.getString("entry-range");
    }

    public RefSeqProducer(String dbDescription) {
        this.kafkaProducer = null;
        final JSONObject jsonObject = new JSONObject(dbDescription);
        id = jsonObject.getString("id");
        urlRoot = jsonObject.getString("url-root");
        fileTemplate = jsonObject.getString("file-template");
        entryNumber = jsonObject.getString("entry-number");
    }

    public String getId() {
        return id;
    }

    public void close(){
        kafkaProducer.close();
    }

    public File downloadDB() throws IOException {
        RemoteDatabase db = prepareDatabase(urlRoot, fileTemplate, entryNumber);
        System.out.println("Downloading " + db.getUrl());
        downloadFtpUrl(db.getUrl(), db.getRootName());
        return new File(DATABASES_ROOT_DIR + "/" +db.getRootName()+".fasta");
    }

    private static RemoteDatabase prepareDatabase(String urlBase, String fileTemplate, String entryNumber){
        int index = Integer.valueOf(entryNumber);
        String rootName = generateRootFileName(fileTemplate, index);
        String url = generateUrl(urlBase, fileTemplate, index);
        return new RemoteDatabase(rootName, url);
    }

    private void downloadFtpUrl(String ftpUrl, String rootFileName) {
        try {
            download(ftpUrl, rootFileName+".tar.gz");
            unzip(rootFileName);
            untar(rootFileName);
            invokeBlastDbCmd(rootFileName);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void download(String ftpUrl, String destinationFileName) throws IOException {
        URL url = new URL(ftpUrl);
        URLConnection conn = url.openConnection();
        InputStream inputStream = conn.getInputStream();
        //Download gz file and unzip it.
        File outputFile = new File(DOWNLOADS_DIR+"/"+destinationFileName);
        outputFile.getParentFile().mkdirs();
        FileOutputStream outputStream = new FileOutputStream(outputFile);
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
        System.out.println("FTP Download complete");
        inputStream.close();
        outputStream.close();
    }

    private void unzip(String rootFileName) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        File gzFile = new File(DOWNLOADS_DIR+"/"+rootFileName+".tar.gz");
        GzipCompressorInputStream gzIn = new GzipCompressorInputStream(new FileInputStream(gzFile));
        FileOutputStream out = new FileOutputStream(DOWNLOADS_DIR+"/"+rootFileName+".tar");
        int n = 0;
        while (-1 != (n = gzIn.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.close();
        gzIn.close();
        gzFile.delete();
        System.out.println("Unzipping complete");
    }

    private static void untar(String rootFileName) throws IOException, InterruptedException {
        File destDir = new File(DATABASES_ROOT_DIR + "/" + generateBaseFileName(rootFileName));
        destDir.mkdirs();
        String tarfileName = DOWNLOADS_DIR + "/" + rootFileName + ".tar";
        ProcessBuilder processBuilder = new ProcessBuilder(
           "tar", "-xf", tarfileName, "-C", destDir.getAbsolutePath())
            .inheritIO();
        int result = processBuilder.start().waitFor();
        if (result == 0) {
            System.out.println("Untarring complete");
            new File(tarfileName).delete();
        } else {
            System.out.println("Untarring failed");
        }
    }

    private static void invokeBlastDbCmd(String dbName) throws IOException, InterruptedException {
        String databaseDirectory = DATABASES_ROOT_DIR + "/" +
                generateBaseFileName(dbName);
        String fastaFileName = dbName+".fasta";
        ProcessBuilder processBuilder = new ProcessBuilder("blastdbcmd",
                "-db", databaseDirectory+"/"+dbName,
                "-entry", "all",
                "-out", DATABASES_ROOT_DIR +"/"+fastaFileName).inheritIO();
        int result = processBuilder.start().waitFor();
        if (result == 0) {
            System.out.println("Blastdbcmd complete. Results in " + DATABASES_ROOT_DIR + "/" + fastaFileName);
            cleanDirectory(databaseDirectory);
        } else {
            System.out.println("Blastdbcmd failed.");
        }
    }

    public void writeFastaToTopic(File fastaFile, String topic) throws IOException {
        System.out.println("Sending file " + fastaFile);
        fastaFile.getParentFile().mkdirs();
        FileInputStream fis = new FileInputStream(fastaFile);

        BufferedReader br = new BufferedReader(new InputStreamReader(fis));

        String line = br.readLine();
        boolean fileNotComplete = true;
        String keyRoot = "";
        while(fileNotComplete) {
            if (line != null && line.startsWith(">")) {
                keyRoot = line;
                line = br.readLine();
            } else if (!keyRoot.isEmpty()) {
                boolean sequenceNotComplete = true;
                int subsequenceNumber = 0;
                while (sequenceNotComplete) {
                    // Iterate here until we end the file or find a new line with key
                    String key = createSequenceKeyFromRoot(keyRoot, subsequenceNumber);
                    StringBuilder currentSequence = new StringBuilder();
                    while ((line = br.readLine()) != null &&
                            !line.startsWith(">") &&
                            currentSequence.length() < (MAX_RECORD_SIZE - line.length())) {
                        currentSequence.append(line);
                    }
                    if (line == null) {
                        sequenceNotComplete = false;
                        fileNotComplete = false;
                    } else if (line.startsWith(">")) {
                        sequenceNotComplete = false;
                        fileNotComplete = true;
                    } else { //Sequence longer than max.
                        sequenceNotComplete = true;
                        fileNotComplete = true;
                        subsequenceNumber++;
                        // Don't want to lose last line
                        currentSequence.append(line);
                    }
                    ProducerRecord<String, String> producerRecord =
                            new ProducerRecord<>(topic,
                                    key,
                                    currentSequence.toString());
                    kafkaProducer.send(producerRecord);
                }
            } else if (line == null){
                fileNotComplete = false;
            } else {
                //Keep going to next key (at start)
                line = br.readLine();
            }
        }

        br.close();
        fastaFile.delete();
    }

    private static String createSequenceKeyFromRoot(String keyRoot, int index){
        String[] keyElements = keyRoot.split("\\|");
        StringBuilder builder = new StringBuilder();

        int segmentIndex = 0;
        for (String string : keyElements) {
            if (builder.length() > 0) {
                builder.append("|");
            }
            if (segmentIndex == 1){
                builder.append(string+"-"+index);
            } else {
                builder.append(string);
            }
            segmentIndex++;
        }
        return builder.toString();

    }

    private static void cleanDirectory(String directory){
        File dir = new File(directory);
        for(File file: dir.listFiles())
            if (!file.isDirectory())
                file.delete();
    }

    private static String generateUrl(String urlBase, String fileTemplate, int index){
        return urlBase + generateSourceFileName(fileTemplate, index);
    }

    public static String generateSourceFileName(String fileTemplate, int index){
        String prefix = index < 10?"0":"";
        return fileTemplate.replace("NNN", prefix+index);
    }

    private static String generateRootFileName(String fileTemplace, int index){
        return generateSourceFileName(fileTemplace, index).replace(".tar.gz", "");
    }

    private static String generateBaseFileName(String rootFileName){
        return rootFileName;
    }

    private static class RemoteDatabase {
        private final String rootName;
        private final String url;

        public RemoteDatabase(String rootName, String url) {
            this.rootName = rootName;
            this.url = url;
        }

        public String getRootName() {
            return rootName;
        }

        public String getUrl() {
            return url;
        }
    }

}
