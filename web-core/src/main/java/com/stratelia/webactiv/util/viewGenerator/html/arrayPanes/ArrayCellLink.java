/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/*
 * CVS Informations
 * 
 * $Id: ArrayCellLink.java,v 1.3 2004/03/17 14:51:19 neysseri Exp $
 * 
 * $Log: ArrayCellLink.java,v $
 * Revision 1.3  2004/03/17 14:51:19  neysseri
 * no message
 *
 * Revision 1.2  2002/10/17 13:34:51  neysseri
 * Glossary report from VSIC to KMedition
 *
 * Revision 1.1.1.1  2002/08/06 14:48:19  nchaix
 * no message
 *
 * Revision 1.5  2002/01/04 14:04:23  mmarengo
 * Stabilisation Lot 2
 * SilverTrace
 * Exception
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class ArrayCellLink extends ArrayCell implements SimpleGraphicElement,
    Comparable {

  private String text = null;
  private String alignement = null;
  private String valignement = null;
  private String color = null;
  private String link = null;
  private String info = null;
  private String target = null;

  /**
   * Constructor declaration
   * 
   * 
   * @param text
   * @param link
   * @param line
   * 
   * @see
   */
  public ArrayCellLink(String text, String link, ArrayLine line) {
    super(line);
    this.text = text;
    this.link = link;
  }

  /**
   * Constructor declaration
   * 
   * 
   * @param text
   * @param link
   * @param info
   * @param line
   * 
   * @see
   */
  public ArrayCellLink(String text, String link, String info, ArrayLine line) {
    super(line);
    this.text = text;
    this.link = link;
    this.info = info;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getText() {
    return text;
  }

  /**
   * @return
   */
  public String getAlignement() {
    return alignement;
  }

  /**
   * @param CellAlign
   */
  public void setAlignement(String alignement) {
    this.alignement = alignement;
  }

  public String getValignement() {
    return valignement;
  }

  /**
   * @param CellAlign
   */
  public void setValignement(String valignement) {
    this.valignement = valignement;
  }

  /**
   * @return
   */
  public String getColor() {
    return color;
  }

  /**
   * @param CellAlign
   */
  public void setColor(String color) {
    this.color = color;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getLink() {
    return link;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getInfo() {
    return info;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  /**
   * Method declaration
   * 
   * 
   * @param other
   * 
   * @return
   * 
   * @see
   */
  public int compareTo(final java.lang.Object other) {
    if (other instanceof ArrayEmptyCell) {
      return 1;
    }
    if (!(other instanceof ArrayCellLink)) {
      return 0;
    }
    ArrayCellLink tmp = (ArrayCellLink) other;

    // return this.getText().compareTo(tmp.getText());
    return this.getText().compareToIgnoreCase(tmp.getText());
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
    result.append("<td");

    if (getAlignement() != null) {
      if (getAlignement().equalsIgnoreCase("center")
          || getAlignement().equalsIgnoreCase("right")) {
        result.append(" align=\"").append(getAlignement()).append("\"");
      }
    }

    if (getValignement() != null) {
      if (getValignement().equalsIgnoreCase("bottom")
          || getValignement().equalsIgnoreCase("top")
          || getValignement().equalsIgnoreCase("baseline")) {
        result.append(" valign=\"").append(getValignement()).append("\"");
      }
    }

    result.append(" class=\"").append(getStyleSheet()).append("\">");

    if (getColor() != null) {
      result.append(" <font color=\"").append(getColor()).append("\">");
    }

    result.append("<a class=\"").append(getStyleSheet()).append("\" ");
    result.append("href=\"").append(getLink()).append("\"");

    if (getTarget() != null)
      result.append(" target=\"").append(getTarget()).append("\"");

    result.append(">");
    result.append(getText());
    result.append("</a>");

    if (getColor() != null) {
      result.append("</font>");
    }

    result.append("</td>\n");
    return result.toString();
  }

}
