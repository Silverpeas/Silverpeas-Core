package com.silverpeas.jobStartPagePeas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class NavBarManager extends Object
{
    // Constants used by urlFactory
    final static int	SPACE		= 0;
    final static int	COMPONENT	= 1;
    final static int	COMPONENTPOPUP	= 7;

    final static int	SPACE_COLLAPSE			= 2;
    final static int	SPACE_EXPANDED			= 3;
    final static int	SPACE_COMPONENT			= 4;
    final static int	SUBSPACE_COMPONENT		= 5;
    final static int	SUBSPACE_LAST_COMPONENT		= 6;
    final static String	POPUP_PARAM_NAME		= "popupDisplay";

    UserDetail      m_user = null;
    boolean         m_bAdministrationAccess = false;
    AdminController m_administrationCtrl = null;
    AbstractComponentSessionController m_SessionCtrl = null;
    String          m_sContext;
    HashSet         m_ManageableSpaces = new HashSet();

    DisplaySorted[] m_Spaces = null;
    String          m_CurrentSpaceId = null;
    DisplaySorted[] m_SpaceComponents = null;
    String          m_CurrentSubSpaceId = null;
    DisplaySorted[] m_SubSpaces = null;
    DisplaySorted[] m_SubSpaceComponents = null;
    long            m_elmtCounter = 0;

    public void resetSpaceCache(String theSpaceId)
    {
        SilverTrace.info("jobStartPagePeas", "NavBarManager.resetSpaceCache()", "root.MSG_GEN_PARAM_VALUE", "Reset Cache Space=" + theSpaceId);
        String spaceId = getShortSpaceId(theSpaceId);
        DisplaySorted elmt = getSpaceCache(spaceId);
        if (elmt != null)
        {
            elmt.copy(buildSpaceObject(spaceId));
            if (spaceId.equals(m_CurrentSpaceId))
            {
                setCurrentSpace(m_CurrentSpaceId);
            }
            else if (spaceId.equals(m_CurrentSubSpaceId))
            {
                setCurrentSubSpace(null);
                setCurrentSubSpace(spaceId);
            }
        }
    }

    public void addSpaceInCache(String theSpaceId)
    {
        String spaceId = getShortSpaceId(theSpaceId);
        DisplaySorted newElmt = buildSpaceObject(spaceId);

        if (newElmt != null)
        {
            m_ManageableSpaces.add(spaceId);
            if (newElmt.type == DisplaySorted.TYPE_SPACE)
            {
                DisplaySorted[] oldSpaces = m_Spaces;
                m_Spaces = new DisplaySorted[oldSpaces.length + 1];
                for (int i = 0; i < oldSpaces.length ; i++ )
                {
                    m_Spaces[i] = oldSpaces[i];
                }
                m_Spaces[oldSpaces.length] = newElmt;
                Arrays.sort(m_Spaces);
            }
            else
            { // Sub Space case : 
                setCurrentSpace(m_CurrentSpaceId);
            }
        }
    }

    public void removeSpaceInCache(String theSpaceId)
    {
        String spaceId = getShortSpaceId(theSpaceId);
        DisplaySorted elmt = getSpaceCache(spaceId);

        if (elmt != null)
        {
            if (elmt.type == DisplaySorted.TYPE_SPACE)
            {
                DisplaySorted[] oldSpaces = m_Spaces;
                int j = 0;

                m_Spaces = new DisplaySorted[oldSpaces.length - 1];
                for (int i = 0; i < oldSpaces.length ; i++ )
                {
                    if (!oldSpaces[i].id.equals(spaceId) && (j < m_Spaces.length))
                    {
                        m_Spaces[j++] = oldSpaces[i];
                    }
                }
                if ((m_CurrentSpaceId != null) && (m_CurrentSpaceId.equals(spaceId)))
                {
                    setCurrentSpace(null);
                }
            }
            else
            {
                DisplaySorted[] oldSpaces = m_SubSpaces;
                int j = 0;

                m_SubSpaces = new DisplaySorted[oldSpaces.length - 1];
                for (int i = 0; i < oldSpaces.length ; i++ )
                {
                    if (!oldSpaces[i].id.equals(spaceId) && (j < m_SubSpaces.length))
                    {
                        m_SubSpaces[j++] = oldSpaces[i];
                    }
                }
                if ((m_CurrentSubSpaceId != null) && (m_CurrentSubSpaceId.equals(spaceId)))
                {
                    setCurrentSubSpace(null);
                }
            }
        }
    }

    public void resetAllCache()
    {
        String currentSpaceId = m_CurrentSpaceId;
        String currentSubSpaceId = m_CurrentSubSpaceId;

        SilverTrace.info("jobStartPagePeas", "NavBarManager.resetAllCache()", "root.MSG_GEN_PARAM_VALUE");
        initWithUser(m_SessionCtrl,m_user, m_bAdministrationAccess);
        if (currentSpaceId != null)
        {
            setCurrentSpace(currentSpaceId);
        }
        if (currentSubSpaceId != null)
        {
            setCurrentSubSpace(currentSubSpaceId);
        }
    }

    public void initWithUser(AbstractComponentSessionController msc,UserDetail user, boolean adminAccess)
    {
        boolean         bIsAdmin = false;
        String          sUserId = user.getId();
        String[]        spaceIds;
        String[]        allManageableSpaceIds;

        SilverTrace.info("jobStartPagePeas", "NavBarManager.initWithUser()", "root.MSG_GEN_PARAM_VALUE", "User=" + sUserId + " AdminAccess=" + adminAccess);
        m_sContext = GeneralPropertiesManager.getGeneralResourceLocator().getString("ApplicationURL");
        m_administrationCtrl = new AdminController(sUserId);
        m_SessionCtrl = msc;
        m_bAdministrationAccess = adminAccess;
        m_user = user;
        m_elmtCounter = 0;
        m_CurrentSpaceId = null;
        m_CurrentSubSpaceId = null;
        m_SubSpaces = new DisplaySorted[0];
        m_SpaceComponents = new DisplaySorted[0];
        m_SubSpaceComponents = new DisplaySorted[0];

        if (adminAccess)
        {
            if (m_user.getAccessLevel().equals("A"))
            {
                bIsAdmin = true;
            }
            if (bIsAdmin)
            {
                allManageableSpaceIds = m_administrationCtrl.getAllSpaceIds();
            }
            else
            {
//                spaceIds   = m_administrationCtrl.getUserManageableSpaceRootIds(sUserId);
                allManageableSpaceIds = m_administrationCtrl.getUserManageableSpaceIds(sUserId);
            }      
            // First of all, add the manageable spaces into the set
            m_ManageableSpaces.clear();
            for (int i = 0; i < allManageableSpaceIds.length ; i++)
            {
                m_ManageableSpaces.add(getShortSpaceId(allManageableSpaceIds[i]));
            }
            spaceIds = m_administrationCtrl.getAllRootSpaceIds();
            m_Spaces = createSpaceObjects(spaceIds, false);
        }
        else
        {
            // TODO
        }
    }

    // Spaces functions
    // ----------------

    public DisplaySorted[] getAvailableSpaces()
    {
        return m_Spaces;
    }

    public String getCurrentSpaceId()
    {
        return m_CurrentSpaceId;
    }

    public DisplaySorted getSpace(String theSpaceId)
    {
        return getSpaceCache(getShortSpaceId(theSpaceId));
    }

    public boolean setCurrentSpace(String theSpaceId)
    {
        String spaceId = getShortSpaceId(theSpaceId);

        m_CurrentSpaceId = spaceId;
        // Reset the selected sub space
        m_CurrentSubSpaceId = null;
        m_SubSpaceComponents = new DisplaySorted[0];
        if ((m_CurrentSpaceId != null) && (m_CurrentSpaceId.length() > 0) && (getSpaceCache(m_CurrentSpaceId) == null))
        {
            m_CurrentSpaceId = null;
        }

        if ((spaceId.length() <= 0) || (m_CurrentSpaceId == null))
        {
            m_SpaceComponents = new DisplaySorted[0];
            m_SubSpaces = new DisplaySorted[0];
        }
        else
        {
            SpaceInst spaceInst = m_administrationCtrl.getSpaceInstById("WA" + spaceId);
            // Get the space's components and sub-spaces
            if (spaceInst == null)
            {
                m_SpaceComponents = new DisplaySorted[0];
                m_SubSpaces = new DisplaySorted[0];
                m_CurrentSpaceId = null;
            }
            else
            {
                m_SpaceComponents = createComponentObjects(spaceInst, false);
                m_SubSpaces = createSpaceObjects(spaceInst.getSubSpaceIds(), true);
            }
        }            
        for (int j=0;j<  m_Spaces.length ; j++)
        {
            buildSpaceHTMLLine(m_Spaces[j]);
        } 
        SilverTrace.info("jobStartPagePeas", "NavBarManager.setCurrentSpace()", "root.MSG_GEN_PARAM_VALUE", "Set Current Space=" + m_CurrentSpaceId);
        return ((m_CurrentSpaceId != null) && (m_CurrentSpaceId.length() > 0));
    }

    public DisplaySorted[] getAvailableSpaceComponents()
    {
        if (m_CurrentSpaceId == null)
        {
            return new DisplaySorted[0];
        }
        return m_SpaceComponents;
    }

    // Sub-Spaces functions
    // --------------------

    public DisplaySorted[] getAvailableSubSpaces()
    {
        if (m_CurrentSpaceId == null)
        {
            return new DisplaySorted[0];
        }
        return m_SubSpaces;
    }

    public String getCurrentSubSpaceId()
    {
        return m_CurrentSubSpaceId;
    }

    public boolean setCurrentSubSpace(String theSpaceId)
    {
        String subSpaceId = getShortSpaceId(theSpaceId);
        SpaceInst sp = null;

        m_CurrentSubSpaceId = subSpaceId;
        if ((m_CurrentSubSpaceId != null) && (m_CurrentSubSpaceId.length() > 0) && (getSpaceCache(m_CurrentSubSpaceId) == null))
        {
            m_CurrentSubSpaceId = null;
        }
        if ((m_CurrentSubSpaceId != null) && (m_CurrentSubSpaceId.length() > 0))
        {
            sp = m_administrationCtrl.getSpaceInstById("WA" + m_CurrentSubSpaceId);
            if (sp == null)
            {
                m_CurrentSubSpaceId = null;
            }
        }
        for (int j=0;j<  m_SubSpaces.length ; j++)
        {
            buildSpaceHTMLLine(m_SubSpaces[j]);
        }
        if ((m_CurrentSubSpaceId != null) && (m_CurrentSubSpaceId.length() > 0))
        {
            m_SubSpaceComponents = createComponentObjects(sp,true);
        }
        else
        {
            m_SubSpaceComponents = new DisplaySorted[0];
        }
        SilverTrace.info("jobStartPagePeas", "NavBarManager.setCurrentSubSpace()", "root.MSG_GEN_PARAM_VALUE", "Set Current SUB Space=" + m_CurrentSubSpaceId);
        return ((m_CurrentSubSpaceId != null) && (m_CurrentSubSpaceId.length() > 0));
    }

    public DisplaySorted[] getAvailableSubSpaceComponents()
    {
        if (m_CurrentSubSpaceId == null)
        {
            return new DisplaySorted[0];
        }
        return m_SubSpaceComponents;
    }

/* 
    Protected functions
    --------------------------------------------------------------------------------------------------------------------------------------------------------------
*/

    protected DisplaySorted getSpaceCache(String spaceId)
    {
        int i;
        
        if (spaceId == null)
        {
            return null;
        }

        for (i = 0; i < m_Spaces.length; i++)
        {
            if (spaceId.equals(m_Spaces[i].id))
            {
                return m_Spaces[i];
            }
        }

        for (i = 0; i < m_SubSpaces.length; i++)
        {
            if (spaceId.equals(m_SubSpaces[i].id))
            {
                return m_SubSpaces[i];
            }
        }

        return null;
    }

    protected DisplaySorted getComponentCache(String componentId)
    {
        int i;
        
        if (componentId == null)
        {
            return null;
        }

        for (i = 0; i < m_SpaceComponents.length; i++)
        {
            if (componentId.equals(m_SpaceComponents[i].id))
            {
                return m_SpaceComponents[i];
            }
        }

        for (i = 0; i < m_SubSpaceComponents.length; i++)
        {
            if (componentId.equals(m_SubSpaceComponents[i].id))
            {
                return m_SubSpaceComponents[i];
            }
        }

        return null;
    }

    protected DisplaySorted[] createSpaceObjects(String[] spaceIds, boolean goRecurs)
    {
        DisplaySorted[] valret = null;
        int j;

        if (spaceIds == null)
        {
            return new DisplaySorted[0];
        }
        valret = new DisplaySorted[spaceIds.length];
        for (j=0;j<  valret.length ; j++)
        {
            valret[j] = buildSpaceObject(spaceIds[j]);
        } 
        Arrays.sort(valret);
        if (goRecurs)
        {
            DisplaySorted[] parents = valret;
            DisplaySorted[] childs = null;
            ArrayList       alValret = new ArrayList();
            int             i;
            SpaceInst       spaceInst;
            
            for (j=0;j<  parents.length ; j++)
            {
                alValret.add(parents[j]);
                spaceInst = m_administrationCtrl.getSpaceInstById("WA" + parents[j].id);
                childs = createSpaceObjects(spaceInst.getSubSpaceIds(),true);
                for (i=0;i<  childs.length ; i++)
                {
                    alValret.add(childs[i]);
                }                
            }
            valret = (DisplaySorted[])alValret.toArray(new DisplaySorted[0]);
        }
        return valret;
    }

    protected DisplaySorted buildSpaceObject(String spaceId)
    {
        DisplaySorted valret = new DisplaySorted();
        SpaceInst     spaceInst;

        valret.id = getShortSpaceId(spaceId);
        valret.isVisible = true;
        spaceInst = m_administrationCtrl.getSpaceInstById("WA" + valret.id);
        if ((spaceInst.getDomainFatherId() == null) || (spaceInst.getDomainFatherId().length() <= 0) || (spaceInst.getDomainFatherId().equals("0")))
        {
            valret.type = DisplaySorted.TYPE_SPACE;
            valret.isAdmin = m_ManageableSpaces.contains(valret.id);
            if (!valret.isAdmin)
            { // Rattrapage....
                String[] manageableSubSpaceIds = m_administrationCtrl.getUserManageableSubSpaceIds(m_user.getId(), valret.id);
                if ((manageableSubSpaceIds == null) || (manageableSubSpaceIds.length <= 0))
                {
                    valret.isVisible = false;
                }
            }
        }
        else
        {
            valret.type = DisplaySorted.TYPE_SUBSPACE;
            valret.isAdmin = isAdminOfSpace(spaceInst);
            if (!valret.isAdmin)
            { // Rattrapage....
                String[] manageableSubSpaceIds = m_administrationCtrl.getUserManageableSubSpaceIds(m_user.getId(), valret.id);
                if ((manageableSubSpaceIds == null) || (manageableSubSpaceIds.length <= 0))
                {
                    valret.isVisible = false;
                }
            }
        }
        valret.name = spaceInst.getName(m_SessionCtrl.getLanguage());
        valret.orderNum = spaceInst.getOrderNum();
        valret.deep = spaceInst.getLevel();
        buildSpaceHTMLLine(valret);
        SilverTrace.info("jobStartPagePeas", "NavBarManager.buildSpaceObject()", "root.MSG_GEN_PARAM_VALUE", "Space=" + valret.id + " Name=" + valret.name + " Type=" + valret.type);
        return valret;
    }

    protected String getShortSpaceId(String spaceId)
    {
        if ((spaceId != null) && (spaceId.startsWith("WA")))
        {
            return spaceId.substring(2);
        }
        else
        {
            return (spaceId == null) ? "" : spaceId;
        }
    }
    
    protected void buildSpaceHTMLLine(DisplaySorted space)
    {
        if (space.isVisible)
        {
            if (space.type == DisplaySorted.TYPE_SUBSPACE)
            {
                String link;
                int    objType;
                String spaceName;
                StringBuffer spacesSpaces = new StringBuffer();

                objType = (space.id.equals(m_CurrentSubSpaceId)) ? SPACE_EXPANDED : SPACE_COLLAPSE;
                link = "GoToSubSpace?SubSpace="+space.id;
                if (m_SessionCtrl.isSpaceInMaintenance(space.id))
                    spaceName = space.name + " (M)";
                else
                    spaceName = space.name;
                for (int i = 0; i < space.deep - 1; i++)
                {
                    spacesSpaces.append("&nbsp&nbsp");
                }
                space.htmlLine = spacesSpaces.toString() + "<a name=\""+space.id+"\"/>" + urlFactory(link, "space"+space.id, "", spaceName, SPACE, objType, m_sContext, "", space);
            }
            else
            {
                StringBuffer sb = new StringBuffer();
                sb.append("<option ");
                if (space.id.equals(m_CurrentSpaceId))
                    sb.append("selected ");
                sb.append("value=" + space.id + ">" + space.name);
                if (m_SessionCtrl.isSpaceInMaintenance(space.id))
                    sb.append(" (M)");
                sb.append("</option>");
                space.htmlLine = sb.toString();
            }
        }
        else
        {
            space.htmlLine = "";
        }
    }

    protected boolean isAdminOfSpace(SpaceInst spaceInst)
    {
        boolean     valret = m_ManageableSpaces.contains(getShortSpaceId(spaceInst.getId())) || m_ManageableSpaces.contains(getShortSpaceId(spaceInst.getDomainFatherId()));
        SpaceInst   parcSpaceInst = spaceInst;

        while ((!valret) && (parcSpaceInst.getDomainFatherId() != null) && (parcSpaceInst.getDomainFatherId().length() > 0) && (!parcSpaceInst.getDomainFatherId().equals("0")))
        {
            parcSpaceInst = m_administrationCtrl.getSpaceInstById(parcSpaceInst.getDomainFatherId());
            valret = m_ManageableSpaces.contains(getShortSpaceId(parcSpaceInst.getId()));
        }
        
        return valret;
    }

    protected DisplaySorted[] createComponentObjects(SpaceInst spaceInst, boolean subSpaces)
    {
        // Get the space's components
        ArrayList       components = spaceInst.getAllComponentsInst();
        ComponentInst   ci;
        String          label;
        String          link;
        DisplaySorted[] valret;
        int             objType;
        boolean         isTheSpaceAdmin = isAdminOfSpace(spaceInst);
        StringBuffer    componentsSpaces = new StringBuffer();

        valret = new DisplaySorted[components.size()];
        for (int i = 0;i < components.size(); i++)
        {
            valret[i] = new DisplaySorted();
            ci = (ComponentInst)components.get(i);
            valret[i].name = ci.getLabel(m_SessionCtrl.getLanguage());
            if (valret[i].name == null)
            {
                valret[i].name = ci.getName();
            }
            valret[i].orderNum = ci.getOrderNum();
            valret[i].id = ci.getId();
            valret[i].type = DisplaySorted.TYPE_COMPONENT;
            valret[i].isAdmin = isTheSpaceAdmin;
            valret[i].isVisible = isTheSpaceAdmin;
            valret[i].deep = spaceInst.getLevel();
        }
        Arrays.sort(valret);
        for (int i = 0;i < components.size(); i++)
        {
            if (valret[i].isVisible)
            {
                ci = (ComponentInst)components.get(i);
                // Build HTML Line
                label = ci.getLabel(m_SessionCtrl.getLanguage());
                if ((label == null) || (label.length() == 0))
                    label = ci.getName();
                    
//                String id = ci.getId().substring(ci.getName().length());
                link = "GoToComponent?ComponentId=" +ci.getId();
                if (subSpaces)
                {
                    if (i+1 == components.size())
                    {
                        objType = SUBSPACE_LAST_COMPONENT;
                    }
                    else
                    {
                        objType = SUBSPACE_COMPONENT;
                    }
                }
                else
                {
                    objType = SPACE_COMPONENT;
                }
                componentsSpaces = new StringBuffer();
                for (int j = 0; j < valret[i].deep - 1; j++)
                {
                    componentsSpaces.append("&nbsp&nbsp");
                }
                valret[i].htmlLine = componentsSpaces.toString() + urlFactory(link, "element" + m_elmtCounter++, ci.getName(), label, getComponentElementType(ci), objType, m_sContext, (m_bAdministrationAccess) ? "startPageContent" : "MyMain", valret[i]);
            }
            else
            {
                valret[i].htmlLine = "";
            }
        }
        return valret;
    }

    protected String urlFactory(String link, String elementLabel, String imageLinked, String labelLinked, int elementType, int imageType, String m_sContext, String target, DisplaySorted extraInfos)
    {
        StringBuffer result	= new StringBuffer();
        String boldStart	= "";
        String boldEnd	= "";

        switch (elementType)
        {
        case SPACE :
                target  = "";
                boldStart = "";
                boldEnd   = "";
                break;
        case COMPONENT :
                if ((target != null) && (target.length() > 0))
                {
                    target  = "TARGET=\"" + target + "\"";
                }
                boldStart = "";
                boldEnd   = "";
                break;
        case COMPONENTPOPUP :
                target  = "TARGET=\"_blank\"";
                boldStart = "";
                boldEnd   = "";
                break;
        }
        imageLinked = "<img name=\""+elementLabel+"\" src=\""+ m_sContext +"/util/icons/component/"+ imageLinked + "Small.gif\" border=\"0\" onLoad=\"\" align=\"absmiddle\">";
        switch (imageType)
        {
        case SPACE_COLLAPSE :
                result.append("<a href=\"").append(link).append("\"").append(target).append("><img src=\"").append(m_sContext).append("/util/icons/plusTree.gif\" border=0 align=\"absmiddle\"></a>");
                imageLinked = "<img name=\""+elementLabel+"\" src=\""+ m_sContext +"/util/icons/colorPix/1px.gif\" width=1 height=1 border=0 align=\"absmiddle\">";
                break;
        case SPACE_EXPANDED :
                result.append("<a href=\"").append(link).append("\"").append(target).append("><img src=\"").append(m_sContext).append("/util/icons/minusTree.gif\" border=0 align=\"absmiddle\"></a>");
                imageLinked = "<img name=\""+elementLabel+"\" src=\""+ m_sContext +"/util/icons/colorPix/1px.gif\" width=1 height=1 border=0 align=\"absmiddle\">";
                break;
        case SPACE_COMPONENT :
                break;
        case SUBSPACE_COMPONENT :
                result.append("<img src=\"").append(m_sContext).append("/util/icons/minusTreeT.gif\" border=0 align=\"absmiddle\">");
                break;
        case SUBSPACE_LAST_COMPONENT :
                result.append("<img src=\"").append(m_sContext).append("/util/icons/minusTreeL.gif\" border=0 align=\"absmiddle\">");
                break;
        }
        result.append("<a href=\"").append(link).append("\" ").append(target).append(">").append(imageLinked).append("&nbsp</a>");
        result.append("<a href=\"").append(link).append("\" ").append(target).append(">").append(boldStart).append(labelLinked).append(boldEnd).append("</a><br>");
        return result.toString();
    }
    // check if component is to open in a new window
    protected boolean displayToPopup(ComponentInst componentInst)
    {
		String value = componentInst.getParameterValue(POPUP_PARAM_NAME);
		if (value != null && value.trim().toLowerCase().equals("y"))
		{
			return true;
		}
        return false;
    }
    protected int getComponentElementType(ComponentInst componentInst)
    {
        if (displayToPopup(componentInst) && !m_bAdministrationAccess)
            return COMPONENTPOPUP;
        return COMPONENT;
    }
};
