package org.silverpeas.core.web.util.viewgenerator.html.arraypanes;

/**
 * An array cell supporting javascript action on events.
 * @author mmoquillon
 */
public abstract class ActionableArrayCell extends ArrayCell {

  private String action;

  public ActionableArrayCell(String name,ArrayLine line) {
    super(name, line);
  }

  public String getAction() {
    return action;
  }

  public void setAction(String action) {
    this.action = action;
  }
}
  