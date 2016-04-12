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

package org.silverpeas.web.workflowdesigner.taglib;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.workflow.api.model.DataFolder;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane;
import org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane;
import org.silverpeas.core.web.util.viewgenerator.html.icons.Icon;

/**
 * Class implementing the tag &lt;itemList&gt; from workflowEditor.tld
 */
public class ItemList extends TagSupport {

  private static final long serialVersionUID = -7885970074029478168L;
  private String strContext, strPaneTitleKey, strCurrentScreen;
  private DataFolder items;

  /*
   * (non-Javadoc)
   * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
   */
  public int doStartTag() throws JspException {
    GraphicElementFactory gef;
    MultiSilverpeasBundle resource;
    ArrayPane itemPane;
    ArrayLine row;
    ArrayColumn column;
    String strContextEncoded, strEditURL, strPaneTitle;
    StringBuilder sb = new StringBuilder();

    try {
      gef = (GraphicElementFactory) pageContext.getSession().getAttribute(
          "SessionGraphicElementFactory");
      resource = (MultiSilverpeasBundle) pageContext.getRequest().getAttribute(
          "resources");
      strPaneTitle = resource.getString(strPaneTitleKey);

      itemPane = gef.getArrayPane("itemList", strCurrentScreen, pageContext
          .getRequest(), pageContext.getSession());
      itemPane.setVisibleLineNumber(20);
      itemPane.setTitle(strPaneTitle);
      itemPane.addArrayColumn(resource.getString("GML.name"));
      itemPane.addArrayColumn(resource.getString("GML.type"));
      itemPane.addArrayColumn(resource.getString("workflowDesigner.readonly"));
      itemPane.addArrayColumn(resource.getString("workflowDesigner.computed"));
      itemPane.addArrayColumn(resource
          .getString("workflowDesigner.directoryEntry")); // mapTo
      column = itemPane.addArrayColumn(resource.getString("GML.operations"));
      column.setSortable(false);

      if (items != null) {
        IconPane iconPane;
        Icon updateIcon;
        Icon delIcon;
        Item item;
        Iterator<Item> iterItem = items.iterateItem();

        while (iterItem.hasNext()) {
          item = iterItem.next();
          strContextEncoded = URLEncoder.encode(strContext + "/"
              + item.getName(), "UTF-8");
          strEditURL = "ModifyItem?context=" + strContextEncoded;

          // Create the remove link
          //
          sb.setLength(0);
          sb.append("javascript:confirmRemove('RemoveItem?context=");
          sb.append(strContextEncoded);
          sb.append("', '");
          sb.append(resource.getString("workflowDesigner.confirmRemoveJS"));
          sb.append(" ");
          sb.append(EncodeHelper.javaStringToJsString(item.getName()));
          sb.append(" ?');");

          iconPane = gef.getIconPane();
          updateIcon = iconPane.addIcon();
          delIcon = iconPane.addIcon();
          updateIcon.setProperties(resource
              .getIcon("workflowDesigner.smallUpdate"), resource
              .getString("GML.modify"), strEditURL);
          delIcon.setProperties(resource
              .getIcon("workflowDesigner.smallDelete"), resource
              .getString("GML.delete"), sb.toString());
          iconPane.setSpacing("30px");

          row = itemPane.addArrayLine();
          row.addArrayCellLink(item.getName(), strEditURL);
          row.addArrayCellLink(item.getType() == null ? "" : item.getType(),
              strEditURL);
          row.addArrayCellLink(resource.getString(item.isReadonly() ? "GML.yes"
              : "GML.no"), strEditURL);
          row.addArrayCellLink(resource.getString(item.isComputed() ? "GML.yes"
              : "GML.no"), strEditURL);
          row.addArrayCellLink(item.getMapTo() == null ? "" : item.getMapTo(),
              strEditURL);
          row.addArrayCellIconPane(iconPane);
        }
      }

      pageContext.getOut().println(itemPane.print());
    } catch (IOException e) {
      throw new JspException("Error when printing the Items", e);
    }
    return super.doStartTag();
  }

  /**
   * @return the context
   */
  public String getContext() {
    return strContext;
  }

  /**
   * @param context the context to set
   */
  public void setContext(String context) {
    strContext = context;
  }

  /**
   * @return the currentScreen
   */
  public String getCurrentScreen() {
    return strCurrentScreen;
  }

  /**
   * @param currentScreen the currentScreen to set
   */
  public void setCurrentScreen(String currentScreen) {
    this.strCurrentScreen = currentScreen;
  }

  /**
   * @return the items
   */
  public DataFolder getItems() {
    return items;
  }

  /**
   * @param items the items to set
   */
  public void setItems(DataFolder items) {
    this.items = items;
  }

  /**
   * @return the paneTitleKey
   */
  public String getPaneTitleKey() {
    return strPaneTitleKey;
  }

  /**
   * @param paneTitleKey the paneTitleKey to set
   */
  public void setPaneTitleKey(String paneTitleKey) {
    strPaneTitleKey = paneTitleKey;
  }
}
