package com.stratelia.webactiv.util.repositoryImport;
/*
 * Created by IntelliJ IDEA.
 * User: Mikhail_Nikolaenko
 * Date: Jul 2, 2002
 * Time: 10:26:57 AM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */

public class BatchProperties
{
    private String space_id;
    private String component_id;
    private String theme_id;
    private String path;
    private String publisher_id;
    private int subfolder_level;
    private boolean is_valid_properties;

    public BatchProperties( String space_id, String component_id, String theme_id,
                            String publisher_id, String path, String subfolder)
    {
        this.space_id = space_id;
        this.component_id = component_id;
        this.theme_id = theme_id;
        this.publisher_id = publisher_id;
        this.path = path;
        if ( subfolder == null || "".equals(subfolder) )
        {
            this.subfolder_level = 0;
        }
        else if ( "ALL".equalsIgnoreCase(subfolder) )
        {
            this.subfolder_level = -1;
        }
        else
        {
            this.subfolder_level = (new Integer(subfolder)).intValue();
        }

        if ( (space_id != null) && (!"".equals(space_id)) &&
             (component_id != null) && (!"".equals(component_id)) &&
             (theme_id != null) && (!"".equals(theme_id)) &&
             (publisher_id != null) && (!"".equals(publisher_id)) &&
             (path != null) && (!"".equals(path)) )
        {
            is_valid_properties = true;
        }
        else
        {
            is_valid_properties = false;
        }
    }

    public boolean isValidProperties()
    {
        return is_valid_properties;
    }

    public String getSpaceID()
    {
        return space_id;
    }

    public String getComponentID()
    {
        return component_id;
    }

    public String getThemeID()
    {
        return theme_id;
    }

    public String getPublisherID()
    {
        return publisher_id;
    }

    public String getPath()
    {
        return path;
    }

    public int getSubfolderLevel()
    {
        return subfolder_level;
    }
}
