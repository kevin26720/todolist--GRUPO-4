package madstodolist.service;

import madstodolist.model.Tarea;
import madstodolist.repository.TareaRepository;
import madstodolist.model.Usuario;
import madstodolist.repository.UsuarioRepository;
import madstodolist.dto.TareaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TareaService {

    Logger logger = LoggerFactory.getLogger(TareaService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private TareaRepository tareaRepository;
    
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public TareaData nuevaTareaUsuario(Long idUsuario, String tituloTarea) {
        logger.debug("Añadiendo tarea " + tituloTarea + " al usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findById(idUsuario).orElse(null);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al crear tarea " + tituloTarea);
        }
        Tarea tarea = new Tarea(usuario, tituloTarea);
        tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional(readOnly = true)
    public List<TareaData> allTareasUsuario(Long idUsuario) {
        logger.debug("Devolviendo todas las tareas del usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findByIdWithTareas(idUsuario);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe al listar tareas ");
        }
        List<TareaData> tareas = usuario.getTareas().stream()
                .map(tarea -> modelMapper.map(tarea, TareaData.class))
                .collect(Collectors.toList());
        Collections.sort(tareas, (a, b) -> a.getId() < b.getId() ? -1 : a.getId() == b.getId() ? 0 : 1);
        return tareas;
    }

    @Transactional(readOnly = true)
    public List<TareaData> tareasCompletadasUsuario(Long idUsuario) {
        logger.debug("Devolviendo tareas completadas del usuario " + idUsuario);
        return allTareasUsuario(idUsuario).stream()
                .filter(TareaData::isCompletada)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TareaData> tareasPendientesUsuario(Long idUsuario) {
        logger.debug("Devolviendo tareas pendientes del usuario " + idUsuario);
        return allTareasUsuario(idUsuario).stream()
                .filter(TareaData::isPendiente)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TareaData findById(Long tareaId) {
        logger.debug("Buscando tarea " + tareaId);
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        if (tarea == null) return null;
        else return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData modificaTarea(Long idTarea, String nuevoTitulo) {
        logger.debug("Modificando tarea " + idTarea + " - " + nuevoTitulo);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tarea.setTitulo(nuevoTitulo);
        tarea = tareaRepository.save(tarea);
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public void borraTarea(Long idTarea) {
        logger.debug("Borrando tarea " + idTarea);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        tareaRepository.delete(tarea);
    }

    @Transactional
    public boolean usuarioContieneTarea(Long usuarioId, Long tareaId) {
        Tarea tarea = tareaRepository.findById(tareaId).orElse(null);
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (tarea == null || usuario == null) {
            throw new TareaServiceException("No existe tarea o usuario id");
        }
        return usuario.getTareas().contains(tarea);
    }

    // NUEVOS MÉTODOS PARA MANEJO DE ESTADO COMPLETADA

    @Transactional
    public TareaData toggleTareaCompletada(Long idTarea) {
        logger.debug("Alternando estado de completada para tarea " + idTarea);
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        
        // Alternar el estado
        tarea.alternarEstado();
        tarea = tareaRepository.save(tarea);
        
        logger.info("Tarea " + idTarea + " marcada como " + 
                   (tarea.getCompletada() ? "completada" : "pendiente"));
        
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData marcarComoCompletada(Long idTarea) {
        logger.debug("Marcando tarea " + idTarea + " como completada");
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        
        if (!tarea.getCompletada()) {
            tarea.marcarComoCompletada();
            tarea = tareaRepository.save(tarea);
            logger.info("Tarea " + idTarea + " marcada como completada");
        }
        
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public TareaData marcarComoPendiente(Long idTarea) {
        logger.debug("Marcando tarea " + idTarea + " como pendiente");
        Tarea tarea = tareaRepository.findById(idTarea).orElse(null);
        if (tarea == null) {
            throw new TareaServiceException("No existe tarea con id " + idTarea);
        }
        
        if (tarea.getCompletada()) {
            tarea.marcarComoPendiente();
            tarea = tareaRepository.save(tarea);
            logger.info("Tarea " + idTarea + " marcada como pendiente");
        }
        
        return modelMapper.map(tarea, TareaData.class);
    }

    @Transactional
    public int marcarTodasComoCompletadas(Long idUsuario) {
        logger.debug("Marcando todas las tareas como completadas para usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findByIdWithTareas(idUsuario);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe");
        }
        
        int tareasActualizadas = 0;
        for (Tarea tarea : usuario.getTareas()) {
            if (!tarea.getCompletada()) {
                tarea.marcarComoCompletada();
                tareaRepository.save(tarea);
                tareasActualizadas++;
            }
        }
        
        logger.info("Se marcaron " + tareasActualizadas + " tareas como completadas para usuario " + idUsuario);
        return tareasActualizadas;
    }

    @Transactional
    public int marcarTodasComoPendientes(Long idUsuario) {
        logger.debug("Marcando todas las tareas como pendientes para usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findByIdWithTareas(idUsuario);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe");
        }
        
        int tareasActualizadas = 0;
        for (Tarea tarea : usuario.getTareas()) {
            if (tarea.getCompletada()) {
                tarea.marcarComoPendiente();
                tareaRepository.save(tarea);
                tareasActualizadas++;
            }
        }
        
        logger.info("Se marcaron " + tareasActualizadas + " tareas como pendientes para usuario " + idUsuario);
        return tareasActualizadas;
    }

    @Transactional(readOnly = true)
    public long contarTareasCompletadas(Long idUsuario) {
        logger.debug("Contando tareas completadas para usuario " + idUsuario);
        return allTareasUsuario(idUsuario).stream()
                .filter(TareaData::isCompletada)
                .count();
    }

    @Transactional(readOnly = true)
    public long contarTareasPendientes(Long idUsuario) {
        logger.debug("Contando tareas pendientes para usuario " + idUsuario);
        return allTareasUsuario(idUsuario).stream()
                .filter(TareaData::isPendiente)
                .count();
    }

    @Transactional(readOnly = true)
    public double calcularPorcentajeCompletado(Long idUsuario) {
        logger.debug("Calculando porcentaje de completado para usuario " + idUsuario);
        List<TareaData> tareas = allTareasUsuario(idUsuario);
        if (tareas.isEmpty()) {
            return 0.0;
        }
        
        long completadas = tareas.stream().filter(TareaData::isCompletada).count();
        return (double) completadas / tareas.size() * 100;
    }

    @Transactional
    public void eliminarTareasCompletadas(Long idUsuario) {
        logger.debug("Eliminando todas las tareas completadas para usuario " + idUsuario);
        Usuario usuario = usuarioRepository.findByIdWithTareas(idUsuario);
        if (usuario == null) {
            throw new TareaServiceException("Usuario " + idUsuario + " no existe");
        }
        
        List<Tarea> tareasAEliminar = usuario.getTareas().stream()
                .filter(Tarea::getCompletada)
                .collect(Collectors.toList());
        
        for (Tarea tarea : tareasAEliminar) {
            tareaRepository.delete(tarea);
        }
        
        logger.info("Se eliminaron " + tareasAEliminar.size() + " tareas completadas para usuario " + idUsuario);
    }
}