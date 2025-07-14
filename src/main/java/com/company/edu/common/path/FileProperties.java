package com.company.edu.common.path;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.file")
@Data
public class FileProperties {

    private String uploadDir = "C:/Users/nus05/uploads/";
    private String baseUrl = "http://localhost:8080";
}
