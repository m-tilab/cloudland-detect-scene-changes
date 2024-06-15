package org.example.scenechanges;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class GoogleCloudStorageService {

    private final Storage storage;

    @Value("${spring.ai.vertex.ai.gemini.project-id}")
    private String projectId;

    public GoogleCloudStorageService() {
        this.storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
    }

    public void uploadObject(String bucketName, String filePath, String destFileName) throws IOException {
        Bucket bucket = storage.get(bucketName);
        if (bucket == null) {
            throw new IllegalArgumentException("Bucket not found: " + bucketName);
        }

        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        bucket.create(destFileName, bytes);

        System.out.println("File " + filePath + " uploaded to bucket " + bucketName + " as " + destFileName);
    }

    public void downloadObject(String bucketName, String fileName) {
        Blob blob = storage.get(bucketName, fileName);
        if (blob == null) {
            throw new IllegalArgumentException("File not found: " + fileName);
        }

        blob.downloadTo(Paths.get(fileName));
    }
}
