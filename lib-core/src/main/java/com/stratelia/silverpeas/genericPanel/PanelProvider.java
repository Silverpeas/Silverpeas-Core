/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.genericPanel;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import com.stratelia.webactiv.util.ResourceLocator;

abstract public class PanelProvider
{
    protected String             m_PageName = "";
    protected String             m_PageSubTitle = "";
    protected String[]           m_ColumnsHeader = new String[0];
    protected PanelSearchToken[] m_SearchTokens = null;

    protected ResourceLocator    m_rs = null;
    protected String             m_Language = "fr";

    protected String[]           m_Ids = new String[0];
    protected int                m_FirstDisplayed = 0;
    protected int                m_NbDisplayed = GenericPanelSettings.m_ElementsByPage;

    protected Hashtable          m_ElementsCache = new Hashtable();
    protected HashSet            m_SelectedElements = new HashSet();

    protected boolean            m_FilterValid = true;

    protected PanelMiniFilterToken[]    m_MiniFilters = new PanelMiniFilterToken[0];
    protected PanelMiniFilterSelect     m_SelectMiniFilter = null;

    abstract public PanelLine getElementInfos(String id);
    abstract public void refresh(String[] filters);

    public void setMiniFilter(int filterIndex, String filterValue)
    {
    }

    public PanelMiniFilterSelect getSelectMiniFilter()
    {
        return m_SelectMiniFilter;
    }

    public void setSelectMiniFilter(PanelMiniFilterSelect selectMiniFilter)
    {
        m_SelectMiniFilter = selectMiniFilter;
    }

    public PanelProvider()
    {
    }

    public void initAll(String[] selectedIds)
    {
        resetAllCache();
        m_SelectedElements.clear();
        if (selectedIds != null)
        {
            for (int i = 0; i < selectedIds.length ; i++ )
            {
                m_SelectedElements.add(selectedIds[i]);
            }
        }
        m_FirstDisplayed = 0;
    }

    public void resetAllSelected()
    {
        m_SelectedElements.clear();
    }

    public void resetAllCache()
    {
        m_ElementsCache.clear();
    }

    public void resetOneCache(String id)
    {
        m_ElementsCache.remove(id);
    }

    public PanelLine getCachedElement(String id)
    {
        PanelLine valret = null;

        if (m_ElementsCache.get(id) == null)
        {
            m_ElementsCache.put(id,getElementInfos(id));
            valret = (PanelLine)m_ElementsCache.get(id);
            if (valret != null)
            {
                valret.m_Selected = m_SelectedElements.contains(valret.m_Id);
            }
        }
        else
        {
            valret = (PanelLine)m_ElementsCache.get(id);
        }
        return valret;
    }

    public void setSelectedElement(String id, boolean isSelected)
    {
        PanelLine theElement = getCachedElement(id);
        theElement.m_Selected = isSelected;
        if (isSelected)
        {
            m_SelectedElements.add(id);
        }
        else
        {
            m_SelectedElements.remove(id);
        }
    }

    public void setSelectedElements(Set elements)
    {
        int         i;
        int         max;
        PanelLine   theElement = null;

        // Simple case : less than a page to display or display all
        if ((m_NbDisplayed == -1) || (m_Ids.length <= m_NbDisplayed))
        {
            max = m_Ids.length;
        }
        else if (m_Ids.length <= (m_FirstDisplayed + m_NbDisplayed))
        {
            max = m_Ids.length - m_FirstDisplayed;
        }
        else
        {
            max = m_NbDisplayed;
        }
        for (i = 0; i < max; i++ )
        {
            theElement = getCachedElement(m_Ids[m_FirstDisplayed + i]);
            if (elements.contains(m_Ids[m_FirstDisplayed + i]))
            {
                theElement.m_Selected = true;
                m_SelectedElements.add(m_Ids[m_FirstDisplayed + i]);
            }
            else
            {
                theElement.m_Selected = false;
                m_SelectedElements.remove(m_Ids[m_FirstDisplayed + i]);
            }
        }
    }

    public String[] getSelectedElements()
    {
        return (String[])m_SelectedElements.toArray(new String[0]);
    }

    public int getSelectedNumber()
    {
        return m_SelectedElements.size();
    }

    public boolean isFilterValid()
    {
        return m_FilterValid;
    }

    public PanelSearchToken[] getSearchTokens()
    {
        return m_SearchTokens;
    }

    public String[] getColumnsHeader()
    {
        return m_ColumnsHeader;
    }

    public String getPageName()
    {
        return m_PageName;
    }

    public String getPageSubTitle()
    {
        return m_PageSubTitle;
    }

    public int getElementNumber()
    {
        return m_Ids.length;
    }

    public int getNbMaxDisplayed()
    {
        if (m_NbDisplayed == -1)
        {
            return getElementNumber();
        }
        else
        {
            return m_NbDisplayed;
        }
    }

    public void nextPage()
    {
        if ((m_NbDisplayed != -1) && (m_Ids.length > (m_FirstDisplayed + m_NbDisplayed)))
        {
            m_FirstDisplayed += m_NbDisplayed;
        }
    }

    public void previousPage()
    {
        if ((m_NbDisplayed != -1) && (m_FirstDisplayed > 0))
        {
            if (m_FirstDisplayed >= m_NbDisplayed)
            {
                m_FirstDisplayed -= m_NbDisplayed;
            }
            else
            {
                m_FirstDisplayed = 0;
            }
        }
    }

    public boolean isFirstPage()
    {
        if (m_NbDisplayed == -1)
        {
            return true;
        }
        return (m_FirstDisplayed == 0);
    }

    public boolean isLastPage()
    {
        if (m_NbDisplayed == -1)
        {
            return true;
        }
        return (m_Ids.length <= (m_FirstDisplayed + m_NbDisplayed));
    }

    public PanelLine[] getPage()
    {
        PanelLine[] valret = null;
        int        i;

        // Simple case : less than a page to display or display all
        if ((m_NbDisplayed == -1) || (m_Ids.length <= m_NbDisplayed))
        {
            m_FirstDisplayed = 0;
            valret = new PanelLine[m_Ids.length];
        }
        else if (m_Ids.length <= (m_FirstDisplayed + m_NbDisplayed))
        {
            valret = new PanelLine[m_Ids.length - m_FirstDisplayed];
        }
        else
        {
            valret = new PanelLine[m_NbDisplayed];
        }
        for (i = 0; i < valret.length; i++ )
        {
            valret[i] = getCachedElement(m_Ids[m_FirstDisplayed + i]);
        }
        return valret;
    }

    protected void verifIndexes()
    {
/*        if (m_Ids.length <= m_FirstDisplayed)
        {
            if (m_Ids.length > 0)
            {
                m_FirstDisplayed = m_Ids.length - 1;
            }
            else
            {
                m_FirstDisplayed = 0;
            }
        }*/
        m_FirstDisplayed = 0;
    }

    protected String getSureString(String s)
    {
        if (s == null)
        {
            return "";
        }
        else
        {
            return s;
        }
    }
}
