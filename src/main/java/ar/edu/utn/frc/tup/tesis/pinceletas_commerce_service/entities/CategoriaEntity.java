package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "categorias")
public class CategoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @OneToMany(mappedBy = "categoria")
    @JsonManagedReference // ✅ Omitir si no necesitás incluir productos dentro de categoría
    private List<ProductoEntity> productos;
}
