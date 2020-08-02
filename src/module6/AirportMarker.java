package module6;

import java.util.List;

import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PConstants;
import processing.core.PGraphics;

/** 
 * A class to represent AirportMarkers on a world map.
 *   
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMarker extends CommonMarker {
	public static List<SimpleLinesMarker> routes;
	
	public AirportMarker(Feature city) {
		super(((PointFeature)city).getLocation(), city.getProperties());
	
	}
	
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		pg.fill(11);
		pg.ellipse(x, y, 5, 5);
		// show routes
		if (getClicked()) {
			pg.pushStyle();
			pg.stroke(255,0,0);
			List<ScreenPosition> tc = (List<ScreenPosition>) getProperty("destinationCities");
			for (ScreenPosition sp : tc) {
				pg.line(sp.x-200,sp.y-50,x,y);
			}
			pg.popStyle();
		}
	}

	@Override
	public void showTitle(PGraphics pg, float x, float y) {
		// show rectangle with title
		String title = getName();
		String title1 = title.substring(1);
		String title2 = title1.substring(0, title1.length() - 1);
		
		pg.pushStyle();
		
		pg.rectMode(PConstants.CORNER);
		
		pg.stroke(110);
		pg.fill(255,255,255);
		pg.rect(x, y + 15, pg.textWidth(title2) +6, 18, 5);
		
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.fill(0);
		pg.text(title2, x + 3 , y +18);
		
		
		pg.popStyle();
	}
	
	public String getCity() {
		return getStringProperty("city");
	}
	
	public String getName() {
		return getStringProperty("name");
	}
	
}
