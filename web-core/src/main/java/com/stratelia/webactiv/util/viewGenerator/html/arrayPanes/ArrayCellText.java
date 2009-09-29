/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/*
 * CVS Informations
 * 
 * $Id: ArrayCellText.java,v 1.4 2005/08/18 11:14:57 neysseri Exp $
 * 
 * $Log: ArrayCellText.java,v $
 * Revision 1.4  2005/08/18 11:14:57  neysseri
 * no message
 *
 * Revision 1.3.4.1  2005/07/21 11:51:09  neysseri
 * no message
 *
 * Revision 1.3  2004/03/17 14:51:19  neysseri
 * no message
 *
 * Revision 1.2  2002/10/17 13:34:51  neysseri
 * Glossary report from VSIC to KMedition
 *
 * Revision 1.1.1.1  2002/08/06 14:48:19  nchaix
 * no message
 *
 * Revision 1.6  2002/01/04 14:04:23  mmarengo
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
public class ArrayCellText extends ArrayCell implements SimpleGraphicElement,
    Comparable {
  private String text;
  private String alignement = null;
  private String color = null;
  private String valignement = null;
  private boolean noWrap = false;

  private Comparable compareOn = null;

  /**
   * Constructor declaration
   * 
   * 
   * @param text
   * @param line
   * 
   * @see
   */
  public ArrayCellText(String text, ArrayLine line) {
    super(line);
    this.text = text;
  }

  /**
   * @return
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
   * @param textAlign
   */
  public void setAlignement(String alignement) {
    this.alignement = alignement;
  }

  /**
   * @return
   */
  public boolean getNoWrap() {
    return noWrap;
  }

  /**
   * @param noWrap
   */
  public void setNoWrap(boolean noWrap) {
    this.noWrap = noWrap;
  }

  /**
   * @return
   */
  public String getColor() {
    return color;
  }

  /**
   * @param textAlign
   */
  public void setColor(String color) {
    this.color = color;
  }

  public String getValignement() {
    return valignement;
  }

  /**
   * @param textValign
   */
  public void setValignement(String valignement) {
    this.valignement = valignement;
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

    result.append("<td ");

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

    if (getNoWrap()) {
      result.append(" nowrap");
    }

    result.append(" class=\"").append(getStyleSheet()).append("\">");

    if (getColor() != null) {
      result.append(" <font color=\"").append(getColor()).append("\">");
      result.append(getText());
      result.append("</font>");
    } else {
      result.append(getText());
    }

    result.append("</td>\n");
    return result.toString();
  }

  /**
   * Method declaration
   * 
   * 
   * @param object
   * 
   * @see
   */
  public void setCompareOn(Comparable object) {
    this.compareOn = object;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public Comparable getCompareOn() {
    return this.compareOn;
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
    if (!(other instanceof ArrayCellText)) {
      return 0;
    }
    ArrayCellText tmp = (ArrayCellText) other;

    if (getCompareOn() != null) {
      if (tmp.getCompareOn() != null) {
        return getCompareOn().compareTo(tmp.getCompareOn());
      }
    }
    // if(m_SortMode == ArrayCell.CELLSORT_CASE_INSENSITIVE)
    // {
    return this.getText().compareToIgnoreCase(tmp.getText());
    // }
    // else
    // {
    // return this.getText().compareTo(tmp.getText());
    // }
  }

}
