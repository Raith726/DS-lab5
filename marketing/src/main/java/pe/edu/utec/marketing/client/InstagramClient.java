package pe.edu.utec.marketing.client;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;


@Component
public class InstagramClient {

    private static final String API = "https://graph.facebook.com/v23.0/";

    private final ObjectMapper mapper;

    private final String userId;

    private final String token;

    private final HttpClient http = HttpClient.newHttpClient();


    public InstagramClient(
            ObjectMapper mapper,
            @Value("${instagram.user-id}") String userId,
            @Value("${instagram.token}") String token
    ) {
        this.mapper = mapper;
        this.userId = userId;
        this.token = token;
    }


    public boolean configurado() {

        return !userId.isBlank() && !token.isBlank();
    }


    public String publicarCarrusel(
            List<String> urlsFotos,
            String caption
    ) throws Exception {

        List<String> hijos = new ArrayList<>();

        for (String url : urlsFotos) {

            Map<String, String> datos = new LinkedHashMap<>();
            datos.put("image_url", url);
            datos.put("is_carousel_item", "true");

            String hijo = post(userId + "/media", datos).path("id").asString();

            esperar(hijo);

            hijos.add(hijo);
        }

        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("media_type", "CAROUSEL");
        datos.put("children", String.join(",", hijos));
        datos.put("caption", caption);

        return publicar(post(userId + "/media", datos).path("id").asString());
    }


    public String publicarReel(
            String urlVideo,
            String caption
    ) throws Exception {

        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("media_type", "REELS");
        datos.put("video_url", urlVideo);
        datos.put("caption", caption);

        return publicar(post(userId + "/media", datos).path("id").asString());
    }


    private String publicar(String contenedor) throws Exception {

        esperar(contenedor);

        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("creation_id", contenedor);

        String mediaId =
            post(userId + "/media_publish", datos).path("id").asString();

        return get(mediaId + "?fields=permalink").path("permalink").asString();
    }


    private void esperar(String contenedor) throws Exception {

        for (int i = 0; i < 60; i++) {

            JsonNode estado = get(contenedor + "?fields=status_code,status");

            String codigo = estado.path("status_code").asString();

            if ("FINISHED".equals(codigo)) {

                return;
            }

            if ("ERROR".equals(codigo) || "EXPIRED".equals(codigo)) {

                throw new IllegalStateException(
                    "Instagram rechazo el contenido: " + estado.path("status").asString()
                );
            }

            Thread.sleep(5000);
        }

        throw new IllegalStateException(
            "Instagram tardo demasiado en procesar el contenido"
        );
    }


    private JsonNode post(
            String ruta,
            Map<String, String> datos
    ) throws Exception {

        String cuerpo = datos.entrySet().stream()
            .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
            .collect(Collectors.joining("&"));

        return enviar(
            HttpRequest.newBuilder(URI.create(API + ruta))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo))
        );
    }


    private JsonNode get(String ruta) throws Exception {

        return enviar(HttpRequest.newBuilder(URI.create(API + ruta)).GET());
    }


    private JsonNode enviar(HttpRequest.Builder builder) throws Exception {

        HttpResponse<String> response = http.send(
            builder
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + token)
                .build(),
            HttpResponse.BodyHandlers.ofString()
        );

        JsonNode json = mapper.readTree(response.body());

        if (response.statusCode() != 200) {

            throw new IllegalStateException(
                "Instagram: " + json.path("error").path("message").asString("error " + response.statusCode())
            );
        }

        return json;
    }
}
