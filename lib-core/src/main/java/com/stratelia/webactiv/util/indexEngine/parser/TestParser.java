package com.stratelia.webactiv.util.indexEngine.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Executable used to test a parser.
 */
public class TestParser
{
  static public void main(String[] argv)
  {
    if (argv.length != 2)
    {
      System.err.println("usage: java com...TestParser  mime-type file");
      System.exit(1);
    }
    Parser parser = ParserManager.getParser(argv[0]);
    if (parser == null)
    {
      System.err.println("unknown mime-type : "+ argv[0]);
      System.exit(1);
    }
    Reader reader = parser.getReader(argv[1], null);
    if (parser == null)
    {
      System.err.println("unknown file : "+ argv[1]);
      System.exit(1);
    }
    
    BufferedReader bReader = new BufferedReader(reader);
    try
    {
      String line ;
      while ((line = bReader.readLine()) != null)
      {
        System.out.println(line);
      }
    }
    catch (IOException e)
    {
      System.err.println("io error");
      System.exit(1);
    }
  }
}
