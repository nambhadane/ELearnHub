package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.MaterialDTO;
import com.elearnhub.teacher_service.entity.Material;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.repository.MaterialRepository;
import com.elearnhub.teacher_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class MaterialServiceImpl implements MaterialService {
    
    @Autowired
    private MaterialRepository materialRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${file.upload-dir:uploads/materials}")
    private String uploadDir;
    
    @Override
    public Material uploadMaterial(MultipartFile file, String title, String description,
                                   Long classId, Long uploadedBy) throws IOException {
        // Create upload directory if it doesn't exist
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        
        // Save file to disk
        Path filePath = Paths.get(uploadDir, uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Determine file type
        String fileType = getFileType(originalFilename);
        
        // Create material entity
        Material material = new Material();
        material.setTitle(title);
        material.setDescription(description);
        material.setFileName(originalFilename);
        material.setFilePath(filePath.toString());
        material.setFileType(fileType);
        material.setFileSize(file.getSize());
        material.setClassId(classId);
        material.setUploadedBy(uploadedBy);
        material.setUploadedAt(LocalDateTime.now());
        
        Material savedMaterial = materialRepository.save(material);
        System.out.println("✅ Material uploaded: " + originalFilename + " for class " + classId);
        
        return savedMaterial;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Material getMaterialById(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found with id: " + id));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<MaterialDTO> getMaterialsByClassId(Long classId) {
        List<Material> materials = materialRepository.findByClassIdOrderByUploadedAtDesc(classId);
        
        return materials.stream().map(material -> {
            String uploaderName = "Unknown";
            if (material.getUploadedBy() != null) {
                User uploader = userRepository.findById(material.getUploadedBy()).orElse(null);
                if (uploader != null) {
                    uploaderName = uploader.getName() != null ? uploader.getName() : uploader.getUsername();
                }
            }
            
            return new MaterialDTO(
                    material.getId(),
                    material.getTitle(),
                    material.getDescription(),
                    material.getFileName(),
                    material.getFileType(),
                    material.getFileSize(),
                    material.getClassId(),
                    uploaderName,
                    material.getUploadedAt()
            );
        }).collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public byte[] downloadMaterial(Long id) throws IOException {
        Material material = getMaterialById(id);
        Path filePath = Paths.get(material.getFilePath());
        
        if (!Files.exists(filePath)) {
            throw new IOException("File not found: " + material.getFileName());
        }
        
        return Files.readAllBytes(filePath);
    }
    
    @Override
    public void deleteMaterial(Long id) throws IOException {
        Material material = getMaterialById(id);
        
        // Delete file from disk
        Path filePath = Paths.get(material.getFilePath());
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
        
        // Delete from database
        materialRepository.deleteById(id);
        System.out.println("✅ Material deleted: " + material.getFileName());
    }
    
    private String getFileType(String filename) {
        if (filename == null) return "unknown";
        
        String extension = filename.toLowerCase();
        if (extension.endsWith(".pdf")) return "pdf";
        if (extension.endsWith(".doc") || extension.endsWith(".docx")) return "document";
        if (extension.endsWith(".ppt") || extension.endsWith(".pptx")) return "presentation";
        if (extension.endsWith(".xls") || extension.endsWith(".xlsx")) return "spreadsheet";
        if (extension.endsWith(".mp4") || extension.endsWith(".avi") || extension.endsWith(".mov")) return "video";
        if (extension.endsWith(".mp3") || extension.endsWith(".wav")) return "audio";
        if (extension.endsWith(".jpg") || extension.endsWith(".jpeg") || extension.endsWith(".png") || extension.endsWith(".gif")) return "image";
        if (extension.endsWith(".zip") || extension.endsWith(".rar")) return "archive";
        
        return "file";
    }
}
