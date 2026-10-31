package pe.edu.utec.marketing.model;

import java.util.List;

import jakarta.persistence.*;


@Entity
@Table(name = "guiones")
public class Guion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campana_id", nullable = false)
    private Long campanaId;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String caption;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prompts;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String frases;


    public Guion() {
    }


    public List<String> listaPrompts() {
        return List.of(prompts.split("\n"));
    }


    public List<String> listaFrases() {
        return List.of(frases.split("\n"));
    }


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }


    public Long getCampanaId() {
        return campanaId;
    }


    public void setCampanaId(Long campanaId) {
        this.campanaId = campanaId;
    }


    public String getTitulo() {
        return titulo;
    }


    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }


    public String getTexto() {
        return texto;
    }


    public void setTexto(String texto) {
        this.texto = texto;
    }


    public String getCaption() {
        return caption;
    }


    public void setCaption(String caption) {
        this.caption = caption;
    }


    public String getPrompts() {
        return prompts;
    }


    public void setPrompts(String prompts) {
        this.prompts = prompts;
    }


    public String getFrases() {
        return frases;
    }


    public void setFrases(String frases) {
        this.frases = frases;
    }
}
