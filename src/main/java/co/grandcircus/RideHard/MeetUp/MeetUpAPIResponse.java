package co.grandcircus.RideHard.MeetUp;

import java.util.List;

public class MeetUpAPIResponse {
	
	private List<Result> results;

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "MeetUpAPIResponse [results=" + results + "]";
	}
	
	

}
