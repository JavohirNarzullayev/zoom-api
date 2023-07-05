package uz.narzullayev.zoom.dto.meeting;

import lombok.Getter;
import lombok.Setter;
import uz.narzullayev.zoom.dto.AsJsonString;

import static uz.narzullayev.zoom.dto.meeting.ZoomMeeting.*;

/**
 * Represents a request for a Zoom meeting
 *
 * @author charles.lobo
 */
@Getter
@Setter
public class ZoomMeetingRequest extends AsJsonString {
	public String topic;
	public Integer type;
	public String start_time;
	public Integer duration;
	public String schedule_for;
	public String timezone;
	public String password;
	public String agenda;
	public TrackingField[] tracking_fields;
	public Recurrence recurrence;
	public Settings settings;

	public static ZoomMeetingRequest requestDefaults(String topic, String agenda) {
		ZoomMeetingRequest zmr = new ZoomMeetingRequest();
		zmr.topic = topic;
		zmr.type = 1;
		zmr.agenda = agenda;
		zmr.settings = new Settings();
		zmr.settings.join_before_host = true;
		zmr.settings.approval_type = 2;
		zmr.settings.meeting_authentication = false;
		return zmr;
	}
}
