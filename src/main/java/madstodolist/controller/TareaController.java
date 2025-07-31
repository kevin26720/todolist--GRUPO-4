package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.controller.exception.UsuarioNoLogeadoException;
import madstodolist.controller.exception.TareaNotFoundException;
import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.TareaService;
import madstodolist.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        
        UsuarioData usuario = usuarioService.findById(idUsuario);
        List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);
        
        // Calcular estadísticas
        long totalTareas = tareas.size();
        long tareasCompletadas = tareas.stream().filter(TareaData::isCompletada).count();
        long tareasPendientes = totalTareas - tareasCompletadas;
        double porcentajeCompletado = totalTareas > 0 ? (double) tareasCompletadas / totalTareas * 100 : 0;
        
        model.addAttribute("usuario", usuario);
        model.addAttribute("tareas", tareas);
        model.addAttribute("totalTareas", totalTareas);
        model.addAttribute("tareasCompletadas", tareasCompletadas);
        model.addAttribute("tareasPendientes", tareasPendientes);
        model.addAttribute("porcentajeCompletado", Math.round(porcentajeCompletado));
        
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
        tareaData.setCompletada(tarea.getCompletada());

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

    // NUEVO ENDPOINT: Marcar/desmarcar tarea como completada
    @PutMapping("/tareas/{id}/toggle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleTareaCompletada(@PathVariable("id") Long idTarea) {
        try {
            TareaData tarea = tareaService.findById(idTarea);
            if (tarea == null) {
                throw new TareaNotFoundException();
            }

            verificarAccesoUsuario(tarea.getUsuarioId());

            // Alternar el estado de completada
            TareaData tareaActualizada = tareaService.toggleTareaCompletada(idTarea);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("completed", tareaActualizada.getCompletada());
            response.put("message", tareaActualizada.getCompletada() 
                ? "Tarea marcada como completada" 
                : "Tarea marcada como pendiente");
            response.put("tarea", tareaActualizada);
            
            return ResponseEntity.ok(response);
            
        } catch (TareaNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Tarea no encontrada");
            return ResponseEntity.notFound().build();
            
        } catch (UsuarioNoLogeadoException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "No tienes permisos para modificar esta tarea");
            return ResponseEntity.status(403).body(errorResponse);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error interno del servidor: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // NUEVO ENDPOINT: Marcar específicamente como completada
    @PutMapping("/tareas/{id}/completar")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completarTarea(@PathVariable("id") Long idTarea) {
        try {
            TareaData tarea = tareaService.findById(idTarea);
            if (tarea == null) {
                throw new TareaNotFoundException();
            }

            verificarAccesoUsuario(tarea.getUsuarioId());

            TareaData tareaActualizada = tareaService.marcarComoCompletada(idTarea);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("completed", true);
            response.put("message", "Tarea completada exitosamente");
            response.put("tarea", tareaActualizada);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // NUEVO ENDPOINT: Marcar como pendiente
    @PutMapping("/tareas/{id}/pendiente")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> marcarPendiente(@PathVariable("id") Long idTarea) {
        try {
            TareaData tarea = tareaService.findById(idTarea);
            if (tarea == null) {
                throw new TareaNotFoundException();
            }

            verificarAccesoUsuario(tarea.getUsuarioId());

            TareaData tareaActualizada = tareaService.marcarComoPendiente(idTarea);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("completed", false);
            response.put("message", "Tarea marcada como pendiente");
            response.put("tarea", tareaActualizada);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // NUEVO ENDPOINT: Obtener estadísticas de tareas del usuario
    @GetMapping("/usuarios/{id}/tareas/estadisticas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerEstadisticasTareas(@PathVariable("id") Long idUsuario) {
        try {
            verificarAccesoUsuario(idUsuario);
            
            List<TareaData> tareas = tareaService.allTareasUsuario(idUsuario);
            
            long totalTareas = tareas.size();
            long tareasCompletadas = tareas.stream().filter(TareaData::isCompletada).count();
            long tareasPendientes = totalTareas - tareasCompletadas;
            double porcentajeCompletado = totalTareas > 0 ? (double) tareasCompletadas / totalTareas * 100 : 0;
            
            Map<String, Object> estadisticas = new HashMap<>();
            estadisticas.put("totalTareas", totalTareas);
            estadisticas.put("tareasCompletadas", tareasCompletadas);
            estadisticas.put("tareasPendientes", tareasPendientes);
            estadisticas.put("porcentajeCompletado", Math.round(porcentajeCompletado * 100.0) / 100.0);
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // Agregar estos métodos al TareaController existente

    // ENDPOINT: Completar todas las tareas de un usuario
    @PostMapping("/usuarios/{id}/tareas/completar-todas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> completarTodasLasTareas(@PathVariable("id") Long idUsuario) {
        try {
            verificarAccesoUsuario(idUsuario);
            
            int tareasActualizadas = tareaService.marcarTodasComoCompletadas(idUsuario);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Se completaron %d tareas", tareasActualizadas));
            response.put("tareasActualizadas", tareasActualizadas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ENDPOINT: Marcar todas las tareas como pendientes
    @PostMapping("/usuarios/{id}/tareas/pendientes-todas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> marcarTodasComoPendientes(@PathVariable("id") Long idUsuario) {
        try {
            verificarAccesoUsuario(idUsuario);
            
            int tareasActualizadas = tareaService.marcarTodasComoPendientes(idUsuario);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Se marcaron %d tareas como pendientes", tareasActualizadas));
            response.put("tareasActualizadas", tareasActualizadas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ENDPOINT: Eliminar todas las tareas completadas
    @PostMapping("/usuarios/{id}/tareas/eliminar-completadas")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> eliminarTareasCompletadas(@PathVariable("id") Long idUsuario) {
        try {
            verificarAccesoUsuario(idUsuario);
            
            // Contar tareas completadas antes de eliminar
            long tareasCompletadas = tareaService.contarTareasCompletadas(idUsuario);
            
            tareaService.eliminarTareasCompletadas(idUsuario);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", String.format("Se eliminaron %d tareas completadas", tareasCompletadas));
            response.put("tareasEliminadas", tareasCompletadas);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    // ENDPOINT: Obtener tareas filtradas por estado
    @GetMapping("/usuarios/{id}/tareas/filtro/{estado}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> obtenerTareasPorEstado(
            @PathVariable("id") Long idUsuario,
            @PathVariable("estado") String estado) {
        try {
            verificarAccesoUsuario(idUsuario);
            
            List<TareaData> tareas;
            switch (estado.toLowerCase()) {
                case "completadas":
                    tareas = tareaService.tareasCompletadasUsuario(idUsuario);
                    break;
                case "pendientes":
                    tareas = tareaService.tareasPendientesUsuario(idUsuario);
                    break;
                case "todas":
                default:
                    tareas = tareaService.allTareasUsuario(idUsuario);
                    break;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("tareas", tareas);
            response.put("cantidad", tareas.size());
            response.put("filtro", estado);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
