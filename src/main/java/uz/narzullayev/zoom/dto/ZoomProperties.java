package uz.narzullayev.zoom.dto;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Data
@EnableConfigurationProperties(ZoomProperties.class)
@ConfigurationProperties(prefix = "zoom.properties", ignoreUnknownFields = false)
public class ZoomProperties {
    private final String TOKEN_URL = "https://zoom.us/oauth/token";
    private final String API_ENDPOINT = "https://api.zoom.us/v2";
    private String redirectUrl="http://localhost:9091/zoom/sso";
    private final Long maxResponseSize = 5 * 1024 * 1024L;

    private String clientId="gL5ol801SrSa0kYWgF2hdg";
    private String clientSecret="qHvShbFHihAxp4A1dSoEBeg3VodS621K";
    private String grantType = "authorization_code";

}
