package com.pohovor.demo.storage;

import com.pohovor.demo.model.FileStatus;
import com.pohovor.demo.model.UploadedFile;
import io.vavr.control.Option;

public interface FileStorage {

    void storeFile(UploadedFile uploadedFile);

    void updateStatus(FileStatus fileStatus, String uploadId);

    Option<UploadedFile> getFile(String uploadId);
}
