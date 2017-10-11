package parsers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.SchemaData;
import de.micromata.opengis.kml.v_2_2_0.SimpleData;

import utils.NameSpaceBeautyfier;

public class TrailIDManager implements KmlParseProgressListener{
	private long maxId;
	private List<SimpleData> noIDplacemarks = new ArrayList<>();
	
	private Kml kml;
	private File kmlFile;
	
	public TrailIDManager(Kml kml, File kmlFile, long min){
		this.kml = kml;
		this.kmlFile = kmlFile;
		this.maxId = min;
	}
	
	@Override
	public void onPreParse(int progressTotal) {
	}

	@Override
	public void onParseProgress(int progress) {
	}

	@Override
	public void onParseFolder(Folder folder) {
	}
	
	private List<SchemaData> getSchemaData(ExtendedData extData){
		List<SchemaData> schDataList = extData.getSchemaData();
		
		if(schDataList == null){
			schDataList = new ArrayList<SchemaData>();
			extData.setSchemaData(schDataList);
		}
		
		if(schDataList.size() == 0){
			SchemaData s = new SchemaData();
			schDataList.add(s);
		}
		
		schDataList.get(0).setSchemaUrl("#TPInfoSchema");
		
		return schDataList;
	}

	private SimpleData getIDField(List<SchemaData> schemaData){
		List<SimpleData> simpleDataList = schemaData.get(0).getSimpleData();
		SimpleData sData = simpleDataList.stream()
			.filter(sd -> "ID".equals(sd.getName()))
			.findFirst().orElse(null);

		if(sData == null){
			sData = new SimpleData("ID");
			simpleDataList.add(sData);
		}

		return sData;
	}

	@Override
	public void onParsePlacemark(Placemark p) {
		ExtendedData extData = p.getExtendedData();

		if(extData == null){
			extData = new ExtendedData();
			p.setExtendedData(extData);
		}

		List<SchemaData> schemaData = getSchemaData(extData);
		SimpleData idField = getIDField(schemaData);
		String id = idField.getValue();

		if(id == null){
			noIDplacemarks.add(idField);
		}else{
			long numId = Long.parseLong(id);
			if(numId > maxId)
				maxId = numId;
		}
	}

	@Override
	public void onParseFinish(boolean altitudeWasDownloaded) {
        try {
    		noIDplacemarks.forEach(sd -> sd.setValue("" + (++maxId)));
    		String name = kml.getClass().getSimpleName();
            if ("Kml".equals(name))
                name = name.toLowerCase();
            
            JAXBContext jaxbContext = JAXBContext.newInstance(Kml.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NameSpaceBeautyfier());
            JAXBElement<Kml> jaxbKml = new JAXBElement(new QName("http://www.opengis.net/kml/2.2", name), (Class<Kml>) kml.getClass(), kml);
			jaxbMarshaller.marshal(jaxbKml, kmlFile);
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

}
