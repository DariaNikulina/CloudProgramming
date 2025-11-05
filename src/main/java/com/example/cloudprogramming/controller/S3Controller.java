package com.example.cloudprogramming.controller;

import com.example.cloudprogramming.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/s3")
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping
    public String listBuckets(Model model) {
        List<String> buckets = s3Service.listBuckets();
        model.addAttribute("buckets", buckets);
        return "s3/buckets";
    }

    @PostMapping("/create-bucket")
    public String createBucket(@RequestParam String bucketName) {
        s3Service.createBucket(bucketName);
        return "redirect:/s3";
    }

    @PostMapping("/delete-bucket")
    public String deleteBucket(@RequestParam String bucketName) {
        s3Service.deleteBucket(bucketName);
        return "redirect:/s3";
    }

    @GetMapping("/{bucketName}")
    public String viewBucket(@PathVariable String bucketName, Model model) {
        List<String> objects = s3Service.listObjectsInBucket(bucketName);
        model.addAttribute("bucketName", bucketName);
        model.addAttribute("objects", objects);
        return "s3/files";
    }

    @PostMapping("/{bucketName}/upload")
    public String uploadFile(@PathVariable String bucketName,
                             @RequestParam("file") MultipartFile file) {
        s3Service.uploadFile(bucketName, file);
        return "redirect:/s3/" + bucketName;
    }

    @PostMapping("/{bucketName}/delete-file")
    public String deleteFile(@PathVariable String bucketName,
                             @RequestParam String fileName) {
        s3Service.removeFile(bucketName, fileName);
        return "redirect:/s3/" + bucketName;
    }

    @GetMapping("/{bucketName}/download/{fileName}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable String bucketName,
                                               @PathVariable String fileName) throws IOException {
        try (InputStream inputStream = s3Service.downloadFile(bucketName, fileName)) {
            byte[] data = inputStream.readAllBytes();

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        }
    }
}
