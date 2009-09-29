/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * FrameSogreah.java
 * 
 * Created on 27 mars 2001, 15:22
 */

package com.stratelia.webactiv.util.viewGenerator.html.frame;

/**
 * 
 * @author mraverdy&lloiseau
 * @version 1.0
 */
public class FrameSilverpeasV5 extends AbstractFrame {

  /**
   * Creates new FrameWA2
   */
  public FrameSilverpeasV5() {
    super();
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String printBefore() {
    String result = "";

    result += "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"tableFrame\">\n";
    result += "\t<tr>\n";
    result += "\t\t<td colspan=\"3\" class=\"hautFrame\">\n";
    if (getTitle() != null) {
      result += "<span class=\"titreFenetre\">\n";
      result += getTitle();
      result += "</span>\n";
    }

    return result;

  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String printMiddle() {
    String result = "";

    result += "\t\t</td>\n";
    result += "\t</tr>\n";
    result += "\t<tr>\n";
    result += "\t\t<td colspan=\"3\" class=\"milieuFrame\">\n";

    setMiddle();
    return result;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String printAfter() {
    String result = "";
    String iconsPath = getIconsPath();

    if (!hasMiddle()) {
      result += printMiddle();

    }

    result += "\t\t</td>\n";
    result += "\t</tr>\n";
    result += "<tr>\n";
    result += "\t\t<td class=\"basGaucheFrame\"><img src=\"" + iconsPath
        + "/1px.gif\" height=\"15\"></td>\n";
    result += "\t\t<td class=\"basMilieuFrame\"><img src=\"" + iconsPath
        + "/1px.gif\" height=\"15\"></td>\n";
    result += "\t\t<td class=\"basDroiteFrame\"><img src=\"" + iconsPath
        + "/1px.gif\" height=\"15\"></td>\n";
    result += "\t</tr>\n";
    result += "</table>\n";
    return result;
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
    String result = "";

    result += printBefore();
    if (getTop() != null) {
      result += getTop();
    }
    result += printMiddle();
    if (getBottom() != null) {
      result += getBottom();
    }
    result += printAfter();

    return result;
  }

}
