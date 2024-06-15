package org.example.scenechanges;

import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

@Service
public class GoogleCloudAuthService {

    public String getAccessToken() throws IOException {
        // Load the service account key file
        GoogleCredentials googleCredentials = GoogleCredentials
                .fromStream(new FileInputStream("/service-account-file.json"))
                .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }
}
