package parsers;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LineString;
import de.micromata.opengis.kml.v_2_2_0.MultiGeometry;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import models.ConservationUnit;
import models.River;
import models.TPLocation;
import models.TrailType;
import one.util.streamex.StreamEx;
import utils.ConverterUtils;
import utils.KMLUtils;

public class KmlParser {
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
	
	public static ConservationUnit parseUCFromPolygon(String name, Polygon poly){
		ConservationUnit cUnit = new ConservationUnit();
		cUnit.setName(name);
		
		List<Coordinate> coords = poly.getOuterBoundaryIs().getLinearRing().getCoordinates();
		int nCoords = 0;
		com.snatik.polygon.Polygon.Builder polygonBuilder = com.snatik.polygon.Polygon.Builder();
		
		for(Coordinate c: coords){

			TPLocation loc = ConverterUtils.coordinateToTPLocation(c);
			
			if(nCoords == 0)
				loc.setLongitude(-loc.getLongitude());

			polygonBuilder.addVertex(new com.snatik.polygon.Point(loc.getLatitude(), loc.getLongitude()));
			
			nCoords++;
		}
		
		cUnit.setPolygon(polygonBuilder.build());
		
		return cUnit;
	}
	
	public static List<TPLocation> makeDetailed(LineString line){
		List<Coordinate> coords = new ArrayList<>();
		List<TPLocation> coordsFinal = new ArrayList<>();

		coordsFinal.add(ConverterUtils.coordinateToTPLocation(line.getCoordinates().get(0)));
		
		StreamEx.of(line.getCoordinates()).forPairs((curr,next) -> {
			TPLocation a = ConverterUtils.coordinateToTPLocation(curr);
			TPLocation b = ConverterUtils.coordinateToTPLocation(next);
			
			coordsFinal.add(new TPLocation((a.getLatitude() + b.getLatitude())/2d, (a.getLongitude() + b.getLongitude())/2d));
			coordsFinal.add(b);
		});
		
		return coordsFinal;
	}
	
	public static River parseRiverFromLineString(String name, LineString lineString){
		River r = new River();
		r.setName(name);
		
		List<TPLocation> coords = makeDetailed(lineString);
		
		for(TPLocation c: coords){
			r.addLocation(c);
		}
				
		return r;
	}

	public static List<River> parsePlacemarkRiver(Placemark p, KmlParseProgressListener listener) throws Exception{
		Geometry geometry = p.getGeometry();
		List<River> rivers = new ArrayList<>();
		
		if(geometry instanceof MultiGeometry){
			MultiGeometry mGeo = (MultiGeometry) p.getGeometry();
			List<Geometry> listGeometries = mGeo.getGeometry();
			
			for(Geometry g: listGeometries){
				rivers.add(parseRiverFromLineString(p.getName(), (LineString) g));
			}
		}else{
			rivers.add(parseRiverFromLineString(p.getName(), (LineString) geometry));
		}
		
		if(listener != null)
			listener.onParseProgress((int) count);
		
		return rivers;
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
		
		loc = ConverterUtils.coordinateToTPLocation(coord);

		loc.setId(KMLUtils.getIDFromPlacemarck(p));
		loc.setName(p.getName());

		if(listener != null)
			listener.onParseProgress((int) count);
		
		return loc;
	}
	
	public static TPLocation parsePlacemarkSierras(Placemark p, KmlParseProgressListener listener) throws Exception{
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
		
		loc = ConverterUtils.coordinateToTPLocation(coord);

		loc.setId(KMLUtils.getIDFromPlacemarck(p));
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
		
		loc = ConverterUtils.coordinateToTPLocation(coord);
		
		if(p.getStyleUrl().equals("#map_known"))
			loc.setType(TrailType.WELL_KNOWN);
		else if(p.getStyleUrl().equals("#map_unknown_visited"))
			loc.setType(TrailType.BARELY_KNOWN);
		else if(p.getStyleUrl().equals("#map_unknown_not_visited"))
			loc.setType(TrailType.UNKNOWN);
		else
			return null;

		loc.setId(KMLUtils.getIDFromPlacemarck(p));
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
