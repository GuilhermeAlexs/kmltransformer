import java.io.File;
import java.io.IOException;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import parsers.KmlParser;
import parsers.specificparsers.CaveParserWithUCS;
import parsers.specificparsers.CitiesParser;
import parsers.specificparsers.TrailsParserWithUCS;
import parsers.specificparsers.UCSParser;
import utils.KMLUtils;

public class Main {
	public static void main(String[] args) {
		try {
			String strType = args[0];
			
			if("-h".equals(strType) || "--help".equals(strType)){
				System.out.println("Uso: [trails | cities] [intermediate_file] [input_file] [output_file]");
				
				System.out.println("\nSe voce escolher 'trails' o arquivo de saida sera JSON."
						+ " Se voce escolher 'cities', o arquivo de saida sera CSV.");
				
				return;
			}else if("-v".equals(strType) || "--version".equals(strType)){
				System.out.println("tpconv 1.0");
				
				return;
			}
			
			String strFile = args[1];
			String strOutputName = args[2];
			File file = new File(strFile);
			
			if(file != null){
				Kml kml = Kml.unmarshal(KMLUtils.openKml(file));
	    	    
	    	    if("trails".equals(strType))
	    	    	KmlParser.parseKml(kml, new TrailsParserWithUCS(strOutputName));
	    	    else if("cities".equals(strType))
	    	    	KmlParser.parseKml(kml, new CitiesParser(strOutputName));
	    	    else if("ucs".equals(strType))
	    	    	KmlParser.parseKml(kml, new UCSParser(strOutputName));
	    	    else if("caves".equals(strType))
	    	    	KmlParser.parseKml(kml, new CaveParserWithUCS(strOutputName));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
