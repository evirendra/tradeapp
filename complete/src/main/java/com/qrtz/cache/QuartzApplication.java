package com.qrtz.cache;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QuartzApplication {

	public static void main(String[] args) {
		

		SpringApplication.run(QuartzApplication.class, args);
	}

	@Bean
	public JobDetail sampleJobDetail() {
		return JobBuilder.newJob(SampleJob.class).withIdentity("sampleJob").storeDurably()
				.build();
	}

	@Bean
	public Trigger sampleJobTrigger() {
		SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.simpleSchedule().withIntervalInMilliseconds(1500)
				.repeatForever();

		
		CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule( "0/1 6 22 * * ?");
		

		GregorianCalendar startCalendar = new GregorianCalendar();
		startCalendar.set(Calendar.HOUR_OF_DAY, 9);
		startCalendar.set(Calendar.MINUTE, 15);
		startCalendar.set(Calendar.SECOND, 0);
		System.out.println("start time is :" +startCalendar.getTime());

		GregorianCalendar endCalendar = new GregorianCalendar();
		endCalendar.set(Calendar.HOUR_OF_DAY, 15);
		endCalendar.set(Calendar.MINUTE, 30);
		endCalendar.set(Calendar.SECOND, 0);
		System.out.println("start time is :" +endCalendar.getTime());
		
		
		SimpleTrigger build = null;
		build = 	TriggerBuilder.newTrigger().forJob(sampleJobDetail()).withIdentity("sampleTrigger")
				.startAt(startCalendar.getTime())
				.endAt(endCalendar.getTime())
				.withSchedule( scheduleBuilder)
                .build();
		return build;
	}

}
