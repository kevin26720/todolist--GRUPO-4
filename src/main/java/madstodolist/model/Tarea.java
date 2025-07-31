package madstodolist.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;
import java.time.LocalDateTime;

@Entity
@Table(name = "tareas")
public class Tarea implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    private String titulo;

    // Nuevo campo para marcar como completada
    @Column(name = "completada", nullable = false, columnDefinition = "boolean default false")
    private Boolean completada = false;

    // Campos de auditoría para seguimiento
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_completada")
    private LocalDateTime fechaCompletada;

    @NotNull
    // Relación muchos-a-uno entre tareas y usuario
    @ManyToOne
    // Nombre de la columna en la BD que guarda físicamente
    // el ID del usuario con el que está asociado una tarea
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    // Constructor vacío necesario para JPA/Hibernate.
    // No debe usarse desde la aplicación.
    public Tarea() {
        this.completada = false;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Al crear una tarea la asociamos automáticamente a un usuario
    public Tarea(Usuario usuario, String titulo) {
        this.titulo = titulo;
        this.completada = false;
        this.fechaCreacion = LocalDateTime.now();
        setUsuario(usuario); // Esto añadirá la tarea a la lista de tareas del usuario
    }

    // Getters y setters básicos

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    // Getters y setters para el campo completada
    public Boolean getCompletada() {
        return completada;
    }

    public void setCompletada(Boolean completada) {
        this.completada = completada;
        // Actualizar fecha de completada cuando se marca/desmarca
        if (completada != null && completada) {
            this.fechaCompletada = LocalDateTime.now();
        } else {
            this.fechaCompletada = null;
        }
    }

    // Método de conveniencia para marcar como completada
    public void marcarComoCompletada() {
        setCompletada(true);
    }

    // Método de conveniencia para marcar como pendiente
    public void marcarComoPendiente() {
        setCompletada(false);
    }

    // Método de conveniencia para alternar estado
    public void alternarEstado() {
        setCompletada(!getCompletada());
    }

    // Getters y setters para fechas de auditoría
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaCompletada() {
        return fechaCompletada;
    }

    public void setFechaCompletada(LocalDateTime fechaCompletada) {
        this.fechaCompletada = fechaCompletada;
    }

    // Getters y setters de la relación muchos-a-uno con Usuario

    public Usuario getUsuario() {
        return usuario;
    }

    // Método para establecer la relación con el usuario
    public void setUsuario(Usuario usuario) {
        // Comprueba si el usuario ya está establecido
        if(this.usuario != usuario) {
            this.usuario = usuario;
            // Añade la tarea a la lista de tareas del usuario
            if (usuario != null) {
                usuario.addTarea(this);
            }
        }
    }

    // Método de callback para establecer fecha de creación automáticamente
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (completada == null) {
            completada = false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tarea tarea = (Tarea) o;
        if (id != null && tarea.id != null)
            // Si tenemos los ID, comparamos por ID
            return Objects.equals(id, tarea.id);
        // si no comparamos por campos obligatorios
        return titulo.equals(tarea.titulo) &&
                usuario.equals(tarea.usuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titulo, usuario);
    }

    @Override
    public String toString() {
        return "Tarea{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", completada=" + completada +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaCompletada=" + fechaCompletada +
                '}';
    }
}