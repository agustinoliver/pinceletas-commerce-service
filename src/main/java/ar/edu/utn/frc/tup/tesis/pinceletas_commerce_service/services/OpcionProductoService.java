package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.OpcionProductoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.OpcionProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.OpcionProductoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class OpcionProductoService {

    private final OpcionProductoRepository opcionRepository;

    // Crear opción (sin producto asignado)
    public OpcionProductoDTO crearOpcion(OpcionProductoDTO dto) {
        OpcionProductoEntity opcion = new OpcionProductoEntity();
        opcion.setTipo(dto.getTipo());


        OpcionProductoEntity saved = opcionRepository.save(opcion);
        return mapToDto(saved);
    }

    // Modificar opción existente por ID
    public OpcionProductoDTO modificarOpcion(Long id, OpcionProductoDTO dto) {
        OpcionProductoEntity actual = opcionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Opción no encontrada"));

        actual.setTipo(dto.getTipo());


        OpcionProductoEntity saved = opcionRepository.save(actual);
        return mapToDto(saved);
    }

    // Eliminar opción por ID
    public void eliminarOpcion(Long id) {
        opcionRepository.deleteById(id);
    }

    // Obtener opciones por producto (con producto asignado)
    public List<OpcionProductoDTO> obtenerPorProducto(Long productoId) {
        List<OpcionProductoEntity> opciones = opcionRepository.findByProductoId(productoId);
        return opciones.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Obtener todas las opciones (independiente de producto)
    public List<OpcionProductoDTO> listarTodas() {
        List<OpcionProductoEntity> opciones = opcionRepository.findAll();
        return opciones.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Mapper entity -> dto
    private OpcionProductoDTO mapToDto(OpcionProductoEntity entity) {
        OpcionProductoDTO dto = new OpcionProductoDTO();
        dto.setId(entity.getId());
        dto.setTipo(entity.getTipo());

        return dto;
    }
}
