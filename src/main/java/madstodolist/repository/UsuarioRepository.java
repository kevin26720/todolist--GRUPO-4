package madstodolist.repository;

import madstodolist.model.Usuario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.tareas WHERE u.id = :id")
    Usuario findByIdWithTareas(@Param("id") Long id);

    Optional<Usuario> findByEmail(String email);
}
