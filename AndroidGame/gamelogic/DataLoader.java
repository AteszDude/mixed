package eu.equo.gamelogic;

import java.util.ArrayList;
import java.util.List;

import eu.equo.MainActivity;
import eu.equo.gamelogic.GameMap.MapType;
import android.util.Log;

public class DataLoader {

	private DataLoader() {throw new RuntimeException();}
	
	/** Loads ONE level supplied as a string argument!
	 *  @param The map represented as a String
	 *  @return The map as a GameMap
	 */
	public static GameMap stringToGameMap(String text) {
		String[] data = text.split(",");
		String levelText = "";
		List<TargetValue> targetValues = new ArrayList<>();
		int width = 0, height = 0;
		List<NumericalCell> mapcells = new ArrayList<>();

		
		for(String act : data) {
			String[] act2 = act.split(":");
			String property = act2[0].trim();
			String value = act2[1].trim();

			switch(property) {
			case "text":
				levelText = value;
				break;
			case "targets": {
				String[] values = value.split(" ");
				
				for(String v : values) {
					int targetvalue;
					try {targetvalue = Integer.parseInt(v);
						 targetValues.add(new TargetValue(targetvalue));}
					catch(Exception e) {}
				}
				break;
			}
			case "size": {
				String[] values = value.split(" ");
				try {width = Integer.parseInt(values[0]);
					 height = Integer.parseInt(values[1]);}
			catch(Exception e) {}
				break;
			}
			case "level": {
				String[] values = value.split("\\s+");//Filters out spaces, tabs, new lines
				for(String mapValue : values) {
					
					//Empty cell
					if(mapValue.equals("x") || mapValue.equals("X")) {
						mapcells.add(new NumericalCell());
					}
					else if (mapValue.length() < 2) continue;
					else try {//Operator and the value
					Operator op = Operator.getFromSign(mapValue.substring(0, 1));
					int numerical = Integer.parseInt(mapValue.substring(1, mapValue.length()));
					mapcells.add(new NumericalCell(numerical, op));
					} catch(Exception e) {Log.d(MainActivity.LogFilter, e.getMessage());}
				}
				break;
			}
			default: break;
			}
		}

		return new GameMap(levelText, height, width, targetValues, mapcells, MapType.NORMAL);
	}

}
