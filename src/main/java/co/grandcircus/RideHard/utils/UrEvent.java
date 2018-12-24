package co.grandcircus.RideHard.utils;

import com.ticketmaster.discovery.model.Event.PriceRange;

public class UrEvent {

	private String name;
	private String venue;
	private String city;
	private String date;
	private String time;
	private Double latitude;
	private Double longitude;
	private Double price;
	private String id;
	private Boolean hasPriceRange = false;
	private PriceRange[] priceRanges;

	public UrEvent(String name, String venue, String city, String date, String time, Double latitude, Double longitude,
			Double price, String id) {
		super();
		this.name = name;
		this.venue = venue;
		this.city = city;
		this.date = date;
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.price = price;
		this.id = id;
	}

	public UrEvent(String name, String venue, String city, String date, String time, Double latitude, Double longitude,
			Double price, String id, PriceRange[] priceRanges) {
		super();
		this.name = name;
		this.venue = venue;
		this.city = city;
		this.date = date;
		this.time = time;
		this.latitude = latitude;
		this.longitude = longitude;
		this.price = price;
		this.id = id;
		this.hasPriceRange = true;
		this.priceRanges = priceRanges;
	}

	public String getName() {
		return name;
	}

	public String getVenue() {
		return venue;
	}

	public String getCity() {
		return city;
	}

	public String getDate() {
		return date;
	}

	public String getTime() {
		return time;
	}

	public Double getLatitude() {
		return latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public Double getPrice() {
		return price;
	}

	public String getId() {
		return id;
	}

	public Boolean getHasPriceRange() {
		return hasPriceRange;
	}

	public PriceRange[] getPriceRanges() {
		return priceRanges;
	}
}
