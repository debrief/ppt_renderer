package com.debrief.model;

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

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + height;
    result = prime * result + intervals;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((narrativeEntries == null) ? 0 : narrativeEntries
        .hashCode());
    result = prime * result + ((tracks == null) ? 0 : tracks.hashCode());
    result = prime * result + width;
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    TrackData other = (TrackData) obj;
    if (height != other.height)
      return false;
    if (intervals != other.intervals)
      return false;
    if (name == null)
    {
      if (other.name != null)
        return false;
    }
    else if (!name.equals(other.name))
      return false;
    if (narrativeEntries == null)
    {
      if (other.narrativeEntries != null)
        return false;
    }
    else if (!narrativeEntries.equals(other.narrativeEntries))
      return false;
    if (tracks == null)
    {
      if (other.tracks != null)
        return false;
    }
    else if (!tracks.equals(other.tracks))
      return false;
    if (width != other.width)
      return false;
    return true;
  }
}
