package org.example.scenechanges;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "spring.ai.vertex.ai.gemini.image-captions")
public record ImageCaptionProperties(String url) {
}
