package pe.edu.utec.marketing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utec.marketing.model.Recurso;


public interface RecursoRepository
        extends JpaRepository<Recurso, Long> {

    List<Recurso> findByCampanaIdOrderByIdAsc(Long campanaId);
}
