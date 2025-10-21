package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

/**
 * Servicio para subir imágenes a ImgBB (servicio gratuito de hosting de imágenes).
 * Convierte las imágenes locales en URLs públicas accesibles desde cualquier cliente de email.
 */
@Service
@Slf4j
public class ImageHostingService {

    private final RestTemplate restTemplate;

    @Value("${app.uploads.path:file:uploads/}")
    private String uploadsPath;

    // 🔑 API Key de ImgBB (gratuita) - Obtén la tuya en: https://api.imgbb.com/
    // Por ahora usamos una demo, pero deberías crear tu propia cuenta
    @Value("${imgbb.api.key:4d755673258b82c8c2a0cf9db2f5e8e1}")
    private String imgbbApiKey;

    private static final String IMGBB_API_URL = "https://api.imgbb.com/1/upload";

    public ImageHostingService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Convierte una ruta local de imagen a una URL pública de ImgBB.
     *
     * @param localImagePath Ruta local de la imagen (ej: "/uploads/mate.png")
     * @return URL pública de la imagen o un placeholder si falla
     */
    public String getPublicImageUrl(String localImagePath) {
        if (localImagePath == null || localImagePath.trim().isEmpty()) {
            log.warn("⚠️ Ruta de imagen vacía, usando placeholder");
            return getPlaceholderUrl();
        }

        try {
            // Extraer nombre del archivo
            String fileName = extractFileName(localImagePath);
            log.info("🔍 Procesando imagen: {}", fileName);

            // Construir ruta completa del archivo
            String fullPath = uploadsPath.replace("file:", "") + fileName;
            Path imagePath = Paths.get(fullPath);

            // Verificar que el archivo existe
            if (!Files.exists(imagePath)) {
                log.warn("⚠️ Archivo no existe: {}", fullPath);
                return getPlaceholderUrl();
            }

            // Leer archivo y convertir a Base64
            byte[] imageBytes = Files.readAllBytes(imagePath);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);

            // Subir a ImgBB
            String publicUrl = uploadToImgBB(base64Image, fileName);

            if (publicUrl != null) {
                log.info("✅ Imagen subida exitosamente: {}", publicUrl);
                return publicUrl;
            } else {
                log.warn("⚠️ Falló la subida, usando placeholder");
                return getPlaceholderUrl();
            }

        } catch (IOException e) {
            log.error("❌ Error procesando imagen {}: {}", localImagePath, e.getMessage());
            return getPlaceholderUrl();
        }
    }

    /**
     * Sube una imagen codificada en Base64 a ImgBB.
     *
     * @param base64Image Imagen en Base64
     * @param fileName Nombre del archivo
     * @return URL pública de la imagen o null si falla
     */
    private String uploadToImgBB(String base64Image, String fileName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("key", imgbbApiKey);
            body.add("image", base64Image);
            body.add("name", sanitizeFileName(fileName));

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    IMGBB_API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                if (Boolean.TRUE.equals(responseBody.get("success"))) {
                    Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                    String url = (String) data.get("url");
                    log.info("🌐 URL pública obtenida: {}", url);
                    return url;
                }
            }

            log.error("❌ Respuesta de ImgBB no exitosa: {}", response.getStatusCode());
            return null;

        } catch (Exception e) {
            log.error("❌ Error subiendo a ImgBB: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extrae el nombre del archivo de una ruta completa.
     */
    private String extractFileName(String path) {
        path = path.replace("\\", "/");

        if (path.contains("/")) {
            return path.substring(path.lastIndexOf("/") + 1);
        }
        return path;
    }

    /**
     * Sanitiza el nombre del archivo para evitar problemas con caracteres especiales.
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    /**
     * Retorna una URL de imagen placeholder para casos de error.
     */
    private String getPlaceholderUrl() {
        return "https://via.placeholder.com/300x200/ED620C/FFFFFF?text=Producto+Pinceletas";
    }
}
