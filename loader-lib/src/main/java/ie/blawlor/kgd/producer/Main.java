package ie.blawlor.kgd.producer;

import fi.iki.elonen.NanoHTTPD;
import ie.blawlor.kgd.RefSeqLoader;

import java.io.IOException;
import java.util.Map;

public class Main extends NanoHTTPD {

    private final String kafkaServer;

    public static void main(String[] args) throws Exception {
        try {
            new Main(args[0]);
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
        }
    }

    public Main(String kafkaServer) throws IOException {
        super(8080);
        this.kafkaServer = kafkaServer;
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nRunning! Point your browsers to http://localhost:8080/ \n");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><body><h1>Hello</h1>\n";
        Map<String, String> parms = session.getParms();


        if (parms.get("topic") == null) {
            msg += "<form action='?' method='get'>\n  " +
                    "<p>Id: <input type='text' name='id'></p>\n" +
                    "<p>Topic: <input type='text' name='topic'></p>\n" +
                    "<p>Range: <input type='text' name='range'></p>\n" +
                    "<input type=\"submit\" value=\"Go\">\n" +
                    "</form>\n";
        } else {
            String id = parms.get("id");
            String topic = parms.get("topic");
            String range = parms.get("range");
            String instruction = RefSeqLoader.recreateDBDescription(id, range);
            String loadResult = RefSeqLoader.load(instruction, topic, kafkaServer);
            msg += "<p>" + loadResult + "</p>";
        }
        return newFixedLengthResponse(msg + "</body></html>\n");
    }


}
