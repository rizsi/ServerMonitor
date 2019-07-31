package com.rizsi.servermonitor;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class MonitorService2 extends JobService {

    public static void reschedule(Context c) {
    }
    public static void _reschedule(Context c) {
        JobScheduler jobScheduler = (JobScheduler)c
                .getSystemService(JOB_SCHEDULER_SERVICE);
        boolean start=jobScheduler.getAllPendingJobs().size()==0;
        jobScheduler.cancelAll();
        if(start) {
            ComponentName componentName = new ComponentName(c,
                    MonitorService2.class);
            JobInfo jobInfoObj = new JobInfo.Builder(1, componentName)
                    .setMinimumLatency(10000).setOverrideDeadline(12000)
                    .setPersisted(false)
                    .build();
            jobScheduler.schedule(jobInfoObj);
        }

    }

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.e("TIMER", "Job executed");
        Toast.makeText(getApplicationContext(), "Periodic job!", Toast.LENGTH_LONG).show();
        jobFinished(jobParameters, true);
        //reschedule(getApplicationContext());
        return false;
    }
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }
}