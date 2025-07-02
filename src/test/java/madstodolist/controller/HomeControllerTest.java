package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerUserSession managerUserSession;

    @MockBean
    private UsuarioService usuarioService;

    @Test
    public void aboutAutenticado() throws Exception {
        Long idUsuario = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(idUsuario);
        usuario.setNombre("Test User");

        when(managerUserSession.usuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(usuario);

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"))
                .andExpect(model().attribute("isAuthenticated", true))
                .andExpect(model().attribute("usuario", usuario));
    }

    @Test
    public void aboutNoAutenticado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(view().name("about"))
                .andExpect(model().attribute("isAuthenticated", false))
                .andExpect(model().attributeDoesNotExist("usuario"));
    }

    @Test
    public void accountAutenticado() throws Exception {
        Long idUsuario = 1L;
        UsuarioData usuario = new UsuarioData();
        usuario.setId(idUsuario);

        when(managerUserSession.usuarioLogeado()).thenReturn(idUsuario);
        when(usuarioService.findById(idUsuario)).thenReturn(usuario);

        mockMvc.perform(get("/account"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/usuarios/1/tareas"));
    }

    @Test
    public void accountNoAutenticado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/account"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }
}
