package pe.edu.utec.marketing.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import pe.edu.utec.marketing.model.Campana;
import pe.edu.utec.marketing.model.Guion;
import pe.edu.utec.marketing.model.Recurso;


public interface CampanaService {

    Campana crear(MultipartFile foto, String descripcion) throws Exception;

    List<Campana> listarTodas();

    Campana obtenerPorId(Long id);

    List<Guion> listarGuiones(Long campanaId);

    List<Recurso> listarRecursos(Long campanaId);

    void elegirGuion(Long campanaId, Long guionId);

    void publicar(Long campanaId, String clave);

    boolean tieneVision();

    boolean instagramConfigurado();
}
