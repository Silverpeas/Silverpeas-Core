package com.silverpeas.jobStartPagePeas;

public class DisplaySorted extends Object implements Comparable {
  public static final int TYPE_UNKNOWN = 0;
  public static final int TYPE_COMPONENT = 1;
  public static final int TYPE_SPACE = 2;
  public static final int TYPE_SUBSPACE = 3;

  public String name = "";
  public int orderNum = 0;
  public String id = "";
  public String htmlLine = "";
  public int type = TYPE_UNKNOWN;
  public int deep = 0;
  public boolean isAdmin = true;
  public boolean isVisible = true;

  public int compareTo(Object o) {
    return orderNum - ((DisplaySorted) o).orderNum;
  }

  public void copy(DisplaySorted src) {
    name = src.name;
    orderNum = src.orderNum;
    id = src.id;
    htmlLine = src.htmlLine;
    type = src.type;
    deep = src.deep;
    isAdmin = src.isAdmin;
    isVisible = src.isVisible;
  }
};
