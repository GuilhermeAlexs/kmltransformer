package parsers.specificparsers;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.SchemaData;
import de.micromata.opengis.kml.v_2_2_0.SimpleData;
import models.City;
import parsers.KmlParseProgressListener;

public class CitiesParser implements KmlParseProgressListener {
	private List<City> cities;
	private String outputName;
	
	public CitiesParser(String outputName){
		this.cities = new ArrayList<>();
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
	
	public List<City> getCities(){
		return cities;
	}

	@Override
	public void onParsePlacemark(Placemark p) {
		try {
			ExtendedData extData = p.getExtendedData();
			SchemaData scData = extData.getSchemaData().get(0);

			Iterator<SimpleData> it = scData.getSimpleData().iterator();
			City c = new City();

			while(it.hasNext()){
				SimpleData data = it.next();
				String dataName = data.getName();
				
				if("NM_UF".equals(dataName)){
					c.setUf(data.getValue());
				}else if("NM_LOCALIDADE".equals(dataName)){
					c.setName(data.getValue());
				}else if("LONG".equals(dataName)){
					c.setLongitude(data.getValue());
				}else if("LAT".equals(dataName)){
					c.setLatitude(data.getValue());
				}else{
					continue;
				}
			}
	
			if(c.getName() != null && c.getName().trim() != "")
				cities.add(c);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onParseFinish(boolean altitudeWasDownloaded) {
		try {
		  PrintWriter writer = new PrintWriter(outputName, "UTF-8");

		  for(City c: cities){
			  writer.println(c.getName() + "," + c.getUf() + "," + c.getLatitude() + "," + c.getLongitude());
		  }

		  writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
}
