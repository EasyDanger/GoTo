package co.grandcircus.RideHard.MeetUp;

import co.grandcircus.RideHard.utils.ApiSource;

public class Result {
	
	private Venue venue;
	private String id;
	private long time;
	private Group group;
	private String name;
	private Fee fee;
	private Enum<ApiSource> source = ApiSource.MEETUP;

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Venue getVenue() {
		return venue;
	}
	public void setVenue(Venue venue) {
		this.venue = venue;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public Group getGroup() {
		return group;
	}
	public void setGroup(Group group) {
		this.group = group;
	}
	public Enum<ApiSource> getSource() {
		return source;
	}
	public Fee getFee() {
		return fee;
	}
	public void setFee(Fee fee) {
		this.fee = fee;
	}
}
