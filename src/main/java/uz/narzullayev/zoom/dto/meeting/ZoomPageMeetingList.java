package uz.narzullayev.zoom.dto.meeting;

import lombok.Getter;
import lombok.Setter;
import uz.narzullayev.zoom.dto.AsJsonString;

/**
 * Represents a Zoom Meeting List.
 *
 * @author charles.lobo
 */
@Getter
@Setter
public class ZoomPageMeetingList extends AsJsonString {
	public Integer page_count;
	public Integer page_number;
	public Integer page_size;
	public Integer total_records;

	public ZoomMeetingInfo[] meetings;
}
