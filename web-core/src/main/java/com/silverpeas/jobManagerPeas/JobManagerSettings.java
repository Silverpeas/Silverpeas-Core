/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

/*
 * JobManagerSettings.java
 */

package com.silverpeas.jobManagerPeas;

import java.util.Arrays;
import java.util.Comparator;

import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.ResourceLocator;

/*
 * CVS Informations
 *
 * $Id: JobManagerSettings.java,v 1.5 2008/09/17 04:31:28 neysseri Exp $
 *
 * $Log: JobManagerSettings.java,v $
 * Revision 1.5  2008/09/17 04:31:28  neysseri
 * no message
 *
 * Revision 1.4.12.1  2008/09/10 14:29:25  psliwa
 * New component: Workflow Designer
 *
 * Revision 1.1  2008/05/30 16:10:44  cvsuser
 * *** empty log message ***
 *
 * Revision 1.4  2006/04/19 15:35:15  dlesimple
 * Gestion des Ips Sogreah
 *
 * Revision 1.3  2005/02/23 19:18:35  neysseri
 * intégration Import/Export
 *
 * Revision 1.2.2.1  2005/01/25 09:22:11  neysseri
 * Adding tool ImportExport
 *
 * Revision 1.2  2003/01/09 09:37:23  mguillem
 * correction getString
 *
 * Revision 1.1.1.1  2002/08/06 14:47:55  nchaix
 * no message
 *
 * Revision 1.2  2002/07/17 16:16:37  nchaix
 * Merge branche EPAM_130602
 * Ajout de l'onglet jobTools
 *
 * Revision 1.1.22.1  2002/07/17 06:20:21  neysseri
 * Add a property to display or not the tools tab.
 *
 * Revision 1.1  2002/04/11 06:42:53  tleroi
 * no message
 *
 *
 */

/**
 * This class manage the informations needed for job manager
 *
 * @t.leroi
 */
public class JobManagerSettings
{
    public static int     m_UsersByPage = 10;
    public static int     m_GroupsByPage = 10;
    public static boolean m_IsKMVisible = false;
	public static boolean m_IsToolsVisible = false;
	public static boolean m_IsToolSpecificAuthentVisible = false;
    public static boolean m_IsToolWorkflowDesignerVisible = false;
    public static boolean m_IsTemplateDesignerVisible = false;
    public static boolean m_IsPortletDeployerVisible = false;

    static
    {
        ResourceLocator rs = new ResourceLocator("com.silverpeas.jobManagerPeas.settings.jobManagerPeasSettings", "");

        m_UsersByPage = readInt(rs, "UsersByPage", 10);
        m_GroupsByPage = readInt(rs, "GroupsByPage", 10);
        m_IsKMVisible = readBoolean(rs,"IsKMVisible", false);
		m_IsToolsVisible = readBoolean(rs,"IsToolsVisible", false);
		m_IsToolSpecificAuthentVisible = readBoolean(rs,"IsToolSpecificAuthentVisible", false);
		m_IsToolWorkflowDesignerVisible = readBoolean(rs,"IsToolWorkflowDesignerVisible", false);
		m_IsTemplateDesignerVisible = readBoolean(rs,"IsTemplateDesignerVisible", false);
		m_IsPortletDeployerVisible = readBoolean(rs,"IsPortletDeployerVisible", false);
    }

    static protected int readInt(ResourceLocator rs, String propName, int defaultValue)
    {
        String s = rs.getString(propName, Integer.toString(defaultValue));
        return Integer.parseInt(s);
    }

    static protected boolean readBoolean(ResourceLocator rs, String propName, boolean defaultValue)
    {
        String s = null;
        if (defaultValue)
            s = rs.getString(propName, "true");
        else
            s = rs.getString(propName, "false");

        boolean valret = defaultValue;
        if (defaultValue)
        {
            if (s.equalsIgnoreCase("false"))
            {
                valret = false;
            }
        }
        else
        {
            if (s.equalsIgnoreCase("true"))
            {
                valret = true;
            }
        }

        return valret;
    }

    static public void sortGroups(Group[] toSort)
    {
        Arrays.sort(toSort, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return (((Group)o1).getName()).compareTo(((Group)o2).getName());
            }
            public boolean equals(Object o)
            {
                return false;
            }

        });
    }

    static public void sortUsers(UserDetail[] toSort)
    {
        Arrays.sort(toSort, new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return (((UserDetail)o1).getLastName()).compareTo(((UserDetail)o2).getLastName());
            }
            public boolean equals(Object o)
            {
                return false;
            }

        });
    }
}
