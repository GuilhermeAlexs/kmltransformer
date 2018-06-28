package parsers.specificparsers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;

import com.snatik.polygon.Point;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import models.City;
import models.ConservationUnit;
import models.River;
import models.TPLocation;
import models.TrailEnvironment;
import models.TrailType;
import one.util.streamex.StreamEx;
import parsers.KmlParseProgressListener;
import parsers.KmlParser;
import utils.ConverterUtils;
import utils.GeoUtils;
import utils.KMLUtils;

public class TrailsParserWithUCS implements KmlParseProgressListener {
	private List<TPLocation> locs;
	private String outputName;
    private long nEntries = 0;

	public TrailsParserWithUCS(String outputName){
		this.locs = new ArrayList<TPLocation>();
		this.outputName = outputName;
	}

	@Override
	public void onPreParse(int progressTotal) {
		System.out.println("Starting parsing trails...");
	}

	@Override
	public void onParseProgress(int progress) {
	}

	@Override
	public void onParseFolder(Folder folder) {
		System.out.println("Parsing Trail Folder: " + folder.getName());
	}

	@Override
	public void onParsePlacemark(Placemark p) {
		try {
			TPLocation loc = KmlParser.parsePlacemark(p, this);
			
			if(loc.getLatitude() == 0 && loc.getLongitude() == 0)
				return;
				
			if(loc != null && loc.getName() != null){
				locs.add(loc);
				nEntries++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onParseFinish(boolean altitudeWasDownloaded) {
		try {
			File file = new File("ucs.kml");
	 	    Kml kml = Kml.unmarshal(KMLUtils.openKml(file));
	 	    
			KmlParser.parseKml(kml, new TrailWithUCSParser());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class TrailWithUCSParser implements KmlParseProgressListener{
 	    private List<ConservationUnit> finalUCList = new ArrayList<ConservationUnit>();
 	    
		@Override
		public void onPreParse(int progressTotal) {
			System.out.println("Filling Trails with UC");
		}

		@Override
		public void onParseProgress(int progress) {
		}

		@Override
		public void onParseFolder(Folder folder) {
		}

		@Override
		public void onParsePlacemark(Placemark p) {
			try {
				finalUCList.addAll(KmlParser.parsePlacemarkUC(p, this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private boolean isInside(ConservationUnit uc, TPLocation loc){
			return uc.getPolygon().contains(new Point(loc.getLatitude(), loc.getLongitude()));
		}

		private TPLocation getLocWithUC(List<City> cities, TPLocation loc){
			String ucName = finalUCList
					.stream    ()
					.parallel()
					.filter    (uc -> isInside(uc,loc))
					.map	   (uc -> uc.getName())
					.findFirst ()
					.orElse    (null);

			loc.setUc(ucName);

			double minD = Double.MAX_VALUE;
			
			for(City c: cities){
				double d = GeoUtils.computeDistance(loc.getLatitude(), loc.getLongitude(), 
						Double.parseDouble(c.getLatitude()), Double.parseDouble(c.getLongitude()));
				
				if(d < minD){
					minD = d;
					loc.setNearestCityId(c.getId());
					loc.setNearDistance(d);
					
					if(loc.getType() == TrailType.UNKNOWN){
						String explName = ConverterUtils.toShortUFName(c.getUf()) + loc.getId();
						loc.setName(explName);
					}
				}
			}
			
			return loc;
		}
		
		private List<City> getCities(){
			List<City> cities = new ArrayList<>();
			boolean stop[] = new boolean[1];
			stop[0] = false;
			
			try (Stream<String> stream = Files.lines(Paths.get("citiesV2.csv"))) {
				stream.forEach(line -> {
					if(!stop[0]){
						String f [] = line.split(",");
						
						City c = new City();
						c.setId(f[0]);
						c.setName(f[1]);
						c.setUf(f[2]);
						c.setLatitude(f[4]);
						c.setLongitude(f[5]);
						
						cities.add(c);
	
						/*if(f[0].equals("10078"))
							stop[0] = true;*/
					}
				});

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return cities;
		}
		
		@Override
		public void onParseFinish(boolean altitudeWasDownloaded) {
			try {
				List<City> cities = getCities();
			
				locs.stream  ()
					.parallel()
					.forEach (loc -> getLocWithUC(cities, loc));

				finalUCList.clear();
				File file = new File("rios.kml");
		 	    Kml kml = Kml.unmarshal(KMLUtils.openKml(file));
		 	    
				KmlParser.parseKml(kml, new TrailWithRiverParser());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	class TrailWithRiverParser implements KmlParseProgressListener{
 	    private List<River> finalRiverList = new ArrayList<River>();
 	    
		@Override
		public void onPreParse(int progressTotal) {
			System.out.println("Filling Trails with Rivers");

			System.out.println("Distância teste: " + distanceFromLineToPoint(new TPLocation(-13.151143,-47.568302), 
					new TPLocation(-13.125157,-47.561166), new TPLocation(-14.166691,-47.847182)));
		}

		@Override
		public void onParseProgress(int progress) {
		}

		@Override
		public void onParseFolder(Folder folder) {
		}

		@Override
		public void onParsePlacemark(Placemark p) {
			try {
				finalRiverList.addAll(KmlParser.parsePlacemarkRiver(p, this));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		private double distanceFromLineToPoint2(TPLocation a, TPLocation b, TPLocation c){ //A-B the line, C the point
		     double lat1=a.getLatitude();
		     double lon1=a.getLongitude();
		     
		     double lat2=b.getLatitude();
		     double lon2=b.getLongitude();
		     
		     double lat3=c.getLatitude();
		     double lon3=c.getLongitude();
		     
		     return 111*Math.abs((lat2 - lat1)*lon3 - (lon2 - lon1)*lat3 + lon2*lat1 - lat2*lon1)/Math.sqrt(((lat2 - lat1)*(lat2 - lat1)) + ((lon2 - lon1)*(lon2 - lon1)));
		}
		
		private double distanceFromLineToPoint(TPLocation a, TPLocation b, TPLocation c){ //A-B the line, C the point
		     double lat1=a.getLatitude();
		     double lon1=a.getLongitude();
		     
		     double lat2=b.getLatitude();
		     double lon2=b.getLongitude();
		     
		     double lat3=c.getLatitude();
		     double lon3=c.getLongitude();
		     
		     double EARTH_RADIUS_KM=6371;
		     
		     double sinLat1 = Math.sin(lat1);
		     double sinLat2 = Math.sin(lat2);

		     double cosLat1 = Math.cos(lat1);
		     double cosLat2 = Math.cos(lat2);
		     double cosLat3 = Math.cos(lat3);
		     
		     double y = Math.sin(lon3-lon1)*cosLat3;
		     double x = cosLat1 * Math.sin(lat3) - sinLat1 * cosLat3 * Math.cos(lat3 - lat1);
		     double bearing1=Math.toDegrees(Math.atan2(y,x));
		     bearing1=360-(bearing1+360%360);

		     double y2 = Math.sin(lon2 - lon1) * cosLat2;
		     double x2 = cosLat1 * sinLat2 - sinLat1 * cosLat2 * Math.cos(lat2 - lat1);
		     double bearing2 = Math.toDegrees(Math.atan2(y2, x2));
		     bearing2 = 360 - (bearing2 + 360 % 360);

		     double lat1Rads = Math.toRadians(lat1);
		     double lat3Rads = Math.toRadians(lat3);
		     double dLon = Math.toRadians(lon3 - lon1);

		     double distanceAC = Math.acos(Math.sin(lat1Rads) * Math.sin(lat3Rads)+Math.cos(lat1Rads)*Math.cos(lat3Rads)*Math.cos(dLon)) * EARTH_RADIUS_KM;
		     return (Math.abs(Math.asin(Math.sin(distanceAC)*Math.sin(Math.toRadians(bearing1)-Math.toRadians(bearing2))) * EARTH_RADIUS_KM));
		}

		private double distanceToRiver(River r, TPLocation loc){
			double [] minDist = {Double.MAX_VALUE};
			TPLocation [] rCurr = {null};
			TPLocation [] rNext = {null};
			
			StreamEx.of(r.getLocations()).forPairs((curr,next) -> {
				//Line seg = new Line(curr, next, 1);
				double dist = distanceFromLineToPoint(curr, next, loc);
				if(dist < minDist[0]){
					rCurr[0] = curr;
					rNext[0] = next;
					minDist[0] = dist;
				}
			});

			if(r.getName().equals("Rio Paranã"))
				System.out.println(rCurr[0].getLatitude() + "," + rCurr[0].getLongitude() + "->" + rNext[0].getLatitude() + "," + rNext[0].getLongitude());
		
			return minDist[0];
		}

		private River getRiver(TPLocation loc) {
			double [] minDist = {Double.MAX_VALUE};
			River [] river = {null};
			
			if(!loc.getName().contains("Salto 120")) return null;
			
			finalRiverList.forEach(r -> {
				double dist = distanceToRiver(r, loc);

				//System.out.println("Distância de " + loc.getName() + " para " + r.getName() + " = " + dist);
					if(r.getName().equals("Rio Preto")){
						System.out.println("Distância para " + r.getName() + " = " + dist);
					}
					
					if(dist < minDist[0]) {
						//System.out.println("Distância para " + r.getName() + " = " + dist);
						minDist[0] = dist;
						river[0] = r;
					}
			});
			//-18.244015,-48.99775900000001
			System.out.println(loc.getName() + ":" + river[0].getName() + ":" + minDist[0]);
			return river[0];
		}

		private void printToFile(PrintWriter writer, TPLocation loc){
			writer.println(loc.getId() + "$" + loc.getName() + "$" + loc.getLatitude() + "$" + loc.getLongitude() + 
					"$" + loc.getUc() + "$" + loc.getType().getValue() + "$" + TrailEnvironment.WATERFALL.getValue() +
					"$" + loc.getNearestCityId() + "$" + Math.round(loc.getNearDistance()) + "$" + loc.getRiver());
		}
		
		@Override
		public void onParseFinish(boolean altitudeWasDownloaded) {
			try{
				PrintWriter writer = new PrintWriter(outputName, "UTF-8");
				
				locs.stream  ()
					.parallel()
					.forEach (loc -> {
						River r = getRiver(loc);
						
						if(r != null)
							loc.setRiver(r.getName());
						
						printToFile(writer, loc);
					});

				writer.close();

				System.out.println("Foram analisadas " + nEntries + ".");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
