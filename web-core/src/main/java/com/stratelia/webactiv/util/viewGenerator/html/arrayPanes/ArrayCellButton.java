/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/*
 * CVS Informations
 * 
 * $Id: ArrayCellButton.java,v 1.1.1.1 2002/08/06 14:48:19 nchaix Exp $
 * 
 * $Log: ArrayCellButton.java,v $
 * Revision 1.1.1.1  2002/08/06 14:48:19  nchaix
 * no message
 *
 * Revision 1.3  2002/01/04 14:04:23  mmarengo
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
public class ArrayCellButton extends ArrayCell implements SimpleGraphicElement {

  // -----------------------------------------------------------------------------------------------------------------
  // Attributs
  // -----------------------------------------------------------------------------------------------------------------
  private String name;
  private String value = null;
  private boolean activate = true;
  private String cellAlign = null;

  private String syntax = "";

  // -----------------------------------------------------------------------------------------------------------------
  // Constructeur
  // -----------------------------------------------------------------------------------------------------------------

  /**
   * Constructor declaration
   * 
   * 
   * @param name
   * @param value
   * @param activate
   * @param line
   * 
   * @see
   */
  public ArrayCellButton(String name, String value, boolean activate,
      ArrayLine line) {
    super(line);
    this.name = name;
    this.value = value;
    this.activate = activate;
  }

  // -----------------------------------------------------------------------------------------------------------------
  // Méthodes
  // -----------------------------------------------------------------------------------------------------------------

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getCellAlign() {
    return cellAlign;
  }

  /**
   * Method declaration
   * 
   * 
   * @param cellAlign
   * 
   * @see
   */
  public void setCellAlign(String cellAlign) {
    this.cellAlign = cellAlign;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getName() {
    return name;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getValue() {
    return value;
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public boolean getActivate() {
    return activate;
  }

  // -----------------------------------------------------------------------------------------------------------------
  // Ecriture de l'input en fonction de son type, de sa valeur et de son nom
  // -----------------------------------------------------------------------------------------------------------------

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String getSyntax() {

    syntax += " <input style=\"background-color:#D8D8D8;\" type=\"button\" name=\"";

    // param name
    if (getName() == null) {
      syntax += "boutton\" value=\"";
    } else {
      syntax += getName() + "\" value=\"";
    }

    // param value
    if (getValue() == null) {
      syntax += "\"";
    } else {
      syntax += getValue() + "\"";
    }

    // param activate
    if (getActivate() == false) {
      syntax += " disabled";
    }

    syntax += "\">";

    return syntax;
  }

  // -----------------------------------------------------------------------------------------------------------------

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String print() {
    String result = "<td ";

    if (getCellAlign() != null) {
      if (getCellAlign().equalsIgnoreCase("center")
          || getCellAlign().equalsIgnoreCase("right")) {
        result += " align=\"" + getCellAlign() + "\"";
      }
    }

    result += " >";

    result += getSyntax();

    result += "</td>\n";
    return result;
  }

}
