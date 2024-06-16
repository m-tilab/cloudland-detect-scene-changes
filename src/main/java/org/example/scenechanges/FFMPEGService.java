package org.example.scenechanges;

import com.github.kokorin.jaffree.ffmpeg.FFmpeg;
import org.springframework.stereotype.Service;

@Service
public class FFMPEGService {

    public void generateScreenshotFromVideo(Long sceneTime, String objectName, String filename) {
        FFmpeg.atPath()
                .addArguments("-ss", String.valueOf(sceneTime))
                .addArguments("-i", objectName)
                .addArguments("-frames:v", "1")
                .addArgument(filename)
                .execute();
    }
}
