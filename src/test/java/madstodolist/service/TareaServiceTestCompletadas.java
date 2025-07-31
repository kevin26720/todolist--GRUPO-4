package madstodolist.service;

import madstodolist.dto.TareaData;
import madstodolist.dto.UsuarioData;
import madstodolist.model.Tarea;
import madstodolist.repository.TareaRepository;
import madstodolist.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Sql("/clean-db.sql")
public class TareaServiceTestCompletadas {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private TareaService tareaService;


    @Autowired
    private TareaRepository tareaRepository;

    private UsuarioData crearUsuarioPrueba() {
        UsuarioData usuarioData = new UsuarioData();
        usuarioData.setEmail("test@example.com");
        usuarioData.setNombre("Usuario Test");
        usuarioData.setPassword("password123");
        return usuarioService.registrar(usuarioData);
    }

    @Test
    @Transactional
    public void testNuevaTareaSeCreaPendiente() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        
        // WHEN
        TareaData tarea = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea de prueba");
        
        // THEN
        assertThat(tarea.getCompletada()).isFalse();
        assertThat(tarea.getTitulo()).isEqualTo("Tarea de prueba");
        assertThat(tarea.getFechaCreacion()).isNotNull();
        assertThat(tarea.getFechaCompletada()).isNull();
    }

    @Test
    @Transactional
    public void testToggleTareaCompletada() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        TareaData tarea = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea de prueba");
        
        // WHEN - Marcar como completada
        TareaData tareaCompletada = tareaService.toggleTareaCompletada(tarea.getId());
        
        // THEN
        assertThat(tareaCompletada.getCompletada()).isTrue();
        assertThat(tareaCompletada.getFechaCompletada()).isNotNull();
        
        // WHEN - Marcar como pendiente nuevamente
        TareaData tareaPendiente = tareaService.toggleTareaCompletada(tarea.getId());
        
        // THEN
        assertThat(tareaPendiente.getCompletada()).isFalse();
        assertThat(tareaPendiente.getFechaCompletada()).isNull();
    }

    @Test
    @Transactional
    public void testMarcarComoCompletada() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        TareaData tarea = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea de prueba");
        
        // WHEN
        TareaData tareaCompletada = tareaService.marcarComoCompletada(tarea.getId());
        
        // THEN
        assertThat(tareaCompletada.getCompletada()).isTrue();
        assertThat(tareaCompletada.getFechaCompletada()).isNotNull();
        
        // Verificar que llamar de nuevo no cambia la fecha
        TareaData tareaCompletadaOtraVez = tareaService.marcarComoCompletada(tarea.getId());
        assertThat(tareaCompletadaOtraVez.getFechaCompletada()).isEqualTo(tareaCompletada.getFechaCompletada());
    }

    @Test
    @Transactional
    public void testMarcarComoPendiente() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        TareaData tarea = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea de prueba");
        tareaService.marcarComoCompletada(tarea.getId());
        
        // WHEN
        TareaData tareaPendiente = tareaService.marcarComoPendiente(tarea.getId());
        
        // THEN
        assertThat(tareaPendiente.getCompletada()).isFalse();
        assertThat(tareaPendiente.getFechaCompletada()).isNull();
    }

    @Test
    @Transactional
    public void testFiltrarTareasCompletadas() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 1");
        TareaData tarea2 = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 2");
        TareaData tarea3 = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 3");
        
        // Completar solo tarea1 y tarea3
        tareaService.marcarComoCompletada(tarea1.getId());
        tareaService.marcarComoCompletada(tarea3.getId());
        
        // WHEN
        List<TareaData> tareasCompletadas = tareaService.tareasCompletadasUsuario(usuario.getId());
        List<TareaData> tareasPendientes = tareaService.tareasPendientesUsuario(usuario.getId());
        
        // THEN
        assertThat(tareasCompletadas).hasSize(2);
        assertThat(tareasPendientes).hasSize(1);
        
        assertThat(tareasCompletadas.stream().map(TareaData::getTitulo))
                .containsExactlyInAnyOrder("Tarea 1", "Tarea 3");
        assertThat(tareasPendientes.stream().map(TareaData::getTitulo))
                .containsExactly("Tarea 2");
    }

    @Test
    @Transactional
    public void testContarTareas() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 1");
        TareaData tarea2 = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 2");
        TareaData tarea3 = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 3");
        
        tareaService.marcarComoCompletada(tarea2.getId());
        tareaService.marcarComoCompletada(tarea3.getId());
        
        // WHEN
        long completadas = tareaService.contarTareasCompletadas(usuario.getId());
        long pendientes = tareaService.contarTareasPendientes(usuario.getId());
        double porcentaje = tareaService.calcularPorcentajeCompletado(usuario.getId());
        
        // THEN
        assertThat(completadas).isEqualTo(2);
        assertThat(pendientes).isEqualTo(1);
        assertThat(porcentaje).isEqualTo(66.66666666666666);
    }

    @Test
    @Transactional
    public void testMarcarTodasComoCompletadas() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 1");
        tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 2");
        tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 3");
        
        // WHEN
        int tareasActualizadas = tareaService.marcarTodasComoCompletadas(usuario.getId());
        
        // THEN
        assertThat(tareasActualizadas).isEqualTo(3);
        
        List<TareaData> todasLasTareas = tareaService.allTareasUsuario(usuario.getId());
        assertThat(todasLasTareas).allMatch(TareaData::isCompletada);
    }

    @Test
    @Transactional
    public void testMarcarTodasComoPendientes() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 1");
        TareaData tarea2 = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 2");
        TareaData tarea3 = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 3");
        
        // Completar todas
        tareaService.marcarComoCompletada(tarea1.getId());
        tareaService.marcarComoCompletada(tarea2.getId());
        tareaService.marcarComoCompletada(tarea3.getId());
        
        // WHEN
        int tareasActualizadas = tareaService.marcarTodasComoPendientes(usuario.getId());
        
        // THEN
        assertThat(tareasActualizadas).isEqualTo(3);
        
        List<TareaData> todasLasTareas = tareaService.allTareasUsuario(usuario.getId());
        assertThat(todasLasTareas).allMatch(TareaData::isPendiente);
    }

    @Test
    @Transactional
    public void testEliminarTareasCompletadas() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        TareaData tarea1 = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 1");
        TareaData tarea2 = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 2");
        TareaData tarea3 = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea 3");
        
        // Completar tarea1 y tarea3
        tareaService.marcarComoCompletada(tarea1.getId());
        tareaService.marcarComoCompletada(tarea3.getId());
        
        // WHEN
        tareaService.eliminarTareasCompletadas(usuario.getId());
        
        // THEN
        List<TareaData> tareasRestantes = tareaService.allTareasUsuario(usuario.getId());
        assertThat(tareasRestantes).hasSize(1);
        assertThat(tareasRestantes.get(0).getTitulo()).isEqualTo("Tarea 2");
        assertThat(tareasRestantes.get(0).isCompletada()).isFalse();
    }

    @Test
    @Transactional
    public void testCalcularPorcentajeCompletadoSinTareas() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        
        // WHEN
        double porcentaje = tareaService.calcularPorcentajeCompletado(usuario.getId());
        
        // THEN
        assertThat(porcentaje).isEqualTo(0.0);
    }

    @Test
    @Transactional
    public void testPersistenciaEstadoCompletada() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        TareaData tarea = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea persistente");
        
        // WHEN
        tareaService.marcarComoCompletada(tarea.getId());
        
        // THEN - Verificar directamente en base de datos
        Tarea tareaEntity = tareaRepository.findById(tarea.getId()).orElse(null);
        assertThat(tareaEntity).isNotNull();
        assertThat(tareaEntity.getCompletada()).isTrue();
        assertThat(tareaEntity.getFechaCompletada()).isNotNull();
        
        // Verificar que al obtener por servicio mantiene el estado
        TareaData tareaRecuperada = tareaService.findById(tarea.getId());
        assertThat(tareaRecuperada.getCompletada()).isTrue();
    }

    @Test
    @Transactional
    public void testModificarTareaMantieneEstado() {
        // GIVEN
        UsuarioData usuario = crearUsuarioPrueba();
        TareaData tarea = tareaService.nuevaTareaUsuario(usuario.getId(), "Tarea original");
        tareaService.marcarComoCompletada(tarea.getId());
        
        // WHEN
        TareaData tareaModificada = tareaService.modificaTarea(tarea.getId(), "Tarea modificada");
        
        // THEN
        assertThat(tareaModificada.getTitulo()).isEqualTo("Tarea modificada");
        assertThat(tareaModificada.getCompletada()).isTrue(); // Estado se mantiene
        assertThat(tareaModificada.getFechaCompletada()).isNotNull();
    }

    @Test
    @Transactional
    public void testTareaServiceExceptionTareaNoExiste() {
        // WHEN & THEN
        assertThrows(TareaServiceException.class, () -> {
            tareaService.toggleTareaCompletada(999L);
        });
        
        assertThrows(TareaServiceException.class, () -> {
            tareaService.marcarComoCompletada(999L);
        });
        
        assertThrows(TareaServiceException.class, () -> {
            tareaService.marcarComoPendiente(999L);
        });
    }

    @Test
    @Transactional
    public void testMetodosConvenienciaTareaData() {
        // GIVEN
        TareaData tarea = new TareaData();
        
        // WHEN & THEN - Estado inicial
        assertThat(tarea.isCompletada()).isFalse();
        assertThat(tarea.isPendiente()).isTrue();
        assertThat(tarea.getEstadoTexto()).isEqualTo("Pendiente");
        assertThat(tarea.getEstadoIcono()).isEqualTo("⏳");
        assertThat(tarea.getEstadoCssClass()).isEqualTo("tarea-pendiente");
        
        // WHEN - Completar tarea
        tarea.setCompletada(true);
        
        // THEN
        assertThat(tarea.isCompletada()).isTrue();
        assertThat(tarea.isPendiente()).isFalse();
        assertThat(tarea.getEstadoTexto()).isEqualTo("Completada");
        assertThat(tarea.getEstadoIcono()).isEqualTo("✅");
        assertThat(tarea.getEstadoCssClass()).isEqualTo("tarea-completada");
    }
}