package com.vinecom.rec;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by ndn on 3/20/2015.
 */
public class ImportJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        Import.execute();
    }
}
