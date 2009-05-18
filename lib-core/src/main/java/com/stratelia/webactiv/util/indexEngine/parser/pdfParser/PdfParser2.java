package com.stratelia.webactiv.util.indexEngine.parser.pdfParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.util.PDFTextStripper;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.indexEngine.parser.Parser;

/**
 * the pdfParser parse a pdf file
 */

 public class PdfParser2 implements Parser
 {

   public PdfParser2()
   {
   }

   public Reader getReader(String path, String encoding) {
	   
	   Reader reader = null;
       InputStream file = null;
       PDDocument document = null;
       try
       {
           file = new FileInputStream(path);
           
           document = PDDocument.load(file);
           
           PDFTextStripper extractor = new PDFTextStripper();
           String text = extractor.getText(document);
           
           reader = new StringReader(text);
       }
       catch (Exception e)
       {
	        SilverTrace.error("indexEngine", "PdfParser2", "indexEngine.MSG_IO_ERROR_WHILE_READING", path, e);
       }
       finally
       {
       		try
       		{
       			document.close();
       			file.close();
       		}
       		catch (IOException ioe)
       		{
       			SilverTrace.error("indexEngine", "PdfParser2.getReader()", "indexEngine.MSG_IO_ERROR_WHILE_CLOSING", path, ioe);
       		}
       }
       return reader;
   }

   

 }
