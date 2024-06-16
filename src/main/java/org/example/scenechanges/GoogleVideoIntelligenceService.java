package org.example.scenechanges;

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
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class GoogleVideoIntelligenceService {

    @Value("${spring.ai.vertex.ai.gemini.project-id}")
    private String projectId;

    private static final Logger logger = LoggerFactory.getLogger(GoogleVideoIntelligenceService.class);

    public List<Long> detectSceneChanges(String gcsUri) throws InterruptedException, ExecutionException {
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
                logger.info(String.format("No scene changes detected in %s", gcsUri));
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
