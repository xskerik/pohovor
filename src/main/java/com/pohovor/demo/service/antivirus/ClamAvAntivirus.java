package com.pohovor.demo.service.antivirus;

import io.vavr.collection.HashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.commands.scan.result.ScanResult;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class ClamAvAntivirus implements AntivirusService {

    private final ClamavClient clamavClient;

    public boolean scanFile(byte[] file, String uploadId) {

        log.debug("Scanning file [{}] by antivirus.", uploadId);

        ScanResult scanResult = clamavClient.scan(new ByteArrayInputStream(file));

        if (scanResult instanceof ScanResult.OK) {
            // OK
            return true;
        } else { // if (scanResult instanceof ScanResult.VirusFound)
            Map<String, Collection<String>> viruses = ((ScanResult.VirusFound) scanResult).getFoundViruses();
            log.warn("File [{}] contains some viruses:\n {}", uploadId, HashMap.ofAll(viruses).map(v -> v._1).mkString(",\n"));
            return false;
        }
    }

}
