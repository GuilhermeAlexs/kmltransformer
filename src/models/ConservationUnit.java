package models;
import com.snatik.polygon.Polygon;

public class ConservationUnit {
	private String name;
	private Polygon polygon;
	private TPLocation centroid;
	
	public ConservationUnit() {
	}

	public ConservationUnit(String name, Polygon polygon) {
		this.name = name;
		this.polygon = polygon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Polygon getPolygon() {
		return polygon;
	}

	public void setPolygon(Polygon polygon) {
		this.polygon = polygon;
	}

	public TPLocation getCentroid() {
		return centroid;
	}

	public void setCentroid(TPLocation centroid) {
		this.centroid = centroid;
	}
}
