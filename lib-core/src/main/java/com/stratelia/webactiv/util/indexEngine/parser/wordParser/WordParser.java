package com.stratelia.webactiv.util.indexEngine.parser.wordParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.apache.poi.hwpf.extractor.WordExtractor;

import com.stratelia.webactiv.util.indexEngine.parser.Parser;

/**
 * A WordParser parse a Word file.
 * Use an open source java library named textmining
 * Class WordExtractor extracts the text from a Word 6.0/95/97/2000/XP word doc
 * @author neysseri
 */
public class WordParser implements Parser
{

    /**
     * Constructor declaration
     */
    public WordParser() {}

    /**
     * Method declaration
     * 
     * 
     * @param path
     * @param encoding
     * 
     * @return
     */
    public Reader getReader(String path, String encoding)
    {
    	//SilverTrace.debug("indexEngine", "WordParser.getReader", "root.MSG_GEN_ENTER_METHOD");
        Reader reader = null;
        InputStream file = null;

        try
        {
            file = new FileInputStream(path);
            
            WordExtractor extractor = new WordExtractor(file);
            
            //SilverTrace.debug("indexEngine", "WordParser.getReader", "root.MSG_GEN_PARAM_VALUE", "WordExtrator loaded");
            
            String wordText = extractor.getText();
            
            //SilverTrace.debug("indexEngine", "WordParser.getReader", "root.MSG_GEN_PARAM_VALUE", "text extracted !");
            
            //WordExtractor extractor = new WordExtractor();
            //String wordText = extractor.extractText(file);
            
            reader = new StringReader(wordText);
        }
        catch (Exception e)
        {
	        //SilverTrace.error("indexEngine", "WordParser", "indexEngine.MSG_IO_ERROR_WHILE_READING", path, e);
        }
        finally
        {
        	try
        	{
        		file.close();
        	}
        	catch (IOException ioe)
        	{
        		//SilverTrace.error("indexEngine", "WordParser.getReader()", "indexEngine.MSG_IO_ERROR_WHILE_CLOSING", path, ioe);
        	}
        }
        //SilverTrace.debug("indexEngine", "WordParser.getReader", "root.MSG_GEN_EXIT_METHOD");
        return reader;
    }
}