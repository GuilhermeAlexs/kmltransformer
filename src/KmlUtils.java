import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.snatik.polygon.Polygon.Builder;

import ch.hsr.geohash.GeoHash;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;

public class KmlUtils {
	private static long count = 0;
	
	public static Placemark findByName(Kml kml, String name){
		Document doc = (Document) kml.getFeature();
        List<Feature> listFeat = doc.getFeature();
        Iterator<Feature> it = listFeat.iterator(); 
        
        while(it.hasNext()){
        	Feature feat = it.next();
        	if(feat instanceof Folder){
        		Folder folder = (Folder) feat;
        		Iterator<Feature> itFolder = folder.getFeature().iterator();
        		
        		while(itFolder.hasNext()){
        			Feature featFolder = itFolder.next();
        			if(featFolder instanceof Placemark){
        				Placemark p = (Placemark) featFolder;
        				if(p.getName().equals("name"))
        					return p;
        			}
        		}
        	}
        }
        
        return null;
	}
	
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
	
	public static ConservationUnit parseUCFromPolygon(String name, Polygon poly){
		ConservationUnit cUnit = new ConservationUnit();
		cUnit.setName(name);
		
		List<Coordinate> coords = poly.getOuterBoundaryIs().getLinearRing().getCoordinates();
		//String strCoords = "";
		//TPLocation avgLoc = new TPLocation();
		int nCoords = 0;
		com.snatik.polygon.Polygon.Builder polygonBuilder = com.snatik.polygon.Polygon.Builder();
		
		for(Coordinate c: coords){

			TPLocation loc = coordinateToTPLocation(c);
			
			if(nCoords == 0)
				loc.setLongitude(-loc.getLongitude());
		
			//avgLoc.setLatitude(avgLoc.getLatitude() + loc.getLatitude());
			//avgLoc.setLongitude(avgLoc.getLongitude() + loc.getLongitude());

			polygonBuilder.addVertex(new com.snatik.polygon.Point(loc.getLatitude(), loc.getLongitude()));
			//strCoords = strCoords + GeoHash.withBitPrecision(loc.getLatitude(), loc.getLongitude(), 32).hashCode() + " ";
			
			nCoords++;
		}
		
		//avgLoc.setLatitude(avgLoc.getLatitude() / nCoords);
		//avgLoc.setLongitude(avgLoc.getLongitude() / nCoords);
		
		cUnit.setPolygon(polygonBuilder.build());
		//cUnit.setCentroid(avgLoc);
		
		return cUnit;
	}

	public static List<ConservationUnit> parsePlacemarkUC(Placemark p, KmlParseProgressListener listener) throws Exception{
		Geometry geometry = p.getGeometry();
		List<ConservationUnit> ucs = new ArrayList<ConservationUnit>();
		
		if(geometry instanceof MultiGeometry){
			MultiGeometry mGeo = (MultiGeometry) p.getGeometry();
			List<Geometry> listGeometries = mGeo.getGeometry();
			
			for(Geometry g: listGeometries){
				ucs.add(parseUCFromPolygon(p.getName(), (Polygon) g));
			}
		}else if(geometry instanceof Polygon){
			ucs.add(parseUCFromPolygon(p.getName(), (Polygon) geometry));
		}
		
		if(listener != null)
			listener.onParseProgress((int) count);
		
		return ucs;
	}
	
	public static TPLocation parsePlacemarkCave(Placemark p, KmlParseProgressListener listener) throws Exception{
		String strToCompare = p.getName().trim().toUpperCase();
		if(!(strToCompare.contains("CAVERNA") || strToCompare.contains("GRUTA") || strToCompare.contains("ABISMO")))
			return null;
		
		Geometry geometry = p.getGeometry();
		List<Coordinate> coords = new ArrayList<Coordinate>();
		TPLocation loc = new TPLocation();
		Point point = null;
		
		
		if(geometry instanceof Point){
			point = (Point) p.getGeometry();
			coords.addAll(point.getCoordinates());
		}else{
			return loc;
		}

		count++;
		
		Coordinate coord = coords.get(0);
		
		loc = coordinateToTPLocation(coord);

		loc.setId(count);
		loc.setName(p.getName());

		if(listener != null)
			listener.onParseProgress((int) count);
		
		return loc;
	}
	
	public static TPLocation parsePlacemark(Placemark p, KmlParseProgressListener listener) throws Exception{
		Geometry geometry = p.getGeometry();
		List<Coordinate> coords = new ArrayList<Coordinate>();
		TPLocation loc = new TPLocation();
		Point point = null;
		
		if(geometry instanceof Point){
			point = (Point) p.getGeometry();
			coords.addAll(point.getCoordinates());
		}else{
			return loc;
		}

		count++;
		
		Coordinate coord = coords.get(0);
		
		loc = coordinateToTPLocation(coord);
		
		if(p.getStyleUrl().equals("#map_known"))
			loc.setType(TrailType.WELL_KNOWN);
		else if(p.getStyleUrl().equals("#map_unknown_visited"))
			loc.setType(TrailType.BARELY_KNOWN);
		else if(p.getStyleUrl().equals("#map_unknown_not_visited"))
			loc.setType(TrailType.UNKNOWN);
		else
			return null;

		loc.setId(count);
		loc.setName(p.getName());

		if(listener != null)
			listener.onParseProgress((int) count);
		
		return loc;
	}
	
	private static void traverseKml(Feature feat, KmlParseProgressListener listener){
		if(feat instanceof Folder){
			Folder folder = (Folder) feat;
			listener.onParseFolder(folder);
			Iterator<Feature> it = folder.getFeature().iterator();

			while(it.hasNext())
				traverseKml(it.next(), listener);
		}else if(feat instanceof Placemark){
			listener.onParsePlacemark((Placemark) feat);
		}
		
		return;
	}
	
	public static void parseKml(Kml kml, KmlParseProgressListener listener){
		List<Feature> listFeat;
		
		if(kml.getFeature() instanceof Document){
			Document doc = (Document) kml.getFeature();
			listFeat = doc.getFeature();
		}else if(kml.getFeature() instanceof Folder){
			Folder folder = (Folder) kml.getFeature();
			listFeat = folder.getFeature();
		}else{
			return;
		}
		
        Iterator<Feature> it = listFeat.iterator();
        
		if(listener != null)
			listener.onPreParse(0);
        
        while(it.hasNext())
        	traverseKml(it.next(), listener);
        
        listener.onParseFinish(false);
	}
}
