package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.controller.exception.TareaNotFoundException;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
public class TareaController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TareaService tareaService;

    @Autowired
    private ManagerUserSession managerUserSession;

    private void verificarAccesoUsuario(Long idUsuario) {
        Long idLogeado = managerUserSession.usuarioLogeado();
        if (!idUsuario.equals(idLogeado)) {
            throw new UsuarioNoLogeadoException();
        }
    }

    @GetMapping("/tareas")
    public String redireccionarTareasUsuario() {
        Long idLogeado = managerUserSession.usuarioLogeado();
        return (idLogeado == null) ? "redirect:/login" : "redirect:/usuarios/" + idLogeado + "/tareas";
    }

    @GetMapping("/usuarios/{id}/tareas/nueva")
    public String mostrarFormularioNuevaTarea(@PathVariable("id") Long idUsuario,
            @ModelAttribute TareaData tareaData,
            Model model) {
        verificarAccesoUsuario(idUsuario);
        model.addAttribute("usuario", usuarioService.findById(idUsuario));
        return "formNuevaTarea";
    }

    @PostMapping("/usuarios/{id}/tareas/nueva")
    public String crearTarea(@PathVariable("id") Long idUsuario,
            @ModelAttribute TareaData tareaData,
            RedirectAttributes flash) {
        verificarAccesoUsuario(idUsuario);
        tareaService.nuevaTareaUsuario(idUsuario, tareaData.getTitulo());
        flash.addFlashAttribute("mensaje", "Tarea creada correctamente");
        return "redirect:/usuarios/" + idUsuario + "/tareas";
    }

    @GetMapping("/usuarios/{id}/tareas")
    public String listarTareasUsuario(@PathVariable("id") Long idUsuario, Model model) {
        verificarAccesoUsuario(idUsuario);
        model.addAttribute("usuario", usuarioService.findById(idUsuario));
        model.addAttribute("tareas", tareaService.allTareasUsuario(idUsuario));
        return "listaTareas";
    }

    @GetMapping("/tareas/{id}/editar")
    public String mostrarFormularioEditarTarea(@PathVariable("id") Long idTarea,
            @ModelAttribute TareaData tareaData,
            Model model) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null)
            throw new TareaNotFoundException();

        verificarAccesoUsuario(tarea.getUsuarioId());

        model.addAttribute("usuario", usuarioService.findById(tarea.getUsuarioId()));
        model.addAttribute("tarea", tarea);
        tareaData.setTitulo(tarea.getTitulo());

        return "formEditarTarea";
    }

    @PostMapping("/tareas/{id}/editar")
    public String editarTarea(@PathVariable("id") Long idTarea,
            @ModelAttribute TareaData tareaData,
            RedirectAttributes flash) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null)
            throw new TareaNotFoundException();

        verificarAccesoUsuario(tarea.getUsuarioId());

        tareaService.modificaTarea(idTarea, tareaData.getTitulo());
        flash.addFlashAttribute("mensaje", "Tarea modificada correctamente");
        return "redirect:/usuarios/" + tarea.getUsuarioId() + "/tareas";
    }

    @DeleteMapping("/tareas/{id}")
    @ResponseBody
    public String eliminarTarea(@PathVariable("id") Long idTarea) {
        TareaData tarea = tareaService.findById(idTarea);
        if (tarea == null)
            throw new TareaNotFoundException();

        verificarAccesoUsuario(tarea.getUsuarioId());
        tareaService.borraTarea(idTarea);
        return "";
    }
}
