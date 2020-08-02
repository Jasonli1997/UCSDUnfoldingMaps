package module6;

import java.util.*;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.AbstractShapeMarker;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.MultiMarker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import parsing.ParseFeed;
import processing.core.PApplet;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Your name here
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {
	
	// We will use member variables, instead of local variables, to store the data
	// that the setUp and draw methods will need to access (as well as other methods)
	// You will use many of these variables, but the only one you should need to add
	// code to modify is countryQuakes, where you will store the number of earthquakes
	// per country.
	
	// You can ignore this.  It's to get rid of eclipse warnings
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFILINE, change the value of this variable to true
	private static final boolean offline = true;
	
	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";
	
	// The files containing city names and info and country names and info
	private String cityFile = "city-data.json";
	private String countryFile = "countries.geo.json";
	
	// The map
	private UnfoldingMap map;
	
	// Markers for each city
	private List<Marker> cityMarkers;
	
	// Markers for each route
	private List<Marker> routeMarkers;
	
	// Helper fields for airport markers and route markers
	private HashMap<String, Integer> airportIDs;
	private List<Integer> airportIDList;
	private HashMap<Integer, Location> airports;
	
	// Markers for each earthquake
	private List<Marker> quakeMarkers;

	// A List of country markers
	private List<Marker> countryMarkers;
	
	// A List of airport markers
	private List<Marker> airportMarkers;
	
	// NEW IN MODULE 5
	private CommonMarker lastSelected;
	private CommonMarker lastClicked;
	
	public void setup() {		
		// (1) Initializing canvas and map tiles
		size(900, 700, OPENGL);
		if (offline) {
		    map = new UnfoldingMap(this, 200, 50, 650, 600, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom";  // The same feed, but saved August 7, 2015
		}
		else {
			map = new UnfoldingMap(this, 200, 50, 650, 600, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
		    //earthquakesURL = "2.5_week.atom";
		}
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// FOR TESTING: Set earthquakesURL to be one of the testing files by uncommenting
		// one of the lines below.  This will work whether you are online or offline
		//earthquakesURL = "test1.atom";
		//earthquakesURL = "test2.atom";
		
		// Uncomment this line to take the quiz
		earthquakesURL = "quiz2.atom";
		
		
		// (2) Reading in earthquake data and geometric properties
	    //     STEP 1: load country features and markers
		List<Feature> countries = GeoJSONReader.loadData(this, countryFile);
		countryMarkers = MapUtils.createSimpleMarkers(countries);
		
		//     STEP 2: read in city data
		List<Feature> cities = GeoJSONReader.loadData(this, cityFile);
		cityMarkers = new ArrayList<Marker>();
		List<String> cityNames = new ArrayList<String>();
		for(Feature city : cities) {
		  cityMarkers.add(new CityMarker(city));
		  String name = new CityMarker(city).getStringProperty("name");
		  cityNames.add("\""+name+"\"");
		}		
		
		//     STEP 3: read in airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		airportMarkers = new ArrayList<Marker>();
		airports = new HashMap<Integer, Location>();
		airportIDs = new HashMap<String, Integer>();
		airportIDList = new ArrayList<Integer>();
	    
	    for(PointFeature feature : features) {
	    	AirportMarker m = new AirportMarker(feature);
	    	// only show airports near the select city markers
	    	if (cityNames.contains(m.getCity())) {
	    		m.setRadius(5);
				airportMarkers.add(m);
				
				// put airport in hashmap with OpenFlights unique id for key
				airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
				
				// put airport names in hashmap with their unique id
				airportIDs.put(m.getName(), Integer.parseInt(feature.getId()));
				
				// put airport IDs in a list for later use
				airportIDList.add(Integer.parseInt(feature.getId()));
	    	}
	    }
	    System.out.println(airports);
	    System.out.println(airportIDs);
	    
		//     STEP 4: read in earthquake RSS feed
	    List<PointFeature> earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    quakeMarkers = new ArrayList<Marker>();
	    
	    for(PointFeature feature : earthquakes) {
		  //check if LandQuake
		  if(isLand(feature)) {
		    quakeMarkers.add(new LandQuakeMarker(feature));
		  }
		  // OceanQuakes
		  else {
		    quakeMarkers.add(new OceanQuakeMarker(feature));
		  }
	    }
	    
	    //      STEP 5: read in route data
	    List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
	    routeMarkers = new ArrayList<Marker>();
	    for(ShapeFeature route : routes) {
			
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
			sl.setHidden(true);
		
			//System.out.println(sl.getProperties());
			
			//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			routeMarkers.add(sl);
		}
	    
	    // sort and print the earthquakes by magnitude
	    sortAndPrint(1);

	    // could be used for debugging
	    printQuakes();
	 		
	    // (3) Add markers to map
	    //     NOTE: Country markers are not added to the map.  They are used
	    //           for their geometric properties
	    map.addMarkers(quakeMarkers);
	    map.addMarkers(cityMarkers);
	    map.addMarkers(airportMarkers);
	    
	}  // End setup 
	
	
	public void draw() {
		background(0);
		map.draw();
		addKey();
		
	}
	
	
	// TODO: Add the method:
	//   private void sortAndPrint(int numToPrint)
	// and then call that method from setUp
	private void sortAndPrint(int numToPrint) {
		
		// turn List into an array
		EarthquakeMarker[] quakes = quakeMarkers.toArray(new EarthquakeMarker[quakeMarkers.size()]);
		List<EarthquakeMarker> quakeList = Arrays.asList(quakes);
		Collections.sort(quakeList);
		for (int i = 0; i < numToPrint; i++) {
			System.out.println(quakeList.get(i));
		}
	}
	
	/** Event handler that gets called automatically when the 
	 * mouse moves.
	 */
	@Override
	public void mouseMoved()
	{
		// clear the last selection
		if (lastSelected != null) {
			lastSelected.setSelected(false);
			lastSelected = null;
		
		}
		selectMarkerIfHover(quakeMarkers);
		selectMarkerIfHover(cityMarkers);
		selectMarkerIfHover(airportMarkers);
		//loop();
	}
	
	// If there is a marker selected 
	private void selectMarkerIfHover(List<Marker> markers)
	{
		// Abort if there's already a marker selected
		if (lastSelected != null) {
			return;
		}
		
		for (Marker m : markers) 
		{
			CommonMarker marker = (CommonMarker)m;
			if (marker.isInside(map,  mouseX, mouseY)) {
				lastSelected = marker;
				marker.setSelected(true);
				return;
			}
		}
	}

	
	/** The event handler for mouse clicks
	 * It will display an earthquake and its threat circle of cities
	 * Or if a city is clicked, it will display all the earthquakes 
	 * where the city is in the threat circle
	 */
	@Override
	public void mouseClicked()
	{
		if (lastClicked != null) {
			unhideMarkers();
			lastClicked.setClicked(false);
			lastClicked = null;
		}
		else if (lastClicked == null) 
		{
			checkEarthquakesForClick();
			if (lastClicked == null) {
				checkCitiesForClick();
			}  
			if (lastClicked == null) {
				checkAirportsForClick();
			}
		}
	}
	
	// Helper method that will check if a city marker was clicked on
	// and respond appropriately
	private void checkCitiesForClick()
	{
		if (lastClicked != null) return;
		// Loop over the earthquake markers to see if one of them is clicked
		for (Marker marker : cityMarkers) {
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = (CommonMarker)marker;
				// if clicked is true then a popup menu will appear off the map
				lastClicked.setClicked(true);
				// Hide all the other cities
				for (Marker mhide : cityMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				
				// Hide all the airports
				for (Marker mhide : airportMarkers) {
					mhide.setHidden(true);
				}
				
				HashMap<String, Object> properties = lastClicked.getProperties();
				// initiate a count for the number of nearby earthquakes
				int count = 0;
				for (Marker mhide : quakeMarkers) {
					EarthquakeMarker quakeMarker = (EarthquakeMarker)mhide;
					
					// hide the faraway earthquakes
					if (quakeMarker.getDistanceTo(marker.getLocation()) 
							> quakeMarker.threatCircle()) {
						quakeMarker.setHidden(true);
					} 
					
					// increment count if there is any nearby earthquakes
					else {
						count++;
					}
				}
				Integer countInt = Integer.valueOf(count);
				properties.put("EarthquakeCount", countInt);
				lastClicked.setProperties(properties);
				return;
			}
		}		
	}
	
	// Helper method that will check if an earthquake marker was clicked on
	// and respond appropriately
	private void checkEarthquakesForClick()
	{
		if (lastClicked != null) return;
		// Loop over the earthquake markers to see if one of them is clicked
		for (Marker m : quakeMarkers) {
			EarthquakeMarker marker = (EarthquakeMarker)m;
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;
				// Hide all the other earthquakes
				for (Marker mhide : quakeMarkers) {
					if (mhide != lastClicked) {
						mhide.setHidden(true);
					}
				}
				
				// Hide the cities are not in threat circle
				for (Marker mhide : cityMarkers) {
					if (mhide.getDistanceTo(marker.getLocation()) 
							> marker.threatCircle()) {
						mhide.setHidden(true);
					}
				}
				
				// Hide all the airports
				for (Marker mhide : airportMarkers) {
					mhide.setHidden(true);
				}
				
				return;
			}
		}
	}
	
	// Helper method that will check if an airport marker was clicked on
	// and respond appropriately
	private void checkAirportsForClick() {
		if (lastClicked != null) return;
		// Loop over the airport markers to see if one of them is clicked
		for (Marker m : airportMarkers) {
			AirportMarker marker = (AirportMarker)m;
			if (!marker.isHidden() && marker.isInside(map, mouseX, mouseY)) {
				lastClicked = marker;
				lastClicked.setClicked(true);
				AirportMarker am = (AirportMarker) lastClicked;
				
				int airportID = airportIDs.get(am.getName());
				System.out.println("The airport's name is " + am.getName() + " ID is " + airportID);
				List<Integer> destinations = new ArrayList<Integer>();
				List<Location> destLocations = new ArrayList<Location>();
				//Location source = airports.get(airportID);
				List<ScreenPosition> tc = new ArrayList<ScreenPosition>();
				for (Marker sl : routeMarkers) {
					if (Integer.parseInt((String)sl.getProperty("source")) == airportID) {
						// get the airport ID for all destinations
						int destination = Integer.parseInt((String)sl.getProperty("destination"));
						destinations.add(destination);
						
						// get the screenposition for all destinations
						for (Marker testm : airportMarkers) {
							AirportMarker testMarker = (AirportMarker) testm;
							int testID = airportIDs.get(testMarker.getName());
							if (testID == destination) {
								ScreenPosition sp = testMarker.getScreenPosition(map);
								tc.add(sp);
							}
						}
						/*
						Location loc = airports.get(destination);
						if (loc != null) {
							destLocations.add(loc);
						}
						*/
					}
				}
				System.out.println("All the destinations from the city are: ");
				System.out.println(destinations);
				
				// a sanity check on how many destinations are not included in airport markers
				int notIncluded = 0;
				for (int destination : destinations) {
					if (!airportIDList.contains(destination)) {
						notIncluded++;
					}
				}
				System.out.println("There are, however, " + notIncluded + " destinations not included in airport markers");
				
				HashMap<String, Object> properties = am.getProperties();
				System.out.println(properties);
				properties.put("destinationCities", tc);
				am.setProperties(properties);
				System.out.println("New Properties");
				System.out.println(properties);
				
				// Hide all the earthquakes
				for (Marker mhide : quakeMarkers) {
					mhide.setHidden(true);
				}
				
				// Hide all the cities
				for (Marker mhide : cityMarkers) {
					mhide.setHidden(true);
				}
				
				// Hide all the other airports that are not destinations
				for (Marker mhide : airportMarkers) {
					AirportMarker airportm = (AirportMarker) mhide;
					String airportName = airportm.getName();
					int airportUniqueID = airportIDs.get(airportName);
					if (!destinations.contains(airportUniqueID)) {
						mhide.setHidden(true);
					}
				}
				
				am.setHidden(false);
				
				return;
			}
		}
	}
	
	
	// loop over and unhide all markers
	private void unhideMarkers() {
		for(Marker marker : quakeMarkers) {
			marker.setHidden(false);
		}
			
		for(Marker marker : cityMarkers) {
			marker.setHidden(false);
		}
		
		for(Marker marker : airportMarkers) {
			marker.setHidden(false);
		}
	}
	
	// helper method to draw key in GUI
	private void addKey() {	
		// Remember you can use Processing's graphics methods here
		fill(255, 250, 240);
		
		int xbase = 25;
		int ybase = 50;
		
		rect(xbase, ybase, 150, 250);
		
		fill(0);
		textAlign(LEFT, CENTER);
		textSize(12);
		text("Earthquake Key", xbase+25, ybase+25);
		
		fill(150, 30, 30);
		int tri_xbase = xbase + 35;
		int tri_ybase = ybase + 50;
		triangle(tri_xbase, tri_ybase-CityMarker.TRI_SIZE, tri_xbase-CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE, tri_xbase+CityMarker.TRI_SIZE, 
				tri_ybase+CityMarker.TRI_SIZE);

		fill(0, 0, 0);
		textAlign(LEFT, CENTER);
		text("City Marker", tri_xbase + 15, tri_ybase);
		
		text("Land Quake", xbase+50, ybase+70);
		text("Ocean Quake", xbase+50, ybase+90);
		text("Airport", xbase+50, ybase+108);
		text("Size ~ Magnitude", xbase+25, ybase+125);
		
		fill(255, 255, 255);
		ellipse(xbase+35, 
				ybase+70, 
				10, 
				10);
		rect(xbase+35-5, ybase+90-5, 10, 10);
		
		fill(11);
		ellipse(xbase+35, ybase+108, 5, 5);
		
		fill(color(255, 255, 0));
		ellipse(xbase+35, ybase+140, 12, 12);
		fill(color(0, 0, 255));
		ellipse(xbase+35, ybase+160, 12, 12);
		fill(color(255, 0, 0));
		ellipse(xbase+35, ybase+180, 12, 12);
		
		textAlign(LEFT, CENTER);
		fill(0, 0, 0);
		text("Shallow", xbase+50, ybase+140);
		text("Intermediate", xbase+50, ybase+160);
		text("Deep", xbase+50, ybase+180);

		text("Past hour", xbase+50, ybase+200);
		
		fill(255, 255, 255);
		int centerx = xbase+35;
		int centery = ybase+200;
		ellipse(centerx, centery, 12, 12);

		strokeWeight(2);
		line(centerx-8, centery-8, centerx+8, centery+8);
		line(centerx-8, centery+8, centerx+8, centery-8);
		
		
	}

	
	
	// Checks whether this quake occurred on land.  If it did, it sets the 
	// "country" property of its PointFeature to the country where it occurred
	// and returns true.  Notice that the helper method isInCountry will
	// set this "country" property already.  Otherwise it returns false.
	private boolean isLand(PointFeature earthquake) {
		
		// IMPLEMENT THIS: loop over all countries to check if location is in any of them
		// If it is, add 1 to the entry in countryQuakes corresponding to this country.
		for (Marker country : countryMarkers) {
			if (isInCountry(earthquake, country)) {
				return true;
			}
		}
		
		// not inside any country
		return false;
	}
	
	// prints countries with number of earthquakes
	// You will want to loop through the country markers or country features
	// (either will work) and then for each country, loop through
	// the quakes to count how many occurred in that country.
	// Recall that the country markers have a "name" property, 
	// And LandQuakeMarkers have a "country" property set.
	private void printQuakes() {
		int totalWaterQuakes = quakeMarkers.size();
		for (Marker country : countryMarkers) {
			String countryName = country.getStringProperty("name");
			int numQuakes = 0;
			for (Marker marker : quakeMarkers)
			{
				EarthquakeMarker eqMarker = (EarthquakeMarker)marker;
				if (eqMarker.isOnLand()) {
					if (countryName.equals(eqMarker.getStringProperty("country"))) {
						numQuakes++;
					}
				}
			}
			if (numQuakes > 0) {
				totalWaterQuakes -= numQuakes;
				System.out.println(countryName + ": " + numQuakes);
			}
		}
		System.out.println("OCEAN QUAKES: " + totalWaterQuakes);
	}
	
	
	
	// helper method to test whether a given earthquake is in a given country
	// This will also add the country property to the properties of the earthquake feature if 
	// it's in one of the countries.
	// You should not have to modify this code
	private boolean isInCountry(PointFeature earthquake, Marker country) {
		// getting location of feature
		Location checkLoc = earthquake.getLocation();

		// some countries represented it as MultiMarker
		// looping over SimplePolygonMarkers which make them up to use isInsideByLoc
		if(country.getClass() == MultiMarker.class) {
				
			// looping over markers making up MultiMarker
			for(Marker marker : ((MultiMarker)country).getMarkers()) {
					
				// checking if inside
				if(((AbstractShapeMarker)marker).isInsideByLocation(checkLoc)) {
					earthquake.addProperty("country", country.getProperty("name"));
						
					// return if is inside one
					return true;
				}
			}
		}
			
		// check if inside country represented by SimplePolygonMarker
		else if(((AbstractShapeMarker)country).isInsideByLocation(checkLoc)) {
			earthquake.addProperty("country", country.getProperty("name"));
			
			return true;
		}
		return false;
	}

}