package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.controllers;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.ConfiguracionEnvioRequestDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.dtos.ConfiguracionEnvioResponseDTO;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services.ConfiguracionEnvioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/configuraciones-envio")
@RequiredArgsConstructor
public class ConfiguracionEnvioController {

    private final ConfiguracionEnvioService configuracionEnvioService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ConfiguracionEnvioResponseDTO> crearConfiguracion(
            @RequestBody ConfiguracionEnvioRequestDTO requestDTO) {
        ConfiguracionEnvioResponseDTO response = configuracionEnvioService.crearConfiguracion(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ConfiguracionEnvioResponseDTO> actualizarConfiguracion(
            @PathVariable Long id,
            @RequestBody ConfiguracionEnvioRequestDTO requestDTO) {
        ConfiguracionEnvioResponseDTO response = configuracionEnvioService.actualizarConfiguracion(id, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<ConfiguracionEnvioResponseDTO>> obtenerTodasLasConfiguraciones() {
        List<ConfiguracionEnvioResponseDTO> configuraciones = configuracionEnvioService.obtenerTodasLasConfiguraciones();
        return ResponseEntity.ok(configuraciones);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ConfiguracionEnvioResponseDTO> obtenerConfiguracionPorId(@PathVariable Long id) {
        ConfiguracionEnvioResponseDTO configuracion = configuracionEnvioService.obtenerConfiguracionPorId(id);
        return ResponseEntity.ok(configuracion);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarConfiguracion(@PathVariable Long id) {
        configuracionEnvioService.eliminarConfiguracion(id);
        return ResponseEntity.noContent().build();
    }

    // Endpoint público para obtener la configuración activa
    @GetMapping("/activa")
    public ResponseEntity<ConfiguracionEnvioResponseDTO> obtenerConfiguracionActiva() {
        ConfiguracionEnvioResponseDTO configuracion = configuracionEnvioService.obtenerConfiguracionActiva();
        return ResponseEntity.ok(configuracion);
    }

    // Endpoint para calcular costo de envío (puede ser usado por el frontend)
    @GetMapping("/calcular-costo")
    public ResponseEntity<CostoEnvioResponse> calcularCostoEnvio(@RequestParam BigDecimal subtotal) {
        BigDecimal costo = configuracionEnvioService.obtenerCostoEnvio(subtotal);
        return ResponseEntity.ok(new CostoEnvioResponse(costo));
    }

    // Clase interna para la respuesta del cálculo
    public static class CostoEnvioResponse {
        private BigDecimal costoEnvio;

        public CostoEnvioResponse(BigDecimal costoEnvio) {
            this.costoEnvio = costoEnvio;
        }

        public BigDecimal getCostoEnvio() {
            return costoEnvio;
        }

        public void setCostoEnvio(BigDecimal costoEnvio) {
            this.costoEnvio = costoEnvio;
        }
    }
}
