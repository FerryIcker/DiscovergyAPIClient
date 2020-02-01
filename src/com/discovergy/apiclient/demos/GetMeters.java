package com.discovergy.apiclient.demos;

import com.discovergy.apiclient.DiscovergyApiClient;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Verb;

//import java.io.BufferedReader;
//import java.io.DataOutputStream;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.HttpURLConnection;
//import java.net.MalformedURLException;
//import java.net.ProtocolException;
//import java.net.URL;
//import java.nio.charset.StandardCharsets;
//import java.util.Timer;
//import java.util.TimerTask;
//import org.json.*;

public class GetMeters {
    private static DiscovergyApiClient apiClient;
    private static OAuthRequest request;

    public static void init(String User, String Password) throws Exception {
        apiClient = new DiscovergyApiClient("exampleApiClient", User, Password);
    };

    public static String paramUser = "";
    public static String paramPassword = "";
    public static String HMIpAdresse = "";
    public static String MeterId = "";
    public static String Intervall = "";

    public static void main(String[] args) throws Exception {

        if (args.length < 2) {
            System.exit(-1);
        } else {

            paramUser = args[0];
            paramPassword = args[1];

        };
        System.out.println("Init Parameters");
        init(paramUser, paramPassword);
        request = apiClient.createRequest(Verb.GET, "/meters");
        request.addHeader("Content-Type", "application/json");
        String JsonStr = apiClient.executeRequest(request, 200).getBody();
        System.out.println(JsonStr);
    }
}
