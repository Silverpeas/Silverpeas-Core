/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.webactiv.clipboard.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Properties;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class SilverpeasKeyData implements Serializable
{

    final static String kTitleKEY = "TITLE";
    final static String kAuthorKEY = "AUTHOR";
    final static String kDescKEY = "DESC";
    final static String kHTMLKEY = "HTML";  // rendu HTML de l'object
    final static String kTextKEY = "TEXT";  // rendu HTML de l'object

    private Date        m_CreationDate = null;
    private Properties  m_KeyData = null;

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public SilverpeasKeyData()
    {
        m_KeyData = new Properties();
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public void setTitle(String Title)
    {
        m_KeyData.setProperty(kTitleKEY, Title);
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public void setAuthor(String Author)
    {
        m_KeyData.setProperty(kAuthorKEY, Author);
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public void setCreationDate(Date date)
    {
        m_CreationDate = date;
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public void setDesc(String Desc)
    {
        m_KeyData.setProperty(kDescKEY, Desc);
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public void setText(String Text)
    {
        m_KeyData.setProperty(kTextKEY, Text);
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public void setProperty(String key, String value) throws SKDException
    {
        if (m_KeyData.containsKey(key))
        {
            throw new SKDException("SilverpeasKeyData.setProperty()", SKDException.ERROR, "root.EX_CLIPBOARD_COPY_FAILED", "Key still used (" + key + ")");
        }
        else
        {
            m_KeyData.setProperty(key, value);
        }
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public String getTitle()
    {
        return m_KeyData.getProperty(kTitleKEY);
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public String getAuthor()
    {
        return m_KeyData.getProperty(kAuthorKEY);
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public Date getCreationDate()
    {
        return m_CreationDate;
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public String getDesc()
    {
        return m_KeyData.getProperty(kDescKEY);
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public String getText()
    {
        return m_KeyData.getProperty(kTextKEY);
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public String getProperty(String key)
    {
        return m_KeyData.getProperty(key);
    }

    /**
     * --------------------------------------------------------------------------------------------------------
     * 
     */
    public void toDOM() {}

}

