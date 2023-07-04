package uz.narzullayev.zoom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import uz.narzullayev.zoom.dto.AsJsonString;
import uz.narzullayev.zoom.dto.ZoomAPIException;

/**
 * Represents a Zoom Access Token.
 *
 * @author charles.lobo
 */
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZoomAccessToken  extends AsJsonString {
	public String access_token;
	public String token_type;
	public String refresh_token;
	private long expires_in;
	public String scope;


	public static ZoomAccessToken fromJSONString(String json) throws ZoomAPIException {
		if(json == null) return null;
		return fromString(json, ZoomAccessToken.class);
	}
}
