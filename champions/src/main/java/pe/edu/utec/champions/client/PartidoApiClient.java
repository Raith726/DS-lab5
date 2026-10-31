package pe.edu.utec.champions.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import pe.edu.utec.champions.model.Partido;


@Component
public class PartidoApiClient {

    private final ObjectMapper mapper;

    private final String url;


    public PartidoApiClient(
            ObjectMapper mapper,
            @Value("${api.partidos.url}") String url
    ) {
        this.mapper = mapper;
        this.url = url;
    }


    public List<Partido> obtenerPartidos() throws Exception {

        HttpRequest request =
            HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response =
            HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {

            throw new IllegalStateException(
                "La API respondio " + response.statusCode()
            );
        }

        List<Partido> partidos = new ArrayList<>();

        for (JsonNode nodo : mapper.readTree(response.body()).get("matches")) {

            Partido partido = new Partido();

            partido.setRonda(nodo.get("round").asString());
            partido.setFecha(LocalDate.parse(nodo.get("date").asString()));
            partido.setHora(nodo.path("time").asString(null));
            partido.setEquipoLocal(nodo.get("team1").asString());
            partido.setEquipoVisitante(nodo.get("team2").asString());

            JsonNode marcador = nodo.path("score").path("ft");

            if (marcador.size() == 2) {

                partido.setGolesLocal(marcador.get(0).asInt());
                partido.setGolesVisitante(marcador.get(1).asInt());
            }

            partidos.add(partido);
        }

        return partidos;
    }
}
