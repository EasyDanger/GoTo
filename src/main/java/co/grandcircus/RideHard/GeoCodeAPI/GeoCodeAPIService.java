package co.grandcircus.RideHard.GeoCodeAPI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import co.grandcircus.RideHard.ParkWhizApi.Park;

@Component
public class GeoCodeAPIService {

	@Value("${geoKey}")
	private String apiKey;

	RestTemplate restTemplate = new RestTemplate();

	public Double[] getLatLong(Park park) {
		String addy = park.getAddress() + park.getCity();
		String url = "https://api.geocod.io/v1.3/geocode?q=" + addy + "&api_key=" + apiKey;
		GeoCodeResponse response = restTemplate.getForObject(url, GeoCodeResponse.class);
		Double[] latLong = { response.getResults().get(0).getLocation().getLatitude(),
				response.getResults().get(0).getLocation().getLongitude() };
		return latLong;
	}

}
