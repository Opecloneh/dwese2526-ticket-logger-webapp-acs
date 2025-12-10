package org.iesalixar.daw2.acs.dwese2526_ticket_logger_webapp.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${app.upload-root}")
    private String uploadRootPath;
    private static final String UPLOADS_SUBDIR = "uploads";

    public String saveFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            logger.warn("Intento de guardar un archivo nulo o vacio.");
            return null;
        }
        try {
            //Nombre original y extension
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            // Generar un nombre unico
            String uniqueFileName = UUID.randomUUID().toString();
            if (!fileExtension.isBlank()) {
                uniqueFileName += "." + fileExtension;
            }
            //Directorio basae de uploads: <uploadRootPath>/uploads/
            Path uploadsDir = Paths.get(uploadRootPath).resolve(UPLOADS_SUBDIR);
            //Crear directorios si no existen
            Files.createDirectories(uploadsDir);
            //Ruta completa del archivo
            Path filePath = uploadsDir.resolve(uniqueFileName);
            //Guardar bytes
            Files.write(filePath, file.getBytes());
            logger.info("Archivo {} guardado con exito en {}", uniqueFileName, filePath);
            //Devolvemos la ruta web que usara la vista: /uploads/<nombre>
            return "/uploads/" + uniqueFileName;
        }
        catch (IOException e) {
            logger.error("Error al guardar el archivo: {}", e.getMessage(), e);
            return null;
        }
    }

    public void deleteFile(String filePathOrWebPath){
        if (filePathOrWebPath == null || filePathOrWebPath.isBlank()) {
            logger.warn("Se ha intentado eliminar un archivo con nombre/ruta vacio");
            return;
        }
        try {
            //Si viene como /uploads/xxx, lo normalizamos al nombre del fichero
            String fileName = normalizeFileName(filePathOrWebPath);
            Path uploadsDir = Paths.get(uploadRootPath).resolve(UPLOADS_SUBDIR);
            Path filePath = uploadsDir.resolve(fileName);

            Files.deleteIfExists(filePath);
            logger.info("Archivo {} eliminado con exito ({})");
        } catch (IOException e) {
            logger.error("Error al elminar el archivo {}: {}", filePathOrWebPath, e.getMessage(), e);
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName != null) {
            int lastDot = fileName.lastIndexOf('.');
            if (lastDot > 0 && lastDot < fileName.length() - 1) {
                return fileName.substring(lastDot + 1);
            }
        }
        return "";
    }

    private String normalizeFileName(String filePathOrWebPath) {
        String value = filePathOrWebPath.trim();

        //Si viene con /uploads/ delante, lo quitamos
        if (value.startsWith("/uploads/")) {
            value = value.substring("/uploads/".length());
        }

        //Por seguridad, nos quedamos solo con el ultimo segmento (evitar rutas con ../)
        int lastSlash = value.lastIndexOf('/');
        if (lastSlash >= 0 && lastSlash < value.length() - 1) {
            value = value.substring(lastSlash + 1);
        }
        return value;
    }



}
