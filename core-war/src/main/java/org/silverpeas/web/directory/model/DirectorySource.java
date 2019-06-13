package org.silverpeas.web.directory.model;

public class DirectorySource {

  private String id;
  private String label;
  private String description;
  private boolean selected;

  public DirectorySource(final String id, final String label, String description) {
    this.id = id;
    this.label = label;
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(final String id) {
    this.id = id;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(final String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(final boolean selected) {
    this.selected = selected;
  }

  public boolean isContactsComponent() {
    return id.startsWith("yellow");
  }
}
