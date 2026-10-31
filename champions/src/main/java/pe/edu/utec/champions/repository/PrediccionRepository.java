package pe.edu.utec.champions.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utec.champions.model.Prediccion;


public interface PrediccionRepository
        extends JpaRepository<Prediccion, Long> {

    List<Prediccion> findAllByOrderByFechaRegistroDesc();
}
