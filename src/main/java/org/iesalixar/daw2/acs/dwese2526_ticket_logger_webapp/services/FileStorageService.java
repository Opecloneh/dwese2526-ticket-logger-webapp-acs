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

  /*  @Value("${app.upload-root}")
    private String uploadRootPath;
    private static final String UPLOADS_SUBDIR = "uploads";

    public String saveFIle(MultipartFile file) {
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
*/



}
