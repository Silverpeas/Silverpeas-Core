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
package org.silverpeas.web.workflowdesigner.taglib;

import org.silverpeas.core.util.Charsets;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;
import org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane;
import org.silverpeas.core.workflow.api.model.DataFolder;
import org.silverpeas.core.workflow.api.model.Item;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;

/**
 * Class implementing the tag &lt;itemList&gt; from workflowEditor.tld
 */
@SuppressWarnings("unused")
public class ItemList extends WorkflowTagSupport {

  private static final long serialVersionUID = -7885970074029478168L;
  private String strContext;
  private String strPaneTitleKey;
  private String strCurrentScreen;
  private DataFolder items;

  @Override
  public int doStartTag() throws JspException {
    try {
      var gef = (GraphicElementFactory) pageContext.getSession().getAttribute(
          "SessionGraphicElementFactory");
      var resource = (MultiSilverpeasBundle) pageContext.getRequest().getAttribute(
          "resources");
      String strPaneTitle = resource.getString(strPaneTitleKey);

      var itemPane = gef.getArrayPane("itemList", strCurrentScreen, pageContext
          .getRequest(), pageContext.getSession());
      itemPane.setVisibleLineNumber(20);
      itemPane.setTitle(strPaneTitle);
      itemPane.addArrayColumn(resource.getString("GML.name"));
      itemPane.addArrayColumn(resource.getString("GML.type"));
      itemPane.addArrayColumn(resource.getString("workflowDesigner.readonly"));
      itemPane.addArrayColumn(resource.getString("workflowDesigner.computed"));
      itemPane.addArrayColumn(resource
          .getString("workflowDesigner.directoryEntry")); // mapTo
      var column = itemPane.addArrayColumn(resource.getString("GML.operations"));
      column.setSortable(false);

      if (items != null) {
        addItem(gef, resource, itemPane);
      }

      pageContext.getOut().println(itemPane.print());
    } catch (IOException e) {
      throw new JspException("Error when printing the Items", e);
    }
    return super.doStartTag();
  }

  private void addItem(GraphicElementFactory gef, MultiSilverpeasBundle resource, ArrayPane itemPane) {
    Iterator<Item> iterItem = items.iterateItem();
    while (iterItem.hasNext()) {
      var item = iterItem.next();
      String strContextEncoded = URLEncoder.encode(strContext + "/"
          + item.getName(), Charsets.UTF_8);
      String strEditURL = "ModifyItem?context=" + strContextEncoded;
      String removalJs = "javascript:confirmRemove('RemoveItem', {context: '"
          + strContextEncoded + "'}, '"
          + resource.getString("workflowDesigner.confirmRemoveJS") + " "
          + WebEncodeHelper.javaStringToJsString(item.getName()) + " ?');";

      var iconPane = addIconPane(gef, resource, strEditURL, removalJs);

      var row = itemPane.addArrayLine();
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
