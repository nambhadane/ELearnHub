package com.elearnhub.student_service.service;

import com.elearnhub.student_service.dto.AssignmentDTO;
import com.elearnhub.student_service.dto.ClassDTO;
import com.elearnhub.student_service.dto.LessonDTO;
import com.elearnhub.student_service.dto.SubmissionDTO;
import com.elearnhub.student_service.entity.User;
import com.elearnhub.student_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class StudentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final Path rootLocation = Paths.get("uploads");

    public StudentService() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
    }

    public List<ClassDTO> getClassesForStudent() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = userRepository.findByUsername(username);
        if (student == null) {
            return Collections.emptyList();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            "http://teacher-service/classes/student/" + student.getUserId(),
            HttpMethod.GET,
            entity,
            new org.springframework.core.ParameterizedTypeReference<List<ClassDTO>>() {}
        ).getBody();
    }

    public List<LessonDTO> getLessonsByClass(Long classId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            "http://teacher-service/lessons/class/" + classId,
            HttpMethod.GET,
            entity,
            new org.springframework.core.ParameterizedTypeReference<List<LessonDTO>>() {}
        ).getBody();
    }

    public List<AssignmentDTO> getAssignmentsByClass(Long classId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            "http://teacher-service/assignments/class/" + classId,
            HttpMethod.GET,
            entity,
            new org.springframework.core.ParameterizedTypeReference<List<AssignmentDTO>>() {}
        ).getBody();
    }

    public SubmissionDTO submitAssignment(Long assignmentId, MultipartFile file) throws IOException {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        Path destinationFile = rootLocation.resolve(filename).normalize().toAbsolutePath();
        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = userRepository.findByUsername(username);
        SubmissionDTO submissionDTO = new SubmissionDTO();
        submissionDTO.setAssignmentId(assignmentId);
        submissionDTO.setStudentId(student.getUserId());
        submissionDTO.setFilePath(destinationFile.toString());
        submissionDTO.setSubmissionDate(LocalDateTime.now());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SubmissionDTO> entity = new HttpEntity<>(submissionDTO, headers);
        return restTemplate.postForObject(
            "http://teacher-service/assignments/submissions",
            entity,
            SubmissionDTO.class
        );
    }

    public List<SubmissionDTO> getStudentSubmissions() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User student = userRepository.findByUsername(username);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
            "http://teacher-service/submissions/student/" + student.getUserId(),
            HttpMethod.GET,
            entity,
            new org.springframework.core.ParameterizedTypeReference<List<SubmissionDTO>>() {}
        ).getBody();
    }

    public Resource loadFileAsResource(String filename) throws MalformedURLException {
        Path file = rootLocation.resolve(filename);
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Could not read file: " + filename);
        }
    }
}