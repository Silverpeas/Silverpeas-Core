/*
 * author
 * Mohammed Hguig
 */


package com.stratelia.webactiv.util.indexEngine.parser.psParser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

import com.stratelia.webactiv.util.indexEngine.parser.PipedParser;


/**
 * the psParser parse a postscript file
 */

 public class PsParser extends PipedParser
 {

   public PsParser()
   {
   }

/**
 * outPutContent read the text content
 * of a ps file and store it in out
 * to be ready to be indexed
 */

   public void outPutContent(Writer out, String path, String encoding) throws IOException
   {
     
	   BufferedReader buffer = null;

       try
       {
		  InputStream file = new FileInputStream(path);
		  if (encoding != null)
		  {
			buffer = new BufferedReader(new InputStreamReader(file, encoding));
		  }
		  else
		  {
			buffer = new BufferedReader(new InputStreamReader(file));
		  }

	 	  outPutChar(out, buffer);
		}
		finally
		{
			if (buffer != null) buffer.close();
		}
   }


/**
 * read the text content between parses ( and )
 */

   public void outPutChar(Writer out, BufferedReader buffer) throws IOException
   {
		int ch, para=0, last=0;
		char charr = 0;

        while ( (ch = buffer.read()) != -1 )
		{
			charr = (char) ch;
			switch (ch)
			{
				case '%'  : if (para==0) 
								{
									buffer.readLine();
								}
							else 
								{
									out.write(charr);
								}
				case '\n' : if (last==1) 
								{ 
									out.write("");
									out.write('\n');
									last=0; 
								} 
							break;
				case '('  : if (para++>0) 
								{ 
									out.write(charr); 
								} 
							break;
				case ')'  : if (para-->1) 
								{ 
									out.write(charr); 
								}
							else 
								{
									out.write(' ');
								}
							last=1; break;
				case '\\' : if (para>0)
						switch(charr = (char) buffer.read())
						{
							case '(' :
							case ')' :  out.write(charr); break;
							case 't' :  out.write('\t'); break;
							case 'n' :  out.write('\n'); break;
							case '\\':  out.write('\\'); break;
							case '0' :  case '1' : case '2' : case '3' :
							case '4' :  case '5' : case '6' : case '7' :
									out.write('\\');
							default  :  out.write(charr);
										break;
						}
						break;
                default	  : if (para>0) 
							    {
									out.write(charr); 
								}
			}
						
						
		}
   }
 }
			
