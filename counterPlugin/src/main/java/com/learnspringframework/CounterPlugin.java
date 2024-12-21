package com.learnspringframework;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Mojo(name = "counter", defaultPhase = LifecyclePhase.VERIFY)
public class CounterPlugin extends AbstractMojo {

    // Hardcoded SonarQube values
    private String projectKey = "J-B-Mugundh:maven-plugin";
    private String token = "bd585e14e8b29ac3c181db20e5734276866ae6b8";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Starting SonarCloud Code Analysis...");

            Map<String, String> analysisResults = fetchSonarCloudVulnerabilities();

            logVulnerabilities(analysisResults);
        } catch (Exception e) {
            throw new MojoExecutionException("Error during SonarCloud analysis", e);
        }
    }

    private Map<String, String> fetchSonarCloudVulnerabilities() throws Exception {
        getLog().info("Fetching SonarCloud vulnerabilities...");

        String apiUrl = "https://sonarcloud.io/api/issues/search?componentKeys=" + projectKey + "&types=VULNERABILITY";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to fetch vulnerabilities. Response code: " + responseCode);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return parseSonarCloudVulnerabilitiesResponse(response.toString());
    }

    private Map<String, String> parseSonarCloudVulnerabilitiesResponse(String jsonResponse) throws Exception {
        Map<String, String> vulnerabilities = new HashMap<>();

        // Parse the JSON to extract vulnerabilities
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(jsonResponse);
        com.fasterxml.jackson.databind.JsonNode issuesNode = rootNode.get("issues");

        if (issuesNode != null) {
            for (com.fasterxml.jackson.databind.JsonNode issue : issuesNode) {
                String severity = issue.get("severity").asText();
                String message = issue.get("message").asText();
                vulnerabilities.put(severity, message);
            }
        }
        return vulnerabilities;
    }

    private void logVulnerabilities(Map<String, String> vulnerabilities) {
        if (vulnerabilities.isEmpty()) {
            getLog().info("No vulnerabilities found.");
        } else {
            getLog().warn("Vulnerabilities detected:");
            vulnerabilities.forEach((severity, message) -> {
                getLog().warn(" [" + severity + "] " + message);
            });
        }
    }
}
