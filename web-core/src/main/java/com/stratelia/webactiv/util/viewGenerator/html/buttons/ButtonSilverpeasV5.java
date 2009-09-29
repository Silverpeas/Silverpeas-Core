package com.stratelia.webactiv.util.viewGenerator.html.buttons;

/**
 * 
 * @author neysseri
 * @version
 */
public class ButtonSilverpeasV5 extends AbstractButton {

  /**
   * Creates new ButtonWA
   */
  public ButtonSilverpeasV5() {
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String print() {
    String action = this.action;
    String iconsPath = getIconsPath();

    if (disabled) {
      action = "#";
    }

    StringBuffer str = new StringBuffer();
    str.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
    str.append("<tr>");
    str.append("<td align=\"left\" class=\"gaucheBoutonV5\"><img src=\"")
        .append(iconsPath).append("/px.gif\"></td>");
    str.append("<td nowrap class=\"milieuBoutonV5\"><a href=\"").append(action)
        .append("\">").append(label).append("</a></td>");
    str.append("<td align=\"right\" class=\"droiteBoutonV5\"><img src=\"")
        .append(iconsPath).append("/px.gif\"></td>");
    str.append("</tr>");
    str.append("</table>");

    return str.toString();
  }

}