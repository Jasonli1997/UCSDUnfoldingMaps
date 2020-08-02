package module3;

import java.util.HashMap;
import java.util.Map;

import processing.core.PApplet;

public class testLoadString extends PApplet {
	private Map<String, Float> lifeExpByCountry;
	
	public void tester() {
		lifeExpByCountry = loadLifeExpectancyFromCSV("LifeExpectancyWorldBankModule3.csv");
		System.out.println(3);
	}
	
	private Map<String, Float> loadLifeExpectancyFromCSV(String fileName) {
		// Construct the map
		lifeExpByCountry = new HashMap<String, Float>();
		
		String[] rows = loadStrings(fileName);
		for (String row : rows) {
			String[] columns = row.split(",");
			float value = Float.parseFloat(columns[5]);
			lifeExpByCountry.put(columns[4], value);

		}
		return lifeExpByCountry;
	}
}
