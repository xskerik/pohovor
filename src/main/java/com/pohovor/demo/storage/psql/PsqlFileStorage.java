package com.pohovor.demo.storage.psql;

import com.pohovor.demo.model.FileStatus;
import com.pohovor.demo.model.UploadedFile;
import com.pohovor.demo.storage.FileStorage;
import io.vavr.collection.HashMap;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Option;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@Log4j2
@RequiredArgsConstructor
public class PsqlFileStorage implements FileStorage {


    private final NamedParameterJdbcTemplate jdbc;

    private static final String UPLOAD_ID = "uploadId";
    private static final String CREATED_AT = "created_at";
    private static final String FILE_STATUS = "file_status";
    private static final String FILE_DATA = "file_data";
    private static final String CHECKSUM = "checksum";


    @Override
    @Transactional
    public void storeFile(UploadedFile uploadedFile) {
        log.trace("DB Insert file [{}].", uploadedFile.getUploadId());

        final String insertSql = "insert into files (uploadId, file_status, checksum, file_data) values (?, ?, ?, ?)";

        jdbc.getJdbcTemplate().update(connection -> {
            PreparedStatement ps = connection.prepareStatement(insertSql);
            ps.setString(1, uploadedFile.getUploadId());
            ps.setString(2, uploadedFile.getStatus().name());
            ps.setString(3, uploadedFile.getChecksum());
            ps.setBlob(4, new ByteArrayInputStream(uploadedFile.getData()));
            return ps;
        });
    }

    @Override
    @Transactional
    public void updateStatus(FileStatus fileStatus, String uploadId) {

        log.trace("DB Update file [{}] status to [{}].", uploadId, fileStatus);

        final String updateSql = "UPDATE files SET file_status = :status WHERE uploadId = :upId";
        final Map<String, Object> params = HashMap.of(
                "status", fileStatus.name(),
                "upId", uploadId
        );

        jdbc.update(updateSql, params.toJavaMap());
    }

    @Override
    @Transactional
    public Option<UploadedFile> getFile(String uploadId) {
        List<UploadedFile> resp = jdbc.query("SELECT * FROM files WHERE uploadId = :upId", HashMap.of("upId", uploadId).toJavaMap(), FILE_EXTRACTOR);
        return resp.peekOption();
    }

    private final ResultSetExtractor<List<UploadedFile>> FILE_EXTRACTOR = re -> {

        List<UploadedFile> files = List.of();

        while (re.next()) {
            UploadedFile file = UploadedFile.builder()
                    .uploadId(re.getString(UPLOAD_ID))
                    .createdAt(ZonedDateTime.of(re.getTimestamp(CREATED_AT).toLocalDateTime(), ZoneId.systemDefault()))
                    .checksum(re.getString(CHECKSUM))
                    .status(FileStatus.valueOf(re.getString(FILE_STATUS)))
                    .build();
            files = files.append(file);
        }
        return files;
    };
}
