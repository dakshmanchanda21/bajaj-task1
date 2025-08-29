package com.example.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class StartupTask {

    private final RestTemplate restTemplate;

    public StartupTask() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory()); // ✅ FIX
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("regNo", "22BEC0421");
        requestBody.put("email", "john@example.com");

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, requestBody, Map.class);

            String webhook = (String) response.getBody().get("webhook");
            String accessToken = (String) response.getBody().get("accessToken");

            System.out.println("Webhook URL: " + webhook);
            System.out.println("Access Token: " + accessToken);

            String finalQuery = "SELECT patient_id FROM visits WHERE visit_date BETWEEN '2022-01-01' AND '2022-03-31' GROUP BY patient_id HAVING COUNT(DISTINCT department_id) = 3";

            Map<String, String> answer = new HashMap<>();
            answer.put("finalQuery", finalQuery);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "Bearer " + accessToken);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(answer);
            System.out.println("JSON Body being sent: " + jsonBody);

            HttpEntity<String> httpEntity = new HttpEntity<>(jsonBody, headers);

            System.out.println("Sending final query...");
            ResponseEntity<String> result = restTemplate.postForEntity(webhook, httpEntity, String.class);

            System.out.println("Submission Response: " + result.getStatusCode() + " → " + result.getBody());

        } catch (Exception e) {
            System.err.println("Error during submission:");
            e.printStackTrace();
        }
    }
}
