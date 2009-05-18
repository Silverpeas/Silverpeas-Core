package com.stratelia.webactiv.util.indexEngine.parser;

import java.io.Reader;

/**
 * A parser is used to retrieve the text content of a file.
 */
public interface Parser
{
  /**
   * Returns a Reader giving only the text content of the file.
   */
  Reader getReader(String path, String encoding);
}
