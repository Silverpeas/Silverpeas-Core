package com.stratelia.webactiv.util.indexEngine.parser.htmlParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import au.id.jericho.lib.html.Source;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.parser.Parser;

public class HTMLParser2 implements Parser
{
    public HTMLParser2() {}

    public Reader getReader(String path, String encoding)
    {
        Reader reader = null;
        InputStream file = null;

        try
        {
            file = new FileInputStream(path);
            
            Source source = new Source(file);

            if (source != null)
            	reader = new StringReader(source.getTextExtractor().toString());
        }
        catch (Exception e)
        {
	        SilverTrace.error("indexEngine", "HTMLParser2", "indexEngine.MSG_IO_ERROR_WHILE_READING", path, e);
        }
        finally
        {
        	try
        	{
        		file.close();
        	}
        	catch (IOException ioe)
        	{
        		SilverTrace.error("indexEngine", "HTMLParser2.getReader()", "indexEngine.MSG_IO_ERROR_WHILE_CLOSING", path, ioe);
        	}
        }
        return reader;
    }
}