package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "opciones_producto")
public class OpcionProductoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "producto_id")
    @JsonBackReference
    private ProductoEntity producto;

    private String tipo;

}
