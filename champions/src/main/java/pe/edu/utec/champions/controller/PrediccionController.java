package pe.edu.utec.champions.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import pe.edu.utec.champions.service.PartidoService;
import pe.edu.utec.champions.service.PrediccionService;


@Controller
public class PrediccionController {

    private final PrediccionService service;

    private final PartidoService partidoService;


    public PrediccionController(
            PrediccionService service,
            PartidoService partidoService
    ) {
        this.service = service;
        this.partidoService = partidoService;
    }


    @GetMapping("/partidos/{id}/prediccion")
    public String formulario(
            @PathVariable Long id,
            Model model
    ) {

        model.addAttribute("partido", partidoService.obtenerPorId(id));

        return "predicciones/formulario";
    }


    @PostMapping("/partidos/{id}/prediccion")
    public String registrar(
            @PathVariable Long id,
            @RequestParam(required = false) String resultado,
            @RequestParam(required = false) Integer golesLocal,
            @RequestParam(required = false) Integer golesVisitante,
            Model model
    ) {

        try {

            service.registrar(id, resultado, golesLocal, golesVisitante);

            return "redirect:/predicciones";

        } catch (IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("partido", partidoService.obtenerPorId(id));

            return "predicciones/formulario";
        }
    }


    @GetMapping("/predicciones")
    public String listar(
            Model model
    ) {

        model.addAttribute("predicciones", service.listarTodas());

        return "predicciones/lista";
    }
}
