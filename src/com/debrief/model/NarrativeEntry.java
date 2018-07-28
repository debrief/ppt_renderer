package com.debrief.model;

public class NarrativeEntry
{
  private final String text;
  private final String date;
  private final String elapsed;

  public NarrativeEntry(String text, String date, String elapsed)
  {
    this.text = text;
    this.date = date;
    this.elapsed= elapsed;
  }

  public String getDate()
  {
    return date;
  }

  public String getElapsed()
  {
    return elapsed;
  }

  public String getText()
  {
    return text;
  }
}
