package com.shruti.storage.documentstorage.controller;

import com.shruti.storage.documentstorage.pojo.DocumentResponse;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by user on 4/16/19.
 */

@RestController
public class DocumentController {

    public static Map<String,String> m = new HashMap<>();
    public static Map<String,String> reversem = new HashMap<>();

    @GetMapping("/storage/documents/{id}")
    public ResponseEntity<Object> download(@PathVariable String id, HttpServletRequest request){
        String contentType = null;
        Resource resource = null;
        if(m.get(id) == null){
            return ResponseEntity.notFound().build();
        }
        try {
            resource = loadFileAsResource(m.get(id));
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            if(contentType == null) {
                contentType = "application/octet-stream";
            }
        } catch (IOException ex) {
            DocumentResponse documentResponse = new DocumentResponse();
            documentResponse.setErrorMessage("error in getting document "+ex.getMessage());
            return ResponseEntity.badRequest().body(documentResponse);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/storage/document/{id}")
    public ResponseEntity<Object> deleteFile(@PathVariable String id){
        try {
            if(m.get(id) == null){
                return ResponseEntity.notFound().build();
            }
            Path path = Paths.get(m.get(id));
            Files.deleteIfExists(path);
        } catch (MalformedURLException ex) {
            return ResponseEntity.status(500).body("exception in deleting file");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("exception in deleting file");
        }
        return ResponseEntity.ok().body("document deleted successfully");
    }

    @PutMapping("/storage/document/{id}")
    public ResponseEntity<Object> updateFile(@PathVariable String id, @RequestParam("file") MultipartFile file){
        try {
            if(m.get(id) == null){
                return ResponseEntity.notFound().build();
            }
            Path path = Paths.get(m.get(id));
            Files.delete(path);
            uploadFile(file);
            ResponseEntity res = null;
            try {
                return ResponseEntity.noContent().build();
            } catch (Exception e) {
                return ResponseEntity.status(500).body("exception in updating file "+ e.getMessage());
            }

        } catch (MalformedURLException ex) {
            return ResponseEntity.status(500).body("exception in updating file " + ex.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("exception in updating file " + e.getMessage());
        }

    }

    @PostMapping("/storage/document")
    public ResponseEntity<Object> upload(@RequestParam("file") MultipartFile file){
        DocumentResponse documentResponse = new DocumentResponse();
        if(reversem.get(file.getOriginalFilename()) != null){
            documentResponse.setErrorMessage("file with same name already exists");
            return ResponseEntity.badRequest().body(documentResponse);
        }
        try {
            uploadFile(file);
        } catch (IOException e1) {
            documentResponse.setErrorMessage("exception in uploading file "+ e1.getMessage());
            return ResponseEntity.status(500).body(documentResponse);
        }
        String generatedString = RandomStringUtils.randomAlphanumeric(20);
        m.put(generatedString, file.getOriginalFilename());
        reversem.put(file.getOriginalFilename(), generatedString);
        documentResponse.setId(generatedString);
        ResponseEntity res = null;
        try {
            res = ResponseEntity.created(new URI("/storage/documents/"+generatedString)).body(documentResponse);
        } catch (URISyntaxException e) {
            documentResponse.setErrorMessage("malformed url  "+ e.getMessage());
            return ResponseEntity.status(500).body(documentResponse);
        }
        return res;
    }

    public void uploadFile(MultipartFile file ) throws IOException {
        File fileToStore = new File(file.getOriginalFilename());
        InputStream input = file.getInputStream();
        Files.copy(input, fileToStore.toPath());
    }


    public Resource loadFileAsResource(String fileName) {
        try {
            Path path = Paths.get(fileName);
            Resource resource = new UrlResource(path.toUri());
            return resource;

        } catch (MalformedURLException ex) {
        }

        return null;
    }

}
