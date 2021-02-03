package com.geosparc.gte.engine;

import com.geosparc.graph.base.DGraph;
import com.geosparc.graph.geo.GlobalId;
import com.geosparc.gte.TestUtilities;
import com.geosparc.gte.config.GteConfig;
import com.geosparc.gte.engine.impl.GraphTracingEngineImpl;
import com.geosparc.gte.service.TestMailSenderService;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.FieldSetter;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= {GteConfig.class, GraphTracingEngineImpl.class, TestMailSenderService.class})
@EnableScheduling
@ActiveProfiles("test")
public class GraphTracingEngineTest {
	
    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();
	
	@Autowired
	private GraphTracingEngine engine;

	@BeforeClass
	public static void beforeClass() throws IOException {
		TestUtilities.unzip("shape/riool.zip", testFolder.getRoot());
		TestUtilities.unzip("shape/vha.zip", testFolder.getRoot());
		System.setProperty("temp-directory", testFolder.getRoot().getAbsolutePath());
	}
	
	@Test
	public void testConfigurationLoaded() {
		DGraph<GlobalId, SimpleFeature, SimpleFeature> g = engine.getGraph();
		assertEquals(84, g.edgeSet().size());
		assertEquals(83, g.vertexSet().size());
		assertNotEquals(engine.getStatus().getSerial(), -1);
		assertEquals(engine.getStatus().getStatus(), GraphStatus.Status.READY);
	}

	@Test
	public void testReloadGraph() throws InterruptedException, NoSuchFieldException {
		GraphStatus graphStatus = Mockito.spy(engine.getStatus());
		FieldSetter.setField(engine, engine.getClass().getDeclaredField("status"), graphStatus);
		long serial = engine.getStatus().getSerial();
		new Thread(new Runnable() {
			@Override
			public void run() {
				engine.reload();
			}
		}).run();
		int sleeps = 0;
		while (!engine.getStatus().getStatus().equals(GraphStatus.Status.READY)) {
			if (sleeps++ > 10) {
				throw new RuntimeException("Reloading operation taking to long");
			}
			Thread.sleep(1000);
		}
		Mockito.verify(graphStatus, Mockito.times(1)).setStatus(GraphStatus.Status.READY);
		Mockito.verify(graphStatus, Mockito.times(1)).setSerial(Mockito.anyLong());

		assertEquals(engine.getStatus().getStatus(), GraphStatus.Status.READY);
		assertNotEquals(engine.getStatus().getSerial(), serial);
	}

}
