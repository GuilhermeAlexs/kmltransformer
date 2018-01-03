package parsers.specificparsers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import com.snatik.polygon.Point;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import models.City;
import models.ConservationUnit;
import models.TPLocation;
import models.TrailEnvironment;
import parsers.KmlParseProgressListener;
import parsers.KmlParser;
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

			double minD = 100000000;
			
			cities.stream().forEach(c -> {
				double d = (loc.getLatitude() - Double.parseDouble(c.getLatitude()))*(loc.getLatitude() - Double.parseDouble(c.getLatitude())) + 
						(loc.getLongitude() - Double.parseDouble(c.getLongitude()))*(loc.getLongitude() - Double.parseDouble(c.getLongitude()));
				
				if(d < minD)
					loc.setNearestCityId(c.getId());
			});
			
			return loc;
		}

		private void printToFile(PrintWriter writer, TPLocation loc){
			writer.println(loc.getId() + "$" + loc.getName() + "$" + loc.getLatitude() + "$" + loc.getLongitude() + 
					"$" + loc.getUc() + "$" + loc.getType().getValue() + "$" + TrailEnvironment.WATERFALL.getValue() + "$" + loc.getNearestCityId());
		}		

		private List<City> getCities(){
			List<City> cities = new ArrayList<>();
			
			try (Stream<String> stream = Files.lines(Paths.get("cities.csv"))) {
				stream.forEach(line -> {
					String f [] = line.split(";");
					
					City c = new City();
					c.setId(f[0]);
					c.setLongitude(f[3]);
					c.setLatitude(f[4]);
					
					cities.add(c);
				});

			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return cities;
		}
		
		@Override
		public void onParseFinish(boolean altitudeWasDownloaded) {
			try{
				PrintWriter writer = new PrintWriter(outputName, "UTF-8");
				
				List<City> cities = getCities();
				
				locs.stream  ()
					.parallel()
					.map	 (loc -> getLocWithUC(cities, loc))
					.forEach (loc -> printToFile(writer, loc));

				writer.close();

				System.out.println("Foram analisadas " + nEntries + ".");

				/*
					FileOutputStream fout = new FileOutputStream(outputName);
					ObjectOutputStream oos = new ObjectOutputStream(fout);
					ObjectMapper mapper = new ObjectMapper();
					String resp = mapper.writeValueAsString(locs);
					System.out.println(resp);
					oos.writeObject(resp);
					oos.close();
				*/
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
