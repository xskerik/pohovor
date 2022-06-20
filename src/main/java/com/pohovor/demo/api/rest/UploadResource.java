package com.pohovor.demo.api.rest;


import com.pohovor.demo.model.UploadInfo;
import com.pohovor.demo.service.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("upload")
public class UploadResource {

    private final UploadService uploadService;

    @GetMapping("/{uploadId}")
    public ResponseEntity<UploadInfo> getFileStatus(@PathVariable String uploadId) {
        return uploadService.getFileStatus(uploadId)
                .map(status -> ResponseEntity.ok().body(status))
                .getOrElse(() -> ResponseEntity.notFound().build());
    }


    @PutMapping
    @SneakyThrows
    public ResponseEntity<UploadInfo> uploadFile(HttpServletRequest request) {
        log.debug("Incoming request to upload file.");
        UploadInfo uploadInfo = uploadService.uploadFile(request.getInputStream().readAllBytes());
        return ResponseEntity.ok().body(uploadInfo);
    }
}
