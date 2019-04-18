package com.shruti.storage.documentstorage.controller;

import com.shruti.storage.documentstorage.pojo.DocumentResponse;
import com.shruti.storage.documentstorage.pojo.TextRequest;
import com.shruti.storage.documentstorage.pojo.TextResponse;
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
 * Created by user on 4/17/19.
 */
@RestController
public class TextFileController {

        public static Map<String,String> m = new HashMap<>();
        public static Map<String,String> reversem = new HashMap<>();

        @GetMapping("text/storage/document/{id}")
        public ResponseEntity<Object> download(@PathVariable String id){
            TextResponse textResponse = new TextResponse();
            if(m.get(id) == null){
                return ResponseEntity.notFound().build();
            }else{
                textResponse.setText(m.get(id));
            }

            return ResponseEntity.ok().body(textResponse);
        }

        @DeleteMapping("text/storage/document/{id}")
        public ResponseEntity<String> deleteFile(@PathVariable String id){
            if(m.get(id) == null){
                return ResponseEntity.notFound().build();
            }
            m.remove(id);
            return ResponseEntity.ok().body("document deleted successfully");
        }

        @PutMapping("text/storage/document/{id}")
        public ResponseEntity<Object> updateFile(@PathVariable String id, @RequestBody TextRequest textRequest){
            if(m.get(id) == null){
                return ResponseEntity.notFound().build();
            }
            m.put(id, textRequest.getText());
            return ResponseEntity.noContent().build();
        }

        @PostMapping("text/storage/document")
        public ResponseEntity<Object> upload(@RequestBody TextRequest textRequest){

            if(textRequest.getText() == null || textRequest.getText().isEmpty()){
                return ResponseEntity.badRequest().build();
            }

            String generatedString = RandomStringUtils.randomAlphanumeric(20);
            while(m.get(generatedString) != null){
                generatedString = RandomStringUtils.randomAlphanumeric(20);
            }
            //check in map if generatedString exists for uniqueness
            m.put(generatedString, textRequest.getText());
            DocumentResponse documentResponse = new DocumentResponse();
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
}
