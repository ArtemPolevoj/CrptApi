package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CrptApi {
    private final HttpClient httpClient;
    private final int requestLimit;
    private final long interval;
    private long lastRequestTime;
    private int requestCount;

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.requestLimit = requestLimit;
        this.interval = timeUnit.toMillis(1);
        this.lastRequestTime = System.currentTimeMillis();
        this.requestCount = 0;
    }

    public void createDocument(Object document, String signature) {
        synchronized (this) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastRequestTime >= interval) {
                requestCount = 0;
                lastRequestTime = currentTime;
            }

            if (requestCount >= requestLimit) {
                try {
                    wait(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Convert document to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonDocument = null;
            try {
                jsonDocument = objectMapper.writeValueAsString(document);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Make the API call
            if (jsonDocument != null) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://ismp.crpt.ru/api/v3/lk/documents/create"))
                        .POST(HttpRequest.BodyPublishers.ofString(jsonDocument))
                        .header("Content-Type", "application/json")
                        .header("Signature", signature)
                        .build();

                try {
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println(response.body());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            requestCount++;
            notifyAll();
        }
    }
}
