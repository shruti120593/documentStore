package com.shruti.storage.documentstorage.controller;

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

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id, HttpServletRequest request){
        String contentType = null;
        Resource resource = null;
        try {
            resource = loadFileAsResource(m.get(id));
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
            System.out.println(contentType);
            if(contentType == null) {
                contentType = "application/octet-stream";
            }
        } catch (IOException ex) {
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable String id){

        try {
            Path path = Paths.get(m.get(id));
            Files.deleteIfExists(path);
        } catch (MalformedURLException ex) {
            return ResponseEntity.status(500).body("exception in deleting file");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("exception in deleting file");
        }
        return ResponseEntity.ok().body("success");
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateFile(@PathVariable String id, @RequestParam("file") MultipartFile file){
        try {
            Path path = Paths.get(m.get(id));
            if(reversem.get(file.getOriginalFilename()) == null){
                return ResponseEntity.badRequest().body("file does not exists");
            }
            Files.delete(path);
            uploadFile(file);
            ResponseEntity res = null;
            try {
                res = ResponseEntity.ok().body(id);
                return res;
            } catch (Exception e) {
                return ResponseEntity.status(500).body("exception in updating file");
            }

        } catch (MalformedURLException ex) {
            return ResponseEntity.status(500).body("exception in updating file");
        } catch (IOException e) {
            return ResponseEntity.status(500).body("exception in updating file");
        }

    }

    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file){
        String generatedString = RandomStringUtils.randomAlphanumeric(20);
        if(reversem.get(file.getOriginalFilename()) != null){
            return ResponseEntity.badRequest().body("file already exists");
        }
        try {
            uploadFile(file);
        } catch (IOException e1) {
            return ResponseEntity.status(500).body("exception in storing file");
        }
        m.put(generatedString, file.getOriginalFilename());
        reversem.put(file.getOriginalFilename(), generatedString);
        ResponseEntity res = null;
        try {
            res = ResponseEntity.created(new URI(generatedString)).body(generatedString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
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
