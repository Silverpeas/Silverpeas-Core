/*
 * Created by IntelliJ IDEA.
 * User: mikhail_nikolaenko
 * Date: Aug 15, 2002
 * Time: 3:36:36 PM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.stratelia.webactiv.searchEngine.model;

import java.util.List;
import java.util.ArrayList;

public class AxisFilter {
  public static final String NAME = "NAME";
  public static final String DESCRIPTION = "DESCRIPTION";

  private List filter = (List) new ArrayList(1);
  private int index = -1;

  public AxisFilter() {
  }

  public AxisFilter(String property, String value) {
    this(new AxisFilterNode(property, value));
  }

  public AxisFilter(AxisFilterNode filter_node) {
    filter.add(filter_node);
  }

  public void addCondition(String property, String value) {
    addCondition(new AxisFilterNode(property, value));
  }

  public void addCondition(AxisFilterNode filter_node) {
    filter.add(filter_node);
  }

  public AxisFilterNode getFirstCondition() {
    if (filter.size() > 0) {
      index = 0;
      return (AxisFilterNode) filter.get(0);
    } else {
      return null;
    }
  }

  public AxisFilterNode getNextCondition() {
    if (++index < filter.size()) {
      return (AxisFilterNode) filter.get(index);
    } else {
      return null;
    }
  }

  public int size() {
    return filter.size();
  }
}
