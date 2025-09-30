package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.AccionAuditoria;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "auditoria_categorias")
public class AuditoriaCategoriaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "categoria_id", nullable = false)
    private Long categoriaId; // ✅ Solo el ID, sin @ManyToOne

    private Long usuarioId;

    @Enumerated(EnumType.STRING)
    private AccionAuditoria accion;

    @Column(columnDefinition = "TEXT")
    private String valoresAnteriores;

    @Column(columnDefinition = "TEXT")
    private String valoresNuevos;

    private LocalDateTime fechaAccion;
}
