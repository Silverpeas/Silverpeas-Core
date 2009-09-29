/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

/*
 * ArrayPaneWA.java
 *
 * Created on 10 octobre 2000, 16:11
 */

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import java.util.Vector;

/**
 * The default implementation of ArrayPane interface
 * 
 * @author squere
 * @version 1.0
 */
public class OperationPaneWA2 extends AbstractOperationPane {

  /**
   * Constructor declaration
   * 
   * 
   * @see
   */
  public OperationPaneWA2() {
    super();
  }

  /**
   * Method declaration
   * 
   * 
   * @param iconPath
   * @param altText
   * @param action
   * 
   * @see
   */
  public void addOperation(String iconPath, String altText, String action) {
    String iconsPath = getIconsPath();
    Vector stack = getStack();
    StringBuffer operation = new StringBuffer();

    operation.append("<tr>\n");
    operation.append("<td class=line width=\"1\"><img src=\"")
        .append(iconsPath).append("/1px.gif\" border=\"0\"></td>\n");
    operation
        .append(
            "<td valign=\"top\" class=intfdcolor5 width=\"34\" align=center><a id=\"")
        .append(altText).append("\" href=\"").append(action).append(
            "\"><img src=\"").append(iconPath).append("\" alt=\"").append(
            altText).append("\" border=\"0\"></a></td>\n");
    operation.append("</tr>\n");
    stack.add(operation.toString());
  }

  /**
   * Method declaration
   * 
   * 
   * @see
   */
  public void addLine() {
    String iconsPath = getIconsPath();
    Vector stack = getStack();
    StringBuffer line = new StringBuffer();

    line.append("<tr>\n");
    line.append("<td class=line colspan=\"2\"><img src=\"").append(iconsPath)
        .append("/1px.gif\" width=\"35\" height=\"1\"></td>\n");
    line.append("</tr>\n");
    stack.add(line.toString());
  }

  /**
   * Method declaration
   * 
   * 
   * @return
   * 
   * @see
   */
  public String print() {
    StringBuffer result = new StringBuffer();
    Vector stack = getStack();

    if (stack.size() > 0) {
      addLine();
    }
    result
        .append("<table width=\"34\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
    for (int i = 0; i < stack.size(); i++) {
      result.append((String) stack.elementAt(i));
    }
    result.append("</table>");
    return result.toString();
  }

}