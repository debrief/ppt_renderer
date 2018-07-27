package model;

import java.util.ArrayList;

/**
 * Track data to be added to the pptx file.
 */
public class TrackData
{
  private int height;
  private int width;
  private int intervals;
  private String name;
  private final ArrayList<NarrativeEntry> narrativeEntries  = new ArrayList<>();
  private final ArrayList<Track> tracks = new ArrayList<>();

  public int getHeight()
  {
    return height;
  }

  public int getIntervals()
  {
    return intervals;
  }

  public String getName()
  {
    return name;
  }

  public ArrayList<NarrativeEntry> getNarrativeEntries()
  {
    return narrativeEntries;
  }

  public ArrayList<Track> getTracks()
  {
    return tracks;
  }

  public int getWidth()
  {
    return width;
  }

  public void setHeight(final int height)
  {
    this.height = height;
  }

  public void setIntervals(final int intervals)
  {
    this.intervals = intervals;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  public void setWidth(final int width)
  {
    this.width = width;
  }
}
