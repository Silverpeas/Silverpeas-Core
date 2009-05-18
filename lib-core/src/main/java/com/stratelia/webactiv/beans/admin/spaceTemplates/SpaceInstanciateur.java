package com.stratelia.webactiv.beans.admin.spaceTemplates;

import java.io.File;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.Vector;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.ResourceLocator;

public class SpaceInstanciateur extends Object 
{
    private static ResourceLocator resources=null;
    private static String xmlPackage="";
    private Hashtable spaceTemplates = new Hashtable();

    // Init Function
    static 
    {
        try 
        {
            resources = new ResourceLocator("com.stratelia.webactiv.beans.admin.admin", "");
            xmlPackage = resources.getString("xmlSpaceTemplate");
            xmlPackage = xmlPackage.trim();
        }
        catch (MissingResourceException mre) 
        {
            SilverTrace.fatal("admin", "Instanciateur.static", "admin.MSG_INSTANCIATEUR_RESOURCES_NOT_FOUND", mre);
        }
    }

    public SpaceInstanciateur(Hashtable allComponentsModels)
    {
        File file = new File(xmlPackage);
        String[] list = file.list();

        Vector vector = new Vector();
        for (int i = 0; list != null && i < list.length; i++)
        {
          if (list[i].toLowerCase().endsWith(".xml"))
          {
            vector.addElement(list[i].substring(0, list[i].length() - 4));
          }
        }
        int count = vector.size();
        list = new String[count];
        for (int i = 0; i < list.length; i++)
        {
          list[i] = (String)vector.elementAt(i);
        }
        for (int i = 0; i < list.length; i++)
        {
          String spaceName = list[i];
          String fullPath = xmlPackage + File.separator + spaceName + ".xml";
          SilverTrace.info("admin", "SpaceInstanciateur.SpaceInstanciateur", "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "space name: '" + spaceName + "', full path: '" + fullPath + "'");
          spaceTemplates.put(spaceName, new SpaceTemplate(fullPath,allComponentsModels));
        }
    }

    public Hashtable getAllSpaceTemplates()
    {
        return spaceTemplates;
    }

    public SpaceTemplateProfile[] getTemplateProfiles(String templateName)
    {
        SpaceTemplate st = (SpaceTemplate)spaceTemplates.get(templateName);

        if (st == null)
        {
            SilverTrace.info("admin", "SpaceInstanciateur.getTemplateMappedComponentProfile", "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "template Name : '" + templateName + "' NOT FOUND !!!!!!!!!");
            return new SpaceTemplateProfile[0];
        }
        else
        {
            SilverTrace.info("admin", "SpaceInstanciateur.getTemplateMappedComponentProfile", "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "template Name : '" + templateName);
            return st.getTemplateProfiles();
        }
    }

    public SpaceInst getSpaceToInstanciate(String templateName)
    {
        SpaceTemplate st = (SpaceTemplate)spaceTemplates.get(templateName);

        if (st == null)
        {
            SilverTrace.info("admin", "SpaceInstanciateur.getSpaceToInstanciate", "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "template Name : '" + templateName + "' NOT FOUND !!!!!!!!!");
            return null;
        }
        else
        {
            SilverTrace.info("admin", "SpaceInstanciateur.getSpaceToInstanciate", "admin.MSG_INFO_BUILD_WA_COMPONENT_LIST", "template Name : '" + templateName);
            return st.makeSpaceInst();
        }
    }
} // class
