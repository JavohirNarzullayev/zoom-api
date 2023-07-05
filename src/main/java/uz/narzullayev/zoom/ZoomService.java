package uz.narzullayev.zoom;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uz.narzullayev.zoom.dto.ZoomAccessToken;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZoomService {
    private static final String TOKEN_URL = "https://zoom.us/oauth/token";


    private final RestTemplate restTemplate = new RestTemplate();


    public ZoomAccessToken getToken(String code) {
        var getAccessTokenUrl = TOKEN_URL +
                "?grant_type=authorization_code" +
                "&code=" + code+
                "&redirect_url=http://localhost:9091/zoom/sso";

        var tokenResponse = restTemplate.exchange(getAccessTokenUrl, HttpMethod.POST, getEntity(), ZoomAccessToken.class);
        System.out.println(tokenResponse);

        return tokenResponse.getBody();
    }



    private HttpEntity<?> getEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String basicAuthEncoded =
                Base64.getEncoder().encodeToString(("gL5ol801SrSa0kYWgF2hdg" + ":" + "qHvShbFHihAxp4A1dSoEBeg3VodS621K").getBytes());
        headers.setBasicAuth(basicAuthEncoded);

        return new HttpEntity<>(headers);
    }
}
