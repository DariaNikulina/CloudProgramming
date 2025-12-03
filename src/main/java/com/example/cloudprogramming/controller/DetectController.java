package com.example.cloudprogramming.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/detect")
@RequiredArgsConstructor
public class DetectController {
    private final RestTemplate restTemplate;
    @Value("${vk.token}")
    private String token;


    @GetMapping
    public String showForm() {
        return "detect";
    }


    @PostMapping
    public String detectObjects(@RequestParam("file") MultipartFile file, Model model) {
        if (file == null || file.isEmpty()) {
            model.addAttribute("error", "Файл не выбран");
            return "detect";
        }

        try {
            byte[] bytes = file.getBytes();
            String imageBase64 = Base64.getEncoder().encodeToString(bytes);
            String contentType = file.getContentType();
            String imageDataUri = "data:" + contentType + ";base64," + imageBase64;
            model.addAttribute("imageData", imageDataUri);

            ByteArrayResource fileResource = new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return "filename";
                }
            };

            String metaJson = """
                    {
                      "mode": ["pedestrian"],
                      "images": [{"name":"file"}]
                    }""";

            LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentDispositionFormData("file", fileResource.getFilename());
            fileHeaders.setContentType(MediaType.parseMediaType(Objects.requireNonNull(file.getContentType())));
            HttpEntity<ByteArrayResource> filePart = new HttpEntity<>(fileResource, fileHeaders);
            body.add("file", filePart);

            HttpHeaders metaHeaders = new HttpHeaders();
            metaHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> metaPart = new HttpEntity<>(metaJson, metaHeaders);
            body.add("meta", metaPart);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String url = "https://smarty.mail.ru/api/v1/objects/detect";
            if (token != null && !token.isBlank()) {
               url = url + "?oauth_token=" + token + "&oauth_provider=mcs";
            }

            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            model.addAttribute("status", resp.getStatusCode().value());
            model.addAttribute("json", resp.getBody());

            return "detect";

        } catch (Exception ex) {
            model.addAttribute("error", "Ошибка при отправке запроса: " + ex.getMessage());
            return "detect";
        }
    }
}
