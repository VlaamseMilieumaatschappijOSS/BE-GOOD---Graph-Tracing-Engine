package com.geosparc.gte;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.jgrapht.Graph;
import org.jgrapht.io.ComponentNameProvider;
import org.jgrapht.io.DOTExporter;
import org.opengis.feature.simple.SimpleFeature;

import com.geosparc.graph.base.Idp;
import com.geosparc.graph.geo.GlobalId;

public class TestUtilities {
	
    public static void unzip(String resourcePath, File folder) throws IOException {
        try (ZipInputStream zipIn = new ZipInputStream(
        		TestData.class.getResourceAsStream(resourcePath))) {
	        ZipEntry entry = zipIn.getNextEntry();
	        while (entry != null) {
	            File file = new File(folder, entry.getName());
	            if (entry.isDirectory()) {
	            	file.mkdir();
	            } else {
	            	try (FileOutputStream os = new FileOutputStream(file)) {
	            		IOUtils.copy(zipIn, os);
	            	}
	            }
	            zipIn.closeEntry();
	            entry = zipIn.getNextEntry();
	        }
        }
    }
    
    public static void export(Graph<Idp<GlobalId, SimpleFeature>, 
    		Idp<GlobalId, SimpleFeature>> graph,
    		File file) throws Exception {
    	DOTExporter<Idp<GlobalId, SimpleFeature>, 
    	Idp<GlobalId, SimpleFeature>> exporter = 
    		new DOTExporter<>(new MyComponentNameProvider(), null, null, null, null, null);
		exporter.exportGraph(graph, file);
    }

}

class MyComponentNameProvider implements ComponentNameProvider<Idp<GlobalId, SimpleFeature>> {
	@Override
	public String getName(Idp<GlobalId, SimpleFeature> component) {
		return component.getId().toString().replace(':', '_').replace('-', '_');
	}
}
