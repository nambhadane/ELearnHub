package com.elearnhub.teacher_service.service;

import com.elearnhub.teacher_service.dto.MaterialDTO;
import com.elearnhub.teacher_service.entity.Material;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MaterialService {
    Material uploadMaterial(MultipartFile file, String title, String description, 
                           Long classId, Long uploadedBy) throws IOException;
    
    Material getMaterialById(Long id);
    
    List<MaterialDTO> getMaterialsByClassId(Long classId);
    
    byte[] downloadMaterial(Long id) throws IOException;
    
    void deleteMaterial(Long id) throws IOException;
}
