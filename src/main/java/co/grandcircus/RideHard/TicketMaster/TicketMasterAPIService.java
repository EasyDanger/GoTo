package co.grandcircus.RideHard.TicketMaster;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.ticketmaster.api.Version;
import com.ticketmaster.api.discovery.DiscoveryApiConfiguration;
import com.ticketmaster.api.discovery.operation.ByIdOperation;
import com.ticketmaster.api.discovery.operation.SearchAttractionsOperation;
import com.ticketmaster.api.discovery.operation.SearchEventsOperation;
import com.ticketmaster.api.discovery.operation.SearchVenuesOperation;
import com.ticketmaster.api.discovery.response.PagedResponse;
import com.ticketmaster.api.discovery.response.Response;
import com.ticketmaster.api.discovery.util.Preconditions;
import com.ticketmaster.discovery.model.Attraction;
import com.ticketmaster.discovery.model.Attractions;
import com.ticketmaster.discovery.model.Event;
import com.ticketmaster.discovery.model.Events;
import com.ticketmaster.discovery.model.Page.Link;
import com.ticketmaster.discovery.model.Venue;
import com.ticketmaster.discovery.model.Venues;

import okhttp3.HttpUrl;
import okhttp3.HttpUrl.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Component
public class TicketMasterAPIService {
	
	private RestTemplate restTemplateWithUserAgent;
	
