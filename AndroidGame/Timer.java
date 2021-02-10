/**
 * 
 */
package eu.equo;

/**A class that models a stop watch
 * @author Attila Torda
 *
 */
public class Timer implements Runnable {

	int time;
	TimerCallback callback;
	/**
	 * 
	 */
	public Timer(int value, TimerCallback creator) {
		time = value;
		callback = creator;
	}

	@Override
	public void run() {
	try {
		while(true) {
		Thread.sleep(1000);
		time--;
		callback.timerCallback(time);
		}
	} catch (InterruptedException e) {
		e.printStackTrace();
	}
	}

}
