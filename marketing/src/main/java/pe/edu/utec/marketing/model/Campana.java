package pe.edu.utec.marketing.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;


@Entity
@Table(name = "campanas")
public class Campana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 500)
    private String descripcion;

    @Column(nullable = false)
    private String foto;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 1000)
    private String mensaje;

    @Column(name = "guion_id")
    private Long guionId;

    @Column(name = "url_carrusel")
    private String urlCarrusel;

    @Column(name = "url_reel")
    private String urlReel;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;


    public Campana() {
    }


    public boolean isOcupada() {
        return "GENERANDO".equals(estado) || "PUBLICANDO".equals(estado);
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getDescripcion() {
        return descripcion;
    }


    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


    public String getFoto() {
        return foto;
    }


    public void setFoto(String foto) {
        this.foto = foto;
    }


    public String getEstado() {
        return estado;
    }


    public void setEstado(String estado) {
        this.estado = estado;
    }


    public String getMensaje() {
        return mensaje;
    }


    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }


    public Long getGuionId() {
        return guionId;
    }


    public void setGuionId(Long guionId) {
        this.guionId = guionId;
    }


    public String getUrlCarrusel() {
        return urlCarrusel;
    }


    public void setUrlCarrusel(String urlCarrusel) {
        this.urlCarrusel = urlCarrusel;
    }


    public String getUrlReel() {
        return urlReel;
    }


    public void setUrlReel(String urlReel) {
        this.urlReel = urlReel;
    }


    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }


    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}
