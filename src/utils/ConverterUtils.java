package utils;

import java.util.ArrayList;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import models.TPLocation;

public class ConverterUtils {
	
	public static TPLocation stringToTPLocation(String c){
		String [] coords = c.split(" ");
		
		if(coords.length == 3)
			return new TPLocation(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]), Double.parseDouble(coords[2]));
		else
			return new TPLocation(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));

	}
	
	public static String coordinateToString(Coordinate c){
		return c.getLatitude() + "," + c.getLongitude();
	}

	public static TPLocation coordinateToTPLocation(Coordinate c){
		return new TPLocation(c.getLatitude(), c.getLongitude(), c.getAltitude());
	}

	public static List<Coordinate> tpLocationsToCoordinates(List<TPLocation> locs){
		List<Coordinate> coords = new ArrayList<Coordinate>();
		
		for(TPLocation loc: locs){
			Coordinate coord = new Coordinate(loc.getLongitude(), loc.getLatitude(), loc.getAltitude());
			coords.add(coord);
		}
		
		return coords;		
	}
}
