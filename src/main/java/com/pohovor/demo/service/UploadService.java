package com.pohovor.demo.service;

import com.pohovor.demo.model.FileStatus;
import com.pohovor.demo.model.UploadInfo;
import com.pohovor.demo.model.UploadedFile;
import com.pohovor.demo.service.antivirus.AntivirusService;
import com.pohovor.demo.service.antivirus.ScanResult;
import com.pohovor.demo.service.processing.FileProcessing;
import com.pohovor.demo.storage.FileStorage;
import com.pohovor.demo.DemoApplication;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class UploadService {


    private final FileStorage fileStorage;
    private final AntivirusService antivirusService;

    private final RabbitTemplate amq;

    private final List<FileProcessing> processings;


    public UploadInfo uploadFile(byte[] data) {

        final String checksum = computeContentMD5Value(data);
        final String uploadId = UUID.randomUUID().toString();


        // TODO cache to check file with the same checksum exists already

        final UploadedFile uploadedFile = new UploadedFile(uploadId, ZonedDateTime.now(), checksum, FileStatus.UPLOADING, data);

        fileStorage.storeFile(uploadedFile);

        amq.convertSendAndReceive(DemoApplication.ANTIVIRUS_QUEUE, uploadedFile);

        return new UploadInfo(uploadId, FileStatus.UPLOADING);
    }

    public Option<UploadInfo> getFileStatus(String uploadId) {
        return fileStorage.getFile(uploadId)
                .map(f -> new UploadInfo(f.getUploadId(), f.getStatus()))
                .orElse(() -> Option.none());
    }

    @RabbitListener(queues = DemoApplication.ANTIVIRUS_QUEUE)
    public void checkByAntivirus(UploadedFile uploadedFile) {
        log.debug("Check file [{}] by antivirus.", uploadedFile.getUploadId());

        boolean scanResult = antivirusService.scanFile(uploadedFile.getData(), uploadedFile.getUploadId());

        amq.convertSendAndReceive(DemoApplication.ANTIVIRUS_RESULT_QUEUE, new ScanResult(scanResult, uploadedFile));
    }

    @RabbitListener(queues = DemoApplication.ANTIVIRUS_RESULT_QUEUE)
    public void resultFromAntivirus(ScanResult scanResult) {
        log.debug("File [{}] was scanned, is ok [{}].", scanResult.getUploadedFile().getUploadId(), scanResult.isOk());


        if (scanResult.isOk()) {
            // no processing, save file (set flag to SAVED)
            if (processings.isEmpty()) {
                fileStorage.updateStatus(FileStatus.SAVED, scanResult.getUploadedFile().getUploadId());
            }
            // send for further processing
            else {
                fileStorage.updateStatus(FileStatus.PROCESSING, scanResult.getUploadedFile().getUploadId());
                amq.convertSendAndReceive(DemoApplication.PROCESS_FILE_QUEUE, scanResult.getUploadedFile());
            }
        } else {
            fileStorage.updateStatus(FileStatus.MALFORMED, scanResult.getUploadedFile().getUploadId());
        }
    }

    @SneakyThrows
    private String computeContentMD5Value(byte[] data) {
        return DigestUtils.md5DigestAsHex(data);
    }
}
