package com.example.javafxwordle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class FreeDictionaryAPIIntegration {

    private static final String API_URL = "https://api.dictionaryapi.dev/api/v2/entries/en/";

    public static String getWordDefinition(String word) {
        try {
            // Build the request URL
            String url = API_URL + word.toLowerCase();

            // Create an HTTP client
            HttpClient client = HttpClient.newHttpClient();

            // Create the request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            // Send the request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Handle the response
            if (response.statusCode() == 200) {
                JSONArray jsonResponse = new JSONArray(response.body());

                // Extract the first definition from the response
                JSONObject firstEntry = jsonResponse.getJSONObject(0);
                JSONArray meanings = firstEntry.getJSONArray("meanings");
                JSONObject firstMeaning = meanings.getJSONObject(0);
                JSONArray definitions = firstMeaning.getJSONArray("definitions");
                JSONObject firstDefinition = definitions.getJSONObject(0);

                // Return the definition text
                return firstDefinition.getString("definition");
            } else {
                return "Error: Received status code " + response.statusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while fetching the word definition.";
        }
    }


}
