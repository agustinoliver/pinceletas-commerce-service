package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.CategoriaDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaCategoriaEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CategoriaEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.AccionAuditoria;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.AuditoriaCategoriaRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.CategoriaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final AuditoriaCategoriaRepository auditoriaCategoriaRepository;
    private final ModelMapper modelMapper;

    public CategoriaDTO registrarCategoria(CategoriaDTO dto, Long usuarioId) {
        dto.setId(null); // Asegura que se cree una nueva entidad
        CategoriaEntity entity = modelMapper.map(dto, CategoriaEntity.class);
        CategoriaEntity saved = categoriaRepository.save(entity);
        registrarAuditoria(null, saved, AccionAuditoria.CREAR, usuarioId);
        return modelMapper.map(saved, CategoriaDTO.class);
    }

    public CategoriaDTO modificarCategoria(Long id, CategoriaDTO dto, Long usuarioId) {
        CategoriaEntity original = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        CategoriaEntity copiaAnterior = new CategoriaEntity();
        modelMapper.map(original, copiaAnterior);

        CategoriaEntity nueva = modelMapper.map(dto, CategoriaEntity.class);
        nueva.setId(id);
        CategoriaEntity actualizada = categoriaRepository.save(nueva);

        registrarAuditoria(copiaAnterior, actualizada, AccionAuditoria.MODIFICAR, usuarioId);
        return modelMapper.map(actualizada, CategoriaDTO.class);
    }

    public void eliminarCategoria(Long id, Long usuarioId) {
        CategoriaEntity categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
        // ✅ Primero registramos la auditoría
        registrarAuditoria(categoria, null, AccionAuditoria.ELIMINAR, usuarioId);

        // ✅ Luego eliminamos la categoría
        categoriaRepository.delete(categoria);
    }

    public CategoriaEntity consultarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));
    }

    public List<CategoriaEntity> listarCategorias() {
        return categoriaRepository.findAll();
    }

    public List<AuditoriaCategoriaEntity> consultarAuditoriaCategoria() {
        return auditoriaCategoriaRepository.findAll();
    }

    private void registrarAuditoria(CategoriaEntity anterior, CategoriaEntity nuevo, AccionAuditoria accion, Long usuarioId) {
        AuditoriaCategoriaEntity auditoria = new AuditoriaCategoriaEntity();

        Long categoriaId = (nuevo != null) ? nuevo.getId() : (anterior != null ? anterior.getId() : null);
        if (categoriaId == null) {
            throw new IllegalStateException("No se puede auditar una categoría sin ID");
        }

        auditoria.setCategoriaId(categoriaId);
        auditoria.setAccion(accion);
        auditoria.setUsuarioId(usuarioId);
        auditoria.setFechaAccion(LocalDateTime.now());

        auditoria.setValoresAnteriores(anterior != null ? serializar(anterior) : null);
        auditoria.setValoresNuevos(nuevo != null ? serializar(nuevo) : null);

        auditoriaCategoriaRepository.save(auditoria);
    }

    private String serializar(CategoriaEntity categoria) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(categoria);
        } catch (Exception e) {
            return "{}";
        }
    }
}
