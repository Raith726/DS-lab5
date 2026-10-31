package pe.edu.utec.champions.model;

import java.time.LocalDate;

import jakarta.persistence.*;


@Entity
@Table(
    name = "partidos",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"fecha", "equipo_local", "equipo_visitante"}
    )
)
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String ronda;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(length = 10)
    private String hora;

    @Column(name = "equipo_local", nullable = false, length = 80)
    private String equipoLocal;

    @Column(name = "equipo_visitante", nullable = false, length = 80)
    private String equipoVisitante;

    @Column(name = "goles_local")
    private Integer golesLocal;

    @Column(name = "goles_visitante")
    private Integer golesVisitante;


    public Partido() {
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public String getRonda() {
        return ronda;
    }


    public void setRonda(String ronda) {
        this.ronda = ronda;
    }


    public LocalDate getFecha() {
        return fecha;
    }


    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }


    public String getHora() {
        return hora;
    }


    public void setHora(String hora) {
        this.hora = hora;
    }


    public String getEquipoLocal() {
        return equipoLocal;
    }


    public void setEquipoLocal(String equipoLocal) {
        this.equipoLocal = equipoLocal;
    }


    public String getEquipoVisitante() {
        return equipoVisitante;
    }


    public void setEquipoVisitante(String equipoVisitante) {
        this.equipoVisitante = equipoVisitante;
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
}
