package pe.edu.utec.marketing.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.springframework.stereotype.Component;


@Component
public class ImagenClient {

    private final HttpClient http =
        HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();


    public void generar(
            String prompt,
            int semilla,
            Path destino
    ) throws Exception {

        String corto = prompt.length() > 300 ? prompt.substring(0, 300) : prompt;

        String url = "https://image.pollinations.ai/prompt/"
            + URLEncoder.encode(corto, StandardCharsets.UTF_8).replace("+", "%20")
            + "?width=1080&height=1350&nologo=true&seed=" + semilla;

        String error = "";

        long espera = 12000;

        for (int intento = 0; intento < 30; intento++) {

            HttpRequest request =
                HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(120))
                    .GET()
                    .build();

            try {

                HttpResponse<byte[]> response =
                    http.send(request, HttpResponse.BodyHandlers.ofByteArray());

                String tipo =
                    response.headers().firstValue("content-type").orElse("");

                if (response.statusCode() == 200 && tipo.startsWith("image/")) {

                    Files.write(destino, response.body());

                    return;
                }

                error = "respondio " + response.statusCode();

                espera = response.statusCode() == 402 || response.statusCode() == 429
                    ? 46000
                    : 12000;

            } catch (java.io.IOException e) {

                error = e.getMessage();
            }

            Thread.sleep(espera);
        }

        throw new IllegalStateException(
            "No se pudo generar la imagen: " + error
        );
    }
}
