/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.webactiv.util.viewGenerator.html.operationPanes;

import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public class OperationPaneSilverpeasV5StringFactory extends Object {
  /**
   * Hashtable which contains the specifics code encoded as key and their values
   * are right code encoded
   */
  private static StringBuffer printString1 = null;
  private static StringBuffer printString2 = null;

  public static StringBuffer getPrintString1() {
    if (printString1 == null) {
      synchronized (OperationPaneSilverpeasV5StringFactory.class) {
        if (printString1 == null) {
          printString1 = new StringBuffer();
          printString1
              .append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"1\">");
          printString1.append("<tr><td><img src=\"").append(
              GraphicElementFactory.getIconsPath()).append(
              "/tabs/1px.gif\"></td></tr>\n");
        }
      }
    }
    return printString1;
  }

  public static StringBuffer getPrintString2() {
    if (printString2 == null) {
      synchronized (OperationPaneSilverpeasV5StringFactory.class) {
        if (printString2 == null) {
          printString2 = new StringBuffer();
          printString2.append("</table>\n");
        }
      }
    }
    return printString2;
  }

}