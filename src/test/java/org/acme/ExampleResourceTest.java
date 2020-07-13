package org.acme;

import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.sse.InboundSseEvent;
import javax.ws.rs.sse.SseEventSource;

@QuarkusTest
public class ExampleResourceTest {

    @TestHTTPResource("/hello/stream")
    URL url;

    @Test
    public void testHelloEndpoint() throws InterruptedException {

        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(url.toExternalForm());
        SseEventSource eventSource = SseEventSource.target(target)
                    .reconnectingEvery(1, TimeUnit.SECONDS)
                    .build();

            eventSource.register(onEvent, onError, onComplete);
            eventSource.open();

            

        Thread.sleep(2000);
        
        given()
          .when().get("/hello/start")
          .then()
             .statusCode(200);

        //Consuming events for one hour
        Thread.sleep(60 * 1000);

        client.close();
        System.out.println("End");
    }

    private static Consumer<InboundSseEvent> onEvent = (inboundSseEvent) -> {
        String data = inboundSseEvent.readData();
        System.out.println("* " + data);
        System.out.println("* " + inboundSseEvent.getComment());
        System.out.println("* " + inboundSseEvent.getName());
        System.out.println("* " + inboundSseEvent.getId());
    };

    //Error
    private static Consumer<Throwable> onError = (throwable) -> {
        throwable.printStackTrace();
    };

    //Connection close and there is nothing to receive
    private static Runnable onComplete = () -> {
        System.out.println("Done!");
    };

}