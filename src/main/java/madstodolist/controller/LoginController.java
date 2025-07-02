package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.LoginData;
import madstodolist.dto.RegistroData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import madstodolist.service.UsuarioService.LoginStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/")
    public String redireccionarLogin() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String mostrarFormularioLogin(Model model) {
        model.addAttribute("loginData", new LoginData());
        return "formLogin";
    }

    @PostMapping("/login")
    public String procesarLogin(@ModelAttribute LoginData loginData, Model model) {
        LoginStatus status = usuarioService.login(loginData.geteMail(), loginData.getPassword());

        switch (status) {
            case LOGIN_OK:
                UsuarioData usuario = usuarioService.findByEmail(loginData.geteMail());
                managerUserSession.logearUsuario(usuario.getId());
                return "redirect:/usuarios/" + usuario.getId() + "/tareas";

            case USER_NOT_FOUND:
                model.addAttribute("error", "No existe usuario");
                break;

            case ERROR_PASSWORD:
                model.addAttribute("error", "Contraseña incorrecta");
                break;
        }

        return "formLogin";
    }

    @GetMapping("/registro")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("registroData", new RegistroData());
        return "formRegistro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute RegistroData registroData,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "formRegistro";
        }

        if (usuarioService.findByEmail(registroData.getEmail()) != null) {
            model.addAttribute("error", "El usuario " + registroData.getEmail() + " ya existe");
            return "formRegistro";
        }

        UsuarioData nuevoUsuario = new UsuarioData();
        nuevoUsuario.setEmail(registroData.getEmail());
        nuevoUsuario.setPassword(registroData.getPassword());
        nuevoUsuario.setFechaNacimiento(registroData.getFechaNacimiento());
        nuevoUsuario.setNombre(registroData.getNombre());

        usuarioService.registrar(nuevoUsuario);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        managerUserSession.logout();
        return "redirect:/login";
    }
}
