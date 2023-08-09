package com.nott.minio.propertie;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * @author Nott
 * @Date 2023/8/8
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "minio")
public class MinioProp {

    private String url;

    private Integer port;

    private String username;

    private String password;

    private String bucket;
}
