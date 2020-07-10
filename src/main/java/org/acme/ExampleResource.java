package org.acme;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;
import org.quartz.SchedulerException;
import org.reactivestreams.Publisher;

@Path("/hello")
public class ExampleResource {

    @Inject
    PeriodicTask task;

    @Inject @Channel("new-question") Publisher<String> streamOfQuestions;

    @GET
    @Path("/start")
    public Response hello() throws SchedulerException {
        task.start();
        return Response.ok().build();
    }

    @GET
    @Path("/stop")
    public Response bye() throws SchedulerException {
        task.stop();
        return Response.ok().build();
    }

    @GET
    @Path("/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @SseElementType(MediaType.TEXT_PLAIN)
    public Publisher<String> stream() {
        return streamOfQuestions;
    }

}