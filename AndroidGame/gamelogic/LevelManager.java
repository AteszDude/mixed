package eu.equo.gamelogic;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import eu.equo.MainActivity;
import android.content.res.AssetManager;
import android.util.Log;

/**This class contains all the necessary information about packages and manages the access and modification of stored data.
 * 
 * @author Attila Torda
 *
 */
public class LevelManager {

	//Checks whether the user purchased the full version
	//private static boolean isUpgraded = false;

	//Data stored in classes for security reasons!
	private final static String[] levels = {"tutorial", "demo", "small", "medium", "large", "mixed"};
	//private final static String[] freeLevels = {"tutorial", "demo"};

	//High scores
	private static int[] highScores;
	
	//A cache for the current package
	private static String currentPackage = "";
	private static String[] loadedLevels = new String[] {};
	private static int levelNum = 0;
	
	//Class to access local files
	private static AssetManager am;
	
	public static void setLevelManager(AssetManager newam) {
		am = newam;
	}
	
	private static void loadPackage(String resourceName) {

		String prevPackage = currentPackage;
		String[] prevLevels = loadedLevels;
		
		try {
			InputStream is = am.open(resourceName + ".txt");
		    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		    String result = s.hasNext() ? s.next() : "";
			currentPackage = resourceName;
			loadedLevels = result.split(";");
			levelNum = loadedLevels.length - 1;//The last element in the array is empty!
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(MainActivity.LogFilter, "Error loading text file: " + e);

			//Rollback
//			currentPackage = prevPackage;
//			loadedLevels = prevLevels;
		}

	}
	
	/**Writes the text into the "(resourceName).txt" file. Note, that it overwrites the whole content of the file!
	 * 
	 * @param text
	 * @param resourceName
	 */
	private static void writeToFile(String text, String resourceName) {
		try {
			PrintWriter writer = new PrintWriter("/" + resourceName + ".txt", "UTF-8");
			writer.print(text);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(MainActivity.LogFilter, "Error loading text file: " + e);
		}
	}
	
	/**Reads a text from a file
	 * 
	 * @param resourceName
	 * @return the contents of the file or null
	 */
	public static String readFile(String resourceName) {
		String result = null;
		
		try {
			InputStream is = am.open(resourceName + ".txt");
		    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		    result = s.hasNext() ? s.next() : "";
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(MainActivity.LogFilter, "Error loading text file: " + e);
		}
		
		
		return result;
	}
	
	
	/**Checks if the given package is loaded. If not, load it
	 * 
	 */
	private static void checkPackage(String packageName) {
		if(currentPackage != packageName)
			loadPackage(packageName);
	}
	
	/**
	 * 
	 * @return the names of all available packages
	 */
	public static String[] getPackages() {
		return levels;
	}
	
	public static int getNumOfChallenges() {
		return 3;
	}
	
	/**
	 * 
	 * @param packageName
	 * @return the number of all levels in a package
	 */
	public static int getLevelNum(String packageName) {
			checkPackage(packageName);
			return levelNum;
	}
	
	
	/**
	 * 
	 * @param packageName
	 * @return the number of unlocked levels in a package
	 */
	public static int getunlockedLevels(String packageName) {
		//TODO
		return 20;
	}
		
	/**
	 * 
	 * @param packageName
	 * @param levelNum
	 * @return a String representation of the level
	 */
	public static String getLevelStr(String packageName, int levelNum) {
		checkPackage(packageName);
		return loadedLevels[levelNum - 1];
	}

	/**
	 * 
	 * @param packageName
	 * @param levelNum
	 * @return the loaded level
	 */
	public static GameMap getLevel(String packageName, int levelNum) {
		return DataLoader.stringToGameMap(getLevelStr(packageName, levelNum));
	}
	
	/**Call this method after SUCCESSFULLY completing a level.
	 * 
	 * @param packageName Name of the package
	 * @param levelNum Number of the level
	 * @param score Only in Timed and Challenge modes!
	 * @return
	 */
	public static boolean completeLevel(String packageName, int levelNum, int score) {
		//Returns true if there is a next level otherwise returns false
		//TODO
		
		checkPackage(packageName);
		levelNum++;
		if(getLevelNum(packageName) < levelNum)
			return false;
		
		return true;
	}
}
