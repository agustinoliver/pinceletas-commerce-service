package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CarritoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.ProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.CarritoRepository;
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
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CarritoService carritoService;

    private ProductoEntity productoEntity;
    private CarritoEntity carritoEntity;

    @BeforeEach
    void setUp() {
        productoEntity = new ProductoEntity();
        productoEntity.setId(1L);
        productoEntity.setNombre("Pintura Acrílica");
        productoEntity.setPrecio(new BigDecimal("1500.00"));

        carritoEntity = new CarritoEntity();
        carritoEntity.setId(1L);
        carritoEntity.setUsuarioId(1L);
        carritoEntity.setProducto(productoEntity);
        carritoEntity.setCantidad(2);
    }

    @Test
    void agregarProducto_DeberiaAgregarProductoAlCarrito() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEntity));
        when(carritoRepository.save(any(CarritoEntity.class))).thenReturn(carritoEntity);

        // Act
        CarritoEntity resultado = carritoService.agregarProducto(1L, 1L, 2);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getUsuarioId());
        assertEquals(2, resultado.getCantidad());
        assertEquals(productoEntity, resultado.getProducto());
        verify(productoRepository, times(1)).findById(1L);
        verify(carritoRepository, times(1)).save(any(CarritoEntity.class));
    }

    @Test
    void agregarProducto_ProductoNoExiste_DeberiaLanzarExcepcion() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            carritoService.agregarProducto(1L, 1L, 2);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productoRepository, times(1)).findById(1L);
        verify(carritoRepository, never()).save(any(CarritoEntity.class));
    }

    @Test
    void agregarProducto_ConCantidadCero_DeberiaCrearItem() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEntity));
        carritoEntity.setCantidad(0);
        when(carritoRepository.save(any(CarritoEntity.class))).thenReturn(carritoEntity);

        // Act
        CarritoEntity resultado = carritoService.agregarProducto(1L, 1L, 0);

        // Assert
        assertNotNull(resultado);
        assertEquals(0, resultado.getCantidad());
        verify(carritoRepository, times(1)).save(any(CarritoEntity.class));
    }

    @Test
    void modificarItem_DeberiaActualizarCantidad() {
        // Arrange
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carritoEntity));
        CarritoEntity itemActualizado = new CarritoEntity();
        itemActualizado.setId(1L);
        itemActualizado.setCantidad(5);
        when(carritoRepository.save(any(CarritoEntity.class))).thenReturn(itemActualizado);

        // Act
        CarritoEntity resultado = carritoService.modificarItem(1L, 5);

        // Assert
        assertNotNull(resultado);
        assertEquals(5, resultado.getCantidad());
        verify(carritoRepository, times(1)).findById(1L);
        verify(carritoRepository, times(1)).save(any(CarritoEntity.class));
    }

    @Test
    void modificarItem_ItemNoExiste_DeberiaLanzarExcepcion() {
        // Arrange
        when(carritoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            carritoService.modificarItem(1L, 5);
        });

        assertEquals("Item no encontrado", exception.getMessage());
        verify(carritoRepository, times(1)).findById(1L);
        verify(carritoRepository, never()).save(any(CarritoEntity.class));
    }

    @Test
    void modificarItem_ConCantidadNegativa_DeberiaActualizarItem() {
        // Arrange
        when(carritoRepository.findById(1L)).thenReturn(Optional.of(carritoEntity));
        carritoEntity.setCantidad(-1);
        when(carritoRepository.save(any(CarritoEntity.class))).thenReturn(carritoEntity);

        // Act
        CarritoEntity resultado = carritoService.modificarItem(1L, -1);

        // Assert
        assertNotNull(resultado);
        assertEquals(-1, resultado.getCantidad());
        verify(carritoRepository, times(1)).save(any(CarritoEntity.class));
    }

    @Test
    void eliminarItem_DeberiaEliminarItemDelCarrito() {
        // Arrange
        doNothing().when(carritoRepository).deleteById(1L);

        // Act
        carritoService.eliminarItem(1L);

        // Assert
        verify(carritoRepository, times(1)).deleteById(1L);
    }

    @Test
    void eliminarItem_ConIdInvalido_DeberiaEjecutarDelete() {
        // Arrange
        doNothing().when(carritoRepository).deleteById(999L);

        // Act
        carritoService.eliminarItem(999L);

        // Assert
        verify(carritoRepository, times(1)).deleteById(999L);
    }

    @Test
    void obtenerCarrito_DeberiaRetornarItemsDelUsuario() {
        // Arrange
        CarritoEntity item2 = new CarritoEntity();
        item2.setId(2L);
        item2.setUsuarioId(1L);
        item2.setCantidad(3);

        List<CarritoEntity> items = Arrays.asList(carritoEntity, item2);
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(items);

        // Act
        List<CarritoEntity> resultado = carritoService.obtenerCarrito(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(1L, resultado.get(0).getUsuarioId());
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    void obtenerCarrito_CarritoVacio_DeberiaRetornarListaVacia() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList());

        // Act
        List<CarritoEntity> resultado = carritoService.obtenerCarrito(1L);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    void calcularTotalCarrito_DeberiaCalcularTotalCorrectamente() {
        // Arrange
        ProductoEntity producto2 = new ProductoEntity();
        producto2.setId(2L);
        producto2.setPrecio(new BigDecimal("2500.00"));

        CarritoEntity item2 = new CarritoEntity();
        item2.setId(2L);
        item2.setUsuarioId(1L);
        item2.setProducto(producto2);
        item2.setCantidad(3);

        List<CarritoEntity> items = Arrays.asList(carritoEntity, item2);
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(items);

        // Act
        BigDecimal total = carritoService.calcularTotalCarrito(1L);

        // Assert
        // Producto 1: 1500 * 2 = 3000
        // Producto 2: 2500 * 3 = 7500
        // Total: 10500
        assertNotNull(total);
        assertEquals(new BigDecimal("10500.00"), total);
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    void calcularTotalCarrito_CarritoVacio_DeberiaRetornarCero() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList());

        // Act
        BigDecimal total = carritoService.calcularTotalCarrito(1L);

        // Assert
        assertNotNull(total);
        assertEquals(BigDecimal.ZERO, total);
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
    }

    @Test
    void calcularTotalCarrito_ConUnSoloItem_DeberiaCalcularCorrectamente() {
        // Arrange
        when(carritoRepository.findByUsuarioId(1L)).thenReturn(Arrays.asList(carritoEntity));

        // Act
        BigDecimal total = carritoService.calcularTotalCarrito(1L);

        // Assert
        // 1500 * 2 = 3000
        assertNotNull(total);
        assertEquals(new BigDecimal("3000.00"), total);
        verify(carritoRepository, times(1)).findByUsuarioId(1L);
    }
}