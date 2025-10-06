package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.FavoritoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.FavoritoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.ProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.FavoritoRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoritoServiceTest {

    @Mock
    private FavoritoRepository favoritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private FavoritoService favoritoService;

    private FavoritoDTO favoritoDTO;
    private ProductoEntity productoEntity;
    private FavoritoEntity favoritoEntity;

    @BeforeEach
    void setUp() {
        productoEntity = new ProductoEntity();
        productoEntity.setId(1L);
        productoEntity.setNombre("Pintura Acrílica");
        productoEntity.setPrecio(new BigDecimal("1500.00"));

        favoritoDTO = new FavoritoDTO();
        favoritoDTO.setUsuarioId(1L);
        favoritoDTO.setProductoId(1L);

        favoritoEntity = new FavoritoEntity();
        favoritoEntity.setId(1L);
        favoritoEntity.setUsuarioId(1L);
        favoritoEntity.setProducto(productoEntity);
    }

    @Test
    void agregarAFavoritos_DeberiaAgregarProducto() {
        // Arrange
        when(favoritoRepository.existsByUsuarioIdAndProductoId(1L, 1L)).thenReturn(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEntity));
        when(favoritoRepository.save(any(FavoritoEntity.class))).thenReturn(favoritoEntity);

        // Act
        FavoritoEntity resultado = favoritoService.agregarAFavoritos(favoritoDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getUsuarioId());
        assertEquals(productoEntity, resultado.getProducto());
        verify(favoritoRepository, times(1)).existsByUsuarioIdAndProductoId(1L, 1L);
        verify(productoRepository, times(1)).findById(1L);
        verify(favoritoRepository, times(1)).save(any(FavoritoEntity.class));
    }

    @Test
    void agregarAFavoritos_YaExiste_DeberiaLanzarExcepcion() {
        // Arrange
        when(favoritoRepository.existsByUsuarioIdAndProductoId(1L, 1L)).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            favoritoService.agregarAFavoritos(favoritoDTO);
        });

        assertEquals("Ya está en favoritos", exception.getMessage());
        verify(favoritoRepository, times(1)).existsByUsuarioIdAndProductoId(1L, 1L);
        verify(productoRepository, never()).findById(any());
        verify(favoritoRepository, never()).save(any(FavoritoEntity.class));
    }

    @Test
    void agregarAFavoritos_ProductoNoExiste_DeberiaLanzarExcepcion() {
        // Arrange
        when(favoritoRepository.existsByUsuarioIdAndProductoId(1L, 1L)).thenReturn(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            favoritoService.agregarAFavoritos(favoritoDTO);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(favoritoRepository, times(1)).existsByUsuarioIdAndProductoId(1L, 1L);
        verify(productoRepository, times(1)).findById(1L);
        verify(favoritoRepository, never()).save(any(FavoritoEntity.class));
    }

    @Test
    void agregarAFavoritos_ConUsuarioDiferente_DeberiaAgregar() {
        // Arrange
        favoritoDTO.setUsuarioId(2L);
        when(favoritoRepository.existsByUsuarioIdAndProductoId(2L, 1L)).thenReturn(false);
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEntity));
        favoritoEntity.setUsuarioId(2L);
        when(favoritoRepository.save(any(FavoritoEntity.class))).thenReturn(favoritoEntity);

        // Act
        FavoritoEntity resultado = favoritoService.agregarAFavoritos(favoritoDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(2L, resultado.getUsuarioId());
        verify(favoritoRepository, times(1)).save(any(FavoritoEntity.class));
    }

    @Test
    void eliminarDeFavoritos_DeberiaEliminarFavorito() {
        // Arrange
        doNothing().when(favoritoRepository).deleteByUsuarioIdAndProductoId(1L, 1L);

        // Act
        favoritoService.eliminarDeFavoritos(1L, 1L);

        // Assert
        verify(favoritoRepository, times(1)).deleteByUsuarioIdAndProductoId(1L, 1L);
    }

    @Test
    void eliminarDeFavoritos_ConIdsInvalidos_DeberiaEjecutarDelete() {
        // Arrange
        doNothing().when(favoritoRepository).deleteByUsuarioIdAndProductoId(999L, 999L);

        // Act
        favoritoService.eliminarDeFavoritos(999L, 999L);

        // Assert
        verify(favoritoRepository, times(1)).deleteByUsuarioIdAndProductoId(999L, 999L);
    }

    @Test
    void eliminarDeFavoritos_ProductoNoEnFavoritos_DeberiaEjecutarDelete() {
        // Arrange
        doNothing().when(favoritoRepository).deleteByUsuarioIdAndProductoId(1L, 2L);

        // Act
        favoritoService.eliminarDeFavoritos(1L, 2L);

        // Assert
        verify(favoritoRepository, times(1)).deleteByUsuarioIdAndProductoId(1L, 2L);
    }

    @Test
    void obtenerFavoritos_DeberiaRetornarListaDeFavoritos() {
        // Arrange
        ProductoEntity producto2 = new ProductoEntity();
        producto2.setId(2L);
        producto2.setNombre("Pincel");

        FavoritoEntity favorito2 = new FavoritoEntity();
        favorito2.setId(2L);
        favorito2.setUsuarioId(1L);
        favorito2.setProducto(producto2);

        List<FavoritoEntity> favoritos = Arrays.asList(favoritoEntity, favorito2);
        when(favoritoRepository.findByUsuarioId(1L)).thenReturn(favoritos);

        // Act
        List<FavoritoEntity> resultado = favoritoService.obtenerFavoritos(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getUsuarioId());
        assertEquals(1L, resultado.get(1).getUsuarioId());
        verify(favoritoRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    void obtenerFavoritos_ListaVacia_DeberiaRetornarListaVacia() {
        // Arrange
        when(favoritoRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList());

        // Act
        List<FavoritoEntity> resultado = favoritoService.obtenerFavoritos(1L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(favoritoRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    void obtenerFavoritos_UsuarioSinFavoritos_DeberiaRetornarListaVacia() {
        // Arrange
        when(favoritoRepository.findByUsuarioId(999L)).thenReturn(Arrays.asList());

        // Act
        List<FavoritoEntity> resultado = favoritoService.obtenerFavoritos(999L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(favoritoRepository, times(1)).findByUsuarioId(999L);
    }

    @Test
    void obtenerFavoritos_ConUnSoloFavorito_DeberiaRetornarLista() {
        // Arrange
        when(favoritoRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList(favoritoEntity));

        // Act
        List<FavoritoEntity> resultado = favoritoService.obtenerFavoritos(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(productoEntity, resultado.get(0).getProducto());
        verify(favoritoRepository, times(1)).findByUsuarioId(1L);
    }
}