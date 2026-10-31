package pe.edu.utec.marketing.model;

import jakarta.persistence.*;


@Entity
@Table(name = "recursos")
public class Recurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campana_id", nullable = false)
    private Long campanaId;

    @Column(nullable = false, length = 10)
    private String tipo;

    @Column(nullable = false)
    private String archivo;


    public Recurso() {
    }


    public Recurso(
            Long campanaId,
            String tipo,
            String archivo
    ) {

        this.campanaId = campanaId;
        this.tipo = tipo;
        this.archivo = archivo;
    }


    public Long getId() {
        return id;
    }


    public Long getCampanaId() {
        return campanaId;
    }


    public String getTipo() {
        return tipo;
    }


    public String getArchivo() {
        return archivo;
    }
}
