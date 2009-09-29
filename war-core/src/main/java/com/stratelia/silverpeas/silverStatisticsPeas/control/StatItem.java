/*
 * Created on 13 juil. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.stratelia.silverpeas.silverStatisticsPeas.control;

/**
 * @author BERTINL
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class StatItem {
  private long[] countValues;
  private String cmpId;
  private String name;

  public StatItem(String cmpId, String name, long[] countValues) {
    this.cmpId = cmpId;
    this.name = name;
    this.countValues = countValues;
  }

  /**
   * @return Returns the CountValues.
   */
  public long[] getCountValues() {
    return this.countValues;
  }

  /**
   * @return Returns the name.
   */
  public String getName() {
    return name;
  }

  /**
   * @return Returns the cmpId.
   */
  public String getCmpId() {
    return cmpId;
  }
}
