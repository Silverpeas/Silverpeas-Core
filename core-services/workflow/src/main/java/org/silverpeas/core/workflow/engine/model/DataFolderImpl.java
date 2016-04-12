/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;

import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.GenericRecordTemplate;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.DataFolder;
import org.silverpeas.core.workflow.api.model.Item;
import org.silverpeas.core.workflow.api.model.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Class implementing the representation of the &lt;dataFolder&gt; and &lt;userInfos&gt; elements of
 * a Process Model.
 **/
public class DataFolderImpl implements DataFolder, Serializable {

  private static final long serialVersionUID = -2779065735195457809L;
  private List<Item> itemList;

  /**
   * Constructor
   */
  public DataFolderImpl() {
    itemList = new ArrayList<>();
  }

  /**
   * Get the items
   * @return the items as an array
   */
  @Override
  public Item[] getItems() {
    return itemList.toArray(new Item[itemList.size()]);
  }

  /*
   * (non-Javadoc)
   * @see DataFolder#getItem(java.lang.String)
   */
  @Override
  public Item getItem(String strRoleName) {
    if (strRoleName == null) {
      return null;
    }

    Item search = createItem();
    search.setName(strRoleName);
    int idx = itemList.indexOf(search);

    if (idx >= 0) {
      return itemList.get(idx);
    }
    return null;
  }

  /*
   * (non-Javadoc)
   * @see DataFolder#addItem(com.silverpeas.workflow
   * .api.model.Item)
   */
  @Override
  public void addItem(Item item) {
    itemList.add(item);
  }

  /*
   * (non-Javadoc)
   * @see DataFolder#createItem()
   */
  @Override
  public Item createItem() {
    return new ItemImpl();
  }

  /*
   * (non-Javadoc)
   * @see DataFolder#iterateItem()
   */
  @Override
  public Iterator<Item> iterateItem() {
    return itemList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see DataFolder#removeItem(java.lang.String)
   */
  @Override
  public void removeItem(String strItemName) throws WorkflowException {
    if (itemList == null) {
      return;
    }
    Item item = createItem();
    item.setName(strItemName);
    if (!itemList.remove(item)) {
      throw new WorkflowException("DataFolderImpl.removeItem()",
          "workflowEngine.EX_ITEM_NOT_FOUND",
          strItemName == null ? "<null>" : strItemName);
    }
  }

  /**
   * Converts this object in a RecordTemplate object
   * @param readonly
   * @return the resulting RecordTemplate
   */
  @Override
  public RecordTemplate toRecordTemplate(String role, String lang, boolean readonly) throws
      WorkflowException {
    GenericRecordTemplate rt = new GenericRecordTemplate();
    if (itemList == null) {
      return rt;
    }

    try {
      // Add all fields description in the RecordTemplate
      for (Item item : itemList) {
        // if item is map to a userfull detail, it must not be shown in userinfo form
        if (item.getMapTo() == null || item.getMapTo().length() == 0) {
          // create a new FieldTemplate and set attributes
          GenericFieldTemplate ft = new GenericFieldTemplate(item.getName(), item.getType());

          // add parameters to new FieldTemplate
          Iterator<Parameter> parameters = item.iterateParameter();
          while (parameters.hasNext()) {
            Parameter parameter = parameters.next();
            if (parameter != null) {
              ft.addParameter(parameter.getName(), parameter.getValue());
            }
          }

          if (role != null && lang != null) {
            ft.setReadOnly(readonly);
            ft.setMandatory(!readonly);

            ft.addLabel(item.getLabel(role, lang), lang);
          }

          // add the new FieldTemplate in RecordTemplate
          rt.addFieldTemplate(ft);
        }
      }
    } catch (FormException fe) {
      throw new WorkflowException("DataFolderImpl.toRecordTemplate",
          "workflowEngine.EX_ERR_BUILD_FIELD_TEMPLATE", fe);
    }

    return rt;
  }
}
