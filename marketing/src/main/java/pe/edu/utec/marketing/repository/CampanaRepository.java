package pe.edu.utec.marketing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utec.marketing.model.Campana;


public interface CampanaRepository
        extends JpaRepository<Campana, Long> {

    List<Campana> findAllByOrderByFechaCreacionDesc();

    List<Campana> findByEstadoIn(List<String> estados);
}
