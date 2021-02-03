package com.geosparc.gte.engine;

import com.geosparc.gte.engine.impl.StandoffTimer;
import org.junit.Assert;
import org.junit.Test;

public class StandoffTimerTest {
	
	@Test
	public void testConfigurationLoaded() {
		StandoffTimer t = new StandoffTimer(250L, 1, 2.0);
		t.delay();
		Assert.assertEquals(1, t.getRetryCount());

		long start = System.currentTimeMillis();
		t.delay();
		Assert.assertEquals(2, t.getRetryCount());
		long duration = System.currentTimeMillis() - start;
		Assert.assertTrue(400 < duration);
	}

}
