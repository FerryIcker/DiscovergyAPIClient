package com.discovergy.apiclient.application;

import com.discovergy.apiclient.DiscovergyApiClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.Timestamp;
import java.util.Timer;
import java.util.TimerTask;
import org.json.*;
import java.sql.*;
import java.text.DecimalFormat;

public class MeinProg {

    private static HttpURLConnection con;

    public static void SetPower(String IpAdress, long iPower, long iPower1, long iPower2, long iPower3, long iEnergyOut, long iEnergy)
            throws MalformedURLException, ProtocolException, IOException {

        String url = "http://" + IpAdress + ":8181/tclrega.exe";
        String urlParameters =          "Write(dom.GetObject(\"iPower\").State(\"" + Long.toString(iPower)+ "\"));";
        urlParameters = urlParameters + "Write(dom.GetObject(\"iPower1\").State(\"" + Long.toString(iPower1)+ "\"));";
        urlParameters = urlParameters + "Write(dom.GetObject(\"iPower2\").State(\"" + Long.toString(iPower2)+ "\"));";
        urlParameters = urlParameters + "Write(dom.GetObject(\"iPower3\").State(\"" + Long.toString(iPower3)+ "\"));";
        urlParameters = urlParameters + "Write(dom.GetObject(\"iEnergyOut\").State(\"" + Long.toString(iEnergyOut)+ "\"));";
        urlParameters = urlParameters + "Write(dom.GetObject(\"iEenergy\").State(\"" + Long.toString(iEnergy)+ "\"));";
        //System.out.println(urlParameters);
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);

        try {

            URL myurl = new URL(url);
            con = (HttpURLConnection) myurl.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "Java client");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.write(postData);
            } catch(Exception e) {
                e.printStackTrace();
            }

            StringBuilder content;

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {

                String line;
                content = new StringBuilder();

                while ((line = in.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
            } catch(Exception e) {
                e.printStackTrace();
            }

//			 System.out.println(content.toString());

        } catch(Exception e) {
            e.printStackTrace();
            counter = 0;
        }

        finally {

            con.disconnect();
        }

    }

    private static DiscovergyApiClient apiClient;
    private static OAuthRequest request;

    public static void init(String User, String Password) throws Exception {
        apiClient = new DiscovergyApiClient("exampleApiClient", User, Password);
    };

    public static int counter = 0;

    public static void query(String HMIp, String MeterId) throws Exception {

        try {

            counter = counter + 1;
            if(counter >= 10) {
                request = apiClient.createRequest(Verb.GET, "/last_reading");
                request.addHeader("Content-Type", "application/json");
                request.addParameter("meterId", MeterId);
                request.addParameter("fields", "energy,power,power1,power2,power3,energyOut");
                String JsonStr = apiClient.executeRequest(request, 200).getBody();
			System.out.println(JsonStr);

                JSONObject obj = new JSONObject(JsonStr);
                long zeitpunkt = obj.getLong("time");
                //System.out.println(zeitpunkt);

                long timeStamp = zeitpunkt;
                java.sql.Timestamp stamp = new java.sql.Timestamp(timeStamp);
                //System.out.println(zeitpunkt+" "+stamp);

                long power = obj.getJSONObject("values").getLong("power");
                //System.out.println(zeitpunkt+" power: "+ power);
                //System.out.println(stamp+" power: "+ power);

                long power1 = obj.getJSONObject("values").getLong("power1");
//			System.out.println("power1: "+ power1);

                long power2 = obj.getJSONObject("values").getLong("power2");
//			System.out.println("power2: "+ power2);

                long power3 = obj.getJSONObject("values").getLong("power3");
//			System.out.println("power3: "+ power3);

                long energyOut = obj.getJSONObject("values").getLong("energyOut");
                energyOut = energyOut / 10000000; // Umrechnung in Wh
//			System.out.println("energyOut: "+ energyOut);

                long energy = obj.getJSONObject("values").getLong("energy");
                energy = energy / 10000000; // Umrechnung in Wh
//			System.out.println("energy: "+ energy);

                //
//			long energyProducer8 = obj.getJSONObject("values").getLong("energyProducer8");
//			System.out.println("energyProducer8: "+ energyProducer8);
//
//			long energyProducer9 = obj.getJSONObject("values").getLong("energyProducer9");
//			System.out.println("energyProducer9: "+ energyProducer9);
//
//			long energyProducer10 = obj.getJSONObject("values").getLong("energyProducer10");
                //System.out.println("energyProducer10: "+ energyProducer10);
//
                SetPower(HMIp, power,power1,power2,power3,energyOut,energy);

            }


        } catch (Exception e) {
            e.printStackTrace();
            counter = 0;
            init(paramUser, paramPassword);
        }

    };

    public static String paramUser = "";
    public static String paramPassword = "";
    public static String HMIpAdresse = "";
    public static String MeterId = "";
    public static String Intervall = "";



    public static void main(String[] args) throws Exception {

        if (args.length < 5) {
            System.exit(-1);
        } else {

            paramUser = args[0];
            paramPassword = args[1];
            MeterId = args[2];
            HMIpAdresse = args[3];
            Intervall = args[4];
        }
        ;
        counter=60;// Sofort starten
        init(paramUser, paramPassword);
        TimerTask action = new TimerTask() {
            public void run() {
                try {

                    query(HMIpAdresse, MeterId);

                } catch (Exception e) {
                    e.printStackTrace();
                    counter = 0;
                }
            }
        };
        Timer caretaker = new Timer();
        int intIntervall = Integer.parseInt(Intervall);
        caretaker.schedule(action, 1000, intIntervall);

    }
}
