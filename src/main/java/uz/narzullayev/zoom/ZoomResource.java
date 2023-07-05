package uz.narzullayev.zoom;


import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uz.narzullayev.zoom.dto.*;
import uz.narzullayev.zoom.dto.meeting.ZoomMeeting;
import uz.narzullayev.zoom.dto.meeting.ZoomMeetingListIterator;
import uz.narzullayev.zoom.dto.meeting.ZoomMeetingRequest;

import java.io.IOException;

@RestController
@RequestMapping("/zoom/sso")
@RequiredArgsConstructor
public class ZoomResource {
    private final ZoomService zoomService;
    private final ZoomProperties zoomProperties;

    @GetMapping
    public ZoomAccessToken getAccess(@RequestParam("code") String code,
                                     HttpServletRequest httpServletRequest) throws Exception {
        ZoomAuthorizerOAuth authorizer = new ZoomAuthorizerGetOAuthToken(
                zoomProperties.getClientId(),
                zoomProperties.getClientSecret()
        );
        ZoomAPI za = new ZoomAPI(authorizer, zoomProperties);
        SimplePersist db = new SimplePersist("oauth-token.db");
        ZoomAccessToken tkn = za.requestAccessToken(code);

        ZoomAuthorizerOAuth zoomAuthorizerOAuth = new ZoomAuthorizerOAuth(zoomProperties.getClientId(),
                zoomProperties.getClientSecret(), tkn
        ) {
            @Override
            public void onNewToken(ZoomAccessToken tkn) throws ZoomAPIException {
                try {
                    db.save(tkn);
                } catch (Exception e) {
                    System.err.println("Failed to save latest refreshed access token");
                    e.printStackTrace();
                }
            }
        };
        System.out.println(tkn);
        ZoomAPI zoomAPI = new ZoomAPI(zoomAuthorizerOAuth,zoomProperties);
        ZoomUser me = zoomAPI.getUser("me");
        System.out.println(me);

        ZoomMeetingListIterator me1 = zoomAPI.listMeetings("me");
        System.out.println(me1);
        ZoomMeetingRequest mreq = ZoomMeetingRequest.requestDefaults("Test Meeting", "Let's talk about the weather");
        ZoomMeeting meeting = zoomAPI.createMeeting("me", mreq);

        System.out.println(meeting);
        return tkn;
    }


    @GetMapping("/access")
    public Object access() throws IOException {
        SimplePersist db = new SimplePersist("oauth-token.db");
        ZoomAccessToken load = db.load(ZoomAccessToken.class);
        return load;
    }
}
