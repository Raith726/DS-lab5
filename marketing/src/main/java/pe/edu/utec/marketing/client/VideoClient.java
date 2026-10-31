package pe.edu.utec.marketing.client;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;


@Component
public class VideoClient {

    private static final String FUENTE =
        "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf";

    private static final int CUADROS = 100;


    public void generar(
            List<Path> fotos,
            List<String> textos,
            Path destino
    ) throws Exception {

        List<String> comando = new ArrayList<>(
            List.of("ffmpeg", "-y", "-threads", "2")
        );

        StringBuilder filtro = new StringBuilder();
        StringBuilder uniones = new StringBuilder();
        List<Path> temporales = new ArrayList<>();

        for (int i = 0; i < fotos.size(); i++) {

            comando.add("-i");
            comando.add(fotos.get(i).toString());

            Path texto = destino.resolveSibling(destino.getFileName() + "." + i + ".txt");

            Files.writeString(texto, partirLineas(textos.get(i), 26), StandardCharsets.UTF_8);

            temporales.add(texto);

            filtro.append("[").append(i).append(":v]")
                .append("scale=1440:2560:force_original_aspect_ratio=increase,crop=1440:2560,")
                .append("zoompan=z='min(zoom+0.0009,1.12)':d=").append(CUADROS)
                .append(":x='iw/2-(iw/zoom/2)':y='ih/2-(ih/zoom/2)':s=720x1280:fps=25,")
                .append("drawtext=textfile=").append(texto)
                .append(":expansion=none:fontfile=").append(FUENTE)
                .append(":fontsize=42:fontcolor=white:line_spacing=10")
                .append(":box=1:boxcolor=black@0.55:boxborderw=20")
                .append(":x=(w-text_w)/2:y=h-text_h-180,")
                .append("format=yuv420p,setsar=1[v").append(i).append("];");

            uniones.append("[v").append(i).append("]");
        }

        filtro.append(uniones)
            .append("concat=n=").append(fotos.size()).append(":v=1:a=0[v]");

        comando.addAll(List.of(
            "-f", "lavfi", "-i", "anullsrc=r=44100:cl=stereo",
            "-filter_complex", filtro.toString(),
            "-map", "[v]",
            "-map", fotos.size() + ":a",
            "-shortest",
            "-c:v", "libx264", "-preset", "veryfast", "-pix_fmt", "yuv420p",
            "-c:a", "aac", "-movflags", "+faststart",
            destino.toString()
        ));

        Path log = destino.resolveSibling(destino.getFileName() + ".log");

        Process proceso =
            new ProcessBuilder(comando)
                .redirectErrorStream(true)
                .redirectOutput(log.toFile())
                .start();

        boolean termino = proceso.waitFor(5, TimeUnit.MINUTES);

        if (!termino) {

            proceso.destroyForcibly();
        }

        int salida = termino ? proceso.exitValue() : -1;

        String detalle = salida == 0 ? "" : ultimasLineas(log);

        for (Path temporal : temporales) {

            Files.deleteIfExists(temporal);
        }

        Files.deleteIfExists(log);

        if (salida != 0) {

            throw new IllegalStateException(
                "No se pudo crear el video: " + detalle
            );
        }
    }


    private String partirLineas(
            String texto,
            int maximo
    ) {

        StringBuilder salida = new StringBuilder();
        int largo = 0;

        for (String palabra : texto.trim().split("\\s+")) {

            if (largo > 0 && largo + palabra.length() + 1 > maximo) {

                salida.append("\n");
                largo = 0;

            } else if (largo > 0) {

                salida.append(" ");
                largo++;
            }

            salida.append(palabra);
            largo += palabra.length();
        }

        return salida.toString();
    }


    private String ultimasLineas(Path log) throws Exception {

        List<String> lineas = Files.readAllLines(log, StandardCharsets.ISO_8859_1);

        return String.join(" ", lineas.subList(Math.max(0, lineas.size() - 3), lineas.size()));
    }
}
