package model;

import java.util.ArrayList;

/**
 * Track data to be added to the pptx file.
 */
public class TrackData {
	private int height;
	private int width;
	private int intervals;
	private String name;
	private ArrayList<NarrativeEntry> narrativeEntries;
	private ArrayList<Track> tracks;

	public TrackData() {
		narrativeEntries = new ArrayList<>();
		tracks = new ArrayList<>();
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getIntervals() {
		return intervals;
	}

	public void setIntervals(int intervals) {
		this.intervals = intervals;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<NarrativeEntry> getNarrativeEntries() {
		return narrativeEntries;
	}

	public void setNarrativeEntries(ArrayList<NarrativeEntry> narrativeEntries) {
		this.narrativeEntries = narrativeEntries;
	}

	public ArrayList<Track> getTracks() {
		return tracks;
	}

	public void setTracks(ArrayList<Track> tracks) {
		this.tracks = tracks;
	}
}
