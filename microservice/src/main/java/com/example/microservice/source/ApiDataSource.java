package com.example.microservice.source;

import com.example.microservice.source.interfaces.IDataSource;
import jakarta.persistence.*;
import lombok.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Entity
@DiscriminatorValue("API")
@NoArgsConstructor
@Getter
@Setter
public class ApiDataSource extends BaseDataSource implements IDataSource {
    private String apiUrl;

    @Transient
    private HttpClient client;

    public ApiDataSource(String apiUrl) {
        this.apiUrl = apiUrl;
        this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    @PostLoad
    private void initClient() {
        if (client == null) {
            this.client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        }
    }

    @Override
    public List<String> fetchData() {
        if (apiUrl == null || apiUrl.isBlank()) return Collections.emptyList();
        if (client == null) {
            initClient();
        }
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).timeout(Duration.ofSeconds(10)).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                String body = response.body();
                if (body == null || body.isBlank()) return Collections.emptyList();
                return Arrays.asList(body.split("\\r?\\n"));
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            return Collections.emptyList();
        }
        return Collections.emptyList();
    }
}

