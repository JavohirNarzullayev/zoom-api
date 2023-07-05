package uz.narzullayev.zoom.dto;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import uz.narzullayev.zoom.dto.meeting.ZoomMeeting;
import uz.narzullayev.zoom.dto.meeting.ZoomMeetingListIterator;
import uz.narzullayev.zoom.dto.meeting.ZoomMeetingRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Provides a Java wrapper around the Zoom API, returning Java objects
 * for easy use in code.
 *
 * @author charles.lobo
 */
public class ZoomAPI {

	private final IZoomAuthorizer za;
	private final ZoomProperties zoomProperties;

	public ZoomAPI(IZoomAuthorizer za,ZoomProperties zoomProperties) {
		this.zoomProperties = zoomProperties;
		this.za = za;
	}

	public ZoomUser getUser(String id) throws ZoomAPIException {
		String url = endpoint("users/" + id);
		return get(url, ZoomUser.class);
	}

	public ZoomMeeting createMeeting(String user, ZoomMeetingRequest mreq) throws ZoomAPIException {
		String url = endpoint("/users/" + user + "/meetings");
		return post(url, mreq.toString(), ZoomMeeting.class);
	}

	public ZoomMeetingListIterator listMeetings(String user) throws ZoomAPIException {
		return new ZoomMeetingListIterator(this, user);
	}

	public ZoomAccessToken requestAccessToken(String code) throws ZoomAPIException {
		code = URLEncoder.encode(code, StandardCharsets.UTF_8);
		String redirecturl = zoomProperties.getRedirectUrl();
		redirecturl = URLEncoder.encode(redirecturl, StandardCharsets.UTF_8);
		String url = zoomProperties.getTOKEN_URL() + "?grant_type=authorization_code&code=" + code + "&redirect_uri=" + redirecturl;
		return post(url, null, ZoomAccessToken.class);
	}

	private void refreshAccessToken(IZoomAuthorizer za) throws ZoomAPIException {
		ZoomAccessToken tkn = za.clearOAuthToken();
		if (tkn == null) return;
		ZoomAuthorizerOAuth oauth = (ZoomAuthorizerOAuth) za;
		try {
			String url = zoomProperties.getTOKEN_URL() + "?grant_type=refresh_token&refresh_token=" + URLEncoder.encode(tkn.refresh_token, StandardCharsets.UTF_8);
			ZoomAccessToken refreshed = post(url, null, ZoomAccessToken.class);
			oauth.setNewOAuthToken(refreshed);
			za.onNewToken(refreshed);
		} catch (Throwable e) {
			throw new ZoomAPIException(e);
		}
	}

	public String endpoint(String name) {
		return zoomProperties.getAPI_ENDPOINT() + "/" + name;
	}

	public <T> T post(String url, String reqJSON, Class<T> cls) throws ZoomAPIException {
		return callWithRetry("POST", url, reqJSON, cls);
	}

	public <T> T get(String url, Class<T> cls) throws ZoomAPIException {
		return callWithRetry("GET", url, null, cls);
	}

	/*		understand/
	 * The OAuth token is only valid for a certain period (1 hr?). After that, a new
	 * request must be made with the associated 'refresh_token' to get another valid
	 * oauth token.
	 *
	 * 		outcome/
	 * Make the zoom call. If it fails with an authorization exception, see if we
	 * can refresh this token. If we can, refresh it and try again.
	 */
	private <T> T callWithRetry(String method, String url, String reqJSON, Class<T> cls) throws ZoomAPIException {
		try {
			return call(method, url, reqJSON, cls);
		} catch (ZoomAuthException e) {
			/* ok we got an authorization failure - let's try again */
		}
		if(!za.canRefresh()) {
			throw new ZoomAPIException("Authorization failed");
		}
		refreshAccessToken(za);
		try {
			return call(method, url, reqJSON, cls);
		} catch (ZoomAuthException e) {
			/* yeah - we give up */
			throw new ZoomAPIException("Authorization failed");
		}
	}

	/*		outcome/
	 * Make a generic call to the given URL with the given method and body, read
	 * the response as a JSON and convert it into the requested class type.
	 */
	private <T> T call(String method, String url, String reqJSON, Class<T> cls) throws ZoomAPIException, ZoomAuthException {
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		int status;

		try {

			URL url_ = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) url_.openConnection();
			conn.setRequestMethod(method);
			conn.setRequestProperty("Authorization", za.authHeader());

			if(reqJSON != null && reqJSON.length() > 0) {
				OutputStream out = null;
				try {
					conn.setRequestProperty("content-type", "application/json");
					conn.setDoOutput(true);
					out = conn.getOutputStream();
					out.write(reqJSON.getBytes(StandardCharsets.UTF_8));
					out.flush();
					out.close();
				} catch(Throwable t) {
					if(out != null) out.close();
					throw new ZoomAPIException(t);
				}

			}

			status = conn.getResponseCode();
			InputStream in = status > 299 ? conn.getErrorStream() : conn.getInputStream();
			byte[] buffer = new byte[1024];
			int length, total = 0;
			while((length = in.read(buffer)) != -1) {
				body.write(buffer, 0, length);
				total += length;
				if(total > zoomProperties.getMaxResponseSize()) {
					String cl = conn.getHeaderField("Content-Length");
					in.close();
					String xtract = body.toString(StandardCharsets.UTF_8);
					if(xtract.length() > 256) xtract = xtract.substring(0, 256);
					throw new ZoomAPIException("Response too big (Content-Length: " + cl + "). Read: " + total + " bytes\n" + xtract);
				}
			}
			in.close();

		} catch (Throwable e) {
			throw new ZoomAPIException(e);
		}

		if(status == 401) {
			throw new ZoomAuthException();
		}

		if(status > 299) {
			String msg = "";
			if(status == 400) msg = "Bad Request";
			if(status == 404) msg = "The resource does not exist";
			if(status == 409) msg = "Conflict when executing request";
			if(status == 429) msg = "Too many requests";
			try {
				msg = body.toString(StandardCharsets.UTF_8);
			} catch(Throwable e) { /* ignore */ }
			throw new ZoomAPIException("Call failed with status: " + status + " " + msg);
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			return mapper.readValue(body.toByteArray(), cls);
		} catch(Throwable e) {
			throw new ZoomAPIException(e);
		}
	}

}
