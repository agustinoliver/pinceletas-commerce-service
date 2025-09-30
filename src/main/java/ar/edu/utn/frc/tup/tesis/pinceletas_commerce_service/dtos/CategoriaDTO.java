package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CategoriaDTO {

    private Long id; // Opcional para crear, útil para modificar o devolver

    private String nombre;


}
