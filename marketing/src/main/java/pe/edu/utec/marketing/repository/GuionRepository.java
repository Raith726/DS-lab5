package pe.edu.utec.marketing.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import pe.edu.utec.marketing.model.Guion;


public interface GuionRepository
        extends JpaRepository<Guion, Long> {

    List<Guion> findByCampanaIdOrderByIdAsc(Long campanaId);
}
