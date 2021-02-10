package eu.equo;
import java.util.List;

import eu.equo.gamelogic.*;
import eu.equo.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector;

public class GameActivity extends Activity  implements 
GestureDetector.OnGestureListener, TimerCallback {
	
	//Division of the display
	protected Rect header;//Where the title and info are displayed
	protected Rect gameArea;//The gamemap is presented inside here, it serves as a frame
	protected Rect mapArea;//The exact location of the gamemap
	protected Rect footer;//Additional buttons on the bottom
	
	//2 custom buttons
	protected Rect retryButton;
	protected Rect menuButton;
	
	//The custom popup window
	protected Rect popupWindow;
	
	//Fields related to the game map
	protected GameMap storedMap;
	protected GameMap gamemap;
	String packageName;
	int levelNum;
	
	//Finished levels in timed mode
	int points;
	
	//Time left int timed mode
	int time;
	
	//Thread that handles time
	Thread timerThread;
	
	//Cell sizes according to display sizes
	public final static int SSIZE = 100;
	public final static int MSIZE = 120;
	public final static int LSIZE = 150;
	
	//Time in timed mode
	public final static int MAXTIME = 100;
	
	protected int cellSize = LSIZE;
	int rows;
	int columns;
	
	//Handle user input
    private GestureDetectorCompat mDetector; 
    private final int SWIPE_DISTANCE_THRESHOLD = cellSize / 3;
    private final int SWIPE_VELOCITY_THRESHOLD = -1;

    //The canvas
    protected MyView myview;
    private Point screenSize = new Point();
    
    //The state of the game
    ActivityState activitystate = new NormalState();
    
    //Text supplied by the map
    private String[] infoText;
    
    /** Called when the activity is first created. */
    @SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //Get the size of the screen
        if (android.os.Build.VERSION.SDK_INT >= 13)
        	getWindowManager().getDefaultDisplay().getSize(screenSize);
        else {
        	screenSize.x = getWindowManager().getDefaultDisplay().getWidth();
        	screenSize.y = getWindowManager().getDefaultDisplay().getHeight();
        	}
        
        //Get map from top activity
        Bundle bundle = getIntent().getExtras();
        
        //TIMED MODE
        if(bundle.containsKey("mode") && bundle.getString("mode").equals("timed")) {
        	if(bundle.containsKey("skip") && bundle.getBoolean("skip"))
        		{
                gamemap = bundle.getParcelable("map");
        		time = bundle.getInt("time");
        		points = bundle.getInt("points");
        		}
        	else {
        	time = MAXTIME;
        	generateNextLevel();
        	points = 0;
        	}

        }
        
        //NORMAL MODE AND TIMED MODE!
        else {
        gamemap = bundle.getParcelable("map");
        
        packageName = bundle.getString("package");
        levelNum = bundle.getInt("level");

        }
        //Close activity if gamemap is null
        if(gamemap == null) {finish(); return;}
        
        if(bundle.containsKey("storedmap")) storedMap = bundle.getParcelable("storedmap");
        else storedMap = new GameMap(gamemap);
        
        
        rows = gamemap.height;
        columns = gamemap.width;
        
        //Set cell size
        if(rows >= 5 || columns >= 5) cellSize = MSIZE;
        
        divideScreen();
        
        if(gamemap.text != null && !gamemap.text.equals(""))
        	infoText = gamemap.text.split("\n");
        
        if(infoText != null && infoText.length > 0)
        	activitystate = new WindowShowState();
        
        myview = new MyView(this);
        setContentView(myview);
        
        mDetector = new GestureDetectorCompat(this,this);

    }

    @Override
    public void onPause() {
    super.onPause();

    if(timerThread != null)
    	timerThread.interrupt();
    }
    
    @Override
    public void onResume() {
    super.onResume();
    
    timerThread = new Thread(new Timer(time, this));
    timerThread.start();    
    }
    
    
    /**
     * Inits the 3 Rect classes that represents the division the display: header, game area, footer.
     */
    private void divideScreen() {
    	//Set the titlebar = header
    	header = new Rect(0, 0, screenSize.x, 100);
    	footer = new Rect(0, screenSize.y - 100, screenSize.x, screenSize.y);

    	//Game Area is a frame for the game map
    	gameArea = new Rect(0, header.bottom ,screenSize.x, footer.top);
    	int sizex = cellSize * columns;
    	int sizey = cellSize * rows;

    	//Where the board is placed
    	mapArea = new Rect((gameArea.width() - sizex) / 2,
    			gameArea.top + (gameArea.height() - sizey) / 2,
    			sizex + (gameArea.width() - sizex) / 2,
    			sizey + gameArea.top + (gameArea.height() - sizey) / 2);
    	
    	//Coordinates for the pop up window
    	popupWindow = new Rect((screenSize.x / 2) - 300,
    			(screenSize.y / 2) - 100,
    			(screenSize.x / 2) + 300,
    			(screenSize.y / 2) + 100
    			);
    	
    	//Add buttons
    	menuButton = new Rect(header.right - 155, 5, header.right - 5, 65);
    	retryButton = new Rect(5 , 5, 155, 65);
    	
    }
    
    protected void swipe(Point at, Direction dir) {
    	if(!mapArea.contains(at.x, at.y)) return;
    	
    	at.x = (at.x - mapArea.left) / cellSize;
    	at.y = (at.y - mapArea.top) / cellSize;
    	
    	if(gamemap.move(at.x, at.y, dir))
    		afterMoved();
    }

    /**After a legal move happened
     * 
     */
    private void afterMoved() {
    	if(gamemap.type == GameMap.MapType.TIMED && gamemap.getSate() == GameMap.GameState.WON)
    		generateNextLevel();
    }
    
    /**Generate next level in timed mode, or close the activity
     * 
     */
    private void generateNextLevel() {
    		points++;
    		gamemap = LevelGenerator.generateLevel(points);
    		storedMap = new GameMap(gamemap);
    }
    
    
    @Override
    public void finish() {
      // Prepare data intent 
      Intent data = new Intent();
      
      data.putExtra("map", gamemap);
      data.putExtra("storedmap", storedMap);
      data.putExtra("package", packageName);
      data.putExtra("level", levelNum);

      //When escaping a timed game
      if(gamemap.getSate() == GameMap.GameState.PLAYING && gamemap.type == GameMap.MapType.TIMED) {
    	data.putExtra("time", time);
      }
      
      //Could be left there as well, whatever.
      if(gamemap.type == GameMap.MapType.TIMED)
      data.putExtra("points", points);

    	  
      // Activity finished ok, return the data
      setResult(RESULT_OK, data);
      super.finish();
    }
    
    /**Reset the map
     * 
     */
    private void retry() {
    gamemap = new GameMap(storedMap);
    activitystate = new NormalState();
    }

    
    public class MyView extends View {
        private Paint paint = new Paint();//White background
        private Paint backpaint2 = new Paint();//Background for the game map
        private Paint cellPaint = new Paint();
        private Paint greenPaint = new Paint();
        private Paint grayAlpha = new Paint();
        private TextPaint textPaint = new TextPaint();//Basic text for cells
        private TextPaint textPaint2 = new TextPaint();//Used for popup windows
        private TextPaint evalTextPaint = new TextPaint();//Text for evaluated numbers

        private Bitmap menuImage;
        private Bitmap retryImage;
        private Bitmap winImage;
        private Bitmap loseImage;

        public MyView(Context context) {
             super(context);
             initPaint();
        }

        
        private void initPaint() {
           paint.setColor(Color.WHITE);
           paint.setStyle(Paint.Style.FILL);	
        	
     	   cellPaint.setColor(Color.BLACK);
     	   cellPaint.setStyle(Style.STROKE);
        	
     	   textPaint.setTextSize(40);
     	   textPaint.setColor(Color.BLUE);

     	   textPaint2.setTextSize(20);
     	   textPaint2.setColor(Color.BLACK);

     	   
     	   greenPaint.setColor(Color.GREEN);
     	   greenPaint.setStyle(Style.FILL);
     	   
     	   evalTextPaint.setTextSize(45);
     	   evalTextPaint.setColor(Color.WHITE);
     	   
     	   backpaint2.setColor(Color.parseColor("#E0F7FA"));
     	   backpaint2.setStyle(Style.FILL_AND_STROKE);
     	   
     	   grayAlpha.setColor(Color.LTGRAY);
     	   grayAlpha.setAlpha(122);
     	   
     	   retryImage = BitmapFactory.decodeResource(getResources(), R.drawable.retry);
     	   menuImage = BitmapFactory.decodeResource(getResources(), R.drawable.menu);
     	   winImage = BitmapFactory.decodeResource(getResources(), R.drawable.good);
     	   loseImage = BitmapFactory.decodeResource(getResources(), R.drawable.bad);

        }
        
        @Override
        protected void onDraw(Canvas canvas) {
           super.onDraw(canvas);

           canvas.drawPaint(paint);
           
           drawHeader(canvas, header);
           drawMap(canvas, mapArea.left, mapArea.top, cellSize, cellSize);
           drawFooter(canvas, footer);

           switch(gamemap.getSate()) {
           	case LOST:
          	  canvas.drawBitmap(loseImage, gameArea.right - 200, gameArea.bottom - 50, cellPaint);
           		break;
           	case PLAYING:
           		break;
           	case WON:
           		if(gamemap.type == GameMap.MapType.NORMAL)
           			canvas.drawBitmap(winImage, gameArea.right - 200, gameArea.bottom - 50, cellPaint);
           		else if(gamemap.type == GameMap.MapType.CHALLENGE)
           			canvas.drawText("" + gamemap.score, (header.right / 2) - 50, 50, textPaint2);
           		break;
           	default:
           		break;
           }
           
           //This has to be the last to be called, because it's on top of everything
           if(activitystate.isWindowShow())
        	   drawWindow(canvas, popupWindow);
           
           //TODO: This seems very bad!!!!!!!!!!!!!!!!!!!!!!!!!!!
           try {  
              Thread.sleep(30);  
           } catch (InterruptedException e) { }
           
           invalidate();  // Force a re-draw

       }
       
        /**
         * Draws the title or header part of the display, also including the buttons!
         * @param canvas
         * @param pos
         */
       protected void drawHeader(Canvas canvas, Rect pos) {
    	  canvas.drawRect(pos, paint);
    	  canvas.drawBitmap(menuImage, menuButton.left, menuButton.top, cellPaint);
    	  canvas.drawBitmap(retryImage, retryButton.left, retryButton.top, cellPaint);
    	  
    	  //Draw remaining time in timed mode
    	  if(gamemap.type == GameMap.MapType.TIMED)
    		  canvas.drawText("" + time, (header.right / 2) - 50, 50, textPaint2);
       }
       
       /**Draws the bottom of the display, INCLUDING the target values!
        * 
        * @param canvas
        * @param pos
        */
       protected void drawFooter(Canvas canvas, Rect pos) {
     	  canvas.drawRect(pos, paint);
     	  List<TargetValue> values = gamemap.getTargetValues();
     	  
     	  //Draw target values!
     	  for(int i = 0; i < values.size(); i++) {
     		  canvas.drawCircle((cellSize / 2) + cellSize * i, footer.bottom - (cellSize / 2), cellSize / 2, cellPaint);
     		  canvas.drawText(values.get(i).toString(), (cellSize / 2) - 5 + cellSize * i, footer.bottom + 5 - (cellSize / 2), textPaint);
     	  }

       }
       
       /** Displays the pop-up message! Does NOT check for the gamestate!
        * 
        * @param canvas
        * @param pos
        */
       protected void drawWindow(Canvas canvas, Rect pos) {
    	   //Shade background
    	   canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), grayAlpha);
    	   
    	   //Draw Window
    	   canvas.drawRect(pos, paint);
    	   int at = pos.top + 50;
    	   for(String text : infoText) {
    		   canvas.drawText(text , pos.left, at, textPaint2);
    	   	   at += 30;
    	   }
    	   canvas.drawText("(TOUCH TO CONTINUE)" , pos.left, at, textPaint2);
       }
       
       protected void drawMap(Canvas canvas, int startx, int starty, int width, int height) {
    	   
    	   //Should have less access
    	   NumericalCell[][] map = gamemap.getValues();
    	    
    	   //Draw background, 2 colored squares
    	   for(int i = 0, act = 0; i < gamemap.width * 10; i++, act++)
    		   for(int j = 0; j < gamemap.height * 10; j++, act++)
    			   canvas.drawRect(new Rect((width / 10) * i + startx, (height / 10) * j + starty, 
    					   (width / 10) * (i + 1) + startx, (height / 10) * (j + 1) + starty),
    					   act % 2 == 0 ? paint : backpaint2);
    	   
    	   //Draw cells
    	   for(int i = 0; i < gamemap.width; i++)
    		   for(int j = 0; j < gamemap.height; j++) {
    			   //Do nothing if it's an empty cell
    			   if(map[i][j].getType() == CellType.EMPTY) continue;

    			   //Draw black text on a white background
    			   if(map[i][j].getType() == CellType.NUMERICAL) {
        			   canvas.drawRect(new Rect(width * i + startx, height * j + starty, 
        					   width * (i + 1) + startx, height * (j + 1) + starty), paint);
        			   canvas.drawRect(new Rect(width * i + startx, height * j + starty, 
        					   width * (i + 1) + startx, height * (j + 1) + starty), cellPaint);
    				   canvas.drawText(map[i][j].toString(), width * i + startx + width / 2,
    					   height * j + starty + height / 2, textPaint);
    			   }

    			   //Draw white text on a green background
    			   else if(map[i][j].getType() == CellType.EVALUATED) {
        			   canvas.drawRect(new Rect(width * i + startx, height * j + starty, 
        					   width * (i + 1) + startx, height * (j + 1) + starty), greenPaint);
        			   canvas.drawText(map[i][j].toString(), width * i + startx + width / 2,
        				   height * j + starty + height / 2, evalTextPaint);
    			   }
    			   else assert(false);

    		   }
       }
    }//MyClass
    
	@Override
    public boolean onTouchEvent(MotionEvent me) {
    	return mDetector.onTouchEvent(me);
    	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		//Dismiss popup window
		if(activitystate.isWindowShow()) {
			activitystate = new NormalState();
			return true;
		}
		
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		
		//Load next level if the player won
		if(gamemap.getSate() == GameMap.GameState.WON) {
			finish();
			return true;
		}
		//Rertry this level if the player lost
		else if(gamemap.getSate() == GameMap.GameState.LOST) {
			retry();
			return true;
		}
		else if(menuButton.contains( (int) e.getX(), (int) e.getY())) {
			finish();
			return true;
		}
		else if(retryButton.contains( (int) e.getX(), (int) e.getY())) {
			retry();
			return true;
		}

		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {		
	}
	//http://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
        float distanceX = e2.getX() - e1.getX();
        float distanceY = e2.getY() - e1.getY();
        Point cellAt = new Point((int)e1.getX(), (int)e1.getY());
        
        if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
            if (distanceX > 0)
                swipe(cellAt, Direction.RIGHT);
            else
                swipe(cellAt, Direction.LEFT);
            return true;
        }
        else if(Math.abs(distanceX) < Math.abs(distanceY) && Math.abs(distanceY) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
            if (distanceY > 0)
                swipe(cellAt, Direction.DOWN);
            else
                swipe(cellAt, Direction.UP);
            return true;
        }
        return false;
	}

	@Override
	public void timerCallback(int counter) {
    	time = counter;
    	if(time == 0) {
    		timerThread.interrupt();
    		gamemap.finishTimed();
    		finish();
    	}
	}
}
