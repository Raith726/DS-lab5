package pe.edu.utec.champions.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.utec.champions.model.Partido;
import pe.edu.utec.champions.model.Prediccion;
import pe.edu.utec.champions.repository.PrediccionRepository;


@Service
public class PrediccionServiceImpl
        implements PrediccionService {

    private final PrediccionRepository repository;

    private final PartidoService partidoService;


    public PrediccionServiceImpl(
            PrediccionRepository repository,
            PartidoService partidoService
    ) {
        this.repository = repository;
        this.partidoService = partidoService;
    }


    @Override
    public Prediccion registrar(
            Long partidoId,
            String resultado,
            Integer golesLocal,
            Integer golesVisitante
    ) {

        validar(resultado, golesLocal, golesVisitante);

        Partido partido = partidoService.obtenerPorId(partidoId);

        if (partido.isJugado()) {

            throw new IllegalArgumentException(
                "Este partido ya se jugo, elija uno de los proximos"
            );
        }

        Prediccion prediccion = new Prediccion();

        prediccion.setPartido(partido);
        prediccion.setResultado(resultado);
        prediccion.setGolesLocal(golesLocal);
        prediccion.setGolesVisitante(golesVisitante);
        prediccion.setFechaRegistro(LocalDateTime.now());

        return repository.save(prediccion);
    }


    @Override
    public List<Prediccion> listarTodas() {

        return repository.findAllByOrderByFechaRegistroDesc();
    }


    private void validar(
            String resultado,
            Integer golesLocal,
            Integer golesVisitante
    ) {

        if (
            !"LOCAL".equals(resultado) &&
            !"VISITANTE".equals(resultado) &&
            !"EMPATE".equals(resultado)
        ) {

            throw new IllegalArgumentException(
                "Debe elegir un ganador o empate"
            );
        }


        if (golesLocal == null || golesVisitante == null) {

            throw new IllegalArgumentException(
                "Debe indicar el marcador estimado"
            );
        }


        if (golesLocal < 0 || golesVisitante < 0) {

            throw new IllegalArgumentException(
                "Los goles no pueden ser negativos"
            );
        }


        if (
            ("LOCAL".equals(resultado) && golesLocal <= golesVisitante) ||
            ("VISITANTE".equals(resultado) && golesVisitante <= golesLocal) ||
            ("EMPATE".equals(resultado) && !golesLocal.equals(golesVisitante))
        ) {

            throw new IllegalArgumentException(
                "El marcador no coincide con el resultado elegido"
            );
        }
    }
}
