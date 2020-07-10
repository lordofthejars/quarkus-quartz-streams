package org.acme;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.KeyMatcher.keyEquals;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.OnOverflow;
import org.eclipse.microprofile.reactive.messaging.OnOverflow.Strategy;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.listeners.TriggerListenerSupport;

import io.quarkus.arc.Arc;

@ApplicationScoped
public class PeriodicTask {

    @Inject
    @Channel("new-question")
    @OnOverflow(value = Strategy.DROP)
    Emitter<String> questions;

    @Inject
    Scheduler quartz;

    public void start() throws SchedulerException {
        final JobDetail job = newJob(MyJob.class)
                                .withIdentity("questions", "millionaire")
                                .build();

        final SimpleTrigger trigger = newTrigger()
                                        .withIdentity("questionsTrigger", "millionaire")
                                        .startNow()
                                        .withSchedule(
                                            SimpleScheduleBuilder.simpleSchedule()
                                                .withIntervalInSeconds(20)
                                                .withRepeatCount(4))
                                        .build();

        quartz.getListenerManager()
                .addTriggerListener(new EndTriggerListener("EndTrigger"),
                                            keyEquals(triggerKey("questionsTrigger", "millionaire")));
        quartz.scheduleJob(job, trigger);
    }

    public void send(String message) {
        System.out.println("Send Method executed");
        questions.send(message);
    }

    public void stop() throws SchedulerException {
        quartz.deleteJob(JobKey.jobKey("questions", "millionaire"));
    }

    public class EndTriggerListener extends TriggerListenerSupport {

        private String name;

        public EndTriggerListener(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
        
        @Override
        public void triggerComplete(
            Trigger trigger,
            JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
                System.out.println("Completed Trigger " + triggerInstructionCode.name());
        }

    }

    public static class MyJob implements Job {

        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            System.out.println("HW");
            Arc.container().instance(PeriodicTask.class).get().send("Hello World ");
        }

    }

}