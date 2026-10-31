package pe.edu.utec.champions.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import pe.edu.utec.champions.service.PartidoService;


@Controller
public class PartidoController {

    private final PartidoService service;


    public PartidoController(
            PartidoService service
    ) {
        this.service = service;
    }


    @GetMapping("/")
    public String inicio() {

        return "redirect:/partidos";
    }


    @GetMapping("/partidos")
    public String listar(
            Model model
    ) {

        model.addAttribute("partidos", service.listarTodos());

        return "partidos/lista";
    }
}
