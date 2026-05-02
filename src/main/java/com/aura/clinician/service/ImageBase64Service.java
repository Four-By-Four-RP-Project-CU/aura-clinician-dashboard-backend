package com.aura.clinician.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
    private final HttpClient httpClient;

    public ImageBase64Service(@Qualifier("prescriptionAssetsGridFs") GridFsTemplate prescriptionAssetsGridFs) {
        this.prescriptionAssetsGridFs = prescriptionAssetsGridFs;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    /**
     * Fetches an image from any HTTP/HTTPS URL and returns a base64 data URI.
     * Uses GridFS directly if the URL contains "/gridfs/{fileId}".
     * Returns null on failure.
     */
    public String urlToBase64DataUri(String url) {
        if (url == null || url.isBlank()) return null;

        // If it's a GridFS URL, bypass HTTP and load directly
        int idx = url.lastIndexOf("/gridfs/");
        if (idx >= 0) {
            String fileId = url.substring(idx + "/gridfs/".length());
            return toBase64DataUri(fileId);
        }

        // Otherwise fetch from the external URL (e.g. explainability service)
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                logger.warn("HTTP {} fetching image URL={}", response.statusCode(), url);
                return null;
            }
            byte[] bytes = response.body();
            String contentType = response.headers()
                .firstValue("Content-Type")
                .map(ct -> ct.contains(";") ? ct.substring(0, ct.indexOf(';')).trim() : ct.trim())
                .orElse(inferContentTypeFromUrl(url));
            String encoded = Base64.getEncoder().encodeToString(bytes);
            logger.debug("Fetched URL={} size={} contentType={}", url, bytes.length, contentType);
            return "data:" + contentType + ";base64," + encoded;
        } catch (Exception ex) {
            logger.error("Failed to fetch image from URL={}: {}", url, ex.getMessage());
            return null;
        }
    }

    /**
     * Loads an image from GridFS by its fileId and returns a base64 data URI.
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
            String contentType = resolveGridFsContentType(file);
            String encoded = Base64.getEncoder().encodeToString(bytes);
            logger.debug("Encoded GridFS fileId={} size={} contentType={}", fileId, bytes.length, contentType);
            return "data:" + contentType + ";base64," + encoded;
        } catch (IOException ex) {
            logger.error("Failed to read GridFS file fileId={}: {}", fileId, ex.getMessage());
            return null;
        }
    }

    private String resolveGridFsContentType(GridFSFile file) {
        if (file.getMetadata() != null) {
            for (String key : new String[]{"content_type", "contentType", "_contentType"}) {
                Object ct = file.getMetadata().get(key);
                if (ct instanceof String s && !s.isBlank()) return s;
            }
        }
        return inferContentTypeFromUrl(file.getFilename());
    }

    private String inferContentTypeFromUrl(String url) {
        if (url == null) return "image/jpeg";
        String lower = url.toLowerCase();
        if (lower.contains(".png")) return "image/png";
        if (lower.contains(".jpg") || lower.contains(".jpeg")) return "image/jpeg";
        return "image/jpeg";
    }
}
