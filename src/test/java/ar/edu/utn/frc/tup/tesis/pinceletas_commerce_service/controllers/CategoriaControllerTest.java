package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.CategoriaDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.AuditoriaCategoriaEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CategoriaEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.CategoriaService;
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

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void crearCategoria() throws Exception {
        // Given
        Long usuarioId = 1L;
        CategoriaDTO categoriaDTO = new CategoriaDTO();
        categoriaDTO.setNombre("Electrónicos");


        CategoriaDTO categoriaCreada = new CategoriaDTO();
        categoriaCreada.setId(1L);
        categoriaCreada.setNombre("Electrónicos");


        when(categoriaService.registrarCategoria(any(CategoriaDTO.class), eq(usuarioId)))
                .thenReturn(categoriaCreada);

        // When & Then
        mockMvc.perform(post("/categorias")
                        .param("usuarioId", usuarioId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Electrónicos"));

        verify(categoriaService, times(1)).registrarCategoria(any(CategoriaDTO.class), eq(usuarioId));
    }

    @Test
    void modificarCategoria() throws Exception {
        // Given
        Long id = 1L;
        Long usuarioId = 1L;
        CategoriaDTO categoriaDTO = new CategoriaDTO();
        categoriaDTO.setNombre("Electrónicos Modificado");

        CategoriaDTO categoriaActualizada = new CategoriaDTO();
        categoriaActualizada.setId(id);
        categoriaActualizada.setNombre("Electrónicos Modificado");

        when(categoriaService.modificarCategoria(eq(id), any(CategoriaDTO.class), eq(usuarioId)))
                .thenReturn(categoriaActualizada);

        // When & Then
        mockMvc.perform(put("/categorias/{id}", id)
                        .param("usuarioId", usuarioId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoriaDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Electrónicos Modificado"));

        verify(categoriaService, times(1)).modificarCategoria(eq(id), any(CategoriaDTO.class), eq(usuarioId));
    }

    @Test
    void eliminarCategoria() throws Exception {
        // Given
        Long id = 1L;
        Long usuarioId = 1L;

        doNothing().when(categoriaService).eliminarCategoria(id, usuarioId);

        // When & Then
        mockMvc.perform(delete("/categorias/{id}", id)
                        .param("usuarioId", usuarioId.toString()))
                .andExpect(status().isNoContent());

        verify(categoriaService, times(1)).eliminarCategoria(id, usuarioId);
    }

    @Test
    void listarCategoriasEntity() throws Exception {
        // Given
        CategoriaEntity categoria1 = new CategoriaEntity();
        categoria1.setId(1L);
        categoria1.setNombre("Categoria 1");

        CategoriaEntity categoria2 = new CategoriaEntity();
        categoria2.setId(2L);
        categoria2.setNombre("Categoria 2");

        List<CategoriaEntity> categorias = Arrays.asList(categoria1, categoria2);

        when(categoriaService.listarCategorias()).thenReturn(categorias);

        // When & Then
        mockMvc.perform(get("/categorias/all-con-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[1].id").value(2L));

        verify(categoriaService, times(1)).listarCategorias();
    }

    @Test
    void obtenerCategoriaEntity() throws Exception {
        // Given
        Long id = 1L;
        CategoriaEntity categoria = new CategoriaEntity();
        categoria.setId(id);
        categoria.setNombre("Electrónicos");

        when(categoriaService.consultarCategoria(id)).thenReturn(categoria);

        // When & Then
        mockMvc.perform(get("/categorias/{id}/one-con-products", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("Electrónicos"));

        verify(categoriaService, times(1)).consultarCategoria(id);
    }

    @Test
    void obtenerAuditoria() throws Exception {
        // Given
        AuditoriaCategoriaEntity auditoria1 = new AuditoriaCategoriaEntity();
        auditoria1.setId(1L);

        AuditoriaCategoriaEntity auditoria2 = new AuditoriaCategoriaEntity();
        auditoria2.setId(2L);

        List<AuditoriaCategoriaEntity> auditorias = Arrays.asList(auditoria1, auditoria2);

        when(categoriaService.consultarAuditoriaCategoria()).thenReturn(auditorias);

        // When & Then
        mockMvc.perform(get("/categorias/auditoria"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(categoriaService, times(1)).consultarAuditoriaCategoria();
    }
}