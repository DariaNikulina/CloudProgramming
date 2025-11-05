package com.example.cloudprogramming.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;

    public void createBucket(String bucketName) {
        s3Client.createBucket(request -> request.bucket(bucketName));
    }

    public List<String> listBuckets() {
        return s3Client.listBuckets().buckets()
                .stream().map(Bucket::name).toList();
    }

    public void deleteBucket(String bucketName) {
        s3Client.deleteBucket(request -> request.bucket(bucketName));
    }

    public List<String> listObjectsInBucket(String bucketName) {
        return s3Client.listObjects(request -> request.bucket(bucketName)).contents()
                .stream().map(S3Object::key).toList();
    }

    public void uploadFile(String bucketName, MultipartFile file) {
        try (InputStream stream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(file.getOriginalFilename())
                    .build();
            s3Client.putObject(request,
                    RequestBody.fromInputStream(stream, stream.available()));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка обработки файла");
        }
    }

    public void removeFile(String bucketName, String fileName) {
        s3Client.deleteObject(request ->
                request.bucket(bucketName).key(fileName));
    }

    public InputStream downloadFile(String bucketName, String fileName) {
        return s3Client.getObject(request ->
                request.bucket(bucketName).key(fileName),
                ResponseTransformer.toInputStream());
    }
}
