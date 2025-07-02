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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class NavBarTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerUserSession managerUserSession;

    @MockBean
    private UsuarioService usuarioService;

    private void setupAuthenticatedUser(Long userId, String nombre, String email) {
        UsuarioData mockUser = new UsuarioData();
        mockUser.setId(userId);
        mockUser.setNombre(nombre);
        mockUser.setEmail(email);

        when(managerUserSession.usuarioLogeado()).thenReturn(userId);
        when(usuarioService.findById(userId)).thenReturn(mockUser);
    }

    @Test
    public void muestraNavBarParaUsuarioAutenticado() throws Exception {
        setupAuthenticatedUser(1L, "Santos", "s@gmail.com");

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Tareas")))
                .andExpect(content().string(containsString("Santos")))
                .andExpect(content().string(containsString("nav-link dropdown-toggle")))
                .andExpect(content().string(containsString("navbar navbar-expand-lg navbar-dark bg-dark")))
                .andExpect(model().attribute("isAuthenticated", true));
    }

    @Test
    public void muestraNavBarParaUsuarioNoAutenticado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Iniciar Sesión")))
                .andExpect(content().string(containsString("Registrarse")))
                .andExpect(content().string(containsString("navbar navbar-expand-lg navbar-dark bg-dark")))
                .andExpect(model().attribute("isAuthenticated", false));
    }

    @Test
    public void muestraEnlacesCorrectosParaUsuarioAutenticado() throws Exception {
        setupAuthenticatedUser(1L, "Fernando Aldaz", "fernando@example.com");

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/tareas\"")))
                .andExpect(content().string(containsString("href=\"/account\"")))
                .andExpect(content().string(containsString("href=\"/logout\"")))
                .andExpect(content().string(containsString("href=\"/about\"")))
                .andExpect(content().string(not(containsString("href=\"/login\""))))
                .andExpect(content().string(not(containsString("href=\"/registro\""))));
    }

    @Test
    public void muestraEnlacesCorrectosParaUsuarioNoAutenticado() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(containsString("href=\"/registro\"")))
                .andExpect(content().string(containsString("href=\"/about\"")))
                .andExpect(content().string(not(containsString("href=\"/tareas\""))))
                .andExpect(content().string(not(containsString("href=\"/account\""))));
    }

    @Test
    public void contieneClasesDeBootstrapEnLaNavBar() throws Exception {
        when(managerUserSession.usuarioLogeado()).thenReturn(null);

        mockMvc.perform(get("/about"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("navbar navbar-expand-lg navbar-dark bg-dark")))
                .andExpect(content().string(containsString("navbar-brand")))
                .andExpect(content().string(containsString("navbar-nav")))
                .andExpect(content().string(containsString("nav-link")));
    }
}
