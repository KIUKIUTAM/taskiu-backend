package com.tavinki.taskiu.modules.project.service;

import java.io.InputStream;

import org.springframework.stereotype.Service;

import com.tavinki.taskiu.common.minio.BaseMinioService;
import com.tavinki.taskiu.common.properties.MinioProperties;

import io.minio.MinioClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProjectPictureMinioService extends BaseMinioService {

    public ProjectPictureMinioService(MinioClient minioClient, MinioProperties minioProperties) {
        super(minioClient, minioProperties);
    }
    
    @Override
    protected String getPrefix() {
        return minioProperties.getPrefix().getOrDefault("project", "projects/");
    }
    
    /**
     * Upload project picture
     * @param projectId Project ID
     * @param inputStream File input stream
     * @param contentType Content type
     * @param originalFilename Original filename
     * @return MinIO key
     */
    public String uploadProjectPicture(String projectId, InputStream inputStream, long objectSize, String contentType, String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String filename = projectId + "_" + System.currentTimeMillis() + extension;
        return upload(filename, inputStream, objectSize, contentType);
    }
}
