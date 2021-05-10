package com.geosparc.gte.rest;

import com.geosparc.gte.TestUtilities;
import com.geosparc.gte.VmmGteApplication;
import com.geosparc.gte.engine.GraphStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;

/**
 * @author Oliver May
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {VmmGteApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext
public class UpdateControllerTest {

    @ClassRule
    public static TemporaryFolder testFolder = new TemporaryFolder();

    @LocalServerPort
    private int port;

    private HttpHeaders headers = new HttpHeaders();

    private TestRestTemplate restTemplate = new TestRestTemplate();

    @BeforeClass
    public static void beforeClass() throws IOException {
        TestUtilities.unzip("shape/riool.zip", testFolder.getRoot());
        TestUtilities.unzip("shape/vha.zip", testFolder.getRoot());
        TestUtilities.unzip("shape/areas.zip", testFolder.getRoot());
        System.setProperty("temp-directory", testFolder.getRoot().getAbsolutePath());
    }

    @Before
    public void before() {
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAcceptLanguageAsLocales(Collections.singletonList(Locale.ENGLISH));
    }


    @Test(timeout = 60000)
    public void testUpdate() throws InterruptedException, JSONException {
        while (GraphStatus.Status.UNINITIALIZED.toString().equals(new JSONObject(restTemplate.getForEntity(
                "http://localhost:" + port + "/graph/status", String.class).getBody()).get("status"))) {
            //wait for initial graph
            Thread.sleep(500);
        }

        ResponseEntity<String> statusBefore = restTemplate.getForEntity(
                "http://localhost:" + port + "/graph/status", String.class);

        ResponseEntity<String> update = restTemplate.exchange(
                "http://localhost:" + port + "/graph/update",
                HttpMethod.POST, new HttpEntity<String>(""), String.class);
        // ResponseEntity<String> update2 = restTemplate.exchange(
        //        "http://localhost:" + port + "/graph/update",
        //        HttpMethod.POST, new HttpEntity<String>(""), String.class);

        ResponseEntity<String> statusAfter = restTemplate.getForEntity(
                "http://localhost:" + port + "/graph/status", String.class);

        // Poll for result
        int polls = 0;
        while (new JSONObject(statusAfter.getBody()).get("serial").equals(new JSONObject(statusBefore.getBody()).get("serial"))) {
            if (polls++ >= 10) {
                throw new RuntimeException("Operation is taking too long.");
            }
            Thread.sleep(1000);
            statusAfter = restTemplate.getForEntity(
                    "http://localhost:" + port + "/graph/status", String.class);
        }

        Assert.assertEquals(HttpStatus.OK, statusBefore.getStatusCode());
        Assert.assertEquals(HttpStatus.OK, update.getStatusCode());
        //This test may fail when update2 gets called after the first update is ready
        // Assert.assertEquals(HttpStatus.SERVICE_UNAVAILABLE, update2.getStatusCode());
        Assert.assertEquals(HttpStatus.OK, statusAfter.getStatusCode());

        Assert.assertEquals(new JSONObject(statusBefore.getBody()).get("status"), "READY");
        Assert.assertEquals(new JSONObject(statusAfter.getBody()).get("status"), "READY");
        //Check that serials differ
        Assert.assertNotEquals(new JSONObject(statusBefore.getBody()).get("serial"),
                new JSONObject(statusAfter.getBody()).get("serial"));

    }
}
