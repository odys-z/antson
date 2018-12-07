package com.infochange.frame.util;

/**User of ScheduledTaskWrapper implements the interface.
 * @author odysseus.edu@gmail.com
 */
public interface IScheduledHandler {

	void beginningForeground(String taskName);

	void doInBackground(String taskName, int runcount, long msElapsed, long msLastElapsed);

//	void endingBackground();

	void runOnUiThread(String taskName, int runCount, long msElapsed, long msLastElapsed);

	void stopBackground();
}
