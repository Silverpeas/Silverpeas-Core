/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.util.viewGenerator.html.arrayPanes;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * 
 * @author  jboulet
 * @version
 */

public class ArrayCellInputText extends ArrayCell implements SimpleGraphicElement
{

    // -----------------------------------------------------------------------------------------------------------------
    // Attributs
    // -----------------------------------------------------------------------------------------------------------------
    private String  name;
    private String  value = null;
    private String  size = null;
    private String  maxlength = null;

    private String  cellAlign = null;

    private String  color = null;
    private String  bgcolor = null;
    private String  textAlign = null;
    private boolean readOnly = false;
    private String  action = null;  // Action javaScript

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
     * @param line
     * 
     * @see
     */
    public ArrayCellInputText(String name, String value, ArrayLine line)
    {
        super(line);
        this.name = name;
        this.value = value;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Méthodes
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * @return
     */
    public String getCellAlign()
    {
        return cellAlign;
    }

    /**
     * @param CellAlign
     */
    public void setCellAlign(String cellAlign)
    {
        this.cellAlign = cellAlign;
    }

    /**
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return
     */
    public String getValue()
    {
        return value;
    }

    /**
     * @return
     */
    public String getSize()
    {
        return size;
    }

    /**
     * @param size
     */
    public void setSize(String size)
    {
        this.size = size;
    }


    /**
     * @return
     */
    public String getMaxlength()
    {
        return maxlength;
    }

    /**
     * @param maxlength
     */
    public void setMaxlength(String maxlength)
    {
        this.maxlength = maxlength;
    }

    /**
     * @return
     */
    public String getColor()
    {
        return color;
    }

    /**
     * @param maxlength
     */
    public void setColor(String color)
    {
        this.color = color;
    }

    /**
     * @return
     */
    public String getBgcolor()
    {
        return bgcolor;
    }

    /**
     * @param bgcolor
     */
    public void setBgcolor(String bgcolor)
    {
        this.bgcolor = bgcolor;
    }

    /**
     * @return
     */
    public String getTextAlign()
    {
        return textAlign;
    }

    /**
     * @param textAlign
     */
    public void setTextAlign(String textAlign)
    {
        this.textAlign = textAlign;
    }

    /**
     * @return
     */
    public String getAction()
    {
        return action;
    }

    /**
     * @param action
     */
    public void setAction(String action)
    {
        this.action = action;
    }

    /**
     * @return
     */
    public boolean getReadOnly()
    {
        return readOnly;
    }

    /**
     * @param likeText
     */
    public void setReadOnly(boolean readOnly)
    {
        this.readOnly = readOnly;
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

        syntax += " <input type=\"text\" name=\"";

        // param name
        if (getName() == null)
        {
            syntax += "textfield\" value=\"";
        }
        else
        {
            syntax += getName() + "\" value=\"";
        }

        // param value
        if (getValue() == null)
        {
            syntax += "\"";
        }
        else
        {
            syntax += getValue() + "\"";
        }

        // param size
        if (getSize() != null)
        {
            syntax += " size=\"" + getSize() + "\"";
        }

        // param maxlength
        if (getMaxlength() != null)
        {
            syntax += " maxlength=\"" + getMaxlength() + "\"";
        }

        // set Style
        syntax += " style=\"";

        // param likeText
        if (getReadOnly() == true)
        {
            syntax += "border: 1 solid rgb(255,255,255);";
        }

        // param textAlign
        if (getTextAlign() != null)
        {
            syntax += "text-align:" + getTextAlign() + ";";
        }

        // param color
        if (getColor() != null)
        {
            syntax += " color:" + getColor() + ";";
        }

        // param background color
        if (getBgcolor() != null)
        {
            syntax += " background-color:" + getBgcolor() + ";";
        }

        syntax += "\"";

        // param action JavaScript
        if (getAction() != null)
        {
            syntax += " " + getAction();
        }

        // readOnly ???
        if (getReadOnly() == true)
        {
            syntax += " readOnly";
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

        result += " class=\"" + getStyleSheet() + "\">";

        result += getSyntax();

        result += "</td>\n";
        return result;
    }

}
