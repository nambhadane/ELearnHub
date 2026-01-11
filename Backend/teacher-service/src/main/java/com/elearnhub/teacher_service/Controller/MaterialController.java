package com.elearnhub.teacher_service.Controller;

import com.elearnhub.teacher_service.dto.MaterialDTO;
import com.elearnhub.teacher_service.entity.Material;
import com.elearnhub.teacher_service.entity.User;
import com.elearnhub.teacher_service.service.MaterialService;
import com.elearnhub.teacher_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/materials")
public class MaterialController {
    
    @Autowired
    private MaterialService materialService;
    
    @Autowired
    private UserService userService;
    
    // Upload material
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("classId") Long classId,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Material material = materialService.uploadMaterial(file, title, description, classId, user.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Material uploaded successfully");
            response.put("materialId", material.getId());
            response.put("fileName", material.getFileName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to upload material: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    // Get materials by class
    @GetMapping("/class/{classId}")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<List<MaterialDTO>> getMaterialsByClass(@PathVariable Long classId) {
        try {
            List<MaterialDTO> materials = materialService.getMaterialsByClassId(classId);
            return ResponseEntity.ok(materials);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // Download material
    @GetMapping("/{id}/download")
    @PreAuthorize("hasRole('TEACHER') or hasRole('STUDENT')")
    public ResponseEntity<Resource> downloadMaterial(@PathVariable Long id) {
        try {
            Material material = materialService.getMaterialById(id);
            byte[] data = materialService.downloadMaterial(id);
            
            ByteArrayResource resource = new ByteArrayResource(data);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + material.getFileName() + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(data.length)
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    // Delete material
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<?> deleteMaterial(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Material material = materialService.getMaterialById(id);
            
            // Check if user is the uploader
            if (!material.getUploadedBy().equals(user.getId())) {
                Map<String, String> error = new HashMap<>();
                error.put("message", "You can only delete your own materials");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }
            
            materialService.deleteMaterial(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Material deleted successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Failed to delete material: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}
