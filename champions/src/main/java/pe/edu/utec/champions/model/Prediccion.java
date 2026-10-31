package pe.edu.utec.champions.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;


@Entity
@Table(name = "predicciones")
public class Prediccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "partido_id", nullable = false)
    private Partido partido;

    @Column(nullable = false, length = 20)
    private String resultado;

    @Column(name = "goles_local", nullable = false)
    private Integer golesLocal;

    @Column(name = "goles_visitante", nullable = false)
    private Integer golesVisitante;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;


    public Prediccion() {
    }


    public String getAcierto() {

        if (partido.getGolesLocal() == null) {
            return "Por jugar";
        }

        int real = Integer.compare(partido.getGolesLocal(), partido.getGolesVisitante());
        int estimado = Integer.compare(golesLocal, golesVisitante);

        if (partido.getGolesLocal().equals(golesLocal) && partido.getGolesVisitante().equals(golesVisitante)) {
            return "Marcador exacto";
        }

        return real == estimado ? "Acerto ganador" : "No acerto";
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Partido getPartido() {
        return partido;
    }


    public void setPartido(Partido partido) {
        this.partido = partido;
    }


    public String getResultado() {
        return resultado;
    }


    public void setResultado(String resultado) {
        this.resultado = resultado;
    }


    public Integer getGolesLocal() {
        return golesLocal;
    }


    public void setGolesLocal(Integer golesLocal) {
        this.golesLocal = golesLocal;
    }


    public Integer getGolesVisitante() {
        return golesVisitante;
    }


    public void setGolesVisitante(Integer golesVisitante) {
        this.golesVisitante = golesVisitante;
    }


    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }


    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
