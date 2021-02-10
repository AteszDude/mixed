package eu.equo.gamelogic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import eu.equo.gamelogic.GameMap.MapType;

public class LevelGenerator {

	private final static Random rand = new Random();
	private final static int plusProb = 45;
	private final static int minusProb = 25;
	private final static int multProb = 20;
	private final static int divProb = 10;
		
	private LevelGenerator() {throw new RuntimeException();}
	
	/**Gives a random gamemap, using the generateLevel(int width, int height, int difficulty) method
	 * 
	 * @param level 0 or bigger
	 * @return
	 */
	public static GameMap generateLevel(int level) {
		GameMap result;
		
		if(level < 0) level = 0;
		
		if(level < 4)
			result = generateLevel(2, 2, 3);
		else if(level < 8) {
			if(rand.nextInt(2) == 0)
				result = generateLevel(2, 3, 4);
			else result = generateLevel(3, 2, 4);
		}
		else if(level < 15)
			result = generateLevel(3, 3, level / 2);
		else
			result = generateLevel(4, 4, level / 3);
		
		return result;
	}
	
	public static GameMap generateLevel(int width, int height, int difficulty) {
		
		//Generate a random map
		GameMap map = createRandomMap(width, height, difficulty);
		
		//Generate target values
		List<TargetValue> tv = createRandomTargets(new GameMap(map));
		
		map.getTargetValues().addAll(tv);
		
		return map;
	}
	
	
	private static GameMap createRandomMap(int width, int height, int difficulty) {
		List<NumericalCell> randomValues = new ArrayList<>(width + height);
		for(int i = 0; i < width * height; i++)
			randomValues.add(new NumericalCell(rand.nextInt(difficulty + 1), getWeightedOperator()));
		
		return new GameMap(null, width, height, new ArrayList<TargetValue>(), randomValues, MapType.TIMED);
	}
	
	private static List<TargetValue> createRandomTargets(GameMap map) {
		List<TargetValue> result = new LinkedList<>();
		
		//1: Generate all the cells randomly
		List<Point> availablePoints = new LinkedList<>();
		
	    for (int j = 0; j < map.height; j++) 
	    	for (int i = 0; i < map.width; i++)
	    		availablePoints.add(new Point(i, j));

		//2: Swipe through all the cells, until all of them are evaluated
	    while(availablePoints.size() > 0) {
	    	

		//a: Select a random cell
	    int position = rand.nextInt(availablePoints.size());
		Point next = availablePoints.get(position);
		
		//b: If it's separated, remove from the list, otherwise move in a random direction until possible
		if(map.checkSolitude(next.x, next.y) || map.isEmpty(next.x, next.y))
			availablePoints.remove(position);
		else while(map.move(next.x, next.y, Direction.values()[rand.nextInt(4)]));		
	    }
		
	    //Collect all the remaining, separated targetvalues
	    
	    
	    NumericalCell[][] cells = map.getValues();
	    for (int j = 0; j < map.height; j++) 
	    	for (int i = 0; i < map.width; i++)
	    		if(!cells[i][j].isEmpty())
	    			result.add(new TargetValue(cells[i][j].value));
	    
	    Collections.sort(result);
	    
		return result;
	}
	
	private static Operator getWeightedOperator() {
		return Operator.values()[selectRandom(plusProb, minusProb, multProb, divProb)];
	}
	
	/**
	 * 
	 * @param probabilities 
	 * @return an integer between 0 and the number of input variables 
	 */
	public static int selectRandom(int... probabilities) {
		
		int maxValue = 0;
		
		//Set maximum value
		for(int probability : probabilities) 
			maxValue += probability;
		
		//Get random value
		int value = rand.nextInt(maxValue);
		
		
		//Assign value to input number
		for(int i = 0,  actvalue = 0; i < probabilities.length; i++) {
			actvalue += probabilities[i];
			if(value <= actvalue) return i;
		}
		
		return 0;
	}
}

class Point {
	public final int x, y;
	public Point(int x, int y) {this.x = x; this.y = y;}
}
