package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.OpcionProductoDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.OpcionProductoService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OpcionProductoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OpcionProductoService opcionService;

    @InjectMocks
    private OpcionProductoController opcionProductoController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(opcionProductoController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void crearOpcion() throws Exception {
        // Given
        OpcionProductoDTO opcionDTO = new OpcionProductoDTO();
        opcionDTO.setTipo("Color");

        OpcionProductoDTO opcionCreada = new OpcionProductoDTO();
        opcionCreada.setId(1L);
        opcionCreada.setTipo("Color");

        when(opcionService.crearOpcion(any(OpcionProductoDTO.class)))
                .thenReturn(opcionCreada);

        // When & Then
        mockMvc.perform(post("/opciones-productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(opcionDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.tipo").value("Color"));

        verify(opcionService, times(1)).crearOpcion(any(OpcionProductoDTO.class));
    }

    @Test
    void modificarOpcion() throws Exception {
        // Given
        Long id = 1L;
        OpcionProductoDTO opcionDTO = new OpcionProductoDTO();
        opcionDTO.setTipo("Color Modificado");

        OpcionProductoDTO opcionActualizada = new OpcionProductoDTO();
        opcionActualizada.setId(id);
        opcionActualizada.setTipo("Color Modificado");

        when(opcionService.modificarOpcion(eq(id), any(OpcionProductoDTO.class)))
                .thenReturn(opcionActualizada);

        // When & Then
        mockMvc.perform(put("/opciones-productos/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(opcionDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.tipo").value("Color Modificado"));

        verify(opcionService, times(1)).modificarOpcion(eq(id), any(OpcionProductoDTO.class));
    }

    @Test
    void eliminarOpcion() throws Exception {
        // Given
        Long id = 1L;

        doNothing().when(opcionService).eliminarOpcion(id);

        // When & Then
        mockMvc.perform(delete("/opciones-productos/{id}", id))
                .andExpect(status().isNoContent());

        verify(opcionService, times(1)).eliminarOpcion(id);
    }

    @Test
    void obtenerOpcionesPorProducto() throws Exception {
        // Given
        Long productoId = 1L;

        OpcionProductoDTO opcion1 = new OpcionProductoDTO();
        opcion1.setId(1L);
        opcion1.setTipo("Color");

        OpcionProductoDTO opcion2 = new OpcionProductoDTO();
        opcion2.setId(2L);
        opcion2.setTipo("Tamaño");

        List<OpcionProductoDTO> opciones = Arrays.asList(opcion1, opcion2);

        when(opcionService.obtenerPorProducto(productoId)).thenReturn(opciones);

        // When & Then
        mockMvc.perform(get("/opciones-productos/producto/{productoId}", productoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(opcionService, times(1)).obtenerPorProducto(productoId);
    }

    @Test
    void listarTodasOpciones() throws Exception {
        // Given
        OpcionProductoDTO opcion1 = new OpcionProductoDTO();
        opcion1.setId(1L);

        OpcionProductoDTO opcion2 = new OpcionProductoDTO();
        opcion2.setId(2L);

        List<OpcionProductoDTO> opciones = Arrays.asList(opcion1, opcion2);

        when(opcionService.listarTodas()).thenReturn(opciones);

        // When & Then
        mockMvc.perform(get("/opciones-productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(opcionService, times(1)).listarTodas();
    }
}