package com.stratelia.silverpeas.silverStatisticsPeas.vo;

import org.apache.commons.collections.ListUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nicolas Eysseric
 */
public class ChartVO {

  private String title;
  private List<String> x;
  private List<Long> y;

  public ChartVO(String title, List<String> x) {
    setTitle(title);
    setX(x);
    setY(y);
  }

  public ChartVO(String title, List<String> x, List<Long> y) {
    setTitle(title);
    setX(x);
    setY(y);
  }

  public ChartVO(String title, String[] x, Long[] y) {
    setTitle(title);
    setX(Arrays.asList(x));
    setY(Arrays.asList(y));
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

  public List<String> getX() {
    return x;
  }

  public void setX(final List<String> x) {
    this.x = x;
  }

  public List<Long> getY() {
    return y;
  }

  public void setY(final List<Long> y) {
    this.y = y;
  }

  public void setYAsStrings(final List<String> y) {
    List<Long> res = new ArrayList<Long>();
    for (String str : y) {
      res.add(Long.valueOf(str));
    }
    setY(res);
  }
}
