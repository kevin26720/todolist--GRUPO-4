# Documentación Técnica - Práctica 2

## Introducción

Este documento recoge los principales cambios y nuevas funcionalidades implementadas en la aplicación ToDo como parte de la Práctica 2. Su objetivo es proporcionar una visión clara y técnica de las modificaciones realizadas, dirigida al equipo de desarrollo. La documentación cubre las clases, plantillas Thymeleaf y controladores añadidos, así como ejemplos relevantes de código.

---

## Nuevas funcionalidades implementadas

### 1. **Barra de navegación (Navbar)**

Se ha añadido una barra de menú común (`navbar.html`) a todas las páginas de la aplicación, excepto en las páginas de login y registro. Esta barra está construida con Bootstrap y contiene:

- **Enlace "ToDoList"**: redirige a la página "Acerca de".
- **Enlace "Tareas"**: muestra la lista de tareas del usuario logueado.
- **Nombre de usuario (derecha)** con menú desplegable:
  - **Cuenta** (para futura implementación).
  - **Cerrar sesión**.

En la página "Acerca de", la barra de navegación se adapta:
- Si el usuario **está logueado**, se muestra la barra general.
- Si el usuario **no está logueado**, se muestran enlaces a "Iniciar sesión" y "Registro".

### 2. **Listado de usuarios registrados**

Se ha implementado una nueva ruta `/registrados` que permite acceder a una vista (`registrados.html`) donde se listan todos los usuarios registrados, mostrando:
- Identificador (`ID`).
- Correo electrónico.

Cada entrada del listado incluye un enlace para ver la **descripción detallada** del usuario.

### 3. **Descripción de usuario**

Nueva ruta implementada: `/registrados/{id}`, donde se muestra una página con los siguientes datos del usuario:
- ID
- Nombre
- Email

**Nota:** La contraseña no se muestra por motivos de seguridad.

---

## Nuevas plantillas Thymeleaf

Se agregaron o modificaron las siguientes vistas:

- `navbar.html`: nueva barra de navegación general reutilizable.
- `about.html`: adaptada para mostrar diferente barra según estado de sesión.
- `registrados.html`: muestra la lista de usuarios registrados.
- `usuarioDescripcion.html` (presumiblemente): muestra los datos de un usuario.

---

## Nuevas clases y métodos relevantes

Aunque no se han introducido muchas clases nuevas, sí se ampliaron o ajustaron las siguientes capas:

### Controladores:
- Se añadieron métodos en el controlador de usuario para manejar rutas `/registrados` y `/registrados/{id}`.

Ejemplo de método añadido:

```java
@GetMapping("/registrados")
public String listaUsuarios(Model model) {
    List<Usuario> usuarios = usuarioService.findAll();
    model.addAttribute("usuarios", usuarios);
    return "registrados";
}
