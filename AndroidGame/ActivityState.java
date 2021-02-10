package eu.equo;


/**Represents the current state of the activity. Used together with the GameMap.GameState
 * 
 * @author Attila Torda
 *
 */
public interface ActivityState {

	boolean isPlaying();
	boolean isWindowShow();
}

class NormalState implements ActivityState {

	@Override
	public boolean isPlaying() {
		return true;
	}

	@Override
	public boolean isWindowShow() {
		return false;
	}
	
}


class WindowShowState implements ActivityState {

	@Override
	public boolean isPlaying() {
		return false;
	}

	@Override
	public boolean isWindowShow() {
		return true;
	}
	
}

class PauseState implements ActivityState {

	@Override
	public boolean isPlaying() {
		return false;
	}

	@Override
	public boolean isWindowShow() {
		return false;
	}
	
}

class FinishedState implements ActivityState {

	@Override
	public boolean isPlaying() {
		return false;
	}

	@Override
	public boolean isWindowShow() {
		return false;
	}
	
}
