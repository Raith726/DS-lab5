package pe.edu.utec.champions.service;

import java.util.List;

import pe.edu.utec.champions.model.Prediccion;


public interface PrediccionService {

    Prediccion registrar(
        Long partidoId,
        String resultado,
        Integer golesLocal,
        Integer golesVisitante
    );

    List<Prediccion> listarTodas();
}
