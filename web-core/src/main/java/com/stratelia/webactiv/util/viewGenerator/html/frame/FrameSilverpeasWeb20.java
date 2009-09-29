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
public class FrameSilverpeasWeb20 extends AbstractFrame {

  /**
   * Creates new FrameWA2
   */
  public FrameSilverpeasWeb20() {
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
    String iconsPath = getIconsPath();

    result += "<table width=\"100%\"  border=\"0\" cellspacing=\"0\" cellpadding=\"0\"><tr><td>\n";
    result += "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" class=\"tableFrame\">\n";
    result += "\t<tr>\n";
    result += "\t\t<td>\n";
    if (getTitle() != null) {
      result += "<span class=titreFenetre>\n";
      result += getTitle();
      result += "</span>\n";
    } else {
      result += "<table cellpadding=0 cellspacing=0 border=0><tr><td><img src=\""
          + iconsPath + "/1px.gif\" height=\"5\"></td></tr></table>\n";
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
    result += "\t\t<td>\n";

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
    result += "<tr><td><img src=\"" + iconsPath
        + "/1px.gif\" height=\"5\"></td></tr>\n";
    result += "</table>\n";
    result += "</td><td class=\"shadowFrame\" valign=\"top\"><img src=\""
        + iconsPath + "/pxFond.gif\" width=\"2\" align=\"top\"></td>\n";
    result += "</tr><tr>\n";
    result += "<td class=\"shadowFrame\" width=\"100%\"><img src=\""
        + iconsPath + "/pxFond.gif\" width=\"2\" align=\"left\"></td>\n";
    result += "<td class=\"shadowFrame\"><img src=\"" + iconsPath
        + "/1px.gif\"></td>\n";
    result += "</tr></table>\n";
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
