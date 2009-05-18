/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/*
 * CVS Informations
 * 
 * $Id: ArrayCellRadio.java,v 1.2 2008/09/17 04:40:17 neysseri Exp $
 * 
 * $Log: ArrayCellRadio.java,v $
 * Revision 1.2  2008/09/17 04:40:17  neysseri
 * no message
 *
 * Revision 1.1.1.1.34.1  2008/09/10 14:35:51  psliwa
 * New component: Workflow Designer
 *
 * Revision 1.1  2008/05/30 16:17:29  cvsuser
 * *** empty log message ***
 *
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
public class ArrayCellRadio extends ArrayCell implements SimpleGraphicElement
{

    // -----------------------------------------------------------------------------------------------------------------
    // Attributs
    // -----------------------------------------------------------------------------------------------------------------
    private String  name;
    private String  value = null;
    private boolean checked = false;
    private String  cellAlign = null;
    private String  action = null; // Action javaScript

    private String  syntax = "";

    // -----------------------------------------------------------------------------------------------------------------
    // Constructeur
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Constructor declaration
     * 
     * 
     * @param name
     * @param value
     * @param checked
     * @param line
     * 
     * @see
     */
    public ArrayCellRadio(String name, String value, boolean checked, ArrayLine line)
    {
        super(line);
        this.name = name;
        this.value = value;
        this.checked = checked;
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
    public String getCellAlign()
    {
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
    public void setCellAlign(String cellAlign)
    {
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
    public String getName()
    {
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
    public String getValue()
    {
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
    public boolean getChecked()
    {
        return checked;
    }

    /**
     * Get the JavaScript action
     */
    public String getAction() 
    {
        return action;
    }

    /**
     * Set the JavaScript action e.g. "onLoad=doOnLoad();"
     * 
     * @param strAction the event name and the action
     */
    public void setAction(String strAction) 
    {
        action = strAction;
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
    public String getSyntax()
    {

        syntax += " <input style=\"background-color:#FFFFFF;\" type=\"radio\" name=\"";

        // param name
        if (getName() == null)
        {
            syntax += "radio\" value=\"";
        }
        else
        {
            syntax += getName() + "\" value=\"";
        }

        // param value
        if (getValue() == null)
        {
            syntax += "radio\"";
        }
        else
        {
            syntax += getValue() + "\"";
        }

        // param action JavaScript
        if (getAction() != null) 
        {
            syntax += " " + getAction();
        }

        // param activate
        if (getChecked() == true)
        {
            syntax += " checked";
        }

        syntax += ">";

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
    public String print()
    {
        String result = "<td ";

        if (getCellAlign() != null)
        {
            if (getCellAlign().equalsIgnoreCase("center") || getCellAlign().equalsIgnoreCase("right"))
            {
                result += " align=\"" + getCellAlign() + "\"";
            }
        }

        result += " >";

        result += getSyntax();

        result += "</td>\n";
        return result;
    }

}
