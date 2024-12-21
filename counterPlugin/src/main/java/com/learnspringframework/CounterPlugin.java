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
    private String token = "bd585e14e8b29ac3c181db20e5734276866ae6b8"; // Hardcoded token (Security issue)

    // Unused variable (Code Smell)
    private String unusedVariable = "This is an unused variable.";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            getLog().info("Starting SonarCloud Code Analysis...");

            // Intentionally making the method too large and unreadable (Maintainability Issue)
            performLargeTask();

            Map<String, String> analysisResults = fetchSonarCloudIssues();
            logIssues(analysisResults);
        } catch (Exception e) {
            throw new MojoExecutionException("Error during SonarCloud analysis", e);
        }
    }

    // Large method (Maintainability Issue)
    private void performLargeTask() {
        String result = "";
        for (int i = 0; i < 1000; i++) {
            result += "Some large result accumulation step " + i + "\n";
        }
        getLog().info("Performed large task");
    }

    private Map<String, String> fetchSonarCloudIssues() throws Exception {
        getLog().info("Fetching SonarCloud issues...");

        // Construct the API URL with necessary parameters
        String apiUrl = "https://sonarcloud.io/api/issues/search?componentKeys=" + projectKey + "&resolved=false";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + token);

        int responseCode = connection.getResponseCode();
        getLog().info("Response Code: " + responseCode);

        if (responseCode != 200) {
            // If response code is not 200, read and log the error response
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            StringBuilder errorResponse = new StringBuilder();
            String errorLine;
            while ((errorLine = errorReader.readLine()) != null) {
                errorResponse.append(errorLine);
            }
            errorReader.close();
            getLog().error("Error response: " + errorResponse.toString());
            throw new Exception("Failed to fetch issues. Response code: " + responseCode + ". URL: " + apiUrl);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        // Log raw response for debugging
        getLog().info("Response: " + response.toString());

        return parseSonarCloudIssuesResponse(response.toString());
    }


    private Map<String, String> parseSonarCloudIssuesResponse(String jsonResponse) throws Exception {
        Map<String, String> issues = new HashMap<>();

        // Parse the JSON to extract issues
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(jsonResponse);
        com.fasterxml.jackson.databind.JsonNode issuesNode = rootNode.get("issues");

        if (issuesNode != null) {
            for (com.fasterxml.jackson.databind.JsonNode issue : issuesNode) {
                String severity = issue.get("severity").asText();
                String message = issue.get("message").asText();
                String rule = issue.get("rule").asText();
                String type = issue.get("type").asText();
                issues.put("Severity: " + severity + " | Rule: " + rule + " | Type: " + type, message);
            }
        }
        return issues;
    }

    private void logIssues(Map<String, String> issues) {
        if (issues == null || issues.isEmpty()) {
            getLog().info("No issues found.");
        } else {
            getLog().warn("Issues detected:");
            issues.forEach((key, message) -> {
                getLog().warn(key + " - " + message);
            });
        }
    }
}
