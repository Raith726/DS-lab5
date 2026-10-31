package pe.edu.utec.champions.service;

import java.util.List;

import pe.edu.utec.champions.model.Partido;


public interface PartidoService {

    int cargarDesdeApi() throws Exception;

    List<Partido> listarProximos();

    List<Partido> listarJugados();

    Partido obtenerPorId(Long id);
}
