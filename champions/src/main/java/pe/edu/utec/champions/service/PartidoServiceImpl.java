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

            boolean existe =
                repository.existsByFechaAndEquipoLocalAndEquipoVisitante(
                    partido.getFecha(),
                    partido.getEquipoLocal(),
                    partido.getEquipoVisitante()
                );

            if (!existe) {

                repository.save(partido);

                nuevos++;
            }
        }

        return nuevos;
    }


    @Override
    public List<Partido> listarTodos() {

        return repository.findAllByOrderByFechaAscHoraAscIdAsc();
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
