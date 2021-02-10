package eu.equo;

import eu.equo.gamelogic.GameMap;
import eu.equo.gamelogic.LevelManager;
import eu.equo.gamelogic.GameMap.GameState;
import eu.equo.R;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends ActionBarActivity {
	
	//For testing only
	public static final String LogFilter = "MSG";

	//Sound enabled
	boolean isSound = true;
	
	//Current map
	GameMap gamemap;
	GameMap storedmap;
	String packageName;
	int levelNum;
	int points;
	int time;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		LevelManager.setLevelManager(getAssets());//Give the class access to resources
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	
	//Callbacks for the buttons
	public void helpButton(View view) {
		Intent help = new Intent(MainActivity.this,HelpActivity.class);
		startActivity(help);

	}
	
	public void scoresButton(View view) {
		Intent scores = new Intent(MainActivity.this, HighScoresActivity.class);
		startActivity(scores);
	}

	public void continueGameButton(View view) {
		continueGame();
	}

	public void toggleSound(View view) {
		if(isSound) {
			findViewById(R.id.soundButton).setBackgroundResource(R.drawable.nosound);
			isSound = false;
		}
		else {
			findViewById(R.id.soundButton).setBackgroundResource(R.drawable.sound);
			isSound = true;			
		}
	}

	public void newGameButton(View view) {
		startNewGame();
	}
	
	/**Starts a ModeSelectActivity.
	 * 
	 * @param isContinue true if the User continues a game
	 */
	private void startNewGame() {
		Intent startGame = new Intent(MainActivity.this, ModeSelectActivity.class);
		
		startGame.putExtra("sound", isSound);	
		startGame.putExtra("skip", false);

		startActivityForResult(startGame, 0);		
	}
	
	private void continueGame() {
		Intent startGame = new Intent(MainActivity.this, ModeSelectActivity.class);

		startGame.putExtra("sound", isSound);
		startGame.putExtra("map", gamemap);
		startGame.putExtra("storedmap", storedmap);
		startGame.putExtra("package", packageName);
		startGame.putExtra("level", levelNum);
		startGame.putExtra("skip", true);

		startActivityForResult(startGame, 0);
	}
	
	private void startNextLevel() {
		Intent startGame = new Intent(MainActivity.this, ModeSelectActivity.class);

		levelNum++;
		
		startGame.putExtra("sound", isSound);
		startGame.putExtra("package", packageName);
		startGame.putExtra("level", levelNum);
		startGame.putExtra("skip", true);

		startActivityForResult(startGame, 0);
		
	}
	
	
	/**Gets high score for a given level, 0 = timed, 1 < challenge
	 * 
	 */
	public int getHighScore(int level) {
		
		String key = "Level" + level;
		
		SharedPreferences prefs = this.getSharedPreferences("HighScores", Context.MODE_PRIVATE);
		int score = prefs.getInt(key, 0); //0 is the default value
		
		return score;
	}
	
	/**Sets the high score for a game
	 * 
	 */
	public void setHighScore(int level, int score) {
		
		String key = "Level" + level;

		SharedPreferences prefs = this.getSharedPreferences("HighScores", Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(key, score);
		editor.commit();

	}
	
	private void timedCompleted(int score) {
		if(score > getHighScore(0))
			setHighScore(0, score);
		scoresButton(null);
	}
	
	private void challengeCompleted(int level, int score) {
		if(score > getHighScore(level))
			setHighScore(level, score);
		
		scoresButton(null);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  if (!(resultCode == RESULT_OK)) return;
		  if (!data.hasExtra("map")) return;//If no game was played, skip
		  
		  gamemap = data.getParcelableExtra("map");
		  
		  GameState gamestate = gamemap.getSate();
		  packageName = data.getStringExtra("package");
		  levelNum = data.getIntExtra("level", 0);

		  if(gamemap.type == GameMap.MapType.TIMED) {
			  time = data.getIntExtra("time", 0);
			  points = data.getIntExtra("points", 0);
		  }
		  else if(gamemap.type == GameMap.MapType.CHALLENGE)
			  points = gamemap.score;
		  
		  //Hide continue button
		  if(gamestate != GameState.PLAYING && gamemap != null)
			findViewById(R.id.continueButton).setVisibility(View.INVISIBLE);
		  
		  switch(gamestate) {
		case LOST:
			break;
		case PLAYING:
			findViewById(R.id.continueButton).setVisibility(View.VISIBLE);
			gamemap = data.getParcelableExtra("map");
			break;
		case WON:
			
			switch(gamemap.type) {
			case CHALLENGE:
				challengeCompleted(levelNum, points);
				break;
			case DUMMY:
				break;
			case NORMAL:
		    	if(LevelManager.completeLevel(packageName, levelNum, 0))
		    		startNextLevel();
				break;
			case TIMED:
				timedCompleted(points);
				break;
			default:
				break;
			}
			
			break;
		default:
			break;
		  }
	  }
}
