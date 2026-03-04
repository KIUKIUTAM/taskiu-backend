package com.tavinki.taskiu.common.minio;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.tavinki.taskiu.common.properties.MinioProperties;

@Component
@Slf4j
@RequiredArgsConstructor
public class MinioInitializer implements ApplicationRunner {

    private final MinioClient minioClient;

    private final MinioProperties minioProperties;



    @Override
    public void run(ApplicationArguments args) throws Exception {
        createBucketIfNotExists(minioProperties.getBucketName());
    }

    private void createBucketIfNotExists(String bucketName) throws Exception {
        boolean exists = minioClient.bucketExists(
            BucketExistsArgs.builder().bucket(bucketName).build()
        );
        if (!exists) {
            minioClient.makeBucket(
                MakeBucketArgs.builder().bucket(bucketName).build()
            );
            log.info("Bucket created: {}", bucketName);
        } else {
            log.info("Bucket exists: {}", bucketName);
        }
    }
}
