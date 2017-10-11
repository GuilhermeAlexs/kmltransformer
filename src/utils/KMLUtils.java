package utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.FileUtils;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.SchemaData;
import de.micromata.opengis.kml.v_2_2_0.SimpleData;

public class KMLUtils {
	public static InputStream openKml(File file) throws IOException{
	    String str = FileUtils.readFileToString(file, "ISO-8859-1");
 	    str = str.replace("xmlns=\"http://earth.google.com/kml/2.2\"", "xmlns=\"http://www.opengis.net/kml/2.2\"" );
 	    return new ByteArrayInputStream(str.getBytes( "ISO-8859-1" ));
	}
	
	public static long getIDFromPlacemarck(Placemark p){
		ExtendedData extData = p.getExtendedData();
		List<SchemaData> schDataList = extData.getSchemaData();
		List<SimpleData> simpleDataList = schDataList.get(0).getSimpleData();
		SimpleData sData = simpleDataList.stream()
			.filter(sd -> "ID".equals(sd.getName()))
			.findFirst().orElse(null);
		
		return Long.parseLong(sData.getValue());
	}
}
