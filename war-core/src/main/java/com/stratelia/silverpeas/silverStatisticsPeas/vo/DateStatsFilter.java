package com.stratelia.silverpeas.silverStatisticsPeas.vo;

public abstract class DateStatsFilter {

  private String monthBegin = null;
  private String yearBegin = null;
  private String monthEnd = null;
  private String yearEnd = null;

  /**
   * @param monthBegin
   * @param yearBegin
   * @param monthEnd
   * @param yearEnd
   */
  public DateStatsFilter(String monthBegin, String yearBegin, String monthEnd, String yearEnd) {
    super();
    this.monthBegin = monthBegin;
    this.yearBegin = yearBegin;
    this.monthEnd = monthEnd;
    this.yearEnd = yearEnd;
  }

  /**
   * @return the monthBegin
   */
  public final String getMonthBegin() {
    return monthBegin;
  }

  /**
   * @param monthBegin the monthBegin to set
   */
  public final void setMonthBegin(String monthBegin) {
    this.monthBegin = monthBegin;
  }

  /**
   * @return the yearBegin
   */
  public final String getYearBegin() {
    return yearBegin;
  }

  /**
   * @param yearBegin the yearBegin to set
   */
  public final void setYearBegin(String yearBegin) {
    this.yearBegin = yearBegin;
  }

  /**
   * @return the monthEnd
   */
  public final String getMonthEnd() {
    return monthEnd;
  }

  /**
   * @param monthEnd the monthEnd to set
   */
  public final void setMonthEnd(String monthEnd) {
    this.monthEnd = monthEnd;
  }

  /**
   * @return the yearEnd
   */
  public final String getYearEnd() {
    return yearEnd;
  }

  /**
   * @param yearEnd the yearEnd to set
   */
  public final void setYearEnd(String yearEnd) {
    this.yearEnd = yearEnd;
  }


  
}
