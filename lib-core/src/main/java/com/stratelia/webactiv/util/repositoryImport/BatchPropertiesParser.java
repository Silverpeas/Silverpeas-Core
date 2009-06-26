package com.stratelia.webactiv.util.repositoryImport;
/*
 * Created by IntelliJ IDEA.
 * User: Mikhail_Nikolaenko
 * Date: Jul 2, 2002
 * Time: 10:24:25 AM
 * To change template for new class use 
 * Code Style | Class Templates options (Tools | IDE Options).
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.Vector;

public class BatchPropertiesParser
{
    private static final int INITIAL_CAPACITANCE = 100;
    private Vector properties = new Vector(INITIAL_CAPACITANCE);
    private int properties_pointer;

    private static final String SPACE_DESCRIPTOR = "SPACE";
    private static final String COMPONENT_DESCRIPTOR = "COMPONENT";
    private static final String THEME_DESCRIPTOR = "THEME";
    private static final String PUBLISHER_DESCRIPTOR = "PUBLISHER";
    private static final String PATH_DESCRIPTOR = "PATH";
    private static final String SUBFOLDER_LEVEL = "SUBFOLDERLEVEL";

    public BatchPropertiesParser()
    {
        properties_pointer = -1;
    }

    public BatchPropertiesParser( String file_name )
    {
        loadProperties( file_name );
    }

    public BatchPropertiesParser( Reader file_reader )
    {
        loadProperties( file_reader );
    }

    public boolean loadProperties( String file_name )
    {
        File prop_file = new File( file_name );
        if ( !prop_file.exists() )
        {
            return false;
        }
        try
        {
            FileInputStream fis = new FileInputStream( prop_file );
            InputStreamReader isr = new InputStreamReader( fis );
            return loadProperties((Reader) isr);
        }
        catch ( Exception ex)
        {
            return false;
        }
    }

    public boolean loadProperties( Reader file_reader )
    {
        try
        {
            LineNumberReader lnr = new LineNumberReader( file_reader );

            String space_id;
            String component_id;
            String theme_id;
            String publisher_id;
            String path;
            String subfolder_level;

            String line_to_parse = lnr.readLine();
            String value;

            while ( line_to_parse != null )
            {
                space_id = null;
                component_id = null;
                theme_id = null;
                publisher_id = null;
                path = null;
                subfolder_level = null;

                line_to_parse.trim();
                int index;

                while ( line_to_parse != null && !"<".equals(line_to_parse.trim()) )
                {
                    line_to_parse = lnr.readLine();
                }
                if ( line_to_parse != null )
                {
                    line_to_parse = lnr.readLine();
                    while ( line_to_parse != null && !">".equals(line_to_parse.trim()) )
                    {
                        index = line_to_parse.indexOf("=");
                        if ( index > 0 )
                        {
                            value = line_to_parse.substring( 0, index ).toUpperCase();
                            line_to_parse = line_to_parse.substring(++index).trim();
                            if ( value.indexOf( SPACE_DESCRIPTOR ) >= 0 )
                            {
                                space_id = line_to_parse;
                            }
                            else if( value.indexOf( COMPONENT_DESCRIPTOR ) >= 0 )
                            {
                                component_id = line_to_parse;
                            }
                            else if ( value.indexOf( THEME_DESCRIPTOR ) >= 0 )
                            {
                                theme_id = line_to_parse;
                            }
                            else if ( value.indexOf( PUBLISHER_DESCRIPTOR ) >= 0 )
                            {
                                publisher_id = line_to_parse;
                            }
                            else if ( value.indexOf( PATH_DESCRIPTOR ) >= 0 )
                            {
                                path = line_to_parse;
                            }
                            else if ( value.indexOf( SUBFOLDER_LEVEL ) >= 0 )
                            {
                                subfolder_level = line_to_parse;
                            }
                            line_to_parse = lnr.readLine();
                        }
                    }
                    properties.add( new BatchProperties( space_id, component_id, theme_id,
                                                         publisher_id, path, subfolder_level) );
                }
            }

            if ( properties.size() < 1 )
            {
                return false;
            }
        }
        catch ( Exception ex)
        {
            return false;
        }

        return true;
    }

    public int getPropertiesCount()
    {
        return properties.size();
    }

    public BatchProperties getFirstProperties()
    {
        properties_pointer = 0;
        if ( properties.size() > 0 )
        {
            return (BatchProperties) properties.get(properties_pointer);
        }
        else
        {
            return null;
        }
    }

    public BatchProperties getNextProperties()
    {
        if ( ++properties_pointer < properties.size() )
        {
            return (BatchProperties) properties.get(properties_pointer);
        }
        else
        {
            return null;
        }
    }

    public BatchProperties getProperties( int index )
    {
        if ( index < properties.size() )
        {
            return (BatchProperties) properties.get(index);
        }
        else
        {
            return null;
        }
    }
}
