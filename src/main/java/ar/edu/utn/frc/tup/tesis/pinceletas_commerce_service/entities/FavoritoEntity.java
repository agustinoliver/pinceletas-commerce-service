package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "favoritos")
public class FavoritoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long usuarioId; // ID del usuario externo

    @ManyToOne
    @JoinColumn(name = "producto_id")
    private ProductoEntity producto;
}
