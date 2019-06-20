/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.progressmessage;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.script;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.JavascriptBundleProducer;
import org.silverpeas.core.web.util.viewgenerator.html.JavascriptSettingProducer;

/**
 * @author neysseri
 * @version
 */
public class ProgressMessageSilverpeasV5 extends AbstractProgressMessage {

  public ProgressMessageSilverpeasV5() {
  }

  /**
   * Method declaration
   * @return
   *
   */
  @Override
  public String print() {

    ElementContainer xhtmlRenderer = new ElementContainer();
    ElementContainer jsPluginRenderer = getXHTMLRenderer();
    if (StringUtil.isNotDefined(jsPluginRenderer.toString())) {
      return "";
    }

    String message1 = getMultilang().getString("GEF.progressMessage.message1");
    String message2 = getMultilang().getString("GEF.progressMessage.message2");

    if (getMessages() != null && !getMessages().isEmpty()) {
      String extMessage1 = getMessages().get(0);
      if (StringUtil.isDefined(extMessage1)) {
        message1 = extMessage1;
      }
      if (getMessages().size() >= 2) {
        String extMessage2 = getMessages().get(1);
        if (StringUtil.isDefined(extMessage2)) {
          message2 = extMessage2;
        }
      }
    }

    xhtmlRenderer.addElement(new script().setType("text/javascript").addElement(
        JavascriptSettingProducer.settingVariableName("ProgressMessageSettings")
            .add("progress.message.icon.url", GraphicElementFactory.getIconsPath() + "/inProgress.gif")
            .produce()));

    xhtmlRenderer.addElement(new script().setType("text/javascript").addElement(
        JavascriptBundleProducer.bundleVariableName("ProgressMessageBundle")
            .add("progress.message.1", message1)
            .add("progress.message.2", message2)
            .produce()));

    xhtmlRenderer.addElement(jsPluginRenderer);
    return xhtmlRenderer.toString();
  }
}