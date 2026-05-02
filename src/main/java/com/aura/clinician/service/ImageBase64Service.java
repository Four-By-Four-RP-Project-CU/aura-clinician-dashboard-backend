package com.aura.clinician.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import com.mongodb.client.gridfs.model.GridFSFile;

@Service
public class ImageBase64Service {
    private static final Logger logger = LoggerFactory.getLogger(ImageBase64Service.class);
    private final GridFsTemplate prescriptionAssetsGridFs;

    public ImageBase64Service(@Qualifier("prescriptionAssetsGridFs") GridFsTemplate prescriptionAssetsGridFs) {
        this.prescriptionAssetsGridFs = prescriptionAssetsGridFs;
    }

    /**
     * Loads an image from GridFS by its fileId and returns a base64 data URI
     * (e.g. "data:image/png;base64,....").
     * Returns null if the file is not found or cannot be read.
     */
    public String toBase64DataUri(String fileId) {
        if (fileId == null || fileId.isBlank()) return null;

        ObjectId objectId;
        try {
            objectId = new ObjectId(fileId);
        } catch (IllegalArgumentException ex) {
            logger.warn("Invalid GridFS fileId: {}", fileId);
            return null;
        }

        GridFSFile file = prescriptionAssetsGridFs.findOne(
            new Query(Criteria.where("_id").is(objectId))
        );
        if (file == null) {
            logger.warn("GridFS file not found for fileId={}", fileId);
            return null;
        }

        GridFsResource resource = prescriptionAssetsGridFs.getResource(file);
        try (InputStream is = resource.getInputStream()) {
            byte[] bytes = is.readAllBytes();
            String contentType = resolveContentType(file);
            String encoded = Base64.getEncoder().encodeToString(bytes);
            logger.debug("Encoded GridFS fileId={} size={} contentType={}", fileId, bytes.length, contentType);
            return "data:" + contentType + ";base64," + encoded;
        } catch (IOException ex) {
            logger.error("Failed to read GridFS file fileId={}: {}", fileId, ex.getMessage());
            return null;
        }
    }

    private String resolveContentType(GridFSFile file) {
        if (file.getMetadata() != null) {
            for (String key : new String[]{"content_type", "contentType", "_contentType"}) {
                Object ct = file.getMetadata().get(key);
                if (ct instanceof String s && !s.isBlank()) return s;
            }
        }
        String filename = file.getFilename();
        if (filename != null) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        }
        return "image/jpeg";
    }
}
