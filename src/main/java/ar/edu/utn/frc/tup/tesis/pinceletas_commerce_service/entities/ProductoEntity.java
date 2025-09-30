package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "productos")
public class ProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private BigDecimal precio;

    private String imagen;

    private Boolean activo;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    @JsonBackReference // 🔁 Evita que se serialice "productos" dentro de la categoría
    private CategoriaEntity categoria;

    @ManyToMany
    @JoinTable(
            name = "producto_opciones",
            joinColumns = @JoinColumn(name = "producto_id"),
            inverseJoinColumns = @JoinColumn(name = "opcion_producto_id")
    )
    private List<OpcionProductoEntity> opciones;

}
