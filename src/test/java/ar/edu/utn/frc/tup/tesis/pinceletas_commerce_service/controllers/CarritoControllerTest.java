package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.CarritoRequestDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CarritoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.CarritoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
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
class CarritoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CarritoService carritoService;

    @InjectMocks
    private CarritoController carritoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(carritoController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void agregarProducto() throws Exception {
        // Given
        Long usuarioId = 1L;
        CarritoRequestDTO request = new CarritoRequestDTO();
        request.setProductoId(1L);
        request.setCantidad(2);

        CarritoEntity carritoEntity = new CarritoEntity();
        carritoEntity.setId(1L);
        carritoEntity.setUsuarioId(usuarioId);

        when(carritoService.agregarProducto(eq(usuarioId), eq(request.getProductoId()), eq(request.getCantidad())))
                .thenReturn(carritoEntity);

        // When & Then
        mockMvc.perform(post("/carrito")
                        .param("usuarioId", usuarioId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.usuarioId").value(usuarioId));

        verify(carritoService, times(1)).agregarProducto(usuarioId, request.getProductoId(), request.getCantidad());
    }

    @Test
    void modificarItem() throws Exception {
        // Given
        Long itemId = 1L;
        int nuevaCantidad = 5;

        CarritoEntity carritoActualizado = new CarritoEntity();
        carritoActualizado.setId(itemId);
        carritoActualizado.setCantidad(nuevaCantidad);

        when(carritoService.modificarItem(eq(itemId), eq(nuevaCantidad)))
                .thenReturn(carritoActualizado);

        // When & Then
        mockMvc.perform(put("/carrito/{itemId}", itemId)
                        .param("nuevaCantidad", String.valueOf(nuevaCantidad)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.cantidad").value(nuevaCantidad));

        verify(carritoService, times(1)).modificarItem(itemId, nuevaCantidad);
    }

    @Test
    void eliminarItem() throws Exception {
        // Given
        Long itemId = 1L;

        doNothing().when(carritoService).eliminarItem(itemId);

        // When & Then
        mockMvc.perform(delete("/carrito/{itemId}", itemId))
                .andExpect(status().isNoContent());

        verify(carritoService, times(1)).eliminarItem(itemId);
    }

    @Test
    void obtenerCarrito() throws Exception {
        // Given
        Long usuarioId = 1L;

        CarritoEntity item1 = new CarritoEntity();
        item1.setId(1L);
        item1.setUsuarioId(usuarioId);

        CarritoEntity item2 = new CarritoEntity();
        item2.setId(2L);
        item2.setUsuarioId(usuarioId);

        List<CarritoEntity> carrito = Arrays.asList(item1, item2);

        when(carritoService.obtenerCarrito(usuarioId)).thenReturn(carrito);

        // When & Then
        mockMvc.perform(get("/carrito/{usuarioId}", usuarioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(carritoService, times(1)).obtenerCarrito(usuarioId);
    }

    @Test
    void calcularTotal() throws Exception {
        // Given
        Long usuarioId = 1L;
        BigDecimal total = new BigDecimal("150.50");

        when(carritoService.calcularTotalCarrito(usuarioId)).thenReturn(total);

        // When & Then
        mockMvc.perform(get("/carrito/{usuarioId}/total", usuarioId))
                .andExpect(status().isOk())
                .andExpect(content().string("150.50")); // Cambiado a "150.50" para que coincida

        verify(carritoService, times(1)).calcularTotalCarrito(usuarioId);
    }
}