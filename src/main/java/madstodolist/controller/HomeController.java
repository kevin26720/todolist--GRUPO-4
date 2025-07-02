package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Controller
public class HomeController {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/about")
    public String about(Model model) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        prepararModeloAutenticacion(model, idUsuarioLogeado);
        return "about";
    }

    @GetMapping("/account")
    public String account() {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        if (idUsuarioLogeado == null) {
            return "redirect:/login";
        }

        return "redirect:/usuarios/" + idUsuarioLogeado + "/tareas";
    }

    @GetMapping("/registrados")
    public String listarUsuarios(Model model) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();

        if (idUsuarioLogeado == null) {
            return "redirect:/login";
        }

        prepararModeloAutenticacion(model, idUsuarioLogeado);

        List<Usuario> usuarios = StreamSupport.stream(usuarioRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());

        model.addAttribute("usuarios", usuarios);
        return "registrados";
    }

    private void prepararModeloAutenticacion(Model model, Long idUsuarioLogeado) {
        if (idUsuarioLogeado != null) {
            UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
            model.addAttribute("usuario", usuario);
            model.addAttribute("isAuthenticated", true);
        } else {
            model.addAttribute("isAuthenticated", false);
        }
    }
}
