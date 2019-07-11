package com.infochange.frame.util;

public class ScheduledRunnable implements Runnable {
	private String taskName;
	private IScheduledHandler handler;
//	private boolean stop = false;
	private boolean pause = true;

	private int count = 0;
	private long ms0 = 0;
	private long msLast = 0;
//	private int scheduleMode = ScheduledTaskWrapper.fixedRate;
	
	public ScheduledRunnable(String taskName, IScheduledHandler handler) {
		this.taskName = taskName;
		this.handler = handler;
//		this.scheduleMode = scheduleMode;
	}

	@Override
	public void run() {
		if (count == 0) {
			ms0 = System.currentTimeMillis();
			msLast = ms0;
		}
		if (!pause) {
			long ms = System.currentTimeMillis();
			handler.doInBackground(taskName, count, ms - ms0, ms - msLast);
			handler.runOnUiThread(taskName, count, ms - ms0, ms - msLast);
			count++;
		}
	}

	public void stop() { handler.stopBackground(); }

	public void pause(boolean pausing) { pause = pausing; }
}
