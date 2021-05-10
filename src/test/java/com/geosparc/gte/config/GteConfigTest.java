package com.geosparc.gte.config;

import com.geosparc.gte.config.ConnectionConfig.ConnectionType;
import com.geosparc.gte.config.NetworkConfig.NodeType;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes=GteConfig.class)
@ActiveProfiles("test")
public class GteConfigTest {

	@Autowired 
	private GteConfig configuration;
	
	@BeforeClass
	public static void beforeClass() throws IOException {
		System.setProperty("temp-directory", "/my/dir");
	}
	
	@Test
	public void testConfiguration() {
		configuration.validate();
		
		assertEquals(2, configuration.getNetworks().size());	
		
		NetworkConfig config = configuration.getNetworks().get(0);
		assertEquals("riool", config.getName());
		assertEquals(NodeType.LOGICAL, config.getNodeType());
		assertEquals("file:/my/dir/riool/Streng.shp", config.getEdgeDataStore().get("url"));
		assertNotNull(config.getEdgeFeature());
		assertEquals("Streng", config.getEdgeFeature().getName());
		assertEquals("streng_cod", config.getEdgeFeature().getIdAttribute());
		assertEquals(3, config.getEdgeFeature().getUserAttributes().size());
		assertEquals("str_type", config.getEdgeFeature().getUserAttributes().get(0));
		assertEquals("zuiverings", config.getEdgeFeature().getUserAttributes().get(1));
		assertEquals("the_geom", config.getEdgeFeature().getUserAttributes().get(2));
		assertEquals(4, config.getEdgeFeature().getFilterAttributes().size());
		assertEquals("str_type", config.getEdgeFeature().getFilterAttributes().get(0));
		assertEquals("afgeleidwa", config.getEdgeFeature().getFilterAttributes().get(1));
		assertEquals("begin_p", config.getEdgeFeature().getFilterAttributes().get(2));
		assertEquals("eind_p", config.getEdgeFeature().getFilterAttributes().get(3));
		assertEquals("begin_p", config.getStartAttribute());
		assertEquals("eind_p", config.getEndAttribute());
		assertEquals("file:/my/dir/riool/Punt.shp", config.getNodeDataStore().get("url"));
		assertNotNull(config.getNodeFeature());
		assertEquals("Punt", config.getNodeFeature().getName());
		assertEquals("punt_code_", config.getNodeFeature().getIdAttribute());
		assertEquals(3, config.getNodeFeature().getUserAttributes().size());
		assertEquals("status", config.getNodeFeature().getUserAttributes().get(0));
		assertEquals("peil", config.getNodeFeature().getUserAttributes().get(1));
		assertEquals("the_geom", config.getNodeFeature().getUserAttributes().get(2));
		assertEquals(3, config.getNodeFeature().getFilterAttributes().size());
		assertEquals("status", config.getNodeFeature().getFilterAttributes().get(0));
		assertEquals("pnt_type", config.getNodeFeature().getFilterAttributes().get(1));
		assertEquals("vhas_code", config.getNodeFeature().getFilterAttributes().get(2));
		assertEquals(0, config.getTolerance(), 0.0);

		config = configuration.getNetworks().get(1);
		assertEquals("vha", config.getName());
		assertEquals(NodeType.GEOGRAPHICAL, config.getNodeType());
		assertEquals("file:/my/dir/vha/vha.shp", config.getEdgeDataStore().get("url"));
		assertNotNull(config.getEdgeFeature());
		assertEquals("CODE", config.getEdgeFeature().getIdAttribute());
		assertEquals(3, config.getEdgeFeature().getUserAttributes().size());
		assertEquals("CATEGORIE", config.getEdgeFeature().getUserAttributes().get(0));
		assertEquals("CODE", config.getEdgeFeature().getUserAttributes().get(1));
		assertEquals("the_geom", config.getEdgeFeature().getUserAttributes().get(2));
		assertEquals(2, config.getEdgeFeature().getFilterAttributes().size());
		assertEquals("CATEGORIE", config.getEdgeFeature().getFilterAttributes().get(0));
		assertEquals("WATERLOOP", config.getEdgeFeature().getFilterAttributes().get(1));
		assertEquals("startPoint(the_geom)", config.getStartAttribute());
		assertEquals("endPoint(the_geom)", config.getEndAttribute());
		assertTrue(config.getNodeDataStore().isEmpty());
		assertNull(config.getNodeFeature());
		assertEquals(0.00001, config.getTolerance(), 0.0000001);
		assertNotNull("Must contain key: 'Expose primary keys'", config.getEdgeDataStore().get("Expose primary keys"));

		assertEquals(1, configuration.getConnections().size());	
		ConnectionConfig conn = configuration.getConnections().get(0);
		assertEquals("riool", conn.getSourceNetwork());
		assertEquals("vha", conn.getTargetNetwork());
		assertEquals("vhas_code", conn.getReferenceAttribute());
		assertEquals(ConnectionType.PROJECTED, conn.getConnectionType());
		
		assertEquals(2, configuration.getAreas().size());	
		AreasConfig areas = configuration.getAreas().get(0);
		assertEquals("Risicogebieden", areas.getName());
		assertEquals("Risicogebied", areas.getNativeName());
		assertEquals("file:/my/dir/areas/Risicogebied.shp", areas.getDataStore().get("url"));
		assertEquals(4, areas.getAttributes().size());
		assertEquals("Zone", areas.getAttributes().get(0));
		assertEquals("Oppervlakt", areas.getAttributes().get(1));
		assertEquals("EXTRA", areas.getAttributes().get(2));
		assertEquals("the_geom", areas.getAttributes().get(3));

		AreasConfig areas2 = configuration.getAreas().get(1);
		assertEquals("Risicogebieden2", areas2.getName());
		assertEquals(4, areas2.getAttributes().size());
		assertEquals("Zone", areas2.getAttributes().get(0));
		assertEquals("Oppervlakt", areas2.getAttributes().get(1));
		assertEquals("EXTRA", areas2.getAttributes().get(2));
		assertEquals("the_geom", areas2.getAttributes().get(3));

		Map<String, String> ds = areas2.getDataStore();
		assertNotNull(ds);
		assertEquals(2, ds.size());
		System.out.println(ds.toString());
		assertNotNull("Must contain key: 'Expose primary keys'", ds.get("Expose primary keys"));
	}

}
