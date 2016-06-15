package nl.wtcw.vle.datafluo.core.util;

import java.util.Date;
import java.text.SimpleDateFormat;

public class TimePoint {

  private Date tPoint = null;
  private SimpleDateFormat longsdf = null;
  private SimpleDateFormat shortsdf = null;

  /**
   * This constructor will initialise the stored time to time of creation
   */
  public TimePoint() {
    tPoint = new Date();
    longsdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss:SS");
    //shortsdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
	shortsdf = new SimpleDateFormat("HH:mm:ss");
  }

  /**
   * This method provides the time that this object represents in a string including millisecs
   * @return string that represents the time.
   */
  public String getString() {
    return longsdf.format(tPoint);
  }

  /**
   * This method provides the time that this object represents in a short format string excluding millisecs
   * @return string that represents the time.
   */
  public String getShortString() {
    return shortsdf.format(tPoint);
  }

  /**
   * This method provides the time between this timepoint and
   * the epoch.
   * @return the time in milliseconds elapsed since the epoch.
   */
  public long getMillisecs() {
    return tPoint.getTime();
  }

}
