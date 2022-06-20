package com.pohovor.demo.service.antivirus;

import org.springframework.stereotype.Service;

public interface AntivirusService {

    /**
     * Scan file by antivirus.
     * @param file
     * @param uploadId
     * @return {@code true} when file is ok, otherwise {@code false}
     */
    boolean scanFile(byte[] file, String uploadId);

}
