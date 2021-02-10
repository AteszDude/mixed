package eu.equo;

import eu.equo.gamelogic.GameMap;
import eu.equo.R;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ModeSelectActivity extends ActionBarActivity {

	//The data from other activities, main and game
	Bundle parentBundle;
	Bundle result;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mode_select);
		
		parentBundle = getIntent().getExtras();

		//Continue a game
		if(parentBundle.containsKey("skip") && parentBundle.getBoolean("skip")) {
			if(parentBundle.containsKey("map")) {
			GameMap gamemap = parentBundle.getParcelable("map");
			
			//Continue a level
			switch(gamemap.type) {
			case CHALLENGE:
				challengeMode(null);
				break;
			case NORMAL:
				normalMode(null);
				break;
			case TIMED:
				timedMode(null);
				break;
			default:
				break;
			}
			}
			//Get next level
			else normalMode(null);
		}
	}// onCreate

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.mode_select, menu);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  if (!(resultCode == RESULT_OK)) finish();//Dunno what else could it be
		  if (!data.hasExtra("map")) return;
		  result = data.getExtras();
		  finish();
	}
	
    @Override
    public void finish() {
      // Prepare data intent 
      Intent data = new Intent();
      if(result != null) data.putExtras(result);
      
      // Activity finished ok, return the data
      setResult(RESULT_OK, data);
      super.finish();
    } 
	
	/**Call the LevelSelectActivity to start a new normal mode game
	 * 
	 * @param view
	 */
	public void normalMode(View view) {
		Intent selectLevel = new Intent(ModeSelectActivity.this, LevelSelectActivity.class);
		selectLevel.putExtras(parentBundle);
		selectLevel.putExtra("mode", "normal");
		startActivityForResult(selectLevel, 0);	
	}

	/**Start a new timed mode game without the levelselect
	 * 
	 * @param view
	 */
	public void timedMode(View view) {
		Intent startGame = new Intent(ModeSelectActivity.this, GameActivity.class);
		startGame.putExtras(parentBundle);
		startGame.putExtra("mode", "timed");	
		startActivityForResult(startGame, 0);	
	}

	/**Call the LevelSelectActivity to start a new challenge mode game
	 * 
	 * @param view
	 */
	public void challengeMode(View view) {
		Intent selectLevel = new Intent(ModeSelectActivity.this, ChallengeSelectActivity.class);
		selectLevel.putExtras(parentBundle);
		selectLevel.putExtra("mode", "challenge");
		startActivityForResult(selectLevel, 0);	

	}

}
