package io.openshift.booster;

import io.openshift.booster.test.OpenShiftTestAssistant;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.net.MalformedURLException;
import java.util.concurrent.TimeUnit;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.restassured.RestAssured.get;
import static io.openshift.booster.HttpApplication.template;
import static org.hamcrest.core.IsEqual.equalTo;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class OpenShiftIT {

    private static OpenShiftTestAssistant assistant = new OpenShiftTestAssistant();

    @BeforeClass
    public static void prepare() throws Exception {
        assistant.deployApplication();
    }

    @AfterClass
    public static void cleanup() {
        try {
            assistant.cleanup();
        } catch (Exception e) {
            // Ignore it.
        }
    }

    @Test
    public void testThatWeAreReady() throws Exception {
        assistant.awaitApplicationReadinessOrFail();
        // Check that the route is served.
        await().atMost(5, TimeUnit.MINUTES).catchUncaughtExceptions().until(() -> get().getStatusCode() < 500);
        await().atMost(5, TimeUnit.MINUTES).catchUncaughtExceptions().until(() -> get("/api/greeting")
            .getStatusCode() < 500);

    }

    @Test
    public void testThatWeServeAsExpected() throws MalformedURLException {
        get("/api/greeting").then().body("content", equalTo(String.format(template, "World")));
        get("/api/greeting?name=vert.x").then().body("content", equalTo(String.format(template, "vert.x")));
    }

}
