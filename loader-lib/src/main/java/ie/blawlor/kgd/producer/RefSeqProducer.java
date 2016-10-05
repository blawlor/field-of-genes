package ie.blawlor.kgd.producer;

import net.sf.jfasta.FASTAElement;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONArray;
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
    private final String entryRange;
    private final KafkaProducer<String, String> kafkaProducer;
    private static final int BUFFER_SIZE = 4096;
    private static final String DOWNLOADS_DIR = "downloads";
    private static final String DATABASES_ROOT_DIR = "databases";
    private static final int MAX_RECORD_SIZE = 100000;

    public RefSeqProducer(String dbDescription, KafkaProducer<String, String> kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
        final JSONObject jsonObject = new JSONObject(dbDescription);
        id = jsonObject.getString("id");
        urlRoot = jsonObject.getString("url-root");
        fileTemplate = jsonObject.getString("file-template");
        entryRange = jsonObject.getString("entry-range");
    }

    public String getId() {
        return id;
    }

    public void close(){
        kafkaProducer.close();
    }

    public void loadDB(String topic) throws IOException {
        List<RemoteDatabase> dbs = prepareDatabases(urlRoot, fileTemplate, entryRange);
        for(RemoteDatabase db: dbs){
            System.out.println("Downloading " + db.getUrl());
            downloadFtpUrl(db.getUrl(), db.getRootName());
            File fastaFile = new File(DATABASES_ROOT_DIR + "/" +db.getRootName()+".fasta");
            sendFastaFile(fastaFile, topic);
        }
    }

    private static List<RemoteDatabase> prepareDatabases(String urlBase, String fileTemplate, String entryRange){
        String[] startStop = entryRange.split("-");
        int startIndex = Integer.valueOf(startStop[0]);
        int stopIndex = Integer.valueOf(startStop[1]);
        List<RemoteDatabase> databases = new ArrayList<>();
        for (int index = startIndex; index <= stopIndex; index++){
            String rootName = generateRootFileName(fileTemplate, index);
            String url = generateUrl(urlBase, fileTemplate, index);
            databases.add(new RemoteDatabase(rootName, url));
        }
        return databases;
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
        inputStream.close();
        outputStream.close();
        System.out.println("FTP Download complete");
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

    public void sendFastaFile(File fastaFile, String topic) throws IOException {
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

    protected static List<KeySequence> breakDownSequence(FASTAElement element){
        String keyRoot = element.getHeader();
        String sequence = element.getSequence();
        List<KeySequence> result = new LinkedList<>();
        int sequenceLength = sequence.length();
        int segmentIndex = 0;
        int start = 0;
        int end = start + MAX_RECORD_SIZE;
        while (end < sequenceLength){
            result.add(new KeySequence(createSequenceKeyFromRoot(keyRoot, segmentIndex),
                    sequence.substring(start, end)));
            start = start + MAX_RECORD_SIZE;
            end = start + MAX_RECORD_SIZE;
            segmentIndex++;
        }
        // Catch the overshoot.
        if (start < sequenceLength) {
            result.add(new KeySequence(createSequenceKeyFromRoot(keyRoot, segmentIndex),
                    sequence.substring(start, sequenceLength)));
        }
        return result;
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

    private String createKey(String fastaHeader){
        String[] elements = fastaHeader.split("|");
        return elements[3];
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

    private static String generateSourceFileName(String fileTemplate, int index){
        String prefix = index < 10?"0":"";
        return fileTemplate.replace("NNN", prefix+index);
    }

    private static String generateRootFileName(String fileTemplace, int index){
        return generateSourceFileName(fileTemplace, index).replace(".tar.gz", "");
    }

    private static String generateBaseFileName(String rootFileName){
        int index = rootFileName.lastIndexOf('.');
        return rootFileName.substring(0, index);
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

    static class KeySequence {
        private final String key;
        private final String sequence;

        public KeySequence(String key, String sequence) {
            this.key = key;
            this.sequence = sequence;
        }

        public String getKey() {
            return key;
        }

        public String getSequence() {
            return sequence;
        }
    }
}
