package com.vinecom.rec

import org.quartz.impl.StdSchedulerFactory
import org.quartz.{JobBuilder, SimpleScheduleBuilder, TriggerBuilder}

/**
 * Created by ndn on 3/20/2015.
 */
object Main {
  def main(args: Array[String]) {
    val job = JobBuilder.newJob(classOf[ImportJob])
      .withIdentity("rec", "Import data for recommendation")
      .build()
    val trigger = TriggerBuilder.newTrigger()
      .withIdentity("rec", "Import data for recommendation")
      .withSchedule(SimpleScheduleBuilder.simpleSchedule().withIntervalInMinutes(5).repeatForever())
      .build()
    val scheduler = new StdSchedulerFactory().getScheduler
    scheduler.scheduleJob(job, trigger)
    scheduler.start()
  }
}
