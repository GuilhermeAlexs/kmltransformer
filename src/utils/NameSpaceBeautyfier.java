package utils;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;

public class NameSpaceBeautyfier extends NamespacePrefixMapper {
    private static final String KML_PREFIX = "";
    private static final String KML_URI= "http://www.opengis.net/kml/2.2";
    
    public NameSpaceBeautyfier() {
    }

    @Override
    public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
        if(KML_URI.equals(namespaceUri)) {
            return KML_PREFIX;
        }
        return suggestion;
    }

    @Override
    public String[] getPreDeclaredNamespaceUris() {
        return new String[] { KML_URI };
    }
}