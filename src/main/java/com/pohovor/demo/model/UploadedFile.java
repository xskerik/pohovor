package com.pohovor.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.ZonedDateTime;

@Value
@Builder
@AllArgsConstructor
public class UploadedFile {

    String uploadId;
    ZonedDateTime createdAt;
    String checksum;
    FileStatus status;
    byte [] data;
}
