package com.infochange.frame.util;

import java.util.concurrent.ScheduledFuture;

class TaskInfo {
	private ScheduledRunnable runnable;
	private ScheduledFuture<?> schFeture;
	
	public TaskInfo(ScheduledRunnable runnable,
			ScheduledFuture<?> schFeture) {
		this.runnable = runnable;
		this.schFeture = schFeture;
	}
	
	void stop() {
		runnable.stop();
		schFeture.cancel(true);
	}

	public void pause(boolean isPausing) { runnable.pause(isPausing); }
}
