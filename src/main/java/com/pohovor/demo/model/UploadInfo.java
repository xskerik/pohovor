package com.pohovor.demo.model;

import lombok.Value;

@Value
public class UploadInfo {
    String uploadHash;
    FileStatus status;
}
