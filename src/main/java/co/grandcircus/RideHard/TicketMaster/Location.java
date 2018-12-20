package co.grandcircus.RideHard.TicketMaster;

import co.grandcircus.RideHard.ParkWhizApi.Park;

public class Location {

	private Double longitude;
	private Double latitude;

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	@Override
	public String toString() {
		return "Location [longitude=" + longitude + ", latitude=" + latitude + "]";
	}

	public Location(Double longitude, Double latitude) {
		super();
		this.longitude = longitude;
		this.latitude = latitude;
	}

	public Location() {
	}

	public double distanceFrom(Park park) {
		final double EARTH_RADIUS_FEET = 20902230.9711;
		
		double lat1 = Math.toRadians(latitude);
		double long1 = Math.toRadians(longitude);
		double lat2 = Math.toRadians(park.getLatitude());
		double long2 = Math.toRadians(park.getLongitude());
		// apply the spherical law of cosines with a triangle composed of the
		// two locations and the north pole
		double theCos = Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(long1 - long2);
		double arcLength = Math.acos(theCos);
		return arcLength * EARTH_RADIUS_FEET;
	}

}
