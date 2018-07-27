package model;

import java.time.LocalDateTime;

public class TrackPoint
{
  private float latitude;
  private float longitude;
  private float elevation;
  private LocalDateTime time;
  private float course;
  private float speed;

  public TrackPoint()
  {
  }

  public float getCourse()
  {
    return course;
  }

  public float getElevation()
  {
    return elevation;
  }

  public float getLatitude()
  {
    return latitude;
  }

  public float getLongitude()
  {
    return longitude;
  }

  public float getSpeed()
  {
    return speed;
  }

  public LocalDateTime getTime()
  {
    return time;
  }

  public void setCourse(final float course)
  {
    this.course = course;
  }

  public void setElevation(final float elevation)
  {
    this.elevation = elevation;
  }

  public void setLatitude(final float latitude)
  {
    this.latitude = latitude;
  }

  public void setLongitude(final float longitude)
  {
    this.longitude = longitude;
  }

  public void setSpeed(final float speed)
  {
    this.speed = speed;
  }

  public void setTime(final LocalDateTime time)
  {
    this.time = time;
  }
}
