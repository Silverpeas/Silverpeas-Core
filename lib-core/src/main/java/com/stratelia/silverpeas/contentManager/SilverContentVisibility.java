package com.stratelia.silverpeas.contentManager;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SilverContentVisibility extends Object {
  private String beginDate = "0000/00/00";
  private String endDate = "9999/99/99";
  private boolean isVisible = true;

  // the date format used in database to represent a date
  static private SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

  public SilverContentVisibility(String beginDate, String endDate,
      boolean isVisible) {
    setVisibilityAttributes(beginDate, endDate);
    setVisibilityAttributes(isVisible);
  }

  public SilverContentVisibility(Date beginDate, Date endDate, boolean isVisible) {
    setVisibilityAttributes(beginDate, endDate);
    setVisibilityAttributes(isVisible);
  }

  public SilverContentVisibility(String beginDate, String endDate) {
    setVisibilityAttributes(beginDate, endDate);
    setVisibilityAttributes(true);
  }

  public SilverContentVisibility(Date beginDate, Date endDate) {
    setVisibilityAttributes(beginDate, endDate);
    setVisibilityAttributes(true);
  }

  public SilverContentVisibility(boolean isVisible) {
    setVisibilityAttributes(isVisible);
  }

  public SilverContentVisibility() {
  }

  public void setVisibilityAttributes(String beginDate, String endDate) {
    this.beginDate = beginDate;
    this.endDate = endDate;
  }

  public void setVisibilityAttributes(Date beginDate, Date endDate,
      boolean isVisible) {
    setVisibilityAttributes(beginDate, endDate);
    this.isVisible = isVisible;
  }

  public void setVisibilityAttributes(Date beginDate, Date endDate) {
    if (beginDate != null)
      this.beginDate = formatter.format(beginDate);

    if (endDate != null)
      this.endDate = formatter.format(endDate);
  }

  public void setVisibilityAttributes(boolean isVisible) {
    this.isVisible = isVisible;
  }

  public String getBeginDate() {
    return this.beginDate;
  }

  public String getEndDate() {
    return this.endDate;
  }

  public int isVisible() {
    if (this.isVisible)
      return 1;
    else
      return 0;
  }

}