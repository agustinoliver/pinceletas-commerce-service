package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String telefono;

    // Campos de dirección
    private String calle;
    private String numero;
    private String ciudad;
    private String piso;
    private String barrio;
    private String pais;
    private String provincia;
    private String codigoPostal;
    private String manzana;
    private String lote;
}
