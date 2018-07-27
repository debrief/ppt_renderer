package model;

import java.awt.*;
import java.util.ArrayList;

public class Track {
	private String name;
	private ArrayList<TrackPoint> segments;
	private Color color;

	public Track() {
		segments = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<TrackPoint> getSegments() {
		return segments;
	}

	public void setSegments(ArrayList<TrackPoint> segments) {
		this.segments = segments;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public void setColor(String colorStr) {
		String colors = colorStr.substring(colorStr.indexOf("[") + 1, colorStr.length() - 1);
		String[] temp = colors.split(",");
		int r = Integer.parseInt(temp[0].split("=")[1]);
		int g = Integer.parseInt(temp[1].split("=")[1]);
		int b = Integer.parseInt(temp[2].split("=")[1]);
		this.color = new Color(r, g, b);
	}

	/**
	 * Hexadecimal color in the format %02X%02X%02X lowerCased
	 *
	 * @return Hexadecimal color in the format %02X%02X%02X
	 */
	public String getColorAsString() {
		return String.format("%02X%02X%02X",
				this.color.getRed(),
				this.color.getGreen(),
				this.color.getBlue()).toLowerCase();
	}
}
