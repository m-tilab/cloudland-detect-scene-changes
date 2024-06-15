package org.example.scenechanges;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.videointelligence.v1.AnnotateVideoProgress;
import com.google.cloud.videointelligence.v1.AnnotateVideoRequest;
import com.google.cloud.videointelligence.v1.AnnotateVideoResponse;
import com.google.cloud.videointelligence.v1.Feature;
import com.google.cloud.videointelligence.v1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1.VideoIntelligenceServiceClient;
import com.google.cloud.videointelligence.v1.VideoIntelligenceServiceSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
@EnableConfigurationProperties(ImageCaptionProperties.class)
public class CloudlandDetectSceneChangesApplication {

    private static final Logger logger = LoggerFactory.getLogger(CloudlandDetectSceneChangesApplication.class);

    @Value("${spring.ai.vertex.ai.gemini.project-id}")
    private String projectId;

    final GoogleCloudStorageService googleCloudStorageService;

    public CloudlandDetectSceneChangesApplication(GoogleCloudStorageService googleCloudStorageService) {
        this.googleCloudStorageService = googleCloudStorageService;
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(CloudlandDetectSceneChangesApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Bean
    ApplicationRunner applicationRunner() {

        return args -> {

            String bucketName = "cloudland2024";
            String objectName = "video.mp4";
            String gcsUri = String.format("gs://%s/%s", bucketName, objectName);
            //gs://cloudland2024/video.mp4

            final List<Long> detectedSceneChanges = detectSceneChanges(gcsUri);

            googleCloudStorageService.downloadObject(bucketName, objectName);

            for (Long sceneTime : detectedSceneChanges) {

                String filename = "detected_scene_changes_" + sceneTime + ".jpg";

                generateScreenshotFromVideo(sceneTime, objectName, filename);

                googleCloudStorageService.uploadObject(bucketName, filename, filename);
            }
        };
    }

    private static void generateScreenshotFromVideo(Long sceneTime, String objectName, String filename) {
        FFmpeg.atPath()
                .addArguments("-ss", String.valueOf(sceneTime))
                .addArguments("-i", objectName)
                .addArguments("-frames:v", "1")
                .addArgument(filename)
                .execute();
    }

    private List<Long> detectSceneChanges(String gcsUri) throws InterruptedException, ExecutionException {
        logger.info("scene change detection:");

        List<Long> sceneChanges = new ArrayList<>();

        try (VideoIntelligenceServiceClient client = VideoIntelligenceServiceClient.create(
                VideoIntelligenceServiceSettings.newBuilder().setQuotaProjectId(projectId).build())) {

            // Create an operation that will contain the response when the operation completes.
            AnnotateVideoRequest request =
                    AnnotateVideoRequest.newBuilder()
                            .setInputUri(gcsUri)
                            .addFeatures(Feature.SHOT_CHANGE_DETECTION)
                            .build();

            OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> response =
                    client.annotateVideoAsync(request);

            logger.info("Waiting for operation to complete...");

            List<VideoAnnotationResults> results = response.get().getAnnotationResultsList();
            if (results.isEmpty()) {
                logger.info("No scene changes detected in " + gcsUri);
                return sceneChanges;
            } else {

                logger.info(String.format("Scene changes count: %d", results.getFirst().getShotAnnotationsCount()));

                for (var videoSegment : results.getFirst().getShotAnnotationsList()) {

                    sceneChanges.add(videoSegment.getEndTimeOffset().getSeconds());
                }

            }

            return sceneChanges;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
