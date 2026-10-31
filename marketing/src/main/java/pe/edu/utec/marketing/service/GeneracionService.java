package pe.edu.utec.marketing.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import pe.edu.utec.marketing.client.ImagenClient;
import pe.edu.utec.marketing.client.InstagramClient;
import pe.edu.utec.marketing.client.VideoClient;
import pe.edu.utec.marketing.model.Campana;
import pe.edu.utec.marketing.model.Guion;
import pe.edu.utec.marketing.model.Recurso;
import pe.edu.utec.marketing.repository.CampanaRepository;
import pe.edu.utec.marketing.repository.GuionRepository;
import pe.edu.utec.marketing.repository.RecursoRepository;


@Service
public class GeneracionService {

    private static final Logger log =
        LoggerFactory.getLogger(GeneracionService.class);

    private final CampanaRepository campanas;

    private final GuionRepository guiones;

    private final RecursoRepository recursos;

    private final ImagenClient imagenClient;

    private final VideoClient videoClient;

    private final InstagramClient instagramClient;

    private final Path mediaDir;

    private final String baseUrl;


    public GeneracionService(
            CampanaRepository campanas,
            GuionRepository guiones,
            RecursoRepository recursos,
            ImagenClient imagenClient,
            VideoClient videoClient,
            InstagramClient instagramClient,
            @Value("${app.media-dir}") String mediaDir,
            @Value("${app.base-url}") String baseUrl
    ) {
        this.campanas = campanas;
        this.guiones = guiones;
        this.recursos = recursos;
        this.imagenClient = imagenClient;
        this.videoClient = videoClient;
        this.instagramClient = instagramClient;
        this.mediaDir = Path.of(mediaDir).toAbsolutePath();
        this.baseUrl = baseUrl;
    }


    @Async
    public void generarContenido(Long campanaId) {

        Campana campana = campanas.findById(campanaId).orElseThrow();

        try {

            recursos.deleteAll(recursos.findByCampanaIdOrderByIdAsc(campanaId));

            Guion guion = guiones.findById(campana.getGuionId()).orElseThrow();

            List<String> prompts = guion.listaPrompts();

            List<Path> fotos = new ArrayList<>();

            fotos.add(mediaDir.resolve(campana.getFoto()));

            for (int i = 0; i < 3; i++) {

                String archivo = "c" + campanaId + "_foto" + (i + 1) + ".jpg";

                imagenClient.generar(
                    prompts.get(i),
                    (int) (campanaId * 10 + i),
                    mediaDir.resolve(archivo)
                );

                recursos.save(new Recurso(campanaId, "FOTO", archivo));

                fotos.add(mediaDir.resolve(archivo));

                if (i < 2) {

                    Thread.sleep(46000);
                }
            }

            List<String> textos = new ArrayList<>();

            textos.add(guion.getTitulo());
            textos.addAll(guion.listaFrases());

            String video = "c" + campanaId + "_video.mp4";

            videoClient.generar(fotos, textos, mediaDir.resolve(video));

            recursos.save(new Recurso(campanaId, "VIDEO", video));

            campana.setEstado("CONTENIDO_LISTO");
            campana.setMensaje(null);

        } catch (Exception e) {

            log.error("Error generando contenido", e);

            campana.setEstado("ERROR");
            campana.setMensaje(e.getMessage());
        }

        campanas.save(campana);
    }


    @Async
    public void publicar(Long campanaId) {

        Campana campana = campanas.findById(campanaId).orElseThrow();

        try {

            Guion guion = guiones.findById(campana.getGuionId()).orElseThrow();

            List<String> urlsFotos = new ArrayList<>();
            String urlVideo = null;

            for (Recurso recurso : recursos.findByCampanaIdOrderByIdAsc(campanaId)) {

                String url = baseUrl + "/media/" + recurso.getArchivo();

                if ("FOTO".equals(recurso.getTipo())) {

                    urlsFotos.add(url);

                } else {

                    urlVideo = url;
                }
            }

            if (campana.getUrlCarrusel() == null) {

                campana.setUrlCarrusel(
                    instagramClient.publicarCarrusel(urlsFotos, guion.getCaption())
                );

                campanas.save(campana);
            }

            if (campana.getUrlReel() == null) {

                campana.setUrlReel(
                    instagramClient.publicarReel(urlVideo, guion.getCaption())
                );
            }

            campana.setEstado("PUBLICADA");
            campana.setMensaje(null);

        } catch (Exception e) {

            log.error("Error publicando en Instagram", e);

            campana.setEstado("ERROR");
            campana.setMensaje(e.getMessage());
        }

        campanas.save(campana);
    }
}
