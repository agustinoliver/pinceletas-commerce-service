package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
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

    // ✅ CAMBIO: Ya no es una sola imagen, ahora es una lista
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "producto_imagenes", joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "imagen_url")
    @OrderColumn(name = "orden") // Mantiene el orden de las imágenes
    private List<String> imagenes = new ArrayList<>();

    private Boolean activo;

    @Column(nullable = false)
    private BigDecimal descuentoPorcentaje = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.EAGER) // ✅ Asegura que cargue la categoría
    @JoinColumn(name = "categoria_id")
    @JsonIgnoreProperties("productos") // ✅ Ignora solo la lista de productos dentro de categoría
    private CategoriaEntity categoria;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "producto_opciones",
            joinColumns = @JoinColumn(name = "producto_id"),
            inverseJoinColumns = @JoinColumn(name = "opcion_producto_id")
    )
    private List<OpcionProductoEntity> opciones;

    // ✅ NUEVO: Método helper para obtener la primera imagen (imagen principal)
    @JsonIgnore
    public String getImagenPrincipal() {
        return imagenes != null && !imagenes.isEmpty() ? imagenes.get(0) : null;
    }
}
