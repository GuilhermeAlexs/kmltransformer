package parsers.specificparsers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import com.snatik.polygon.Point;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import models.City;
import models.ConservationUnit;
import models.TPLocation;
import models.TrailEnvironment;
import models.TrailType;
import parsers.KmlParseProgressListener;
import parsers.KmlParser;

public class CaveParserWithUCS implements KmlParseProgressListener {
	private List<TPLocation> locs;
	private String outputName;

	public CaveParserWithUCS(String outputName){
		this.locs = new ArrayList<TPLocation>();
		this.outputName = outputName;
	}

	@Override
	public void onPreParse(int progressTotal) {
		System.out.println("Starting parser...");
	}

	@Override
	public void onParseProgress(int progress) {
	}

	@Override
	public void onParseFolder(Folder folder) {
		System.out.println("Parsing Folder: " + folder.getName());
	}

	@Override
	public void onParsePlacemark(Placemark p) {
		try {
			TPLocation loc = KmlParser.parsePlacemarkCave(p, this);

			if(loc != null && loc.getName() != null){
				locs.add(loc);
				System.out.println(loc.getName());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onParseFinish(boolean altitudeWasDownloaded) {
		
		try {
			File file = new File("ucs.kml");
		    String str = FileUtils.readFileToString(file, "ISO-8859-1");
	 	    str = str.replace("xmlns=\"http://earth.google.com/kml/2.2\"", "xmlns=\"http://www.opengis.net/kml/2.2\"" );
	 	    ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes( "ISO-8859-1" ));
	 	    Kml kml = Kml.unmarshal(bais);
	 	    List<ConservationUnit> finalUCList = new ArrayList<ConservationUnit>();
	 	   
			KmlParser.parseKml(kml, new KmlParseProgressListener(){

				@Override
				public void onPreParse(int progressTotal) {
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
						double d = (loc.getLatitude() - Double.parseDouble(c.getLatitude()))*(loc.getLatitude() - Double.parseDouble(c.getLatitude())) + 
							       (loc.getLongitude() - Double.parseDouble(c.getLongitude()))*(loc.getLongitude() - Double.parseDouble(c.getLongitude()));
						
						System.out.println(c.getId() + "d=" + d + " minD=" + minD);
						
						if(d < minD){
							minD = d;
							loc.setNearestCityId(c.getId());
						}
					}


					return loc;
				}

				private void printToFile(PrintWriter writer, TPLocation loc){
					writer.println(loc.getId() + "$" + loc.getName() + "$" + loc.getLatitude() + "$" + loc.getLongitude() + 
							"$" + loc.getUc() + "$" + TrailType.WELL_KNOWN.getValue() + "$" + TrailEnvironment.CAVE.getValue() + "$" + loc.getNearestCityId());
				}

				private List<City> getCities(){
					List<City> cities = new ArrayList<>();
					boolean stop[] = new boolean[1];
					stop[0] = false;
					
					try (Stream<String> stream = Files.lines(Paths.get("cities.csv"))) {
						stream.forEach(line -> {
							if(!stop[0]){
								String f [] = line.split(";");
								
								City c = new City();
								c.setId(f[0]);
								c.setLongitude(f[3]);
								c.setLatitude(f[4]);
								
								cities.add(c);
			
								if(f[0].equals("10078"))
									stop[0] = true;
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
						 PrintWriter writer = new PrintWriter(outputName, "UTF-8");
						 
						 List<City> cities = getCities();
						 
						 locs.stream  ()
							.map	 (loc -> getLocWithUC(cities, loc))
							.forEach (loc -> printToFile(writer, loc));

						  writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}	
				}
			});
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
