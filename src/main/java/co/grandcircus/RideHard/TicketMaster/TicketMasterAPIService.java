package co.grandcircus.RideHard.TicketMaster;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ticketmaster.discovery.model.Event.PriceRange;

import co.grandcircus.RideHard.utils.UrEvent;

@Component
public class TicketMasterAPIService {

	@Value("${apiKey}")
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

	public List<UrEvent> searchEvents(String keyword, String city, String state, String country) {
		String url = "https://app.ticketmaster.com/discovery/v2/events.json?size=15&apikey=" + apiKey + "&keyword="
				+ keyword + "&city=" + city + "&state=" + state + "&country=" + country;
		TicketMasterAPIResponse response = restTemplate.getForObject(url, TicketMasterAPIResponse.class);
		List<UrEvent> events = convertEvents(response);
		return events;
	}
	
	public List<UrEvent> convertEvents(TicketMasterAPIResponse response) {
		List<UrEvent> events = new ArrayList<UrEvent>();
		if (response.get_embedded() != null) {
			for (Event result : response.get_embedded().getEvents()) {
				if (result.getPriceRanges() != null) {
					Double price = reasonablePrice(result.getPriceRanges()[0]);
					UrEvent event = new UrEvent(
							result.getName(), 
							result.get_embedded().getVenues().get(0).getName(),
							result.get_embedded().getVenues().get(0).getCity().getName(),
							result.getDates().getStart().getLocalDate(), 
							result.getDates().getStart().getLocalTime(),
							result.get_embedded().getVenues().get(0).getLocation().getLatitude(),
							result.get_embedded().getVenues().get(0).getLocation().getLongitude(), 
							price,
							result.getId(), result.getPriceRanges());
					events.add(event);
				}
			}
		}
		return events;
	}

	// Method to try to account for various prices without expensive prices pulling
	// the average price out of whack.
	public Double reasonablePrice(PriceRange priceRange) {
		Double average = priceRange.getMax() + priceRange.getMin();
		if (average <= (priceRange.getMin() * 2)) {
			return average;
		}
		return priceRange.getMin();
	}
}
