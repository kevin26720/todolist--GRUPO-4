package madstodolist.controller;

import madstodolist.authentication.ManagerUserSession;
import madstodolist.dto.LoginData;
import madstodolist.dto.RegistroData;
import madstodolist.dto.UsuarioData;
import madstodolist.service.UsuarioService;
import madstodolist.service.UsuarioService.LoginStatus;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
public class LoginControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private UsuarioService usuarioService;

        @MockBean
        private ManagerUserSession managerUserSession;

        @Test
        public void homeRedirigeALogin() throws Exception {
                mockMvc.perform(get("/"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        public void muestraLogin() throws Exception {
                mockMvc.perform(get("/login"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("formLogin"))
                                .andExpect(model().attributeExists("loginData"));
        }

        @Test
        public void loginExitoso() throws Exception {
                LoginData loginData = new LoginData();
                loginData.seteMail("test@example.com");
                loginData.setPassword("password");

                UsuarioData usuario = new UsuarioData();
                usuario.setId(1L);

                when(usuarioService.login("test@example.com", "password")).thenReturn(LoginStatus.LOGIN_OK);
                when(usuarioService.findByEmail("test@example.com")).thenReturn(usuario);

                mockMvc.perform(post("/login").flashAttr("loginData", loginData))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/usuarios/1/tareas"));
        }

        @Test
        public void loginUsuarioNoExiste() throws Exception {
                LoginData loginData = new LoginData();
                loginData.seteMail("nonexistent@example.com");
                loginData.setPassword("password");

                when(usuarioService.login("nonexistent@example.com", "password"))
                                .thenReturn(LoginStatus.USER_NOT_FOUND);

                mockMvc.perform(post("/login").flashAttr("loginData", loginData))
                                .andExpect(status().isOk())
                                .andExpect(view().name("formLogin"))
                                .andExpect(model().attribute("error", "No existe usuario"));
        }

        @Test
        public void muestraRegistro() throws Exception {
                mockMvc.perform(get("/registro"))
                                .andExpect(status().isOk())
                                .andExpect(view().name("formRegistro"))
                                .andExpect(model().attributeExists("registroData"));
        }

        @Test
        public void registroExitoso() throws Exception {
                RegistroData registroData = new RegistroData();
                registroData.setEmail("new@example.com");
                registroData.setPassword("password");
                registroData.setNombre("New User");

                when(usuarioService.findByEmail("new@example.com")).thenReturn(null);

                mockMvc.perform(post("/registro").flashAttr("registroData", registroData))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));
        }

        @Test
        public void logout() throws Exception {
                mockMvc.perform(get("/logout"))
                                .andExpect(status().is3xxRedirection())
                                .andExpect(redirectedUrl("/login"));

                verify(managerUserSession).logout();
        }
}
