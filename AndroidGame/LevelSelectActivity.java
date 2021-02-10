package eu.equo;

import eu.equo.gamelogic.LevelManager;
import eu.equo.R;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class LevelSelectActivity extends ActionBarActivity {

	GridView grid;
	ArrayAdapter<String> adapter;
	
	private final String[] packages = LevelManager.getPackages();
	private String[] levels = {};
	private State state = State.PackageSelect;
	
	enum State {PackageSelect, LevelSelect};
	
	String selectedPackage = "";
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
			selectedPackage = parentBundle.getString("package");
			selectedLevel = parentBundle.getInt("level");
			startLevel();
			
			return;
		}
		
		
		//Construct buttons
	    grid = (GridView) findViewById(R.id.gridView1);
	    
	    setPackages();

		grid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				selectButton(((TextView) v).getText().toString());
			}
		});
		
	}

	private void selectButton(String text) {
		switch(state) {
		case LevelSelect:
			if(text == "BACK")
				setPackages();
			else {
				selectedLevel = Integer.parseInt(text);
				startLevel();
			}
			break;
		case PackageSelect:
			if(text == "BACK") {finish(); break;}
			
			selectedPackage = text;
			setLevels(LevelManager.getLevelNum(text));
			break;
		default:
			break;
		}
		   Toast.makeText(getApplicationContext(),
			text, Toast.LENGTH_SHORT).show();
		
	}
	
	private void setLevels(int num) {
		levels = new String[num + 1];
		for(int i = 1; i <= num; i++)
			levels[i - 1] = "" + i;
		
		levels[num] = "BACK";
		
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, levels);
		grid.invalidateViews();
		grid.setAdapter(adapter);
		state = State.LevelSelect;
	}
	
	private void setPackages() {
		String [] packagesWBack = new String[packages.length + 1];
		for(int i = 0; i < packages.length; i++)
			packagesWBack[i] = packages[i];
		
		packagesWBack[packagesWBack.length - 1] = "BACK";
				
		adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, packagesWBack);
		grid.invalidateViews();
		grid.setAdapter(adapter);
		
		state = State.PackageSelect;
	}
	
	private void startLevel() {
		Intent startGame = new Intent(LevelSelectActivity.this, GameActivity.class);
		startGame.putExtras(parentBundle);
		startGame.putExtra("package", selectedPackage);
		startGame.putExtra("level", selectedLevel);
		startGame.putExtra("map", LevelManager.getLevel(selectedPackage, selectedLevel));

		startActivityForResult(startGame, 0);	

	}
	
	private void continueLevel() {
		Intent startGame = new Intent(LevelSelectActivity.this, GameActivity.class);
		startGame.putExtras(parentBundle);
		startActivityForResult(startGame, 0);	

	}
	
    @Override
    public void finish() {
      // Prepare data intent 
      Intent data = new Intent();
      if(result != null) data.putExtras(result);
      
      data.putExtra("package", selectedPackage);
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

//Not used atm
class ImageAdapter extends BaseAdapter {
    private Context mContext;

    public ImageAdapter(Context c) {
        mContext = c;
    }

    @Override
	public int getCount() {
        return mThumbIds.length;
    }

    @Override
	public Object getItem(int position) {
        return null;
    }

    @Override
	public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    @Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        } else {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    private Integer[] mThumbIds = {
            R.drawable.purchase, R.drawable.purchase,
            R.drawable.purchase, R.drawable.purchase,
            R.drawable.purchase, R.drawable.purchase,
            R.drawable.purchase, R.drawable.purchase,
            R.drawable.purchase, R.drawable.purchase,
            R.drawable.purchase, R.drawable.purchase,
            R.drawable.purchase, R.drawable.purchase,
            R.drawable.purchase, R.drawable.purchase,
            R.drawable.purchase, R.drawable.purchase,
            R.drawable.purchase, R.drawable.purchase,
            R.drawable.purchase, R.drawable.purchase,
    };
}
