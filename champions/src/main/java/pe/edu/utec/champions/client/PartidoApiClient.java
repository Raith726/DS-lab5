package pe.edu.utec.champions.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import pe.edu.utec.champions.model.Partido;


@Component
public class PartidoApiClient {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");

    private static final DateTimeFormatter MES =
        DateTimeFormatter.ofPattern("yyyyMM");

    private static final DateTimeFormatter HORA =
        DateTimeFormatter.ofPattern("HH:mm");

    private final ObjectMapper mapper;

    private final HttpClient http = HttpClient.newHttpClient();

    private final String url;

    private final YearMonth desde;

    private final YearMonth hasta;


    public PartidoApiClient(
            ObjectMapper mapper,
            @Value("${api.partidos.url}") String url,
            @Value("${api.partidos.desde}") String desde,
            @Value("${api.partidos.hasta}") String hasta
    ) {
        this.mapper = mapper;
        this.url = url;
        this.desde = YearMonth.parse(desde);
        this.hasta = YearMonth.parse(hasta);
    }


    public List<Partido> obtenerPartidos() throws Exception {

        List<Partido> partidos = new ArrayList<>();

        for (YearMonth mes = desde; !mes.isAfter(hasta); mes = mes.plusMonths(1)) {

            HttpRequest request =
                HttpRequest.newBuilder(URI.create(url + "?dates=" + mes.format(MES)))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response =
                http.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {

                throw new IllegalStateException(
                    "La API respondio " + response.statusCode()
                );
            }

            for (JsonNode evento : mapper.readTree(response.body()).path("events")) {

                partidos.add(leerPartido(evento));
            }
        }

        return partidos;
    }


    private Partido leerPartido(JsonNode evento) {

        ZonedDateTime fecha =
            OffsetDateTime.parse(evento.get("date").asString())
                .atZoneSameInstant(LIMA);

        Partido partido = new Partido();

        partido.setApiId(evento.get("id").asString());
        partido.setRonda(nombreRonda(evento.path("season").path("slug").asString("")));
        partido.setFecha(fecha.toLocalDate());
        partido.setHora(fecha.format(HORA));

        boolean terminado =
            evento.path("status").path("type").path("completed").asBoolean(false);

        for (JsonNode equipo : evento.path("competitions").get(0).path("competitors")) {

            String nombre = equipo.path("team").path("displayName").asString();

            Integer goles = terminado
                ? Integer.valueOf(equipo.path("score").asString("0"))
                : null;

            if ("home".equals(equipo.path("homeAway").asString())) {

                partido.setEquipoLocal(nombre);
                partido.setGolesLocal(goles);

            } else {

                partido.setEquipoVisitante(nombre);
                partido.setGolesVisitante(goles);
            }
        }

        return partido;
    }


    private String nombreRonda(String slug) {

        return switch (slug) {
            case "league-phase" -> "Fase de liga";
            case "knockout-round-playoffs" -> "Playoffs";
            case "round-of-16" -> "Octavos de final";
            case "quarterfinals" -> "Cuartos de final";
            case "semifinals" -> "Semifinales";
            case "final" -> "Final";
            default -> slug.replace("-", " ");
        };
    }
}
