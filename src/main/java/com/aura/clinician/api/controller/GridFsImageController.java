package com.aura.clinician.api.controller;

import java.io.IOException;
import java.io.InputStream;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.mongodb.client.gridfs.model.GridFSFile;

@RestController
@RequestMapping("/api/v1/images")
public class GridFsImageController {
    private static final Logger logger = LoggerFactory.getLogger(GridFsImageController.class);
    private final GridFsTemplate prescriptionAssetsGridFs;

    public GridFsImageController(@Qualifier("prescriptionAssetsGridFs") GridFsTemplate prescriptionAssetsGridFs) {
        this.prescriptionAssetsGridFs = prescriptionAssetsGridFs;
    }

    @GetMapping("/gridfs/{fileId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String fileId) {
        ObjectId objectId;
        try {
            objectId = new ObjectId(fileId);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid fileId format: " + fileId);
        }

        GridFSFile file = prescriptionAssetsGridFs.findOne(
            new Query(Criteria.where("_id").is(objectId))
        );
        if (file == null) {
            logger.warn("GridFS file not found: {}", fileId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found: " + fileId);
        }

        GridFsResource resource = prescriptionAssetsGridFs.getResource(file);
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] data = inputStream.readAllBytes();
            String contentType = resolveContentType(file);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("max-age=86400");
            logger.info("Serving GridFS image fileId={} size={} contentType={}", fileId, data.length, contentType);
            return new ResponseEntity<>(data, headers, HttpStatus.OK);
        } catch (IOException ex) {
            logger.error("Error reading GridFS image fileId={}: {}", fileId, ex.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error reading image");
        }
    }

    private String resolveContentType(GridFSFile file) {
        if (file.getMetadata() != null) {
            for (String key : new String[]{"content_type", "contentType", "_contentType"}) {
                Object ct = file.getMetadata().get(key);
                if (ct instanceof String s && !s.isBlank()) {
                    return s;
                }
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
