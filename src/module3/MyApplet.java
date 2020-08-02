package module3;
import processing.core.*;

public class MyApplet extends PApplet{
	private PImage backgroundImage;
	private String URL = "https://www.processing.org/img/processing3-logo.png";
	
	public void setup() {
		// Set canvas size
		size(200, 200);
		
		// Load the background image
		backgroundImage = loadImage(URL, "png");
		
	}
	
	public void draw() {
		image(backgroundImage, 0, 0);
	}
}
