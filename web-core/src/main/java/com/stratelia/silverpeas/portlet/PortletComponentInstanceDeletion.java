/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.portlet;

import com.silverpeas.admin.components.ComponentInstanceDeletion;
import com.stratelia.silverpeas.portlet.model.PortletRowRow;
import com.stratelia.silverpeas.portlet.model.PortletSchema;
import org.silverpeas.core.admin.OrganizationController;
import org.silverpeas.util.ServiceProvider;
import org.silverpeas.util.logging.SilverLogger;

import javax.inject.Singleton;

/**
 * @author mmoquillon
 */
@Singleton
public class PortletComponentInstanceDeletion implements ComponentInstanceDeletion {

  /**
   * Deletes the resources belonging to the specified component instance. This method is invoked
   * by Silverpeas when a component instance is being deleted.
   * @param componentInstanceId the unique identifier of a component instance.
   */
  @Override
  public void delete(final String componentInstanceId) {
    PortletSchema schema = null;
    int id = ServiceProvider.getService(OrganizationController.class)
        .getComponentInst(componentInstanceId)
        .getLocalId();
    try {
      schema = new PortletSchema();
      PortletRowRow[] portletRowDeleted = schema.portletRow.dereferenceInstanceId(id);
      for (PortletRowRow aPortletRowDeleted : portletRowDeleted) {
        schema.portletColumn.delete(aPortletRowDeleted.getPortletColumnId());
        schema.portletState.dereferencePortletRowId(aPortletRowDeleted.getId());
      }
      schema.commit();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
      try {
        if (schema != null) {
          schema.rollback();
        }
      } catch (Exception ex) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    } finally {
      try {
        if (schema != null) {
          schema.close();
        }
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    }
  }
}