	// This is an instance initialization block. It runs when a new instance of the class is created--
	// right before the constructor.
	{
	    // This configures the restTemplateWithUserAgent to include a User-Agent header with every HTTP
		// request. Some of the APIs in this demo have a bug where User-Agent is required.
		ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
	        request.getHeaders().add(HttpHeaders.USER_AGENT, "Spring");
	        return execution.execute(request, body);
	    };
	    restTemplateWithUserAgent = new RestTemplateBuilder().additionalInterceptors(interceptor).build();
	}

  private Logger logger = LoggerFactory.getLogger(TicketMasterAPIService.class);

  private static final String USER_AGENT = "User-Agent";
  private  String apiKeyQueryParam;
  private  OkHttpClient client;
  private  DiscoveryApiConfiguration configuration;
  private  ObjectMapper mapper;
  private  String apiKey;
  private  HashMap<Class<?>, String> pathByType;


  @Autowired
  public TicketMasterAPIService(@Value("${apiKey}") String apiKey) {
    this(apiKey, DiscoveryApiConfiguration.builder().build());
  }

  public TicketMasterAPIService(String apiKey, DiscoveryApiConfiguration configuration) {
    Preconditions.checkNotNull(apiKey, "The API key is mandatory");
    this.apiKey = apiKey;
    this.configuration = configuration;
    this.mapper = new ObjectMapper() //
        .registerModule(new JodaModule()) //
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    this.client = new OkHttpClient.Builder()
                 .readTimeout(configuration.getSocketTimeout(), TimeUnit.MILLISECONDS)
                 .connectTimeout(configuration.getSocketConnectTimeout(), TimeUnit.MILLISECONDS)
                 .build();

    this.pathByType = new HashMap<>();
    this.pathByType.put(Event.class, "events");
    this.pathByType.put(Attraction.class, "attractions");
    this.pathByType.put(Venue.class, "venues");

    this.apiKeyQueryParam=configuration.getApiKeyQueryParam();
  }

  public Response<Event> getEvent(ByIdOperation operation) throws IOException {
    return getById(operation, Event.class);
  }

  public Response<Venue> getVenue(ByIdOperation operation) throws IOException {
    return getById(operation, Venue.class);
  }

  public Response<Attraction> getAttraction(ByIdOperation operation) throws IOException {
    return getById(operation, Attraction.class);
  }

  public PagedResponse<Events> searchEvents(SearchEventsOperation operation) throws IOException {
    logger.debug("searchEvents invoked with {}", operation);

    Builder builder = urlBuilder(pathByType.get(Event.class));
    for (Entry<String, String> e : operation.getQueryParameters().entrySet()) {
      builder.addQueryParameter(e.getKey(), e.getValue());
    }

    logger.debug("searchEvents about to load {}", builder.build());
    Request request = getRequest(builder.build());
    okhttp3.Response response = client.newCall(request).execute();

    return new PagedResponse<Events>(response, mapper, Events.class);
  }

  public PagedResponse<Attractions> searchAttractions(SearchAttractionsOperation operation)
      throws IOException {
    logger.debug("searchAttractions invoked with {}", operation);

    Builder builder = urlBuilder(pathByType.get(Attraction.class));
    for (Entry<String, String> e : operation.getQueryParameters().entrySet()) {
      builder.addQueryParameter(e.getKey(), e.getValue());
    }

    logger.debug("searchAttractions about to load {}", builder.build());
    Request request = getRequest(builder.build());
    okhttp3.Response response = client.newCall(request).execute();

    return new PagedResponse<Attractions>(response, mapper, Attractions.class);
  }

  public PagedResponse<Venues> searchVenues(SearchVenuesOperation operation) throws IOException {
    logger.debug("searchVenues invoked with {}", operation);

    Builder builder = urlBuilder(pathByType.get(Venue.class));
    for (Entry<String, String> e : operation.getQueryParameters().entrySet()) {
      builder.addQueryParameter(e.getKey(), e.getValue());
    }

    logger.debug("searchVenues about to load {}", builder.build());
    Request request = getRequest(builder.build());
    okhttp3.Response response = client.newCall(request).execute();

    return new PagedResponse<Venues>(response, mapper, Venues.class);
  }

  public <T> PagedResponse<T> nextPage(PagedResponse<T> response) throws IOException {
    if (response == null || response.getNextPageLink() == null) {
      return null;
    }

    return navigateTo(response.getNextPageLink(), response.getType());
  }

  public <T> PagedResponse<T> previousPage(PagedResponse<T> response) throws IOException {
    Link previous = response.getPreviousPageLink();
    if (previous == null) {
      return null;
    }

    return navigateTo(response.getPreviousPageLink(), response.getType());
  }

  private Builder baseUrlBuilder() {
    Builder builder = new Builder();
    builder.scheme(configuration.getProtocol());
    builder.host(configuration.getDomainName());
    if (configuration.isPortSet()) {
      builder.port(configuration.getPort());
    }
    return builder;
  }

  // Package protected for testing purposess
  Builder urlBuilder(String path) {
    Builder builder =
        baseUrlBuilder().addPathSegment(configuration.getApiPackage())
            .addPathSegment(configuration.getApiVersion()).addPathSegment(path);

    if (configuration.getPathModifier() != DiscoveryApiConfiguration.PathModifier.NONE) {
      builder.addPathSegment(configuration.getPathModifier().getModifier());
    }

    return builder;
  }

  private <T> Response<T> getById(ByIdOperation operation, Class<T> clazz) throws IOException {
    logger.debug("get{} invoked with {}", clazz.getSimpleName(), operation);
    Builder builder = urlBuilder(pathByType.get(clazz));

    builder.addPathSegment(operation.getId());
    for (Entry<String, String> e : operation.getQueryParameters().entrySet()) {
      builder.addQueryParameter(e.getKey(), e.getValue());
    }

    Request request = getRequest(builder.build());
    okhttp3.Response response = client.newCall(request).execute();

    return new Response<T>(response, mapper, clazz);
  }

  private <T> PagedResponse<T> navigateTo(Link link, Class<T> type) throws IOException {
    HttpUrl baseUrl = baseUrlBuilder().build();
    HttpUrl nextUrl = baseUrl.resolve(link.getRelativeHref());
    logger.debug("About to navigate to {}", nextUrl);
    Request request = getRequest(nextUrl);

    okhttp3.Response nextResponse = client.newCall(request).execute();

    return new PagedResponse<T>(nextResponse, mapper, type);
  }


  private Request getRequest(HttpUrl url) {
    Builder urlBuilder = url.newBuilder().addQueryParameter(apiKeyQueryParam, apiKey);
    if (configuration.getDefaultLocale() != null && url.queryParameter("locale") == null) {
      urlBuilder.addQueryParameter("locale", configuration.getDefaultLocale());
    }

    return new Request.Builder().url(urlBuilder.build())
        .addHeader(USER_AGENT, Version.getUserAgent()).build();
  }
}
