package pe.edu.utec.champions.service;

import java.util.List;

import org.springframework.stereotype.Service;

import pe.edu.utec.champions.client.PartidoApiClient;
import pe.edu.utec.champions.model.Partido;
import pe.edu.utec.champions.repository.PartidoRepository;


@Service
public class PartidoServiceImpl
        implements PartidoService {

    private final PartidoRepository repository;

    private final PartidoApiClient apiClient;


    public PartidoServiceImpl(
            PartidoRepository repository,
            PartidoApiClient apiClient
    ) {
        this.repository = repository;
        this.apiClient = apiClient;
    }


    @Override
    public int cargarDesdeApi() throws Exception {

        int nuevos = 0;

        for (Partido partido : apiClient.obtenerPartidos()) {

            Partido guardado =
                repository.findByApiId(partido.getApiId()).orElse(null);

            if (guardado == null) {

                repository.save(partido);

                nuevos++;

            } else {

                guardado.setRonda(partido.getRonda());
                guardado.setFecha(partido.getFecha());
                guardado.setHora(partido.getHora());
                guardado.setGolesLocal(partido.getGolesLocal());
                guardado.setGolesVisitante(partido.getGolesVisitante());

                repository.save(guardado);
            }
        }

        return nuevos;
    }


    @Override
    public List<Partido> listarProximos() {

        return repository.findByGolesLocalIsNullOrderByFechaAscHoraAsc();
    }


    @Override
    public List<Partido> listarJugados() {

        return repository.findByGolesLocalIsNotNullOrderByFechaDescHoraDesc();
    }


    @Override
    public Partido obtenerPorId(Long id) {

        return repository
                .findById(id)
                .orElseThrow(
                    () -> new IllegalArgumentException(
                        "Partido no encontrado"
                    )
                );
    }
}
