package com.pohovor.demo.service.antivirus;

import com.pohovor.demo.model.UploadedFile;
import lombok.Value;

@Value
public class ScanResult {

    boolean ok;
    UploadedFile uploadedFile;
}
