/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.list;

import org.apache.ecs.ElementContainer;
import org.apache.ecs.xhtml.div;
import org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPaneTag;
import org.silverpeas.core.web.util.viewgenerator.html.buttons.ButtonTag;

import javax.servlet.jsp.JspException;
import java.io.Serializable;

import static java.lang.String.format;
import static org.silverpeas.core.util.StringUtil.getBooleanValue;
import static org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination.INDEX_PARAMETER_NAME;
import static org.silverpeas.core.web.util.viewgenerator.html.pagination.Pagination.ITEMS_PER_PAGE_PARAM;

/**
 * Create a new simple list pane.
 * @author silveryocha
 */
public class AccumulativeListPaneTag extends AbstractListPaneTag {
  private static final long serialVersionUID = -237212701930752379L;
  static final String NEXT_PARAMETER_NAME = "AccListNextAction";

  private String targetListId;
  private String nextActionLabel;
  private int batchSize = DEFAULT_NB_ITEM_PER_PAGE;
  private boolean moreItems = false;

  public void setTargetListId(final String targetListId) {
    this.targetListId = targetListId;
    setId(targetListId + "-pane");
  }

  public void setNextActionLabel(final String nextActionLabel) {
    this.nextActionLabel = nextActionLabel;
  }

  String getNextActionLabel() {
    return nextActionLabel;
  }

  public void setBatchSize(final int batchSize) {
    this.batchSize = batchSize;
  }

  void setMoreItems(final boolean moreItems) {
    this.moreItems = moreItems;
  }

  @Override
  public int doStartTag() throws JspException {
    if (!getBooleanValue(getRequest().getParameter(NEXT_PARAMETER_NAME))) {
      clearState();
    }
    return super.doStartTag();
  }

  @Override
  public int doEndTag() throws JspException {
    final div listPane = new div();
    listPane.setID(getId());
    listPane.setClass("acc-list-pane");
    listPane.addElement(getBodyContent().getString());
    if (moreItems) {
      listPane.addElement(getActionPane());
    }
    new ElementContainer().addElement(listPane).output(pageContext.getOut());
    return super.doEndTag();
  }

  private ElementContainer getActionPane() throws JspException {
    final State state = getState();
    final String baseUrl = getRoutingAddress();
    final StringBuilder url = new StringBuilder(baseUrl);
    if (baseUrl.indexOf('?') < 0) {
      url.append("?");
    } else {
      url.append("&");
    }
    url.append(NEXT_PARAMETER_NAME).append("=true");
    url.append("&").append(ITEMS_PER_PAGE_PARAM).append("=").append(state.getBatchSize());
    url.append("&").append(INDEX_PARAMETER_NAME).append("=").append(state.getNextStartIndex());
    final String nextAction = format("sp.accListPane.nextItems('%s', '%s')", url, targetListId);
    final ButtonPaneTag buttonPaneTag = new ButtonPaneTag();
    buttonPaneTag.setPageContext(pageContext);
    buttonPaneTag.setParent(this);
    buttonPaneTag.setCssClass("acc-list-pane-actions");
    final ButtonTag buttonTag = new ButtonTag();
    buttonTag.setPageContext(pageContext);
    buttonTag.setParent(buttonPaneTag);
    buttonTag.setClasses("acc-list-pane-next-action");
    buttonTag.setLabel(getNextActionLabel());
    buttonTag.setAction("javascript:" + nextAction);
    buttonTag.doEndTag();
    return buttonPaneTag.getContent();
  }

  State getState() {
    final String sessionKey = this.getClass().getSimpleName() + getVar();
    State state = (State) getSession().getAttribute(sessionKey);
    if (state == null) {
      state = new State(batchSize);
      getSession().setAttribute(sessionKey, state);
    }
    return state;
  }

  private void clearState() {
    final String sessionKey = this.getClass().getSimpleName() + getVar();
    getSession().removeAttribute(sessionKey);
  }

  static class State implements Serializable {
    private static final long serialVersionUID = 2766488468504645179L;
    private final int batchSize;
    private int currentListSize = -1;
    private int currentStartIndex = -1;
    private int nextStartIndex = -1;

    State(final int batchSize) {
      this.batchSize = batchSize;
    }

    int getBatchSize() {
      return batchSize;
    }

    int getCurrentListSize() {
      return currentListSize;
    }

    void setCurrentListSize(final int currentListSize) {
      this.currentListSize = currentListSize;
    }

    int getCurrentStartIndex() {
      return currentStartIndex;
    }

    void setCurrentStartIndex(final int previousStartIndex) {
      this.currentStartIndex = previousStartIndex;
    }

    int getNextStartIndex() {
      return nextStartIndex;
    }

    void setNextStartIndex(final int nextStartIndex) {
      this.nextStartIndex = nextStartIndex;
    }

    boolean isFirstDisplay() {
      return currentListSize == -1;
    }
  }
}
