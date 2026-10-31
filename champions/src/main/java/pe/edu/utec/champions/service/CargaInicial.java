package pe.edu.utec.champions.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


@Component
public class CargaInicial
        implements ApplicationRunner {

    private static final Logger log =
        LoggerFactory.getLogger(CargaInicial.class);

    private final PartidoService service;


    public CargaInicial(
            PartidoService service
    ) {
        this.service = service;
    }


    @Override
    public void run(ApplicationArguments args) {

        try {

            int nuevos = service.cargarDesdeApi();

            log.info("Partidos nuevos guardados: {}", nuevos);

        } catch (Exception e) {

            log.error("No se pudo consumir la API de partidos", e);
        }
    }
}
