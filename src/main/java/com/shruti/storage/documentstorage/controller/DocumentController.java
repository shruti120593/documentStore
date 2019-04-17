package com.shruti.storage.documentstorage.controller;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by user on 4/16/19.
 */

@RestController
public class DocumentController {

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable String id, HttpServletRequest request){
        String contentType = null;
        Resource resource = null;
        try {
            resource = loadFileAsResource(id);
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
    public void deleteFile(@PathVariable String id){

        try {
            Path path = Paths.get(id);
            Files.deleteIfExists(path);

        } catch (MalformedURLException ex) {
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @PutMapping("/update/{id}")
    public void updateFile(@RequestParam("file") MultipartFile file){
        try {
            Path path = Paths.get(file.getOriginalFilename());
            Files.delete(path);
            upload(file);

        } catch (MalformedURLException ex) {
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @PostMapping("/upload")
    public void upload(@RequestParam("file") MultipartFile file){
        File fileToStore = new File(file.getOriginalFilename());
        try {
            InputStream input = file.getInputStream();
            Files.copy(input, fileToStore.toPath());
        }catch (IOException e1) {
                e1.printStackTrace();
        }
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
