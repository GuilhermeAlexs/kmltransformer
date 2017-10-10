import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

public class UCSParser implements KmlParseProgressListener {
	private PrintWriter writer;
	List<ConservationUnit> finalUCList = new ArrayList<ConservationUnit>();
	
	public UCSParser(String outputName){
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
			List<ConservationUnit> ucs = KmlUtils.parsePlacemarkUC(p, this);
			finalUCList.addAll(ucs);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onParseFinish(boolean altitudeWasDownloaded) {
		writer.close();
	}
}
