import java.io.File;
import java.io.IOException;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import parsers.KmlParser;
import parsers.TrailIDManager;
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
			}else if("-id".equals(strType)){
				idTool(args);
			}else if("-conv".equals(strType)){
				convTool(args);
			}		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void convTool(String [] args) throws IOException{
		String strType = args[1];
		String strFile = args[2];
		String strOutputName = args[3];
		File file = new File(strFile);

		if(file != null){
			Kml kml = Kml.unmarshal(KMLUtils.openKml(file));
    	    
    	    if("-trail".equals(strType))
    	    	KmlParser.parseKml(kml, new TrailsParserWithUCS(strOutputName));
    	    else if("-city".equals(strType))
    	    	KmlParser.parseKml(kml, new CitiesParser(strOutputName));
    	    else if("-uc".equals(strType))
    	    	KmlParser.parseKml(kml, new UCSParser(strOutputName));
    	    else if("-cave".equals(strType))
    	    	KmlParser.parseKml(kml, new CaveParserWithUCS(strOutputName));
		}
	}

	private static void idTool(String [] args) throws IOException{
		String strIDMin = args[1];
		String strInputName = args[2];
		String strOutputName = args[3];
		File fileInput = new File(strInputName);
		File fileOutput = new File(strOutputName);

		if(fileInput != null){
			Kml kml = Kml.unmarshal(KMLUtils.openKml(fileInput));
    	    KmlParser.parseKml(kml, new TrailIDManager(kml, fileOutput, Long.parseLong(strIDMin)));
		}
	}
}
