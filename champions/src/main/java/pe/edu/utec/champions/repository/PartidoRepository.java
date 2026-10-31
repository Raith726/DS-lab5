package pe.edu.utec.champions.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utec.champions.model.Partido;


public interface PartidoRepository
        extends JpaRepository<Partido, Long> {

    Optional<Partido> findByApiId(String apiId);

    List<Partido> findByGolesLocalIsNullOrderByFechaAscHoraAsc();

    List<Partido> findByGolesLocalIsNotNullOrderByFechaDescHoraDesc();
}
