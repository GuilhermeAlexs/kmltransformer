package parsers.specificparsers;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.snatik.polygon.Point;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import models.ConservationUnit;
import models.TPLocation;
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
				System.out.println(loc.getName() + " , " + loc.getLatitude() + " , " + loc.getLongitude());
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

				@Override
				public void onParseFinish(boolean altitudeWasDownloaded) {
					try {
						  for(TPLocation loc: locs){
								for(ConservationUnit uc: finalUCList){
									if(uc.getPolygon().contains(new Point(loc.getLatitude(), loc.getLongitude()))){
										loc.setUc(uc.getName());
										break;
									}
								}
						  }

						  PrintWriter writer = new PrintWriter(outputName, "UTF-8");

						  for(TPLocation loc: locs){
							  writer.println(loc.getName() + "$" + loc.getLatitude() + "$" + loc.getLongitude() + "$" + loc.getUc() + "$" + "null" + "$" + "3");
						  }

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
