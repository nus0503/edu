package com.company.edu.common.path;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.file")
@Data
public class FileProperties {

    @Value( "${file.upload-dir}")
    private String uploadDir;

    private String baseUrl = "http://localhost:8080";
}
