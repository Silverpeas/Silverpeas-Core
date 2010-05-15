/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.workflow.engine.model;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Vector;

import com.silverpeas.form.FormException;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.record.GenericFieldTemplate;
import com.silverpeas.form.record.GenericRecordTemplate;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.model.DataFolder;
import com.silverpeas.workflow.api.model.Item;
import com.silverpeas.workflow.api.model.Parameter;

/**
 * Class implementing the representation of the &lt;dataFolder&gt; and &lt;userInfos&gt; elements of
 * a Process Model.
 **/
public class DataFolderImpl implements DataFolder, Serializable {
  private Vector itemList;

  /**
   * Constructor
   */
  public DataFolderImpl() {
    itemList = new Vector();
  }

  /**
   * Get the items
   * @return the items as an array
   */
  public Item[] getItems() {
    return (Item[]) itemList.toArray(new ItemImpl[0]);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.DataFolder#getItem(java.lang.String)
   */
  public Item getItem(String strRoleName) {
    Item search = createItem();
    int idx;

    if (strRoleName == null)
      return null;

    search.setName(strRoleName);
    idx = itemList.indexOf(search);

    if (idx >= 0)
      return (Item) itemList.get(idx);
    else
      return null;
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.DataFolder#addItem(com.silverpeas.workflow
   * .api.model.Item)
   */
  public void addItem(Item item) {
    itemList.add(item);
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.DataFolder#createItem()
   */
  public Item createItem() {
    return new ItemImpl();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.DataFolder#iterateItem()
   */
  public Iterator iterateItem() {
    return itemList.iterator();
  }

  /*
   * (non-Javadoc)
   * @see com.silverpeas.workflow.api.model.DataFolder#removeItem(java.lang.String)
   */
  public void removeItem(String strItemName) throws WorkflowException {
    Item item = createItem();

    item.setName(strItemName);

    if (itemList == null)
      return;

    if (!itemList.remove(item))
      throw new WorkflowException("DataFolderImpl.removeItem()", //$NON-NLS-1$
          "workflowEngine.EX_ITEM_NOT_FOUND", // $NON-NLS-1$
          strItemName == null ? "<null>" //$NON-NLS-1$
              : strItemName);
  }

  /**
   * Converts this object in a RecordTemplate object
   * @return the resulting RecordTemplate
   */
  public RecordTemplate toRecordTemplate(String role, String lang,
      boolean readonly) throws WorkflowException {
    GenericRecordTemplate rt = new GenericRecordTemplate();

    if (itemList == null)
      return rt;

    try {
      // Add all fields description in the RecordTemplate
      for (int i = 0; i < itemList.size(); i++) {
        // Get the item definition
        Item item = (Item) itemList.get(i);

        // if item is map to a userfull detail, it must not be shown in userinfo
        // form
        if (item.getMapTo() == null || item.getMapTo().length() == 0) {
          // create a new FieldTemplate and set attributes
          GenericFieldTemplate ft = new GenericFieldTemplate(item.getName(),
              item.getType());

          // add parameters to new FieldTemplate
          Iterator parameters = item.iterateParameter();
          Parameter parameter = null;
          while (parameters.hasNext()) {
            parameter = (ParameterImpl) parameters.next();
            if (parameter != null)
              ft.addParameter(parameter.getName(), parameter.getValue());
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
