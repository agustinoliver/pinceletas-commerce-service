package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.OpcionProductoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.OpcionProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.OpcionProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpcionProductoServiceTest {

    @Mock
    private OpcionProductoRepository opcionRepository;

    @InjectMocks
    private OpcionProductoService opcionProductoService;

    private OpcionProductoDTO opcionDTO;
    private OpcionProductoEntity opcionEntity;

    @BeforeEach
    void setUp() {
        opcionDTO = new OpcionProductoDTO();
        opcionDTO.setId(1L);
        opcionDTO.setTipo("Color Rojo");

        opcionEntity = new OpcionProductoEntity();
        opcionEntity.setId(1L);
        opcionEntity.setTipo("Color Rojo");
    }

    @Test
    void crearOpcion_DeberiaCrearNuevaOpcion() {
        // Arrange
        when(opcionRepository.save(any(OpcionProductoEntity.class))).thenReturn(opcionEntity);

        // Act
        OpcionProductoDTO resultado = opcionProductoService.crearOpcion(opcionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Color Rojo", resultado.getTipo());
        verify(opcionRepository, times(1)).save(any(OpcionProductoEntity.class));
    }

    @Test
    void crearOpcion_ConTipoVacio_DeberiaCrearOpcion() {
        // Arrange
        opcionDTO.setTipo("");
        opcionEntity.setTipo("");
        when(opcionRepository.save(any(OpcionProductoEntity.class))).thenReturn(opcionEntity);

        // Act
        OpcionProductoDTO resultado = opcionProductoService.crearOpcion(opcionDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals("", resultado.getTipo());
        verify(opcionRepository, times(1)).save(any(OpcionProductoEntity.class));
    }

    @Test
    void crearOpcion_ConTipoNull_DeberiaCrearOpcion() {
        // Arrange
        opcionDTO.setTipo(null);
        opcionEntity.setTipo(null);
        when(opcionRepository.save(any(OpcionProductoEntity.class))).thenReturn(opcionEntity);

        // Act
        OpcionProductoDTO resultado = opcionProductoService.crearOpcion(opcionDTO);

        // Assert
        assertNotNull(resultado);
        assertNull(resultado.getTipo());
        verify(opcionRepository, times(1)).save(any(OpcionProductoEntity.class));
    }

    @Test
    void modificarOpcion_DeberiaActualizarOpcion() {
        // Arrange
        OpcionProductoDTO dtoActualizado = new OpcionProductoDTO();
        dtoActualizado.setTipo("Color Azul");

        OpcionProductoEntity entityActualizada = new OpcionProductoEntity();
        entityActualizada.setId(1L);
        entityActualizada.setTipo("Color Azul");

        when(opcionRepository.findById(1L)).thenReturn(Optional.of(opcionEntity));
        when(opcionRepository.save(any(OpcionProductoEntity.class))).thenReturn(entityActualizada);

        // Act
        OpcionProductoDTO resultado = opcionProductoService.modificarOpcion(1L, dtoActualizado);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Color Azul", resultado.getTipo());
        verify(opcionRepository, times(1)).findById(1L);
        verify(opcionRepository, times(1)).save(any(OpcionProductoEntity.class));
    }

    @Test
    void modificarOpcion_OpcionNoExiste_DeberiaLanzarExcepcion() {
        // Arrange
        when(opcionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            opcionProductoService.modificarOpcion(1L, opcionDTO);
        });

        assertEquals("Opción no encontrada", exception.getMessage());
        verify(opcionRepository, times(1)).findById(1L);
        verify(opcionRepository, never()).save(any(OpcionProductoEntity.class));
    }

    @Test
    void modificarOpcion_ConIdInvalido_DeberiaLanzarExcepcion() {
        // Arrange
        when(opcionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            opcionProductoService.modificarOpcion(999L, opcionDTO);
        });

        assertEquals("Opción no encontrada", exception.getMessage());
        verify(opcionRepository, times(1)).findById(999L);
    }

    @Test
    void eliminarOpcion_DeberiaEliminarOpcion() {
        // Arrange
        doNothing().when(opcionRepository).deleteById(1L);

        // Act
        opcionProductoService.eliminarOpcion(1L);

        // Assert
        verify(opcionRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminarOpcion_ConIdInvalido_DeberiaEjecutarDelete() {
        // Arrange
        doNothing().when(opcionRepository).deleteById(999L);

        // Act
        opcionProductoService.eliminarOpcion(999L);

        // Assert
        verify(opcionRepository, times(1)).deleteById(999L);
    }

    @Test
    void obtenerPorProducto_DeberiaRetornarOpcionesDelProducto() {
        // Arrange
        OpcionProductoEntity opcion2 = new OpcionProductoEntity();
        opcion2.setId(2L);
        opcion2.setTipo("Tamaño Grande");

        List<OpcionProductoEntity> opciones = Arrays.asList(opcionEntity, opcion2);
        when(opcionRepository.findByProductoId(1L)).thenReturn(opciones);

        // Act
        List<OpcionProductoDTO> resultado = opcionProductoService.obtenerPorProducto(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals("Color Rojo", resultado.get(0).getTipo());
        assertEquals("Tamaño Grande", resultado.get(1).getTipo());
        verify(opcionRepository, times(1)).findByProductoId(1L);
    }

    @Test
    void obtenerPorProducto_ProductoSinOpciones_DeberiaRetornarListaVacia() {
        // Arrange
        when(opcionRepository.findByProductoId(1L)).thenReturn(Arrays.asList());

        // Act
        List<OpcionProductoDTO> resultado = opcionProductoService.obtenerPorProducto(1L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(opcionRepository, times(1)).findByProductoId(1L);
    }

    @Test
    void obtenerPorProducto_ProductoNoExiste_DeberiaRetornarListaVacia() {
        // Arrange
        when(opcionRepository.findByProductoId(999L)).thenReturn(Arrays.asList());

        // Act
        List<OpcionProductoDTO> resultado = opcionProductoService.obtenerPorProducto(999L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(opcionRepository, times(1)).findByProductoId(999L);
    }

    @Test
    void listarTodas_DeberiaRetornarTodasLasOpciones() {
        // Arrange
        OpcionProductoEntity opcion2 = new OpcionProductoEntity();
        opcion2.setId(2L);
        opcion2.setTipo("Tamaño Grande");

        OpcionProductoEntity opcion3 = new OpcionProductoEntity();
        opcion3.setId(3L);
        opcion3.setTipo("Material Acrílico");

        List<OpcionProductoEntity> opciones = Arrays.asList(opcionEntity, opcion2, opcion3);
        when(opcionRepository.findAll()).thenReturn(opciones);

        // Act
        List<OpcionProductoDTO> resultado = opcionProductoService.listarTodas();

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size());
        assertEquals("Color Rojo", resultado.get(0).getTipo());
        assertEquals("Tamaño Grande", resultado.get(1).getTipo());
        assertEquals("Material Acrílico", resultado.get(2).getTipo());
        verify(opcionRepository, times(1)).findAll();
    }

    @Test
    void listarTodas_SinOpciones_DeberiaRetornarListaVacia() {
        // Arrange
        when(opcionRepository.findAll()).thenReturn(Arrays.asList());

        // Act
        List<OpcionProductoDTO> resultado = opcionProductoService.listarTodas();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(opcionRepository, times(1)).findAll();
    }

    @Test
    void listarTodas_ConUnaOpcion_DeberiaRetornarLista() {
        // Arrange
        when(opcionRepository.findAll()).thenReturn(Arrays.asList(opcionEntity));

        // Act
        List<OpcionProductoDTO> resultado = opcionProductoService.listarTodas();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("Color Rojo", resultado.get(0).getTipo());
        verify(opcionRepository, times(1)).findAll();
    }
}