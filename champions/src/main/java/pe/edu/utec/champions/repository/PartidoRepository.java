package pe.edu.utec.champions.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utec.champions.model.Partido;


public interface PartidoRepository
        extends JpaRepository<Partido, Long> {

    boolean existsByFechaAndEquipoLocalAndEquipoVisitante(
        LocalDate fecha,
        String equipoLocal,
        String equipoVisitante
    );

    List<Partido> findAllByOrderByFechaAscHoraAscIdAsc();
}
