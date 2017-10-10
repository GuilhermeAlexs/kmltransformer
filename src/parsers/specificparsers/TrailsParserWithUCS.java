package parsers.specificparsers;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.snatik.polygon.Point;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import models.ConservationUnit;
import models.TPLocation;
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

		private TPLocation getLocWithUC(TPLocation loc){
			String ucName = finalUCList
					.stream    ()
					.filter    (uc -> isInside(uc,loc))
					.map	   (uc -> uc.getName())
					.findFirst ()
					.orElse    (null);

			loc.setUc(ucName);

			return loc;
		}

		private void printToFile(PrintWriter writer, TPLocation loc){
			writer.println(loc.getName() + "$" + loc.getLatitude() + "$" + loc.getLongitude() + 
					"$" + loc.getUc() + "$" + loc.getType().getValue() + "$" + "1");
		}

		@Override
		public void onParseFinish(boolean altitudeWasDownloaded) {
			try{
				PrintWriter writer = new PrintWriter(outputName, "UTF-8");

				locs.stream  ()
					.map	 (loc -> getLocWithUC(loc))
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
