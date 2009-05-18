/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.genericPanel;

import java.util.Hashtable;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

public class AllUserPanel extends PanelProvider
{
    protected static final int FILTER_LASTNAME    = 0;
    protected static final int FILTER_FIRSTNAME   = 1;

    protected static final int COL_LASTNAME    = 0;
    protected static final int COL_FIRSTNAME   = 1;
    protected static final int COL_EMAIL       = 2;

    protected OrganizationController m_oc = new OrganizationController();

    protected Hashtable m_AllUserDetail = new Hashtable();

    public AllUserPanel(String language, String title)
    {
    	initAll(language, title);
    }

    public void initAll(String language, String title)
    {
        String[] filters = new String[2];

        // Set the language
        m_Language = language;

        ResourceLocator message = GeneralPropertiesManager.getGeneralMultilang(m_Language);

        // Set the resource locator for columns header
        m_rs = new ResourceLocator("com.stratelia.silverpeas.genericPanel.multilang.genericPanelBundle",m_Language);

        // Set the Page name
        m_PageName = title;
        m_PageSubTitle = m_rs.getString("genericPanel.usersList");

        // Set column headers
        m_ColumnsHeader = new String[3];
        m_ColumnsHeader[COL_LASTNAME] = message.getString("GML.lastName");
        m_ColumnsHeader[COL_FIRSTNAME] = message.getString("GML.firstName");
        m_ColumnsHeader[COL_EMAIL] = message.getString("GML.email");

        // Build search tokens
        m_SearchTokens = new PanelSearchToken[2];

        m_SearchTokens[FILTER_LASTNAME] = new PanelSearchEdit(0, message.getString("GML.lastName"),"");
        m_SearchTokens[FILTER_FIRSTNAME] = new PanelSearchEdit(1, message.getString("GML.firstName"),"");

        // Set filters and get Ids
        filters[FILTER_FIRSTNAME] = "";
        filters[FILTER_LASTNAME] = "";
        refresh(filters);
    }

    public void refresh(String[] filters)
    {
        UserDetail modelUser = new UserDetail();
        UserDetail[] result = null;

        if ((filters[FILTER_FIRSTNAME] != null) && (filters[FILTER_FIRSTNAME].length() > 0))
        {
            modelUser.setFirstName(filters[FILTER_FIRSTNAME] + "%");
        }
        else
        {
            modelUser.setFirstName("");
        }

        if ((filters[FILTER_LASTNAME] != null) && (filters[FILTER_LASTNAME].length() > 0))
        {
            modelUser.setLastName(filters[FILTER_LASTNAME] + "%");
        }
        else
        {
            modelUser.setLastName("");
        }

        result = m_oc.searchUsers(modelUser, true);
        m_AllUserDetail.clear();
        m_Ids = new String[result.length];
        for (int i = 0; i < result.length ; i++ )
        {
            m_Ids[i] = result[i].getId();
            m_AllUserDetail.put(m_Ids[i],result[i]);
        }

        // Set search tokens values
        ((PanelSearchEdit)m_SearchTokens[FILTER_FIRSTNAME]).m_Text = getSureString(filters[FILTER_FIRSTNAME]);
        ((PanelSearchEdit)m_SearchTokens[FILTER_LASTNAME]).m_Text = getSureString(filters[FILTER_LASTNAME]);
        verifIndexes();
    }

    public PanelLine getElementInfos(String id)
    {
        UserDetail  theUser = (UserDetail)m_AllUserDetail.get(id);
        String[]    theValues;
        PanelLine   valret = null;

        SilverTrace.info("genericPanel", "AllUserPanel.getElementInfos()", "root.GEN_MSG_PARAM_VALUE", "id=" + id);
        theValues = new String[3];
        theValues[COL_LASTNAME] = theUser.getLastName();
        theValues[COL_FIRSTNAME] = theUser.getFirstName();
        theValues[COL_EMAIL] = theUser.geteMail();
        valret = new PanelLine(theUser.getId(),theValues,false);
        return valret;
    }
}
