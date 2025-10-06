package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.CategoriaDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaCategoriaEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CategoriaEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.enums.AccionAuditoria;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.AuditoriaCategoriaRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.CategoriaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private AuditoriaCategoriaRepository auditoriaCategoriaRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private CategoriaService categoriaService;

    private CategoriaDTO categoriaDTO;
    private CategoriaEntity categoriaEntity;

    @BeforeEach
    void setUp() {
        categoriaDTO = new CategoriaDTO();
        categoriaDTO.setId(1L);
        categoriaDTO.setNombre("Pinturas");

        categoriaEntity = new CategoriaEntity();
        categoriaEntity.setId(1L);
        categoriaEntity.setNombre("Pinturas");

    }

    @Test
    void registrarCategoria_DeberiaCrearCategoria() {
        // Arrange
        when(modelMapper.map(any(CategoriaDTO.class), eq(CategoriaEntity.class)))
                .thenReturn(categoriaEntity);
        when(categoriaRepository.save(any(CategoriaEntity.class)))
                .thenReturn(categoriaEntity);
        when(modelMapper.map(any(CategoriaEntity.class), eq(CategoriaDTO.class)))
                .thenReturn(categoriaDTO);
        when(auditoriaCategoriaRepository.save(any(AuditoriaCategoriaEntity.class)))
                .thenReturn(new AuditoriaCategoriaEntity());

        // Act
        CategoriaDTO resultado = categoriaService.registrarCategoria(categoriaDTO, 1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Pinturas", resultado.getNombre());
        verify(categoriaRepository, times(1)).save(any(CategoriaEntity.class));
        verify(auditoriaCategoriaRepository, times(1)).save(any(AuditoriaCategoriaEntity.class));
    }

    @Test
    void registrarCategoria_DeberiaEstablecerIdNulo() {
        // Arrange
        categoriaDTO.setId(999L);
        when(modelMapper.map(any(CategoriaDTO.class), eq(CategoriaEntity.class)))
                .thenReturn(categoriaEntity);
        when(categoriaRepository.save(any(CategoriaEntity.class)))
                .thenReturn(categoriaEntity);
        when(modelMapper.map(any(CategoriaEntity.class), eq(CategoriaDTO.class)))
                .thenReturn(categoriaDTO);

        // Act
        categoriaService.registrarCategoria(categoriaDTO, 1L);

        // Assert
        assertNull(categoriaDTO.getId());
        verify(categoriaRepository, times(1)).save(any(CategoriaEntity.class));
    }

    @Test
    void modificarCategoria_DeberiaActualizarCategoria() {
        // Arrange
        CategoriaEntity original = new CategoriaEntity();
        original.setId(1L);
        original.setNombre("Nombre Original");

        CategoriaEntity actualizada = new CategoriaEntity();
        actualizada.setId(1L);
        actualizada.setNombre("Pinturas");

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(original));

        // Usamos doAnswer para manejar múltiples llamadas a map con diferentes argumentos
        doAnswer(invocation -> {
            Object source = invocation.getArgument(0);
            Object dest = invocation.getArgument(1);
            // Si el destino es CategoriaEntity vacía, es la copia
            if (dest instanceof CategoriaEntity && ((CategoriaEntity) dest).getId() == null) {
                ((CategoriaEntity) dest).setId(original.getId());
                ((CategoriaEntity) dest).setNombre(original.getNombre());
            }
            return null;
        }).when(modelMapper).map(any(CategoriaEntity.class), any(CategoriaEntity.class));

        when(modelMapper.map(any(CategoriaDTO.class), eq(CategoriaEntity.class)))
                .thenReturn(actualizada);
        when(categoriaRepository.save(any(CategoriaEntity.class)))
                .thenReturn(actualizada);
        when(modelMapper.map(any(CategoriaEntity.class), eq(CategoriaDTO.class)))
                .thenReturn(categoriaDTO);

        // Act
        CategoriaDTO resultado = categoriaService.modificarCategoria(1L, categoriaDTO, 1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Pinturas", resultado.getNombre());
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, times(1)).save(any(CategoriaEntity.class));
        verify(auditoriaCategoriaRepository, times(1)).save(any(AuditoriaCategoriaEntity.class));
    }

    @Test
    void modificarCategoria_DeberiaLanzarExcepcionSiNoExiste() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.modificarCategoria(1L, categoriaDTO, 1L);
        });

        assertEquals("Categoría no encontrada", exception.getMessage());
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, never()).save(any(CategoriaEntity.class));
    }

    @Test
    void eliminarCategoria_DeberiaEliminarYRegistrarAuditoria() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity));
        doNothing().when(categoriaRepository).delete(any(CategoriaEntity.class));
        when(auditoriaCategoriaRepository.save(any(AuditoriaCategoriaEntity.class)))
                .thenReturn(new AuditoriaCategoriaEntity());

        // Act
        categoriaService.eliminarCategoria(1L, 1L);

        // Assert
        verify(categoriaRepository, times(1)).findById(1L);
        verify(auditoriaCategoriaRepository, times(1)).save(any(AuditoriaCategoriaEntity.class));
        verify(categoriaRepository, times(1)).delete(categoriaEntity);
    }

    @Test
    void eliminarCategoria_DeberiaLanzarExcepcionSiNoExiste() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.eliminarCategoria(1L, 1L);
        });

        assertEquals("Categoría no encontrada", exception.getMessage());
        verify(categoriaRepository, times(1)).findById(1L);
        verify(categoriaRepository, never()).delete(any(CategoriaEntity.class));
    }

    @Test
    void consultarCategoria_DeberiaRetornarCategoria() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity));

        // Act
        CategoriaEntity resultado = categoriaService.consultarCategoria(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Pinturas", resultado.getNombre());
        verify(categoriaRepository, times(1)).findById(1L);
    }

    @Test
    void consultarCategoria_DeberiaLanzarExcepcionSiNoExiste() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoriaService.consultarCategoria(1L);
        });

        assertEquals("Categoría no encontrada", exception.getMessage());
    }

    @Test
    void listarCategorias_DeberiaRetornarListaDeCategorias() {
        // Arrange
        CategoriaEntity categoria2 = new CategoriaEntity();
        categoria2.setId(2L);
        categoria2.setNombre("Pinceles");

        List<CategoriaEntity> categorias = Arrays.asList(categoriaEntity, categoria2);
        when(categoriaRepository.findAll()).thenReturn(categorias);

        // Act
        List<CategoriaEntity> resultado = categoriaService.listarCategorias();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    void listarCategorias_DeberiaRetornarListaVacia() {
        // Arrange
        when(categoriaRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<CategoriaEntity> resultado = categoriaService.listarCategorias();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(categoriaRepository, times(1)).findAll();
    }

    @Test
    void consultarAuditoriaCategoria_DeberiaRetornarListaDeAuditorias() {
        // Arrange
        AuditoriaCategoriaEntity auditoria1 = new AuditoriaCategoriaEntity();
        auditoria1.setId(1L);
        auditoria1.setAccion(AccionAuditoria.CREAR);

        AuditoriaCategoriaEntity auditoria2 = new AuditoriaCategoriaEntity();
        auditoria2.setId(2L);
        auditoria2.setAccion(AccionAuditoria.MODIFICAR);

        List<AuditoriaCategoriaEntity> auditorias = Arrays.asList(auditoria1, auditoria2);
        when(auditoriaCategoriaRepository.findAll()).thenReturn(auditorias);

        // Act
        List<AuditoriaCategoriaEntity> resultado = categoriaService.consultarAuditoriaCategoria();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(auditoriaCategoriaRepository, times(1)).findAll();
    }
}