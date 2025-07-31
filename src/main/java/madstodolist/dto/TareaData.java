package madstodolist.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

public class TareaData implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String titulo;
    private Long usuarioId;
    private Boolean completada = false;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCompletada;

    // Constructor vacío
    public TareaData() {
        this.completada = false;
    }

    // Constructor con parámetros básicos
    public TareaData(String titulo, Long usuarioId) {
        this.titulo = titulo;
        this.usuarioId = usuarioId;
        this.completada = false;
    }

    // Constructor completo
    public TareaData(Long id, String titulo, Long usuarioId, Boolean completada) {
        this.id = id;
        this.titulo = titulo;
        this.usuarioId = usuarioId;
        this.completada = completada != null ? completada : false;
    }

    // Getters y setters

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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Boolean getCompletada() {
        return completada;
    }

    public void setCompletada(Boolean completada) {
        this.completada = completada != null ? completada : false;
    }

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

    // Métodos de conveniencia

    public boolean isCompletada() {
        return completada != null && completada;
    }

    public boolean isPendiente() {
        return !isCompletada();
    }

    public String getEstadoTexto() {
        return isCompletada() ? "Completada" : "Pendiente";
    }

    public String getEstadoIcono() {
        return isCompletada() ? "✅" : "⏳";
    }

    // Método para obtener el CSS class según el estado
    public String getEstadoCssClass() {
        return isCompletada() ? "tarea-completada" : "tarea-pendiente";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TareaData))
            return false;
        TareaData tareaData = (TareaData) o;
        return Objects.equals(id, tareaData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TareaData{" +
                "id=" + id +
                ", titulo='" + titulo + '\'' +
                ", usuarioId=" + usuarioId +
                ", completada=" + completada +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaCompletada=" + fechaCompletada +
                '}';
    }
}