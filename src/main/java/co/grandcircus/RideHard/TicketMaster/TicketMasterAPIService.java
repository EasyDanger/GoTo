package co.grandcircus.RideHard.TicketMaster;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

//import co.grandcircus.RideHard.ParkWhizApi.Park;

@Component
public class TicketMasterAPIService {

	@Value("${apiKey}")
	private String apiKey;
	
	private RestTemplate restTemplate;

	// This is an instance initialization block. It runs when a new instance of the
	// class is created--
	// right before the constructor.
	{
		// This configures the restTemplateWithUserAgent to include a User-Agent header
		// with every HTTP
		// request. Some of the APIs in this demo have a bug where User-Agent is
		// required.
		ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
			request.getHeaders().add(HttpHeaders.USER_AGENT, "Spring");
			return execution.execute(request, body);
		};
		restTemplate = new RestTemplateBuilder().additionalInterceptors(interceptor).build();
	}

	
	public TicketMasterAPIResponse getPark() {

		// Map<String, String> request = new HashMap<>();
		// request.put("q=",

		String url = "https://app.ticketmaster.com/discovery/v2/events.json?size=1&apikey=" + apiKey;

		TicketMasterAPIResponse response = restTemplate.getForObject(url, TicketMasterAPIResponse.class);

		return response;
	}

	
	public TicketMasterAPIResponse searchEvents(String keyword) {
		String url = "https://app.ticketmaster.com/discovery/v2/events.json?size=100&apikey=" + apiKey + "&keyword="
				+ keyword + "";
		TicketMasterAPIResponse response = restTemplate.getForObject(url, TicketMasterAPIResponse.class);
		return response;

	}

}
