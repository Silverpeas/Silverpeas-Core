/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

/*
 * ButtonWA.java
 *
 * Created on 10 octobre 2000, 16:18
 */

package org.silverpeas.core.web.util.viewgenerator.html.buttons;

import org.apache.ecs.xhtml.script;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neysseri
 * @version
 */
public abstract class AbstractButton implements Button {

  private static final Pattern ACTION_JS_DETECTION =
      Pattern.compile("(?i)^(\\s*([a-z]+)\\s*:\\s*([a-z]+)\\s*=\\s*)");

  public String label;
  private String action;
  public boolean disabled;
  private String actionPreProcessing;

  // private String iconsPath = null;

  /**
   * Creates new ButtonWA
   */
  public AbstractButton() {
  }

  /**
   * Method declaration
   * @param label
   * @param action
   * @param disabled
   * @see
   */
  @Override
  public void init(String label, String action, boolean disabled) {
    this.label = label;
    this.action = action;
    this.disabled = disabled;
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  /**
   * Method declaration
   * @param s
   * @see
   */
  @Override
  public void setRootImagePath(String s) {
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  @Override
  public final String print() {
    StringBuilder sb = new StringBuilder();

    // Handling action pre processing
    if (StringUtil.isDefined(actionPreProcessing)) {
      String theAction = action;
      String javascriptSpecif = "";
      Matcher javascriptMatcher = ACTION_JS_DETECTION.matcher(action);
      if (javascriptMatcher.find()) {
        theAction = theAction.substring(javascriptMatcher.group(1).length());
        javascriptSpecif = javascriptMatcher.group(2) + ":" + javascriptMatcher.group(3) + "=";
      } else {
        theAction = "jQuery('<form>', {'method':'GET', 'action':'" + theAction + "'}).submit();";
        theAction = unEscapeForMessageFormatting(theAction);
        javascriptSpecif = "javascript:onClick=";
      }

      String tempActionPreProcessing = escapeForMessageFormatting(actionPreProcessing);
      tempActionPreProcessing = MessageFormat
          .format(tempActionPreProcessing.replace("'", "''").replace("@#<#@action@#>#@", "{0}"),
              theAction);
      tempActionPreProcessing = unEscapeForMessageFormatting(tempActionPreProcessing);

      // Writing the function that handles the action call.
      script actionPreProcessFunction = new script().setType("text/javascript");
      actionPreProcessFunction.addElement("function handleButtonAction() {\n");
      actionPreProcessFunction.addElement(tempActionPreProcessing);
      actionPreProcessFunction.addElement("\n}");
      sb.append(actionPreProcessFunction.toString()).append("\n");

      // Changing action
      action = javascriptSpecif + "handleButtonAction();";
    }

    // Add button HTML
    sb.append(renderButtonHtml());
    return sb.toString();
  }

  abstract protected String renderButtonHtml();

  @Override
  public void setActionPreProcessing(final String actionPreProcessing) {
    this.actionPreProcessing = actionPreProcessing;
  }

  /**
   * Gets the action with pre processing if any.
   * @return the action as string.
   */
  protected String getAction() {
    return action;
  }

  private String escapeForMessageFormatting(String jsContent) {
    String escapedJsContent = actionPreProcessing.replaceAll("[{]", "@#<#@");
    return escapedJsContent.replaceAll("[}]", "@#>#@");
  }

  private String unEscapeForMessageFormatting(String escapedJsContent) {
    String jsContent = escapedJsContent.replaceAll("@#<#@", "{");
    return jsContent.replaceAll("@#>#@", "}");
  }
}
