import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Run this class second to interact with the server.
 */
public class AdCampaign {

    private static final String USER_AGENT = "Chrome/41.0.2228.0";

    /**
     * This POSTs a campaign and then GETs it back again.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
         sendPost();
         sendGet();
    }

    /**
     * Has a hardcoded JSON post string to send to the ad server.
     * @throws Exception
     */
    private static void sendPost() throws Exception {

        String urlAddress               = "http://localhost:8090/ad";
        URL url                         = new URL(urlAddress);
        HttpURLConnection connection    = (HttpURLConnection) url.openConnection();

        // Add request header
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Data to POST
        String postData                 = "{\"partner_id\":\"testPartner\",\"duration\":\"100\",\"ad_content\":\"string_of_content_to_display_as_ad\"}";

        // Send post request
        connection.setDoOutput(true);
        DataOutputStream wr             = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(postData);
        wr.flush();
        wr.close();

        // Get server response
        int responseCode                = connection.getResponseCode();
        String responseMsg              = connection.getResponseMessage();
        System.out.println("\nSending 'POST' request to URL : " + urlAddress);
        System.out.println("Post parameters : " + postData);
        System.out.println("Response Code : " + responseCode);
        System.out.println("Response Message : " + responseMsg);

        BufferedReader in               = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response           = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
    }

    /**
     * Creates the GET connection to the server and response. Has a hardcoded campaign name.
     * @throws IOException
     */
    private static void sendGet() throws IOException {
        String campaignName             = "testPartner";
        String urlAddress               = "http://localhost:8090/ad/" + campaignName;
        URL url                         = new URL(urlAddress);
        HttpURLConnection connection    = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        int responseCode                = connection.getResponseCode();
        String responseMsg              = connection.getResponseMessage();
        System.out.println("\nSending 'GET' request to URL : " + urlAddress);
        System.out.println("Get parameters : " + campaignName);
        System.out.println("Response Code : " + responseCode);
        System.out.println("Response Message : " + responseMsg);

        BufferedReader in               = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response           = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(response.toString());
    }
}
