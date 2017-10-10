package utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;

public class KMLUtils {
	public static InputStream openKml(File file) throws IOException{
	    String str = FileUtils.readFileToString(file, "ISO-8859-1");
 	    str = str.replace("xmlns=\"http://earth.google.com/kml/2.2\"", "xmlns=\"http://www.opengis.net/kml/2.2\"" );
 	    return new ByteArrayInputStream(str.getBytes( "ISO-8859-1" ));
	}
}
