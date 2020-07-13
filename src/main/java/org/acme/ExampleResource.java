package org.acme;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.jboss.resteasy.annotations.SseElementType;
import org.quartz.SchedulerException;

import io.smallrye.mutiny.Multi;

@Path("/hello")
public class ExampleResource {

    @Inject
    PeriodicTask task;

    @Inject @Channel("new-question") Multi<String> streamOfQuestions;

    private Sse sse;
    private SseBroadcaster sseBroadcaster;
    private OutboundSseEvent.Builder eventBuilder;

    @Context
    public void setSse(Sse sse) {
        this.sse = sse;
        this.eventBuilder = sse.newEventBuilder();
        this.sseBroadcaster = sse.newBroadcaster();
    }

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
    //@SseElementType(MediaType.TEXT_PLAIN)
    public Multi<OutboundSseEvent> stream() {
        return streamOfQuestions.map(data -> 
            this.eventBuilder
                        .name("question")
                        .mediaType(MediaType.APPLICATION_JSON_TYPE)
                        .data(String.class, data)
                        .reconnectDelay(1000)
                        .comment("a question")
                        .build());
    }

}