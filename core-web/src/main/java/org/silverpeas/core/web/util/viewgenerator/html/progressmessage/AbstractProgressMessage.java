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

package org.silverpeas.core.web.util.viewgenerator.html.progressmessage;

import org.silverpeas.core.util.URLUtil;
import java.util.List;

import org.silverpeas.core.util.LocalizationBundle;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.script;

/**
 * @author neysseri
 * @version
 */
public abstract class AbstractProgressMessage implements ProgressMessage {

  protected List<String> messages;
  protected LocalizationBundle multilang;
  private ElementContainer xhtmlRenderer = new ElementContainer();

  public AbstractProgressMessage() {
  }

  @Override
  public void init(List<String> messages) {
    this.messages = messages;
    script progressMessage = new script().setType("text/javascript").
        setSrc(URLUtil.getApplicationURL() + "/util/javaScript/progressMessage.js");
    xhtmlRenderer.addElement(progressMessage);
  }

  @Override
  public void setMultilang(LocalizationBundle resource) {
    multilang = resource;
  }

  public List<String> getMessages() {
    return messages;
  }

  public LocalizationBundle getMultilang() {
    return multilang;
  }

  protected ElementContainer getXHTMLRenderer() {
    return xhtmlRenderer;
  }
}
