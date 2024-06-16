package org.example.scenechanges;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@EnableConfigurationProperties(ImageCaptionProperties.class)
public class CloudlandDetectSceneChangesApplication {

    final GoogleCloudStorageService googleCloudStorageService;
    final GoogleVideoIntelligenceService googleVideoIntelligenceService;
    final FFMPEGService ffmpegService;

    public CloudlandDetectSceneChangesApplication(GoogleCloudStorageService googleCloudStorageService,
                                                  GoogleVideoIntelligenceService googleVideoIntelligenceService,
                                                  FFMPEGService ffmpegService) {
        this.googleCloudStorageService = googleCloudStorageService;
        this.googleVideoIntelligenceService = googleVideoIntelligenceService;
        this.ffmpegService = ffmpegService;
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

            final List<Long> detectedSceneChanges = googleVideoIntelligenceService.detectSceneChanges(gcsUri);

            googleCloudStorageService.downloadObject(bucketName, objectName);

            for (Long sceneTime : detectedSceneChanges) {

                String filename = "detected_scene_changes_" + sceneTime + ".jpg";

                ffmpegService.generateScreenshotFromVideo(sceneTime, objectName, filename);

                googleCloudStorageService.uploadObject(bucketName, filename, filename);
            }
        };
    }

}
