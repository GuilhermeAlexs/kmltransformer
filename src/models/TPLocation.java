package models;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.List;

public class TPLocation implements Serializable{
	private static final long serialVersionUID = 5063591282101764535L;

	@JsonProperty
	private long id;
	@JsonProperty
	private String name;
	@JsonProperty
	private double latitude;
	@JsonProperty
	private double longitude;
	@JsonProperty
	private double altitude;
	@JsonProperty
	private TrailType type;
	@JsonProperty
	private String uc;
	@JsonProperty
	private String river;
	@JsonProperty
	private TrailEnvironment environment;
	@JsonIgnore
	private double nearDistance;
	@JsonIgnore
	private List<City> nearestCities;
	@JsonIgnore
	private String nearestCityId;
	
	public TPLocation() {
		super();
	}
	
	public TPLocation(double latitude, double longitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public TPLocation(double latitude, double longitude, double altitude) {
		super();
		this.latitude = latitude;
		this.longitude = longitude;
		this.altitude = altitude;
	}

	public TPLocation(String name, double latitude, double longitude, TrailType type) {
		super();
		this.name = name;
		this.latitude = latitude;
		this.longitude = longitude;
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	
	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public TrailType getType() {
		return type;
	}

	public void setType(TrailType type) {
		this.type = type;
	}

	public double getNearDistance() {
		return nearDistance;
	}

	public void setNearDistance(double nearDistance) {
		this.nearDistance = nearDistance;
	}

	public List<City> getNearestCities() {
		return nearestCities;
	}

	public void setNearestCities(List<City> nearestCities) {
		this.nearestCities = nearestCities;
	}

    public String getUc() {
        return uc;
    }

    public void setUc(String uc) {
        this.uc = uc;
    }

	public TrailEnvironment getEnvironment() {
		return environment;
	}

	public void setEnvironment(TrailEnvironment environment) {
		this.environment = environment;
	}

	public String getNearestCityId() {
		return nearestCityId;
	}

	public void setNearestCityId(String nearestCityId) {
		this.nearestCityId = nearestCityId;
	}

	public String getRiver() {
		return river;
	}

	public void setRiver(String river) {
		this.river = river;
	}
}
