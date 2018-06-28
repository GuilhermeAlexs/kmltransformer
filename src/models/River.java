package models;
import java.util.ArrayList;
import java.util.List;

public class River {
	private String name;
	private List<TPLocation> locations = new ArrayList<>();
	
	public River() {
	}

	public River(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<TPLocation> getLocations() {
		return locations;
	}

	public void setLocations(List<TPLocation> locations) {
		this.locations = locations;
	}

	public void addLocation(double latitude, double longitude){
		locations.add(new TPLocation(latitude, longitude));
	}
	
	public void addLocation(TPLocation loc){
		locations.add(loc);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		locations.forEach(l -> {
			sb.append(l.getLatitude() + "," + l.getLongitude() + "\n");
		});
		
		return sb.toString();
	}
}
