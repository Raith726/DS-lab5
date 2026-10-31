package pe.edu.utec.marketing.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import pe.edu.utec.marketing.client.GuionesClient;
import pe.edu.utec.marketing.client.InstagramClient;
import pe.edu.utec.marketing.model.Campana;
import pe.edu.utec.marketing.model.Guion;
import pe.edu.utec.marketing.model.Recurso;
import pe.edu.utec.marketing.repository.CampanaRepository;
import pe.edu.utec.marketing.repository.GuionRepository;
import pe.edu.utec.marketing.repository.RecursoRepository;


@Service
public class CampanaServiceImpl
        implements CampanaService, ApplicationRunner {

    private final CampanaRepository campanas;

    private final GuionRepository guiones;

    private final RecursoRepository recursos;

    private final GuionesClient guionesClient;

    private final InstagramClient instagramClient;

    private final GeneracionService generacion;

    private final Path mediaDir;

    private final String clavePublicar;


    public CampanaServiceImpl(
            CampanaRepository campanas,
            GuionRepository guiones,
            RecursoRepository recursos,
            GuionesClient guionesClient,
            InstagramClient instagramClient,
            GeneracionService generacion,
            @Value("${app.media-dir}") String mediaDir,
            @Value("${app.publicar-clave}") String clavePublicar
    ) {
        this.campanas = campanas;
        this.guiones = guiones;
        this.recursos = recursos;
        this.guionesClient = guionesClient;
        this.instagramClient = instagramClient;
        this.generacion = generacion;
        this.mediaDir = Path.of(mediaDir).toAbsolutePath();
        this.clavePublicar = clavePublicar;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {

        Files.createDirectories(mediaDir);

        for (Campana campana : campanas.findByEstadoIn(List.of("GENERANDO", "PUBLICANDO"))) {

            campana.setEstado("ERROR");
            campana.setMensaje("El proceso se interrumpio al reiniciar la aplicacion, intente de nuevo");

            campanas.save(campana);
        }
    }


    @Override
    public Campana crear(
            MultipartFile foto,
            String descripcion
    ) throws Exception {

        String tipo = foto.getContentType();

        if (foto.isEmpty() || !("image/jpeg".equals(tipo) || "image/png".equals(tipo))) {

            throw new IllegalArgumentException(
                "Suba una foto en formato JPG o PNG"
            );
        }


        if (!guionesClient.tieneVision() && (descripcion == null || descripcion.isBlank())) {

            throw new IllegalArgumentException(
                "Describa el negocio y el producto de la foto"
            );
        }


        if (descripcion != null && descripcion.length() > 500) {

            throw new IllegalArgumentException(
                "La descripcion no puede pasar de 500 caracteres"
            );
        }

        List<Guion> propuestos =
            guionesClient.generar(descripcion, foto.getBytes(), tipo);

        String archivo =
            UUID.randomUUID() + ("image/png".equals(tipo) ? ".png" : ".jpg");

        Files.createDirectories(mediaDir);

        foto.transferTo(mediaDir.resolve(archivo));

        Campana campana = new Campana();

        campana.setDescripcion(descripcion);
        campana.setFoto(archivo);
        campana.setEstado("GUIONES_LISTOS");
        campana.setFechaCreacion(LocalDateTime.now());

        campanas.save(campana);

        for (Guion guion : propuestos) {

            guion.setCampanaId(campana.getId());

            guiones.save(guion);
        }

        return campana;
    }


    @Override
    public List<Campana> listarTodas() {

        return campanas.findAllByOrderByFechaCreacionDesc();
    }


    @Override
    public Campana obtenerPorId(Long id) {

        return campanas
                .findById(id)
                .orElseThrow(
                    () -> new IllegalArgumentException(
                        "Campana no encontrada"
                    )
                );
    }


    @Override
    public List<Guion> listarGuiones(Long campanaId) {

        return guiones.findByCampanaIdOrderByIdAsc(campanaId);
    }


    @Override
    public List<Recurso> listarRecursos(Long campanaId) {

        return recursos.findByCampanaIdOrderByIdAsc(campanaId);
    }


    @Override
    public void elegirGuion(
            Long campanaId,
            Long guionId
    ) {

        Campana campana = obtenerPorId(campanaId);

        if (campana.isOcupada()) {

            return;
        }

        Guion guion = guiones.findById(guionId).orElse(null);

        if (guion == null || !guion.getCampanaId().equals(campanaId)) {

            throw new IllegalArgumentException(
                "Elija uno de los guiones propuestos"
            );
        }

        campana.setGuionId(guionId);
        campana.setEstado("GENERANDO");
        campana.setMensaje(null);

        campanas.save(campana);

        generacion.generarContenido(campanaId);
    }


    @Override
    public void publicar(
            Long campanaId,
            String clave
    ) {

        if (clavePublicar.isBlank() || !clavePublicar.equals(clave)) {

            throw new IllegalArgumentException(
                "La clave de publicacion no es correcta"
            );
        }


        if (!instagramClient.configurado()) {

            throw new IllegalArgumentException(
                "Instagram no esta configurado en el servidor"
            );
        }

        Campana campana = obtenerPorId(campanaId);

        if (campana.isOcupada() || "PUBLICADA".equals(campana.getEstado())) {

            return;
        }

        campana.setEstado("PUBLICANDO");
        campana.setMensaje(null);

        campanas.save(campana);

        generacion.publicar(campanaId);
    }


    @Override
    public boolean tieneVision() {

        return guionesClient.tieneVision();
    }


    @Override
    public boolean instagramConfigurado() {

        return instagramClient.configurado();
    }
}
