/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.selectionPeas.control;

import java.util.ArrayList;
import java.util.Set;

import com.stratelia.silverpeas.genericPanel.GenericPanel;
import com.stratelia.silverpeas.genericPanel.PanelLine;
import com.stratelia.silverpeas.genericPanel.PanelOperation;
import com.stratelia.silverpeas.genericPanel.PanelProvider;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionJdbcParams;
import com.stratelia.silverpeas.selectionPeas.BrowsePanelProvider;
import com.stratelia.silverpeas.selectionPeas.CacheManager;
import com.stratelia.silverpeas.selectionPeas.CacheManagerJdbcConnector;
import com.stratelia.silverpeas.selectionPeas.CacheManagerUsersGroups;
import com.stratelia.silverpeas.selectionPeas.SelectionPeasSettings;
import com.stratelia.silverpeas.selectionPeas.jdbc.JdbcConnectorSetting;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class SelectionPeasSessionController extends AbstractComponentSessionController
{
    protected Selection     m_Selection = null;
    protected GenericPanel  m_SearchSetPanel = null;
    protected GenericPanel  m_SearchElementPanel = null;
    protected String        m_Context = "";
    protected CacheManager  m_Cm = null;
    protected ArrayList     m_SetPath = new ArrayList();

    protected BrowsePanelProvider[]  m_NavBrowse = new BrowsePanelProvider[CacheManager.CM_NBTOT];
    protected PanelProvider[]        m_NavCart = new PanelProvider[CacheManager.CM_NBTOT];

    protected String     m_SelectionType = "";
//    protected boolean    m_fromUserPanel = false;

    /**
     * Standard Session Controller Constructeur
     * 
     * 
     * @param mainSessionCtrl   The user's profile
     * @param componentContext  The component's profile
     * 
     * @see
     */
    public SelectionPeasSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
    {
        super(mainSessionCtrl, componentContext, "com.stratelia.silverpeas.selectionPeas.multilang.selectionPeasBundle", "com.stratelia.silverpeas.selectionPeas.settings.selectionPeasIcons");
        setComponentRootName(URLManager.CMP_SELECTIONPEAS);
    	m_Context = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
    }
    
    public String getSelectionType()
    {
    	return m_SelectionType;
    }

    public void initSC(String selectionType)
    {
        SilverTrace.info("selectionPeas", "SelectionPeasSessionController.initSC", "root.MSG_GEN_PARAM_VALUE", "SelectionType=" + selectionType);
        m_SearchSetPanel = null;
        m_SearchElementPanel = null;
        m_Selection = getSelection();
        m_NavBrowse[CacheManager.CM_SET] = null;
        m_NavBrowse[CacheManager.CM_ELEMENT] = null;
        m_SetPath.clear();
        m_SelectionType = selectionType;

        if (Selection.TYPE_USERS_GROUPS.equals(m_SelectionType))
        {
            m_Cm = new CacheManagerUsersGroups(getLanguage(), getMultilang(), getIcon(), m_Selection, getUserDetail());
        }
        else if (Selection.TYPE_JDBC_CONNECTOR.equals(m_SelectionType))
        {
        	m_Selection.setMultiSelect(false);
        	m_Selection.setPopupMode(true);
        	m_Selection.setHostComponentName(new PairObject("Sélection d'un élément", null));
        	m_Selection.setHostPath(null);
        	m_Selection.setElementSelectable(false);
            m_Cm = new CacheManagerJdbcConnector(getLanguage(), getMultilang(), getIcon(), m_Selection);
        }
        // Preset Ids
        m_Cm.setSelected(CacheManager.CM_ELEMENT,m_Selection.getSelectedElements(),true);
        m_Cm.setSelected(CacheManager.CM_SET,m_Selection.getSelectedSets(),true);
    }
    
    public void updateJdbcParameters(JdbcConnectorSetting jdbcSetting, String tableName, String columnsNames,
    	String formIndex, String fieldsNames)
    {
    	SelectionJdbcParams selectionJdbcParams = new SelectionJdbcParams(jdbcSetting.getDriverClassName(),
    		jdbcSetting.getUrl(), jdbcSetting.getLogin(), jdbcSetting.getPassword(), tableName, columnsNames, formIndex,
    		fieldsNames);
    	m_Selection.setExtraParams(selectionJdbcParams);
    }

   // -------------------------------------------------------------------------------------------------------------------
   // -------------------------------------------     Navigation Functions ----------------------------------------------
   // -------------------------------------------------------------------------------------------------------------------

    public boolean isMultiSelect()
    {
        return m_Selection.isMultiSelect();
    }

    public String getZoomToSetURL()
    {
        return m_Context + getComponentUrl() + "ZoomToSetInfos";
    }

    public String getZoomToElementURL()
    {
        return m_Context + getComponentUrl() + "ZoomToElementInfos";
    }

    public String getStartingFunction()
    {
        String theStartingPage;

        if (!(Selection.FIRST_PAGE_DEFAULT.equalsIgnoreCase(SelectionPeasSettings.m_FirstPage)))
        {
            theStartingPage = SelectionPeasSettings.m_FirstPage;
        }
        else
        {
            if (Selection.FIRST_PAGE_DEFAULT.equalsIgnoreCase(m_Selection.getFirstPage()))
            {
                theStartingPage = SelectionPeasSettings.m_DefaultPage;
            }
            else
            {
                theStartingPage = m_Selection.getFirstPage();
            }
        }
        if (Selection.FIRST_PAGE_SEARCH_ELEMENT.equalsIgnoreCase(theStartingPage) && (!m_Selection.isElementSelectable()))
        {
            if (m_Selection.isSetSelectable())
            {
                theStartingPage = Selection.FIRST_PAGE_SEARCH_SET;
            }
            else
            {
                theStartingPage = Selection.FIRST_PAGE_BROWSE;
            }
        }
        if (Selection.FIRST_PAGE_SEARCH_SET.equalsIgnoreCase(theStartingPage) && (!m_Selection.isSetSelectable()))
        {
            if (m_Selection.isElementSelectable())
            {
                theStartingPage = Selection.FIRST_PAGE_SEARCH_ELEMENT;
            }
            else
            {
                theStartingPage = Selection.FIRST_PAGE_BROWSE;
            }
        }
        if (Selection.FIRST_PAGE_CART.equalsIgnoreCase(theStartingPage) && (!m_Selection.isMultiSelect()))
        {
            if (m_Selection.isSetSelectable())
            {
                theStartingPage = Selection.FIRST_PAGE_SEARCH_SET;
            }
            else if (m_Selection.isElementSelectable())
            {
                theStartingPage = Selection.FIRST_PAGE_SEARCH_ELEMENT;
            }
            else
            {
                theStartingPage = Selection.FIRST_PAGE_BROWSE;
            }
        }
        return theStartingPage;
    }

    public String getSearchSet()
    {
        if (m_SearchSetPanel == null)
        {
            m_SearchSetPanel = new GenericPanel();
            m_SearchSetPanel.setCancelURL(m_Context + getComponentUrl() + "Cancel");
            m_SearchSetPanel.setGoBackURL(m_Context + getComponentUrl() + "ReturnSearchSet");
            m_SearchSetPanel.setZoomToItemURL(getZoomToSetURL());
            m_SearchSetPanel.setPopupMode(false);
            m_SearchSetPanel.setMultiSelect(m_Selection.isMultiSelect());
            m_SearchSetPanel.setSelectable(m_Selection.isSetSelectable());
            m_SearchSetPanel.setZoomToItemInPopup(true);
            m_SearchSetPanel.setPanelOperations(getOperations("DisplaySearchSet"));
            m_SearchSetPanel.setPanelProvider(m_Cm.getSearchPanelProvider(CacheManager.CM_SET, m_Cm, m_Selection.getExtraParams()));
            m_SearchSetPanel.setHostSpaceName(m_Selection.getHostSpaceName());
            if (m_Selection.isPopupMode())
            {
                PairObject po = null;
                PairObject[] apo = null;
                PairObject[] emptyapo = null;

                if (m_Selection.getHostComponentName() != null)
                {
                    po = new PairObject(m_Selection.getHostComponentName().getFirst(),"");
                }
                m_SearchSetPanel.setHostComponentName(po);
                apo = m_Selection.getHostPath();
                if (apo != null)
                {
                    emptyapo = new PairObject[apo.length];
                    for (int i = 0; i < apo.length ; i++)
                    {
                        if (apo[i] != null)
                        {
                            emptyapo[i] = new PairObject(apo[i].getFirst(),"");
                        }
                        else
                        {
                            emptyapo[i] = null;
                        }
                    }
                }
                m_SearchSetPanel.setHostPath(emptyapo);
            }
            else
            {
                m_SearchSetPanel.setHostComponentName(m_Selection.getHostComponentName());
                m_SearchSetPanel.setHostPath(m_Selection.getHostPath());
            }
            setGenericPanel("SearchSet",m_SearchSetPanel);
        }
        return GenericPanel.getGenericPanelURL("SearchSet");
    }

    public String returnSearchSet()
    {
        GenericPanel gp = getGenericPanel("SearchSet");
        String       theOperation = gp.getSelectedOperation();
        if (GenericPanel.OPERATION_VALIDATE.equals(theOperation))
        {
            return "Validate";
        }
        else
        {
            return theOperation;
        }
    }

    public String getSearchElement()
    {
        if (m_SearchElementPanel == null)
        {
            m_SearchElementPanel = new GenericPanel();
            m_SearchElementPanel.setCancelURL(m_Context + getComponentUrl() + "Cancel");
            m_SearchElementPanel.setGoBackURL(m_Context + getComponentUrl() + "ReturnSearchElement");
            m_SearchElementPanel.setZoomToItemURL(getZoomToElementURL());
            m_SearchElementPanel.setPopupMode(false);
            m_SearchElementPanel.setMultiSelect(m_Selection.isMultiSelect());
            m_SearchElementPanel.setSelectable(m_Selection.isElementSelectable());
            m_SearchElementPanel.setZoomToItemInPopup(true);
            m_SearchElementPanel.setPanelOperations(getOperations("DisplaySearchElement"));
            m_SearchElementPanel.setPanelProvider(m_Cm.getSearchPanelProvider(CacheManager.CM_ELEMENT, m_Cm, m_Selection.getExtraParams()));
            m_SearchElementPanel.setHostSpaceName(m_Selection.getHostSpaceName());
            if (m_Selection.isPopupMode())
            {
                PairObject po = null;
                PairObject[] apo = null;
                PairObject[] emptyapo = null;

                if (m_Selection.getHostComponentName() != null)
                {
                    po = new PairObject(m_Selection.getHostComponentName().getFirst(),"");
                }
                m_SearchElementPanel.setHostComponentName(po);
                apo = m_Selection.getHostPath();
                if (apo != null)
                {
                    emptyapo = new PairObject[apo.length];
                    for (int i = 0; i < apo.length ; i++)
                    {
                        if (apo[i] != null)
                        {
                            emptyapo[i] = new PairObject(apo[i].getFirst(),"");
                        }
                        else
                        {
                            emptyapo[i] = null;
                        }
                    }
                }
                m_SearchElementPanel.setHostPath(emptyapo);
            }
            else
            {
                m_SearchElementPanel.setHostComponentName(m_Selection.getHostComponentName());
                m_SearchElementPanel.setHostPath(m_Selection.getHostPath());
            }
            setGenericPanel("SearchElement",m_SearchElementPanel);
        }
        return GenericPanel.getGenericPanelURL("SearchElement");
    }

    public String returnSearchElement()
    {
        GenericPanel gp = getGenericPanel("SearchElement");
        String       theOperation = gp.getSelectedOperation();
        if (GenericPanel.OPERATION_VALIDATE.equals(theOperation))
        {
            return "Validate";
        }
        else
        {
            return theOperation;
        }
    }

    public String getGoBackURL()
    {
        return getSureString(m_Selection.getGoBackURL());
    }

    public String getCancelURL()
    {
        return getSureString(m_Selection.getCancelURL());
    }

    public boolean isPopup()
    {
        return m_Selection.isPopupMode();
    }

    public boolean isSetSelectable()
    {
        return m_Selection.isSetSelectable();
    }

    public boolean isElementSelectable()
    {
        return m_Selection.isElementSelectable();
    }

    public PairObject getHostComponentName()
    {
        return m_Selection.getHostComponentName();
    }

    public String getHostSpaceName()
    {
        return m_Selection.getHostSpaceName();
    }

    public PairObject[] getHostPath()
    {
        return m_Selection.getHostPath();
    }

    public PanelLine[] getSetPath()
    {
        return (PanelLine[])m_SetPath.toArray(new PanelLine[0]);
    }

    public String[][] getInfos(int what,String theId)
    {
        return m_Cm.getContentInfos(what,theId);
    }

    public String getContentText(int what)
    {
        return m_Cm.getContentText(what);
    }
    public String[] getContentColumns(int what)
    {
        return m_Cm.getContentColumnsNames(what);
    }

    public String[][] getContent(int what,String theId)
    {
        return m_Cm.getContentLines(what,theId);
    }

    public PanelOperation[] getOperations(String currentFunction)
    {
        ArrayList poList = new ArrayList();
 
        poList.add(m_Cm.getPanelOperation("DisplayBrowse"));
        if (m_Selection.isElementSelectable())
        {
            poList.add(m_Cm.getPanelOperation("DisplaySearchElement"));
        }
        if (m_Selection.isSetSelectable())
        {
            poList.add(m_Cm.getPanelOperation("DisplaySearchSet"));
        }
        if (m_Selection.isMultiSelect())
        {
            poList.add(new PanelOperation(getString("selectionPeas.helpCart"), m_Context + getIcon().getString("selectionPeas.showPanier"), "DisplayCart"));
        }
        if ("DisplayCart".equals(currentFunction))
        {
            poList.add(new PanelOperation("", "", ""));
            poList.add(new PanelOperation(getString("selectionPeas.removeFromCart"), m_Context + getIcon().getString("selectionPeas.selectDelete"), "RemoveSelectedFromCart",getString("selectionPeas.confirmRemoveFromCart")));
            poList.add(new PanelOperation(getString("selectionPeas.removeAllFromCart"), m_Context + getIcon().getString("selectionPeas.allDelete"), "RemoveAllFromCart",getString("selectionPeas.confirmRemoveFromCart")));
        }
        return (PanelOperation[])poList.toArray(new PanelOperation[0]);
    }
    
    public void validate()
    {
/*        if (m_fromUserPanel)
        {
            setSelectionToUserPanel();
        }
        else
        {
  */          m_Selection.setSelectedSets(m_Cm.getSelectedIds(CacheManager.CM_SET));
            m_Selection.setSelectedElements(m_Cm.getSelectedIds(CacheManager.CM_ELEMENT));
//        }
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

   // -------------------------------------------------------------------------------------------------------------------
   // -------------------------------------------     Browse Functions ----------------------------------------------
   // -------------------------------------------------------------------------------------------------------------------

    public void initBrowse()
    {
        if (m_NavBrowse[CacheManager.CM_SET] == null)
        {
            m_NavBrowse[CacheManager.CM_SET] = m_Cm.getBrowsePanelProvider(CacheManager.CM_SET, m_Cm, m_Selection.getExtraParams());
        }
        if (m_NavBrowse[CacheManager.CM_ELEMENT] == null)
        {
            m_NavBrowse[CacheManager.CM_ELEMENT] = m_Cm.getBrowsePanelProvider(CacheManager.CM_ELEMENT, m_Cm, m_Selection.getExtraParams());
        }
    }

    public String getSelectedNumber()
    {
        return Integer.toString(m_NavBrowse[CacheManager.CM_SET].getSelectedNumber() + m_NavBrowse[CacheManager.CM_ELEMENT].getSelectedNumber());
    }

    public String getText(int what)
    {
        return m_NavBrowse[what].getPageName();
    }

    public boolean[] getNavigation(int what)
    {
        boolean[] valret = new boolean[2];
        valret[0] = (!m_NavBrowse[what].isFirstPage());
        valret[1] = (!m_NavBrowse[what].isLastPage());
        return valret;
    }
    
    public void setSelected(int what, Set selectedSets)
    {
        m_NavBrowse[what].setSelectedElements(selectedSets);
    }

    public void setOneSelected(int what, String selected)
    {
        m_Cm.unselectAll();
        m_Cm.setSelected(what,selected,true);
    }

    public String[] getColumnsHeader(int what)
    {
        return m_NavBrowse[what].getColumnsHeader();
    }

    public PanelLine[] getPage(int what)
    {
        return m_NavBrowse[what].getPage();
    }
    
    public void nextPage(int what)
    {
        m_NavBrowse[what].nextPage();
    }

    public void previousPage(int what)
    {
        m_NavBrowse[what].previousPage();
    }

    public String getMiniFilterString(int what)
    {
        return m_NavBrowse[what].getSelectMiniFilter().getHTMLDisplay();
    }

    public void setMiniFilter(String theValue, String theFilter)
    {
        m_NavBrowse[Integer.parseInt(theFilter.substring(1,2))].setMiniFilter(Integer.parseInt(theFilter.substring(3)), theValue);
    }

    public void setParentSet(String parentSetId)
    {
        int   parc = 0;

        m_NavBrowse[CacheManager.CM_SET].setNewParentSet(parentSetId);
        m_NavBrowse[CacheManager.CM_ELEMENT].setNewParentSet(parentSetId);
        if ((parentSetId == null) || (parentSetId.length() <= 0) || (parentSetId.equals("-1")))
        {
            m_SetPath.clear();
        }
        else
        {
            while ((parc < m_SetPath.size()) && (!parentSetId.equals(((PanelLine)m_SetPath.get(parc)).m_Id)))
            {
                parc++;
            }
            if (parc < m_SetPath.size())
            {
                parc++;
                while (parc < m_SetPath.size())
                {   // Group found -> go back to it
                    m_SetPath.remove(parc);
                }
            }
            else
            {
                m_SetPath.add(m_Cm.getInfos(CacheManager.CM_SET,parentSetId));
            }
        }
    }
    
   // -------------------------------------------------------------------------------------------------------------------
   // -------------------------------------------     CART Functions       ----------------------------------------------
   // -------------------------------------------------------------------------------------------------------------------

    public void initCart()
    {
        m_NavCart[CacheManager.CM_SET] = m_Cm.getCartPanelProvider(CacheManager.CM_SET, m_Cm, m_Selection.getExtraParams());
        m_NavCart[CacheManager.CM_ELEMENT] = m_Cm.getCartPanelProvider(CacheManager.CM_ELEMENT, m_Cm, m_Selection.getExtraParams());
    }

    public String getCartSelectedNumber()
    {
        return Integer.toString(m_NavCart[CacheManager.CM_SET].getSelectedNumber() + m_NavCart[CacheManager.CM_ELEMENT].getSelectedNumber());
    }

    public String getCartText(int what)
    {
        return m_NavCart[what].getPageName();
    }

    public String[] getCartColumnsHeader(int what)
    {
        return m_NavCart[what].getColumnsHeader();
    }

    public boolean[] getCartNavigation(int what)
    {
        boolean[] valret = new boolean[2];
        valret[0] = (!m_NavCart[what].isFirstPage());
        valret[1] = (!m_NavCart[what].isLastPage());
        return valret;
    }
    
    public void setCartSelected(int what, Set selectedSets)
    {
        m_NavCart[what].setSelectedElements(selectedSets);
    }

    public PanelLine[] getCartPage(int what)
    {
        return m_NavCart[what].getPage();
    }
    
    public void nextCartPage(int what)
    {
        m_NavCart[what].nextPage();
    }

    public void previousCartPage(int what)
    {
        m_NavCart[what].previousPage();
    }

    public String getCartMiniFilterString(int what)
    {
        return m_NavCart[what].getSelectMiniFilter().getHTMLDisplay();
    }

    public void setCartMiniFilter(String theValue, String theFilter)
    {
        m_NavCart[Integer.parseInt(theFilter.substring(1,2))].setMiniFilter(Integer.parseInt(theFilter.substring(3)), theValue);
    }

    public void removeAllFromCart()
    {
        m_Cm.unselectAll();
    }

    public void removeSelectedFromCart()
    {
        String[]  sel;
        int       i;

        sel = m_NavCart[CacheManager.CM_SET].getSelectedElements();
        for (i = 0; i < sel.length; i++)
        {
            m_Cm.setSelected(CacheManager.CM_SET,sel[i],false);
        }
        sel = m_NavCart[CacheManager.CM_ELEMENT].getSelectedElements();
        for (i = 0; i < sel.length; i++)
        {
            m_Cm.setSelected(CacheManager.CM_ELEMENT,sel[i],false);
        }
    }
    
}
