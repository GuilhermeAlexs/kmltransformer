

import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Placemark;

public interface KmlParseProgressListener {
	public void onPreParse(int progressTotal);
	public void onParseProgress(int progress);
	public void onParseFolder(Folder folder);
	public void onParsePlacemark(Placemark p);
	public void onParseFinish(boolean altitudeWasDownloaded);
}
