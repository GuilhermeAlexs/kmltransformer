package models;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

public class River {
	private String name;
	private List<Vector2D> locations = new ArrayList<>();
	
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

	public List<Vector2D> getLocations() {
		return locations;
	}

	public void setLocations(List<Vector2D> locations) {
		this.locations = locations;
	}

	public void addLocation(double latitude, double longitude){
		locations.add(new Vector2D(latitude,longitude));
	}
	
	public void addLocation(Vector2D loc){
		locations.add(loc);
	}
}
