/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
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

package com.stratelia.webactiv.util.viewGenerator.html.progressMessage;

import com.stratelia.silverpeas.peasCore.URLManager;
import java.util.List;

import com.stratelia.webactiv.util.ResourceLocator;
import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.script;

/**
 * @author neysseri
 * @version
 */
public abstract class AbstractProgressMessage implements ProgressMessage {

  protected List<String> messages;
  protected ResourceLocator multilang;
  private ElementContainer xhtmlRenderer = new ElementContainer();

  public AbstractProgressMessage() {
  }

  @Override
  public void init(List<String> messages) {
    this.messages = messages;
    script progressMessage = new script().setType("text/javascript").
        setSrc(URLManager.getApplicationURL() + "/util/javaScript/progressMessage.js");
    xhtmlRenderer.addElement(progressMessage);
  }

  @Override
  public void setMultilang(ResourceLocator resource) {
    multilang = resource;
  }

  public List<String> getMessages() {
    return messages;
  }

  public ResourceLocator getMultilang() {
    return multilang;
  }

  protected ElementContainer getXHTMLRenderer() {
    return xhtmlRenderer;
  }
}
