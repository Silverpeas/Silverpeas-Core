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

package com.stratelia.silverpeas.portlet;

import com.stratelia.silverpeas.portlet.model.PortletColumnRow;
import com.stratelia.silverpeas.portlet.model.PortletRowRow;
import com.stratelia.silverpeas.portlet.model.PortletSchema;
import com.stratelia.silverpeas.silverpeasinitialize.CallBack;
import com.stratelia.silverpeas.silverpeasinitialize.CallBackManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 * @author
 */
public class PortletCallBack implements CallBack {

  private final CallBackManager callBackManager = CallBackManager.get();

  public PortletCallBack() {
  }

  @Override
  public void subscribe() {
    callBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_USER,
        this);
    callBackManager.subscribeAction(
        CallBackManager.ACTION_BEFORE_REMOVE_COMPONENT, this);
    callBackManager.subscribeAction(CallBackManager.ACTION_BEFORE_REMOVE_SPACE,
        this);
  }

  @Override
  public void doInvoke(int action, int iParam, String sParam, Object extraParam) {
    PortletSchema schema = null;
    try {
      schema = new PortletSchema();

      SilverTrace.info("portlet", "PortletCallBack.doInvoke()",
          "root.MSG_GEN_ENTER_METHOD", callBackManager.getInvokeString(action,
          iParam, sParam, extraParam));
      if (action == CallBackManager.ACTION_BEFORE_REMOVE_USER) {
        schema.portletState.dereferenceUserId(iParam);
      } else if (action == CallBackManager.ACTION_BEFORE_REMOVE_COMPONENT) {
        PortletRowRow[] portletRowDeleted = schema.portletRow
            .dereferenceInstanceId(iParam);
        for (PortletRowRow aPortletRowDeleted : portletRowDeleted) {
          schema.portletColumn
              .delete(aPortletRowDeleted.getPortletColumnId());
          schema.portletState.dereferencePortletRowId(aPortletRowDeleted
              .getId());
        }
      } else if (action == CallBackManager.ACTION_BEFORE_REMOVE_SPACE) {
        PortletColumnRow[] portletColumnDeleted = schema.portletColumn
            .dereferenceSpaceId(iParam);
        PortletRowRow[] portletRowDeleted;
        for (PortletColumnRow aPortletColumnDeleted : portletColumnDeleted) {
          portletRowDeleted = schema.portletRow
              .dereferencePortletColumnId(aPortletColumnDeleted.getId());
          for (PortletRowRow aPortletRowDeleted : portletRowDeleted) {
            schema.portletState.dereferencePortletRowId(aPortletRowDeleted
                .getId());
          }
        }
      }
      schema.commit();
    } catch (Exception e) {
      SilverTrace.error("portlet", "PortletCallBack.doInvoke()",
          "portlet.EX_GENERAL", callBackManager.getInvokeString(action, iParam,
          sParam, extraParam), e);
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        SilverTrace.warn("portlet", "PortletCallBack.doInvoke()",
            "root.EX_ERR_ROLLBACK", callBackManager.getInvokeString(action,
            iParam, sParam, extraParam), ex);
      }
    } finally {
      try {
        if (schema != null) {
          schema.close();
        }
      } catch (Exception e) {
        SilverTrace.warn("portlet", "PortletCallBack.doInvoke()",
            "portlet.EX_CANT_CLOSE_SCHEMA", callBackManager.getInvokeString(
            action, iParam, sParam, extraParam), e);
      }
    }
  }
}
