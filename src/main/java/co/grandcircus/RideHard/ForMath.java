package co.grandcircus.RideHard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import co.grandcircus.RideHard.HereCodeAPI.HereCodeAPIService;
import co.grandcircus.RideHard.ParkDao.ParkDao;
import co.grandcircus.RideHard.ParkWhizApi.Park;
import co.grandcircus.RideHard.ParkWhizApi.ParkWhizAPIService;
import co.grandcircus.RideHard.utils.ParkingByDistanceComparator;
import co.grandcircus.RideHard.utils.UrEvent;

//Class to contain the math and science portions of the data. These methods process the data, so the controller class can hold only controllers.
@Component
public class ForMath {

	// Class fields.
	// Database for user entered Park objects.
	@Autowired
	private ParkDao pd;
	// API service fields.
	@Autowired
	private ParkWhizAPIService pwas;
	@Autowired
	private HereCodeAPIService geo;

	// Calculates the gas cost base on driving distance and IRS mileage factor for
	// 2018.
	Double gasCalc(Double drivingDistance) {
		Double gasPrice = drivingDistance * 0.16;
		return gasPrice;
	}

	// Calls on the Parkwhiz API to respond a list of park locations in the area of
	// the given event.
	List<Park> findParkingFromApi(HttpSession session) {
		// Pulls useful variables from the session.
		double howFar = (double) session.getAttribute("howFar");
		UrEvent event = (UrEvent) session.getAttribute("Event");

		// Stores the response from the Parkwhiz API.
		Park[] response = pwas.getPark(event.getLatitude(), event.getLongitude(), event.getDate(), event.getTime(),
				howFar);

		// List to store to store the park objects from the API
		ArrayList<Park> currentParks = new ArrayList<>();
		// Checks to determine whether the parking objects have prices associated with
		// them.
		for (Park park : response) {
			if (park.getPrice() != null) {
				currentParks.add(park);
			}
		}
		return currentParks;
	}

	// Parses database for user entered park objects within the given radius from
	// the event.
	List<Park> findParkingFromDatabase(HttpSession session) {
		// Pulls relevant data from the session.
		double howFarMiles = (double) session.getAttribute("howFar");
		// Converts to feet.
		double howFarFeet = howFarMiles * 5280;
		UrEvent event = (UrEvent) session.getAttribute("Event");

		// An empty list to store the Park objects from the database that fall within
		// the given radius.
		List<Park> psList = new ArrayList<Park>();
		// Stores all the park objects from the database.
		List<Park> fullList = pd.findall();
		// Fill the new list based on the datbase park objects' distance from the event.
		for (Park park : fullList) {
			if ((event.distanceFrom(park) <= howFarFeet) && (park.getPrice() != null)) {
				psList.add(park);
			}
		}
		// Orders new list.
		psList = orderList(psList, session);
		return psList;
	}

	// Method to order the park object lists.
	List<Park> orderList(List<Park> parks, HttpSession session) {
		UrEvent event = (UrEvent) session.getAttribute("Event");
		// Converts distance from event to feet.
		for (Park park : parks) {
			// It verifies that each park object has a lat and long.
			if ((park.getLatitude() == null) || (park.getLongitude() == null)) {
				park.setLatLong(geo.getLatLong(park));
			}
			park.setDistanceInFeet(event.distanceFrom(park));
		}
		// Sorts the list based on distance from event location.
		Collections.sort(parks, new ParkingByDistanceComparator());
		return parks;
	}

	// Method to filter out events from search that have no times, as timeless
	// events break the ParkWhiz API
	public List<UrEvent> filterTimeless(List<UrEvent> events) {
		List<UrEvent> filteredEvents = new ArrayList<UrEvent>();
		for (UrEvent event : events) {
			if (event.getTime() != null) {
				filteredEvents.add(event);
			}
		}
		return filteredEvents;
	}

	// Method to determine the park location with the best ratio of price to
	// distance.
	public Park bestValue(List<Park> allParking) {
		Park temp = new Park();
		Double tempValue = null;

		// Loop to determine which Park object has the "best value"
		for (int i = 0; i < allParking.size(); i++) {
			// Variable stores the "value" factor of the park object, based on distance and
			// weighted price.
			Double value = (allParking.get(i).getPrice() * allParking.get(i).getPrice())
					+ (allParking.get(i).getDistanceInFeet());
			// Determines if current park object has lower value than the previous.
			if ((tempValue == null) || (value < tempValue)) {
				tempValue = value;
				temp = allParking.get(i);
			}
		}
		return temp;
	}
}