package utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import models.TPLocation;

public class ConverterUtils {
	public static Map<String,String> ufs = new Hashtable<>();
	
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
	
	public static String toShortUFName(String name){
		if(ufs.size() == 0){
			ufs.put("acre","AC");
			ufs.put("alagoas","AL");
			ufs.put("amapá","AP");
			ufs.put("amazonas","AM");
			ufs.put("bahia","BA");
			ufs.put("ceará","CE");
			ufs.put("distrito federal","DF");
			ufs.put("espírito santo","ES");
			ufs.put("goiás","GO");
			ufs.put("maranhão","MA");
			ufs.put("mato grosso","MT");
			ufs.put("mato grosso do sul","MS");
			ufs.put("minas gerais","MG");
			ufs.put("pará","PA");
			ufs.put("paraíba","PB");
			ufs.put("paraná","PR");
			ufs.put("pernambuco","PE");
			ufs.put("piauí","PI");
			ufs.put("rio de janeiro","RJ");
			ufs.put("rio grande do norte","RN");
			ufs.put("rio grande do sul","RS");
			ufs.put("rondônia","RO");
			ufs.put("roraima","RR");
			ufs.put("santa catarina","SC");
			ufs.put("são paulo","SP");
			ufs.put("sergipe","SE");
			ufs.put("tocantins","TO");
		}

		return ufs.getOrDefault(name.toLowerCase(), name.toUpperCase());
	}
}
