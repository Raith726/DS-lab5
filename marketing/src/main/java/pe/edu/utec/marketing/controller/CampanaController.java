package pe.edu.utec.marketing.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pe.edu.utec.marketing.model.Campana;
import pe.edu.utec.marketing.model.Guion;
import pe.edu.utec.marketing.model.Recurso;
import pe.edu.utec.marketing.service.CampanaService;


@Controller
public class CampanaController {

    private final CampanaService service;


    public CampanaController(
            CampanaService service
    ) {
        this.service = service;
    }


    @GetMapping("/")
    public String lista(
            Model model
    ) {

        model.addAttribute("campanas", service.listarTodas());

        return "lista";
    }


    @GetMapping("/campanas/nueva")
    public String nueva(
            Model model
    ) {

        model.addAttribute("vision", service.tieneVision());

        return "nueva";
    }


    @PostMapping("/campanas")
    public String crear(
            @RequestParam("foto") MultipartFile foto,
            @RequestParam(required = false) String descripcion,
            Model model
    ) {

        try {

            Campana campana = service.crear(foto, descripcion);

            return "redirect:/campanas/" + campana.getId();

        } catch (Exception e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("vision", service.tieneVision());
            model.addAttribute("descripcion", descripcion);

            return "nueva";
        }
    }


    @GetMapping("/campanas/{id}")
    public String detalle(
            @PathVariable Long id,
            Model model
    ) {

        Campana campana = service.obtenerPorId(id);

        List<Guion> guiones = service.listarGuiones(id);
        List<Recurso> recursos = service.listarRecursos(id);

        model.addAttribute("campana", campana);
        model.addAttribute("guiones", guiones);

        model.addAttribute(
            "fotos",
            recursos.stream().filter(r -> "FOTO".equals(r.getTipo())).toList()
        );

        model.addAttribute(
            "video",
            recursos.stream().filter(r -> "VIDEO".equals(r.getTipo())).findFirst().orElse(null)
        );

        model.addAttribute(
            "elegido",
            guiones.stream().filter(g -> g.getId().equals(campana.getGuionId())).findFirst().orElse(null)
        );

        model.addAttribute("instagram", service.instagramConfigurado());

        return "detalle";
    }


    @PostMapping("/campanas/{id}/elegir")
    public String elegir(
            @PathVariable Long id,
            @RequestParam(required = false) Long guionId,
            RedirectAttributes atributos
    ) {

        try {

            service.elegirGuion(id, guionId == null ? -1L : guionId);

        } catch (IllegalArgumentException e) {

            atributos.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/campanas/" + id;
    }


    @PostMapping("/campanas/{id}/publicar")
    public String publicar(
            @PathVariable Long id,
            @RequestParam(required = false) String clave,
            RedirectAttributes atributos
    ) {

        try {

            service.publicar(id, clave);

        } catch (IllegalArgumentException e) {

            atributos.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/campanas/" + id;
    }
}
