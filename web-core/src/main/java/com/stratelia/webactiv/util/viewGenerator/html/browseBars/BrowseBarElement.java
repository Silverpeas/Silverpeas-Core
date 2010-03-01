package com.stratelia.webactiv.util.viewGenerator.html.browseBars;

public class BrowseBarElement {

  private String label;
  private String link;
  private String id;

  public BrowseBarElement(String label, String link) {
    this(label, link, null);
  }

  public BrowseBarElement(String label, String link, String id) {
    this.label = label;
    this.link = link;
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public String getLink() {
    return link;
  }

  public String getId() {
    return id;
  }


}
