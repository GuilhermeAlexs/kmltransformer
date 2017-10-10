package parsers.specificparsers;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import models.TPLocation;
import parsers.KmlParseProgressListener;
import parsers.KmlParser;

public class TrailsParser implements KmlParseProgressListener {
	private List<TPLocation> locs;
	private String outputName;

	public TrailsParser(String outputName){
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
			TPLocation loc = KmlParser.parsePlacemark(p, this);

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
			FileOutputStream fout = new FileOutputStream(outputName);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			ObjectMapper mapper = new ObjectMapper();
			String resp = mapper.writeValueAsString(locs);
			System.out.println(resp);
			oos.writeObject(resp);
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
