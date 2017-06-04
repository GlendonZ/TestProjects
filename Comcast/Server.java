import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Properties;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Run this class first to set up the server.
 */
public class Server {

    public static String propertiesFile = System.getProperty("user.dir") + "\\ad.properties";

    /**
     * Starts up the server on a localhost address and registers the HttpHandler.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8090), 0);
        server.createContext("/ad", new AdHandler());
        server.start();
    }

    /**
     * For the sake of simplicity this method will parse the incoming JSON and handle it. Typically this should be
     * done by a business layer class.
     */
    static class AdHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String method   = t.getRequestMethod();

            // Since both URIs are the same, this checks what type of request it is.
            if (method.equals("POST")) {
                InputStream is          = t.getRequestBody();
                Charset ch              = null;
                String jsonContent      = null;
                try {
                    jsonContent = IOUtils.toString(is, ch);
                } catch (IOException e) {
                    writeResponse(t, e.toString(), 200);
                }

                // I am storing data in a local properties file. If it does not exist it gets created.
                File file               = new File(propertiesFile);
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        writeResponse(t, e.toString(), 200);
                    }
                }

                // Parses the incoming JSON object.
                JSONParser jsonParser   = new JSONParser();
                try {
                    JSONObject json         = (JSONObject) jsonParser.parse(jsonContent);
                    Long currentTime        = System.currentTimeMillis() / 1000;
                    String partner          = json.get("partner_id").toString();
                    Long duration           = Long.valueOf(json.get("duration").toString());
                    String adContent        = json.get("ad_content").toString();

                    Properties properties   = new Properties();
                    FileInputStream ioIn    = new FileInputStream(propertiesFile);
                    properties.load(ioIn);

                    // If the partner already exists in the file then a response is noted as such.
                    if (properties.get(partner) != null) {
                        Long curDuration = Long.valueOf(properties.get(partner + ".duration").toString());
                        Long curCreateTime = Long.valueOf(properties.get(partner + ".createTime").toString());

                        if (curCreateTime + curDuration < currentTime) {
                            writeResponse(t, "No active ad campaigns exist for the specified partner.", 200);
                        } else {
                            writeResponse(t, "Only one active campaign can exist for a given partner.", 200);
                        }
                        return;
                    }

                    // Write out the incoming data if it is new to the properties file.
                    FileOutputStream output = new FileOutputStream(propertiesFile);
                    properties.setProperty(partner, partner);
                    properties.setProperty(partner + ".duration", duration.toString());
                    properties.setProperty(partner + ".createTime", currentTime.toString());
                    properties.setProperty(partner + ".adContent", adContent);
                    properties.store(output, null);
                    output.close();

                } catch (ParseException e) {
                    writeResponse(t, e.toString(), 200);
                    return;
                } catch (FileNotFoundException e) {
                    writeResponse(t, e.toString(), 200);
                } catch (IOException e) {
                    writeResponse(t, e.toString(), 200);
                }

                // Tell the POSTer that the info was received and stored.
                writeResponse(t, "We received: " + jsonContent + ". and have saved it.", 201);

            } else if (method.equals("GET")) {
                String partner          = t.getRequestURI().toString().replace("/ad/","");

                Properties properties   = new Properties();
                FileInputStream ioIn    = new FileInputStream(propertiesFile);
                properties.load(ioIn);

                // Retrieve the ad properties if they exist, else return a message stating they don't.
                if (properties.get(partner) != null) {
                    Long curDuration    = Long.valueOf(properties.get(partner + ".duration").toString());
                    Long curCreateTime  = Long.valueOf(properties.get(partner + ".createTime").toString());
                    String curAd        = properties.get(partner + ".adContent").toString();
                    Long currentTime        = System.currentTimeMillis() / 1000;

                    if (curCreateTime + curDuration < currentTime) {
                        writeResponse(t, "No active ad campaigns exist for the specified partner.", 200);
                    } else {
                        JSONObject json = new JSONObject();
                        json.put("partner_id", partner);
                        json.put("duration", curDuration);
                        json.put("createTime", curCreateTime);
                        json.put("ad_content", curAd);
                        writeResponse(t, json.toString(), 200);
                    }
                } else {
                    writeResponse(t, "No ad exists in the database currently.", 200);
                }
            }
        }
    }

    /**
     * Handle writing out a response to the stream.
     * @param t             HttpExchange object.
     * @param response      Message to return.
     * @param code          Code to return.
     * @throws IOException
     */
    private static void writeResponse(HttpExchange t, String response, Integer code) throws IOException {
        t.sendResponseHeaders(code, response.length());
        OutputStream os = t.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}