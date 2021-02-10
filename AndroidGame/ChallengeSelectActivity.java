package eu.equo;

import eu.equo.gamelogic.GameMap;
import eu.equo.gamelogic.LevelManager;
import eu.equo.R;
import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ChallengeSelectActivity extends ActionBarActivity {

	GridView grid;
	ArrayAdapter<String> adapter;
	
	int selectedLevel = -1;
	
	//The data from the parent activity
	Bundle parentBundle;
	Bundle result;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_level_select);

		parentBundle = getIntent().getExtras();

		if(parentBundle.containsKey("skip") && parentBundle.getBoolean("skip"))
		{
			//Continue game
			if(parentBundle.containsKey("map")) continueLevel();
			
			//Load next level
			selectedLevel = parentBundle.getInt("level");
			startLevel();
			
			return;
		}
		
		
		//Construct buttons
	    grid = (GridView) findViewById(R.id.gridView1);
	    
	    setLevels(LevelManager.getNumOfChallenges());

		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				selectButton(((TextView) v).getText().toString());
			}
		});
		
	}

	private void selectButton(String text) {

			if(text == "BACK")
				finish();
			else {
				selectedLevel = Integer.parseInt(text);
				startLevel();
			}
			
		   Toast.makeText(getApplicationContext(),
			text, Toast.LENGTH_SHORT).show();
		
	}
	
	private void setLevels(int num) {
		String[] levels = new String[num + 1];
		for(int i = 1; i <= num; i++)
			levels[i - 1] = "" + i;
		
		levels[num] = "BACK";
		
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, levels);
		grid.invalidateViews();
		grid.setAdapter(adapter);
	}
	
	private void startLevel() {
		Intent startGame = new Intent(ChallengeSelectActivity.this, GameActivity.class);
		startGame.putExtras(parentBundle);
		startGame.putExtra("level", selectedLevel);

		GameMap map = LevelManager.getLevel("challenges", selectedLevel);
		map.type = GameMap.MapType.CHALLENGE;
		startGame.putExtra("map", map);

		
		startActivityForResult(startGame, 0);	

	}
	
	private void continueLevel() {
		Intent startGame = new Intent(ChallengeSelectActivity.this, GameActivity.class);
		startGame.putExtras(parentBundle);
		startActivityForResult(startGame, 0);	

	}
	
    @Override
    public void finish() {
      // Prepare data intent 
      Intent data = new Intent();
      if(result != null) data.putExtras(result);
      
      data.putExtra("level", selectedLevel);
      
      // Activity finished ok, return the data
      setResult(RESULT_OK, data);
      super.finish();
    } 

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	  if (!(resultCode == RESULT_OK)) finish();//Dunno what else could it be
		  if (!data.hasExtra("map")) return;
		  result = data.getExtras();
		  finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.level_select, menu);
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
}