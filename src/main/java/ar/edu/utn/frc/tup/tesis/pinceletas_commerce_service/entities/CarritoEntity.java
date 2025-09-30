package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "carrito")
public class CarritoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId; // Solo se guarda el ID del usuario, no se relaciona como entidad

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private ProductoEntity producto;

    private Integer cantidad;

}
