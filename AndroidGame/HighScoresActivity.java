package eu.equo;

import eu.equo.R;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class HighScoresActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_high_scores);
		
		setScores(0, R.id.timedPoint);
		setScores(1, R.id.challenge1Point);
		setScores(2, R.id.challenge2Point);
		setScores(3, R.id.challenge3Point);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.high_scores, menu);
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
	
	/**This method updates the high scores on the screen, which are by default set to 0.
	 * 
	 */
	private void setScores(int level, int id) {
		TextView tv = (TextView) findViewById(id);
		if(tv!= null)
			tv.setText(getHighScore(level) + "pts");
	}
	
	/**Returns a high score of a map
	 * 
	 * @return
	 */
	/**Gets high score for a given level, 0 = timed, 1 < challenge
	 * 
	 */
	public int getHighScore(int level) {
		
		String key = "Level" + level;
		
		SharedPreferences prefs = this.getSharedPreferences("HighScores", Context.MODE_PRIVATE);
		int score = prefs.getInt(key, 0);
		
		return score;
	}
}
