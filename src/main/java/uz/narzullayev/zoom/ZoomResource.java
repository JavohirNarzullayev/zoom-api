package uz.narzullayev.zoom;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.narzullayev.zoom.dto.*;

@RestController
@RequestMapping("/zoom/sso")
@RequiredArgsConstructor
public class ZoomController {
    private final ZoomService zoomService;
    private final ZoomProperties zoomProperties;

    @GetMapping
    public ZoomAccessToken getAccess(@RequestParam("code") String code,
                                     HttpServletRequest httpServletRequest) throws Exception {
        var authorizer = new ZoomAuthorizerGetOAuthToken(
                zoomProperties.getClientId(),
                zoomProperties.getClientSecret()
        );
        ZoomAPI za = new ZoomAPI(authorizer, zoomProperties);
        SimplePersist db = new SimplePersist("oauth-token.db");
        ZoomAccessToken tkn = za.requestAccessToken(code);
        db.save(tkn);
        ZoomUser me = za.getUser("me");
        System.out.println(tkn);

        return tkn;
    }
}
