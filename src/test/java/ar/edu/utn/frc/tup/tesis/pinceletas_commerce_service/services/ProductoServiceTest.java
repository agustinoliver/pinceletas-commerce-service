package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.ProductoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CategoriaEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.OpcionProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.ProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.AuditoriaProductoRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.CategoriaRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.OpcionProductoRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private AuditoriaProductoRepository auditoriaProductoRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private OpcionProductoRepository opcionProductoRepository;

    @Mock
    private MultipartFile imagenFile;

    @InjectMocks
    private ProductoService productoService;

    private ProductoDTO productoDTO;
    private ProductoEntity productoEntity;
    private CategoriaEntity categoriaEntity;
    private OpcionProductoEntity opcionEntity;

    @BeforeEach
    void setUp() {
        categoriaEntity = new CategoriaEntity();
        categoriaEntity.setId(1L);
        categoriaEntity.setNombre("Pinturas");

        opcionEntity = new OpcionProductoEntity();
        opcionEntity.setId(1L);
        opcionEntity.setTipo("Color Rojo");

        // ✅ CORREGIDO: ProductoDTO con lista de imágenes
        productoDTO = new ProductoDTO();
        productoDTO.setId(1L);
        productoDTO.setNombre("Pintura Acrílica");
        productoDTO.setDescripcion("Pintura de alta calidad");
        productoDTO.setPrecio(new BigDecimal("1500.00"));
        productoDTO.setImagenes(Arrays.asList("/uploads/imagen1.jpg", "/uploads/imagen2.jpg")); // ✅ CAMBIADO
        productoDTO.setActivo(true);
        productoDTO.setCategoriaId(1L);
        productoDTO.setOpcionesIds(Arrays.asList(1L));

        // ✅ CORREGIDO: ProductoEntity con lista de imágenes
        productoEntity = new ProductoEntity();
        productoEntity.setId(1L);
        productoEntity.setNombre("Pintura Acrílica");
        productoEntity.setDescripcion("Pintura de alta calidad");
        productoEntity.setPrecio(new BigDecimal("1500.00"));
        productoEntity.setImagenes(Arrays.asList("/uploads/imagen1.jpg", "/uploads/imagen2.jpg")); // ✅ CAMBIADO
        productoEntity.setActivo(true);
        productoEntity.setCategoria(categoriaEntity);
        productoEntity.setOpciones(Arrays.asList(opcionEntity));
    }

    @Test
    void registrarProductoConImagen_DeberiaCrearProducto() throws IOException {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity)); // ✅ AGREGADO
        when(opcionProductoRepository.findAllById(anyList()))
                .thenReturn(Arrays.asList(opcionEntity));
        when(productoRepository.save(any(ProductoEntity.class)))
                .thenReturn(productoEntity);
        when(imagenFile.isEmpty()).thenReturn(false);
        when(imagenFile.getOriginalFilename()).thenReturn("imagen.jpg");
        when(imagenFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // Act
        ProductoEntity resultado = productoService.registrarProductoConImagen(productoDTO, imagenFile, 1L);

        // Assert
        assertNotNull(resultado);
        assertEquals("Pintura Acrílica", resultado.getNombre());
        verify(productoRepository, times(1)).save(any(ProductoEntity.class));
        verify(auditoriaProductoRepository, times(1)).save(any(AuditoriaProductoEntity.class));
    }

    @Test
    void registrarProductoConImagen_SinImagen_DeberiaCrearProducto() {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity)); // ✅ AGREGADO
        when(opcionProductoRepository.findAllById(anyList()))
                .thenReturn(Arrays.asList(opcionEntity));
        when(productoRepository.save(any(ProductoEntity.class)))
                .thenReturn(productoEntity);

        // Act
        ProductoEntity resultado = productoService.registrarProductoConImagen(productoDTO, null, 1L);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository, times(1)).save(any(ProductoEntity.class));
    }

    @Test
    void registrarProductoConImagen_ErrorAlGuardarImagen_DeberiaLanzarExcepcion() throws IOException {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity)); // ✅ AGREGADO
        when(imagenFile.isEmpty()).thenReturn(false);
        when(imagenFile.getOriginalFilename()).thenReturn("imagen.jpg");
        when(imagenFile.getInputStream()).thenThrow(new IOException("Error de lectura"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.registrarProductoConImagen(productoDTO, imagenFile, 1L);
        });

        assertTrue(exception.getMessage().contains("Error al guardar imagen"));
    }

    @Test
    void modificarProducto_DeberiaActualizarProducto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEntity));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity));
        when(opcionProductoRepository.findAllById(anyList()))
                .thenReturn(Arrays.asList(opcionEntity));
        when(productoRepository.save(any(ProductoEntity.class)))
                .thenReturn(productoEntity);

        // ✅ CORREGIDO: Mock del mapper para evitar NPE
        ProductoEntity productoAnterior = new ProductoEntity();
        productoAnterior.setId(1L);
        productoAnterior.setNombre("Nombre Anterior");
        when(modelMapper.map(any(ProductoEntity.class), eq(ProductoEntity.class)))
                .thenReturn(productoAnterior);

        // Act
        ProductoDTO resultado = productoService.modificarProducto(1L, productoDTO, 1L);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository, times(1)).findById(1L);
        verify(productoRepository, times(1)).save(any(ProductoEntity.class));
        verify(auditoriaProductoRepository, times(1)).save(any(AuditoriaProductoEntity.class));
    }

    @Test
    void modificarProducto_DeberiaLanzarExcepcionSiNoExiste() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.modificarProducto(1L, productoDTO, 1L);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productoRepository, never()).save(any(ProductoEntity.class));
    }

    @Test
    void modificarProducto_CategoriaNoExiste_DeberiaLanzarExcepcion() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEntity));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        // ✅ CORREGIDO: Mock del mapper para evitar NPE
        ProductoEntity productoAnterior = new ProductoEntity();
        productoAnterior.setId(1L);
        productoAnterior.setNombre("Nombre Anterior");
        when(modelMapper.map(any(ProductoEntity.class), eq(ProductoEntity.class)))
                .thenReturn(productoAnterior);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.modificarProducto(1L, productoDTO, 1L);
        });

        assertEquals("Categoría no encontrada", exception.getMessage());
    }

    @Test
    void modificarProductoConImagen_DeberiaActualizarConNuevaImagen() throws IOException {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEntity));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity));
        when(opcionProductoRepository.findAllById(anyList()))
                .thenReturn(Arrays.asList(opcionEntity));
        when(productoRepository.save(any(ProductoEntity.class)))
                .thenReturn(productoEntity);
        when(imagenFile.isEmpty()).thenReturn(false);
        when(imagenFile.getOriginalFilename()).thenReturn("nueva-imagen.jpg");
        when(imagenFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        // ✅ CORREGIDO: Mock del mapper para evitar NPE
        ProductoEntity productoAnterior = new ProductoEntity();
        productoAnterior.setId(1L);
        productoAnterior.setNombre("Nombre Anterior");
        when(modelMapper.map(any(ProductoEntity.class), eq(ProductoEntity.class)))
                .thenReturn(productoAnterior);

        // Act
        ProductoEntity resultado = productoService.modificarProductoConImagen(1L, productoDTO, imagenFile, 1L);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository, times(1)).save(any(ProductoEntity.class));
        verify(auditoriaProductoRepository, times(1)).save(any(AuditoriaProductoEntity.class));
    }

    @Test
    void eliminarProducto_DeberiaEliminarYRegistrarAuditoria() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEntity));
        doNothing().when(productoRepository).delete(any(ProductoEntity.class));

        // Act
        productoService.eliminarProducto(1L, 1L);

        // Assert
        verify(productoRepository, times(1)).findById(1L);
        verify(auditoriaProductoRepository, times(1)).save(any(AuditoriaProductoEntity.class));
        verify(productoRepository, times(1)).delete(productoEntity);
    }

    @Test
    void eliminarProducto_DeberiaLanzarExcepcionSiNoExiste() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.eliminarProducto(1L, 1L);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(productoRepository, never()).delete(any(ProductoEntity.class));
    }

    @Test
    void getProductoById_DeberiaRetornarProducto() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEntity));

        // Act
        ProductoEntity resultado = productoService.getProductoById(1L);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Pintura Acrílica", resultado.getNombre());
        verify(productoRepository, times(1)).findById(1L);
    }

    @Test
    void getProductoById_DeberiaLanzarExcepcionSiNoExiste() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productoService.getProductoById(1L);
        });

        assertEquals("Producto no encontrado", exception.getMessage());
    }

    @Test
    void listarProductos_DeberiaRetornarListaDeProductos() {
        // Arrange
        ProductoEntity producto2 = new ProductoEntity();
        producto2.setId(2L);
        producto2.setNombre("Pincel");
        producto2.setImagenes(Arrays.asList("/uploads/pincel.jpg")); // ✅ CORREGIDO

        List<ProductoEntity> productos = Arrays.asList(productoEntity, producto2);
        when(productoRepository.findAll()).thenReturn(productos);

        // Act
        List<ProductoEntity> resultado = productoService.listarProductos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(productoRepository, times(1)).findAll();
    }

    @Test
    void consultarAuditoriasProductos_DeberiaRetornarListaDeAuditorias() {
        // Arrange
        AuditoriaProductoEntity auditoria1 = new AuditoriaProductoEntity();
        auditoria1.setId(1L);

        AuditoriaProductoEntity auditoria2 = new AuditoriaProductoEntity();
        auditoria2.setId(2L);

        List<AuditoriaProductoEntity> auditorias = Arrays.asList(auditoria1, auditoria2);
        when(auditoriaProductoRepository.findAll()).thenReturn(auditorias);

        // Act
        List<AuditoriaProductoEntity> resultado = productoService.consultarAuditoriasProductos();

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        verify(auditoriaProductoRepository, times(1)).findAll();
    }

    // ✅ NUEVO: Test para validar descuento
    @Test
    void modificarProducto_DescuentoInvalido_DeberiaLanzarExcepcion() {
        // Arrange
        when(productoRepository.findById(1L)).thenReturn(Optional.of(productoEntity));

        ProductoDTO dtoConDescuentoInvalido = new ProductoDTO();
        dtoConDescuentoInvalido.setDescuentoPorcentaje(new BigDecimal("150")); // Descuento inválido

        // ✅ CORREGIDO: Mock del mapper para evitar NPE
        ProductoEntity productoAnterior = new ProductoEntity();
        productoAnterior.setId(1L);
        productoAnterior.setNombre("Nombre Anterior");
        when(modelMapper.map(any(ProductoEntity.class), eq(ProductoEntity.class)))
                .thenReturn(productoAnterior);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            productoService.modificarProducto(1L, dtoConDescuentoInvalido, 1L);
        });

        assertTrue(exception.getMessage().contains("El descuento debe estar entre 0 y 100"));
    }

    // ✅ NUEVO: Test para múltiples imágenes
    @Test
    void registrarProductoConMultiplesImagenes_DeberiaCrearProducto() throws IOException {
        // Arrange
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoriaEntity));
        when(opcionProductoRepository.findAllById(anyList()))
                .thenReturn(Arrays.asList(opcionEntity));
        when(productoRepository.save(any(ProductoEntity.class)))
                .thenReturn(productoEntity);

        MultipartFile imagenFile1 = mock(MultipartFile.class);
        MultipartFile imagenFile2 = mock(MultipartFile.class);

        when(imagenFile1.isEmpty()).thenReturn(false);
        when(imagenFile1.getOriginalFilename()).thenReturn("imagen1.jpg");
        when(imagenFile1.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        when(imagenFile2.isEmpty()).thenReturn(false);
        when(imagenFile2.getOriginalFilename()).thenReturn("imagen2.jpg");
        when(imagenFile2.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        List<MultipartFile> imagenes = Arrays.asList(imagenFile1, imagenFile2);

        // Act
        ProductoEntity resultado = productoService.registrarProductoConMultiplesImagenes(
                productoDTO, imagenes, 1L);

        // Assert
        assertNotNull(resultado);
        verify(productoRepository, times(1)).save(any(ProductoEntity.class));
        verify(auditoriaProductoRepository, times(1)).save(any(AuditoriaProductoEntity.class));
    }
}