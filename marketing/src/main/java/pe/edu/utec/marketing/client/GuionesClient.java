package pe.edu.utec.marketing.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import pe.edu.utec.marketing.model.Guion;


@Component
public class GuionesClient {

    private static final String POLLINATIONS =
        "https://text.pollinations.ai/openai";

    private final ObjectMapper mapper;

    private final String geminiKey;

    private final String geminiModel;

    private final HttpClient http = HttpClient.newHttpClient();


    public GuionesClient(
            ObjectMapper mapper,
            @Value("${ia.gemini-key}") String geminiKey,
            @Value("${ia.gemini-model}") String geminiModel
    ) {
        this.mapper = mapper;
        this.geminiKey = geminiKey;
        this.geminiModel = geminiModel;
    }


    public boolean tieneVision() {

        return !geminiKey.isBlank();
    }


    public List<Guion> generar(
            String descripcion,
            byte[] foto,
            String tipoFoto
    ) throws Exception {

        String prompt = armarPrompt(descripcion);

        try {

            return leerGuiones(descripcion, 
                tieneVision()
                    ? llamarGemini(prompt, foto, tipoFoto)
                    : llamarPollinations(prompt)
            );

        } catch (JacksonException | IllegalStateException e) {

            if (!tieneVision()) {

                Thread.sleep(46000);
            }

            return leerGuiones(descripcion, 
                tieneVision()
                    ? llamarGemini(prompt, foto, tipoFoto)
                    : llamarPollinations(prompt)
            );
        }
    }


    private String armarPrompt(String descripcion) {

        String negocio = descripcion == null || descripcion.isBlank()
            ? "El negocio y el producto son los que se ven en la foto adjunta."
            : "Negocio y producto: " + descripcion + ".";

        return "Eres redactor publicitario de una agencia en Peru. " + negocio
            + " Propone 3 guiones distintos para una campana en Instagram: uno emocional, uno divertido y uno directo a la venta."
            + " Responde solo con JSON valido, sin texto adicional, con esta forma exacta:"
            + " {\"guiones\":[{\"titulo\":\"maximo 6 palabras\",\"texto\":\"guion de maximo 60 palabras\","
            + "\"caption\":\"texto de la publicacion con 3 hashtags\","
            + "\"frases\":[\"frase de maximo 7 palabras\",\"frase 2\",\"frase 3\"],"
            + "\"prompts\":[\"detailed English photo prompt showing the product, no text\",\"second English photo prompt showing the product\",\"third English photo prompt showing the product\"]}]}. El campo prompts es una sola lista con 3 textos."
            + " Todo en espanol con tildes y ortografia correcta, excepto prompts, que van en ingles.";
    }


    private String llamarGemini(
            String prompt,
            byte[] foto,
            String tipoFoto
    ) throws Exception {

        Map<String, Object> cuerpo = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(
                    Map.of("text", prompt),
                    Map.of("inline_data", Map.of(
                        "mime_type", tipoFoto,
                        "data", Base64.getEncoder().encodeToString(foto)
                    ))
                )
            )),
            "generationConfig", Map.of("responseMimeType", "application/json")
        );

        HttpRequest request =
            HttpRequest.newBuilder(URI.create(
                    "https://generativelanguage.googleapis.com/v1beta/models/"
                    + geminiModel + ":generateContent"
                ))
                .timeout(Duration.ofSeconds(80))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", geminiKey)
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cuerpo)))
                .build();

        HttpResponse<String> response =
            http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {

            throw new IllegalStateException(
                "Gemini respondio " + response.statusCode()
            );
        }

        return mapper.readTree(response.body())
            .at("/candidates/0/content/parts/0/text")
            .asString();
    }


    private String llamarPollinations(String prompt) throws Exception {

        Map<String, Object> cuerpo = Map.of(
            "model", "openai-fast",
            "max_tokens", 6000,
            "reasoning_effort", "low",
            "response_format", Map.of("type", "json_object"),
            "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        HttpRequest request =
            HttpRequest.newBuilder(URI.create(POLLINATIONS))
                .timeout(Duration.ofSeconds(80))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(cuerpo)))
                .build();

        HttpResponse<String> response =
            http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 402 || response.statusCode() == 429) {

            Thread.sleep(46000);

            response = http.send(request, HttpResponse.BodyHandlers.ofString());
        }

        if (response.statusCode() != 200) {

            throw new IllegalStateException(
                "El servicio de texto respondio " + response.statusCode()
            );
        }

        return mapper.readTree(response.body())
            .at("/choices/0/message/content")
            .asString();
    }


    private List<Guion> leerGuiones(
            String descripcion,
            String respuesta
    ) {

        int inicio = respuesta.indexOf('{');
        int fin = respuesta.lastIndexOf('}');

        if (inicio < 0 || fin < inicio) {

            throw new IllegalStateException(
                "La IA no devolvio guiones validos, intente de nuevo"
            );
        }

        JsonNode lista =
            mapper.readTree(respuesta.substring(inicio, fin + 1)).path("guiones");

        if (lista.size() < 3) {

            throw new IllegalStateException(
                "La IA no devolvio 3 guiones, intente de nuevo"
            );
        }

        List<Guion> guiones = new ArrayList<>();

        for (int i = 0; i < 3; i++) {

            JsonNode nodo = lista.get(i);

            Guion guion = new Guion();

            if (nodo.path("titulo").asString("").isBlank() || nodo.path("texto").asString("").isBlank()) {

                throw new IllegalStateException(
                    "La IA devolvio un guion incompleto, intente de nuevo"
                );
            }

            guion.setTitulo(nodo.path("titulo").asString());
            guion.setTexto(nodo.path("texto").asString());
            guion.setCaption(nodo.path("caption").asString(nodo.path("texto").asString()));
            guion.setPrompts(armarPrompts(nodo, descripcion));
            guion.setFrases(unirTres(nodo.path("frases"), guion.getTitulo()));

            guiones.add(guion);
        }

        return guiones;
    }


    private String unirTres(
            JsonNode nodo,
            String respaldo
    ) {

        List<String> valores = new ArrayList<>();

        for (JsonNode valor : nodo) {

            if (valor.isString() && !valor.asString().isBlank() && valores.size() < 3) {

                valores.add(limpiar(valor));
            }
        }

        while (valores.size() < 3) {

            valores.add(respaldo);
        }

        return String.join("\n", valores);
    }


    private String armarPrompts(
            JsonNode nodo,
            String descripcion
    ) {

        List<String> valores = new ArrayList<>();

        for (String campo : List.of("prompts", "prompts2", "prompts3", "prompt2", "prompt3")) {

            JsonNode valor = nodo.path(campo);

            if (valor.isString() && !valor.asString().isBlank()) {

                valores.add(limpiar(valor));
            }

            for (JsonNode item : valor) {

                if (item.isString() && !item.asString().isBlank()) {

                    valores.add(limpiar(item));
                }
            }
        }

        String base = valores.isEmpty()
            ? "commercial product photo of " + descripcion
            : valores.get(0);

        if (valores.size() < 2) {

            valores.add(base + ", extreme close-up, shallow depth of field");
        }

        if (valores.size() < 3) {

            valores.add(base + ", lifestyle scene, natural light, wide shot");
        }

        return String.join("\n", valores.subList(0, 3));
    }


    private String limpiar(JsonNode nodo) {

        return nodo.asString().replace("\n", " ").trim();
    }
}
