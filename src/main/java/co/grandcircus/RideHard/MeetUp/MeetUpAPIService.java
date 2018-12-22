package co.grandcircus.RideHard.MeetUp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import co.grandcircus.RideHard.utils.UrEvent;

@Component
public class MeetUpAPIService {

	@Value("${meetupKey}")
	private String apiKey;

	private RestTemplate restTemplate;

	{ // This configures the restTemplateWithUserAgent to include a User-Agent header
		// with every HTTP request.
		ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
			request.getHeaders().add(HttpHeaders.USER_AGENT, "Spring");
			return execution.execute(request, body);
		};
		restTemplate = new RestTemplateBuilder().additionalInterceptors(interceptor).build();
	}

	// Method to search for events by keyword.
	public List<UrEvent> searchEventsByAll(String keyword, String city, String country) {
		String url = "https://api.meetup.com/2/open_events.json?text=" + keyword
				+ "&sign=true&photo-host=public&country=" + country + "&city=" + city
				+ "&limited_events=true&radius=smart&page=100&key=" + apiKey;
		MeetUpAPIResponse response = restTemplate.getForObject(url, MeetUpAPIResponse.class);
		List<UrEvent> events = convertEvents(response);
		return events;
	}
	
	public List<UrEvent> searchEventsByAll(String keyword, String city, String state, String country) {
		String url = "https://api.meetup.com/2/open_events.json?text=" + keyword
				+ "sign=true&photo-host=public&country=" + country + "&city=" + city + "&state=" + state
				+ "&limited_events=true&radius=smart&page=100&key=" + apiKey;
		System.out.println("citySearchEvents: " + url);
		MeetUpAPIResponse response = restTemplate.getForObject(url, MeetUpAPIResponse.class);
		List<UrEvent> events = convertEvents(response);
		return events;
	}
	
	// Method to search for events by keyword.
	public List<UrEvent> searchEventsByCity(String city, String country) {
		String url = "https://api.meetup.com/2/open_events.json?sign=true&photo-host=public&country=" + country + "&city=" + city
				+ "&limited_events=true&radius=smart&page=100&key=" + apiKey;
		System.out.println("SearchEventsByCity: " + url);
		MeetUpAPIResponse response = restTemplate.getForObject(url, MeetUpAPIResponse.class);
		List<UrEvent> events = convertEvents(response);
		return events;
	}

//	https://api.meetup.com/2/open_events?

	// Method to search events by in the US, which requires a state input.
	public List<UrEvent> searchEventsByCity(String city, String state, String country) {
		String url = "https://api.meetup.com/2/open_events.json?sign=true&photo-host=public&country=" + country + "&city=" + city + "&state=" + state
				+ "&limited_events=true&radius=smart&page=100&key=" + apiKey;
		System.out.println("citySearchEvents: " + url);
		MeetUpAPIResponse response = restTemplate.getForObject(url, MeetUpAPIResponse.class);
		List<UrEvent> events = convertEvents(response);
		return events;
	}

	// Method to search events for both city and keyword. Overloads keyword search.
	public List<UrEvent> searchEvents(String keyword) {
		String url = "https://api.meetup.com/2/open_events.json?text=" + keyword
				+ "&sign=true&photo-host=public&limited_events=true&radius=smart&page=100&key=" + apiKey;
		System.out.println("searchEvents: " + url);
		MeetUpAPIResponse response = restTemplate.getForObject(url, MeetUpAPIResponse.class);
		List<UrEvent> events = convertEvents(response);
		return events;
	}

	public List<UrEvent> convertEvents(MeetUpAPIResponse response) {
		List<UrEvent> events = new ArrayList<UrEvent>();
		System.out.println(response);
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat timeFormat = new SimpleDateFormat("HH:mm");
		for (Result result : response.getResults()) {
			// Creating date from milliseconds
			// using Date() constructor
			Date dateD = new Date(result.getTime());
			Date timeD = new Date(result.getTime());
			String date = dateFormat.format(dateD);
			String time = timeFormat.format(timeD);
//			System.out.println(" " + result.getVenue().getName() /*+ result.getVenue().getCity() +
//					date + time + result.getVenue().getLatitude() + result.getVenue().getLongitude() +
//					result.getFee().getAmount() + result.getId()*/);
			if (result.getFee() == null) {
				Fee fee = new Fee();
				fee.setAmount(0.0);
				result.setFee(fee);
			}
			if (result.getVenue() != null) {
				UrEvent event = new UrEvent(result.getName(), result.getVenue().getName(), result.getVenue().getCity(),
						date, time, result.getVenue().getLatitude(), result.getVenue().getLongitude(),
						result.getFee().getAmount(), result.getId());
				events.add(event);
			}
		}
		return events;
	}

}
