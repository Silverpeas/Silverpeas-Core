/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * FrameWA2.java
 * 
 * Created on 27 mars 2001, 15:22
 */

package com.stratelia.webactiv.util.viewGenerator.html.frame;

/**
 * 
 * @author mraverdy&lloiseau
 * @version 1.0
 */
public class FrameWA2 extends AbstractFrame {

  /**
   * Creates new FrameWA2
   */
  public FrameWA2() {
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
    StringBuffer result = new StringBuffer(FrameWA2StringFactory
        .getPrintBeforeString());
    String iconsPath = getIconsPath();

    /*
     * result.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n"
     * ); result.append("\t<tr>\n"); result.append("\t\t<td>\n");result.append(
     * "\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n"
     * ); result.append("\t\t\t\t<tr>\n"); result.append("\t\t\t\t\t<td>\n");
     * result.append(
     * "\t\t\t\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n"
     * ); result.append("\t\t\t\t\t\t\t<tr>\n");result.append(
     * "\t\t\t\t\t\t\t\t<td rowspan=\"2\" class=intfdcolor11 width=\"1\"><img src=\""
     * )
     * .append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result
     * .append("\t\t\t\t\t\t\t\t<td colspan=\"2\" class=intfdcolor11><img src=\""
     * )
     * .append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result
     * .append("\t\t\t\t\t\t\t\t<td rowspan=\"2\"><img src=\"").append(iconsPath
     * ).append("/frame/htdroit.gif\" width=\"6\" height=\"5\"></td>\n");
     * result.append("\t\t\t\t\t\t\t</tr>\n");
     * result.append("\t\t\t\t\t\t\t<tr>\n");
     * result.append("\t\t\t\t\t\t\t\t<td class=intfdcolor4 width=\"1\"><img src=\""
     * )
     * .append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result.append(
     * "\t\t\t\t\t\t\t\t<td class=intfdcolor51 width=\"100%\" height=\"4\"><img src=\""
     * )
     * .append(iconsPath).append("/1px.gif\" width=\"1\" height=\"4\"></td>\n");
     * result.append("\t\t\t\t\t\t\t</tr>\n");
     * result.append("\t\t\t\t\t\t</table>\n");
     * result.append("\t\t\t\t\t</td>\n"); result.append("\t\t\t\t</tr>\n");
     * result.append("\t\t\t</table>\n");result.append(
     * "\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n"
     * ); result.append("\t\t\t\t<tr>\n");
     * result.append("\t\t\t\t\t<td class=intfdcolor11 width=\"1\"><img src=\""
     * ).
     * append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result
     * .append("\t\t\t\t\t<td class=intfdcolor4 width=\"1\"><img src=\"").append
     * (iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result.append("\t\t\t\t\t<td class=intfdcolor51 width=\"100%\">");
     */

    if (getTitle() != null) {
      result.append("<span class=titreFenetre>\n");
      result.append(getTitle());
      result.append("</span>\n");
    } else {
      result.append(
          "<table cellpadding=0 cellspacing=0 border=0><tr><td><img src=\"")
          .append(iconsPath).append("/1px.gif\"></td></tr></table>\n");
    }

    return result.toString();

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
    StringBuffer result = new StringBuffer(FrameWA2StringFactory
        .getPrintMiddleString());
    // String iconsPath = getIconsPath();

    /*
     * result.append("</td>\n");
     * result.append("\t\t\t\t\t<td class=intfdcolor11 width=\"1\"><img src=\""
     * ).
     * append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result
     * .append("\t\t\t\t\t<td class=intfdcolor12 width=\"1\"><img src=\"").
     * append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result.append("\t\t\t\t</tr>\n"); result.append("\t\t\t</table>\n");
     * result.append(
     * "\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n"
     * ); result.append("\t\t\t\t<tr>\n");
     * result.append("\t\t\t\t\t<td class=intfdcolor11 width=\"1\"><img src=\""
     * ).
     * append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result
     * .append("\t\t\t\t\t<td class=intfdcolor4 width=\"1\"><img src=\"").append
     * (iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result.append("\t\t\t\t\t<td class=intfdcolor51 width=\"100%\">\n");
     * result.append("\t\t\t\t\t");
     */

    setMiddle();
    return result.toString();
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
    StringBuffer result = new StringBuffer();
    // String iconsPath = getIconsPath();

    if (!hasMiddle()) {
      result.append(printMiddle());

    }

    result.append(FrameWA2StringFactory.getPrintAfterString());

    /*
     * result.append("\t\t\t\t\t</td>\n");
     * result.append("\t\t\t\t\t<td class=intfdcolor11 width=\"1\"><img src=\""
     * ).
     * append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result
     * .append("\t\t\t\t\t<td class=intfdcolor12 width=\"1\"><img src=\"").
     * append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result.append("\t\t\t\t</tr>\n"); result.append("\t\t\t</table>\n");
     * result.append(
     * "\t\t\t<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n"
     * ); result.append("\t\t\t\t<tr>\n");result.append(
     * "\t\t\t\t\t<td rowspan=\"2\" class=intfdcolor11 width=\"1\"><img src=\""
     * ).
     * append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result
     * .append("\t\t\t\t\t<td class=intfdcolor4 width=\"1\"><img src=\"").append
     * (iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result.append(
     * "\t\t\t\t\t<td class=intfdcolor51 width=\"100%\" height=\"100%\"><img src=\""
     * )
     * .append(iconsPath).append("/1px.gif\" width=\"1\" height=\"4\"></td>\n");
     * result
     * .append("\t\t\t\t\t<td rowspan=\"3\"><img src=\"").append(iconsPath)
     * .append("/frame/bsdroit.gif\" width=\"6\" height=\"6\"></td>\n");
     * result.append("\t\t\t\t</tr>\n"); result.append("\t\t\t\t<tr>\n");
     * result.append(
     * "\t\t\t\t\t<td colspan=\"2\" class=intfdcolor11 height=\"1\"><img src=\""
     * )
     * .append(iconsPath).append("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result.append("\t\t\t\t</tr>\n"); result.append("\t\t\t\t<tr>\n");
     * result.
     * append("\t\t\t\t\t<td width=\"1\"><img src=\"").append(iconsPath).append
     * ("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result.append("\t\t\t\t\t<td width=\"1\"><img src=\""
     * ).append(iconsPath).append
     * ("/1px.gif\" width=\"1\" height=\"1\"></td>\n");
     * result.append("\t\t\t\t\t<td class=intfdcolor12 height=\"2\"><img src=\""
     * )
     * .append(iconsPath).append("/1px.gif\" width=\"1\" height=\"2\"></td>\n");
     * result.append("\t\t\t\t</tr>\n"); result.append("\t\t\t</table>\n");
     * result.append("\t\t</td>\n"); result.append("\t</tr>\n");
     * result.append("</table>\n");
     */

    return result.toString();
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
    StringBuffer result = new StringBuffer();

    result.append(printBefore());
    if (getTop() != null) {
      result.append(getTop());
    }
    result.append(printMiddle());
    if (getBottom() != null) {
      result.append(getBottom());
    }
    result.append(printAfter());

    return result.toString();
  }

}