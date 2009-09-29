package com.stratelia.silverpeas.pdc.model;

//import java.util.ArrayList;
import java.util.List;

import com.stratelia.silverpeas.classifyEngine.PertinentAxis;

/**
 * @author Nicolas EYSSERIC
 */
public class SearchAxis extends PertinentAxis implements java.io.Serializable {
  // private String axisName = null;
  private int axisRootId = -1;

  private AxisHeader axis = null;

  private List values = null;

  public SearchAxis(int axisId, int nbObjects) {
    super();
    setAxisId(axisId);
    setNbObjects(nbObjects);
  }

  public String getAxisName() {
    return axis.getName();
  }

  public String getAxisName(String lang) {
    return axis.getName(lang);
  }

  /*
   * public void setAxisName(String axisName) { this.axisName = axisName; }
   */

  public void setAxis(AxisHeader axis) {
    this.axis = axis;
  }

  public int getAxisRootId() {
    return axisRootId;
  }

  public void setAxisRootId(int rootId) {
    axisRootId = rootId;
  }

  /**
   * @return a List of SearchValue
   */
  public List getValues() {
    return values;
  }

  /**
   * @param list
   */
  public void setValues(List list) {
    values = list;
  }

  /*
   * public void addValue(SearchValue value) { if (values == null) values = new
   * ArrayList();
   * 
   * values.add(value); }
   */

}