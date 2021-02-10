package eu.equo.gamelogic;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class GameMap implements Parcelable{

	NumericalCell[][] map;
	public final int width;
	public final int height;
	public final String text;

	//The state of the map
	public enum GameState {PLAYING, WON, LOST};
	
	//Game mode
	public enum MapType {NORMAL, TIMED, CHALLENGE, DUMMY};
	
	private GameState gamestate = GameState.PLAYING;
	public MapType type;
	
	protected List<TargetValue> targetValues = new ArrayList<>();
	
	//Only in challenge mode
	public int score;
	
	public GameMap(String text, int width, int height, List<TargetValue> targetValues, List<NumericalCell> mapcells, MapType type) {
		this.width = width;
		this.height = height;
		this.targetValues = targetValues;
		this.text = text;
		
		map = new NumericalCell[width][height];
		
		int act = 0;
		for(int j = 0; j < height; j++)
			for(int i = 0; i < width; i++)
			 {
				NumericalCell actValue;
				if(act < mapcells.size()) actValue = mapcells.get(act);
				else actValue = new NumericalCell();
				map[i][j] = actValue;
				act++;
			}
		
		this.type = type;
	}
		
	/**Copy constructor
	 * 
	 * @param other
	 */
	public GameMap(GameMap other) {
		this.width = other.width;
		this.height = other.height;
		this.text = other.text;
		this.gamestate = other.gamestate;
		this.type = other.type;
		
		map = new NumericalCell[width][height];
				
	    for (int j = 0; j < height; j++) 
	    	for (int i = 0; i < width; i++)
		map[i][j] = new NumericalCell(other.map[i][j]);
	    		
		for(TargetValue tv : other.targetValues)
			targetValues.add(new TargetValue(tv));
		
	}
	
	/**Moves a cell into the given direction.
	 * 
	 * @param x coordinate
	 * @param y coordinate
	 * @param dir which direction to move
	 * @return true if a move was made, false if it was incorrect
	 */
	public boolean move(int x, int y, Direction dir) {
		if(gamestate == GameState.WON || !(isInside(x, y)) || map[x][y].getType() == CellType.EMPTY) return false;

		int newx = x, newy = y;

		switch(dir) {
		case RIGHT: newx++; break;
		case LEFT: newx--; break;
		case UP: newy--; break;
		case DOWN: newy++; break;
		default: return false;
		}
		
		if(!(isInside(newx, newy)) || map[newx][newy].getType() == CellType.EMPTY) return false;

		//Merge the two cells into one
		map[newx][newy].merge(map[x][y]);
		
		//Zero the old cell
		map[x][y].setEmpty();
		
		//Check if the new cell can be evaluated (ie. it contains no neighbours)
		for(int i = 0; i < 4; i++) {
		//Check for all the 4 neighbours of the moved cell
		switch(i) {
		case 0: x++; break;
		case 1: x -= 2; break;
		case 2: x += 1; y--; break;
		case 3: y += 2; break;
		default: break;
		}
		
		if(isInside(x, y) && !map[x][y].isEmpty() && checkSolitude(x, y)) {
			map[x][y].evaluate();

			//CHALLENGE type maps are finished ater the first evaluation happened
			if(type == MapType.CHALLENGE) {
				gamestate = GameState.WON;
				score = map[x][y].value;
				return true;
			}
			
			int value = map[x][y].value;
			boolean isLost = true;
			for(TargetValue tv : targetValues)
				if(tv.holdValue(value)) {isLost = false; break;}
			if(isLost)
				gamestate = GameState.LOST;
			}
		}
		
		checkState();
		
		return true;
	}
	/**Checks whether the game is lost or won
	 * 
	 * @return
	 */
	private void checkState() {
		//It's not possible to unlose or unwin a game
		if(gamestate == GameState.WON || gamestate == GameState.LOST) return;

		//Check whether all the map values are empty and evaluated
		for(int i = 0; i < width; i++)
			for(int j = 0; j < height; j++)
				if(!(map[i][j].getType() == CellType.EMPTY || map[i][j].getType() == CellType.EVALUATED))
					return;
		
		//Check whether all the target values are assigned, if not return LOST
		for(TargetValue val : targetValues) {
			if(!val.isAssigned()) {
			gamestate = GameState.LOST;
			return;
			}
		}
		
		//Everything is OK, so set the state to WON
		gamestate = GameState.WON;
		}

	/**Getter for the gamestate
	 * 
	 * @return the current state of the game: WIN or LOSE or PLAYING
	 */
	public GameState getSate() {
		return gamestate;
	}
	
	/**Finishes a timed map!
	 * 
	 */
	public void finishTimed() {
		if(type == MapType.TIMED)
			gamestate = GameState.WON;
	}
	
	/**
	 * Is the given point inside the map?
	 * @param x coordinate
	 * @param y coordinate
	 * @return true if inside
	 */
	public boolean isInside(int x, int y) {
		if(x < 0 || x >= width || y >= height || y < 0) return false;
		return true;
	}
	
	/**Does the cell at point x,y have any non-empty neighbours
	 * 
	 * @param x coordinate
	 * @param y coordinate
	 * @return true if it doesn't have any neighbours
	 */
	boolean checkSolitude(int x, int y) {		
		return ( (x == 0 || map[x - 1][y].isEmpty()) &&
			(x == width - 1 || map[x + 1][y].isEmpty()) &&
			(y == 0 || map[x][y - 1].isEmpty()) &&
			(y == height - 1 || map[x][y + 1].isEmpty())
			) ? true : false;		
	}
	
	/**Used by the level generator
	 * 
	 * @param x
	 * @param y
	 * @return true if it's an empty cell
	 */
	boolean isEmpty(int x, int y) {
		return map[x][y].isEmpty();
	}
	
	/**Getter, not modification safe!
	 * 
	 * @return
	 */
	public NumericalCell[][] getValues() {
		return map;
	}
	
	/**Getter, not modification safe!
	 * 
	 * @return
	 */
	public List<TargetValue> getTargetValues() {
		return targetValues;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		
	    for (int j = 0; j < height; j++) {
	    	for (int i = 0; i < width; i++)
	    		result.append(map[i][j]);
	    		result.append("/n");
	    }
		return result.toString();
	}
	
	//*****Parcelling methods
	
	public GameMap(Parcel in) {
		width = in.readInt();
		height = in.readInt();
		text = in.readString();
		gamestate = GameState.valueOf(in.readString());

		
		map = new NumericalCell[width][height];
		Parcelable[] temp = in.createTypedArray(NumericalCell.CREATOR);

		int w = 0, h = 0;
		for(int i = 0; i < temp.length; i++) {
			map[w][h] = (NumericalCell) temp[i];
			w++;
			if(w == width) {w = 0; h++;}
		}
		
		in.readList(targetValues, TargetValue.class.getClassLoader());
		
		type = MapType.valueOf(in.readString());
		
		score = in.readInt();
		}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(width);
		dest.writeInt(height);
		dest.writeString(text);
		dest.writeString(gamestate.name());
		
		//2D -> 1D for parceling
		Parcelable[] temp = new Parcelable[width * height];
		int act = 0;
	    for (int j = 0; j < height; j++) 
	    	for (int i = 0; i < width; i++, act++)
	            temp[act] = map[i][j];
		dest.writeTypedArray(temp, 0);
		
		dest.writeList(targetValues);
		
		dest.writeString(type.name());
		dest.writeInt(score);
	}

	@Override
	public int describeContents() {
		return 0;
	}

    public static final Parcelable.Creator<GameMap> CREATOR = new Parcelable.Creator<GameMap>() {
        @Override
		public GameMap createFromParcel(Parcel in) {
            return new GameMap(in); 
        }

        @Override
		public GameMap[] newArray(int size) {
            return new GameMap[size];
        }
    };

}

