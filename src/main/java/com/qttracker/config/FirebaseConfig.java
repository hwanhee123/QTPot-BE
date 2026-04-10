package com.qttracker.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream credentialsStream = getCredentialsStream();
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                    .build();
            FirebaseApp.initializeApp(options);
        }
    }

    private InputStream getCredentialsStream() throws IOException {
        // EC2 서버의 파일시스템 경로 우선 확인
        File externalFile = new File("/home/ec2-user/firebase-service-account.json");
        if (externalFile.exists()) {
            return new FileInputStream(externalFile);
        }
        // 없으면 classpath(로컬 개발용) 에서 읽기
        return new ClassPathResource("firebase-service-account.json").getInputStream();
    }
}
