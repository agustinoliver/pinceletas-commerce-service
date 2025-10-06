package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.ProductoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.ProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.ProductoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productoController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void crearProductoConImagen() throws Exception {
        // Given
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        ProductoEntity productoEntity = new ProductoEntity();
        productoEntity.setId(1L);
        productoEntity.setNombre("Producto Test");

        when(productoService.registrarProductoConImagen(any(ProductoDTO.class), any(), anyLong()))
                .thenReturn(productoEntity);

        // When & Then
        mockMvc.perform(multipart("/productos/productos")
                        .file(imagen)
                        .param("nombre", "Producto Test")
                        .param("descripcion", "Descripción test")
                        .param("precio", "99.99")
                        .param("activo", "true")
                        .param("categoriaId", "1")
                        .param("usuarioId", "1")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Producto Test"));

        verify(productoService, times(1)).registrarProductoConImagen(any(ProductoDTO.class), any(), eq(1L));
    }

    @Test
    void modificarProductoConImagen() throws Exception {
        // Given
        Long id = 1L;
        MockMultipartFile imagen = new MockMultipartFile(
                "imagen",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        ProductoEntity productoEntity = new ProductoEntity();
        productoEntity.setId(id);
        productoEntity.setNombre("Producto Modificado");

        when(productoService.modificarProductoConImagen(eq(id), any(ProductoDTO.class), any(), anyLong()))
                .thenReturn(productoEntity);

        // When & Then
        mockMvc.perform(multipart("/productos/{id}/con-imagen", id)
                        .file(imagen)
                        .param("nombre", "Producto Modificado")
                        .param("precio", "199.99")
                        .param("activo", "true")
                        .param("categoriaId", "1")
                        .param("usuarioId", "1")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Producto Modificado"));

        verify(productoService, times(1)).modificarProductoConImagen(eq(id), any(ProductoDTO.class), any(), eq(1L));
    }

    @Test
    void modificarProducto() throws Exception {
        // Given
        Long id = 1L;
        Long usuarioId = 1L;
        ProductoDTO productoDTO = new ProductoDTO();
        productoDTO.setNombre("Producto Modificado");

        // El servicio modificarProducto retorna ProductoDTO según tu implementación
        ProductoDTO productoModificadoDTO = new ProductoDTO();
        productoModificadoDTO.setId(id);
        productoModificadoDTO.setNombre("Producto Modificado");

        when(productoService.modificarProducto(eq(id), any(ProductoDTO.class), eq(usuarioId)))
                .thenReturn(productoModificadoDTO);

        // El servicio getProductoById retorna ProductoEntity
        ProductoEntity productoEntity = new ProductoEntity();
        productoEntity.setId(id);
        productoEntity.setNombre("Producto Modificado");

        when(productoService.getProductoById(id)).thenReturn(productoEntity);

        // When & Then
        mockMvc.perform(put("/productos/{id}", id)
                        .param("usuarioId", usuarioId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Producto Modificado"));

        verify(productoService, times(1)).modificarProducto(eq(id), any(ProductoDTO.class), eq(usuarioId));
        verify(productoService, times(1)).getProductoById(id);
    }

    @Test
    void eliminarProducto() throws Exception {
        // Given
        Long id = 1L;
        Long usuarioId = 1L;

        doNothing().when(productoService).eliminarProducto(id, usuarioId);

        // When & Then
        mockMvc.perform(delete("/productos/{id}", id)
                        .param("usuarioId", usuarioId.toString()))
                .andExpect(status().isNoContent());

        verify(productoService, times(1)).eliminarProducto(id, usuarioId);
    }

    @Test
    void obtenerProducto() throws Exception {
        // Given
        Long id = 1L;
        ProductoEntity producto = new ProductoEntity();
        producto.setId(id);
        producto.setNombre("Producto Test");

        when(productoService.getProductoById(id)).thenReturn(producto);

        // When & Then
        mockMvc.perform(get("/productos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Producto Test"));

        verify(productoService, times(1)).getProductoById(id);
    }

    @Test
    void listarProductos() throws Exception {
        // Given
        ProductoEntity producto1 = new ProductoEntity();
        producto1.setId(1L);

        ProductoEntity producto2 = new ProductoEntity();
        producto2.setId(2L);

        List<ProductoEntity> productos = Arrays.asList(producto1, producto2);

        when(productoService.listarProductos()).thenReturn(productos);

        // When & Then
        mockMvc.perform(get("/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(productoService, times(1)).listarProductos();
    }

    @Test
    void obtenerAuditorias() throws Exception {
        // Given
        AuditoriaProductoEntity auditoria1 = new AuditoriaProductoEntity();
        auditoria1.setId(1L);

        AuditoriaProductoEntity auditoria2 = new AuditoriaProductoEntity();
        auditoria2.setId(2L);

        List<AuditoriaProductoEntity> auditorias = Arrays.asList(auditoria1, auditoria2);

        when(productoService.consultarAuditoriasProductos()).thenReturn(auditorias);

        // When & Then
        mockMvc.perform(get("/productos/auditorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(productoService, times(1)).consultarAuditoriasProductos();
    }
}