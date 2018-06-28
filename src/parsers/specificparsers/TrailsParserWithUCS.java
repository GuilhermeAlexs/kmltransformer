package parsers.specificparsers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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

			if(loc != null && loc.getName() != null && loc.getType() == TrailType.WELL_KNOWN){
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

			try (Stream<String> stream = Files.lines(Paths.get("/home/guilherme/eclipse-workspace/kmltransformer/citiesV2.csv"))) {
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

		private double distanceToRiver(River r, TPLocation loc){
			double [] minDist = {Double.MAX_VALUE};

			for(TPLocation l: r.getLocations()) {
				double dist = GeoUtils.computeDistance(l.getLatitude(), l.getLongitude(), loc.getLatitude(), loc.getLongitude());

				if(dist < minDist[0])
					minDist[0] = dist;

				if(dist > 500000)
					break;
			}

			/*StreamEx.of(r.getLocations()).forPairs((curr,next) -> {
				//Line seg = new Line(curr, next, 1);
				double dist = distanceFromLineToPoint3(curr, next, loc);
				if(dist < minDist[0]){
					rCurr[0] = curr;
					rNext[0] = next;
					minDist[0] = dist;
				}
			});*/

			return minDist[0];
		}

		private River getRiver(TPLocation loc) {
			double [] minDist = {Double.MAX_VALUE};
			River [] river = {null};

			finalRiverList.parallelStream().forEach(r -> {
				double dist = distanceToRiver(r, loc);

				if(dist <= 900) {
					if(dist < minDist[0]) {
						minDist[0] = dist;
						river[0] = r;
					}
				}
			});

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
				int [] riverToWaterfallCounter = {0};

				locs.stream  ()
					.parallel()
					.forEach (loc -> {
						River r = getRiver(loc);

						if(r != null) {
							loc.setRiver(r.getName());
							riverToWaterfallCounter[0]++;
						}

						printToFile(writer, loc);
					});

				writer.close();

				System.out.println("Foram analisadas " + nEntries + " trilhas.");
				System.out.println("Quantidade de trilhas com rio: " + riverToWaterfallCounter[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
