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

package org.silverpeas.web.pdc.control;

import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;

public class PdcUtilizationSessionController extends AbstractComponentSessionController {

  private static String SETTINGS_FILE = "org.silverpeas.pdcPeas.settings.pdcPeasSettings";

  private String currentView = "P";
  private Axis currentAxis = null;
  private PdcManager pdcManager = null;

  // PDC field manager.
  private PdcFieldTemplateManager pdcFieldTemplateManager =
      ServiceProvider.getService(PdcFieldTemplateManager.class);

  public PdcUtilizationSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle, String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle, SETTINGS_FILE);
  }

  public void init(String componentId) {
    pdcFieldTemplateManager.reset();
    if (componentId != null) {
      if (this.getComponentId() == null) {
        this.context.setCurrentComponentId(componentId);
      } else {
        if (!this.getComponentId().equals(componentId)) {
          currentView = "P";
          currentAxis = null;
          this.context.setCurrentComponentId(componentId);
        }
      }
    }
  }

  public boolean isAxisInvarianceUsed() {
    return getSettings().getBoolean("useAxisInvariance", false);
  }

  public void init() {
    this.context.setCurrentComponentId(null);
    currentView = "P";
    currentAxis = null;
  }

  private PdcManager getPdcManager() {
    if (pdcManager == null) {
      pdcManager = (PdcManager) new GlobalPdcManager();
    }
    return pdcManager;
  }

  public void setCurrentView(String view) throws PdcException {
    currentView = view;
  }

  public String getCurrentView() throws PdcException {
    return currentView;
  }

  private void setCurrentAxis(Axis axis) {
    currentAxis = axis;
  }

  public Axis getCurrentAxis() throws PdcException {
    return currentAxis;
  }

  public PdcFieldTemplateManager getPdcFieldTemplateManager() {
    return pdcFieldTemplateManager;
  }

  public List<AxisHeader> getPrimaryAxis() throws PdcException {
    return getPdcManager().getAxisByType("P");
  }

  public List<AxisHeader> getSecondaryAxis() throws PdcException {
    return getPdcManager().getAxisByType("S");
  }

  public List<AxisHeader> getAxis() throws PdcException {
    return getPdcManager().getAxisByType(getCurrentView());
  }

  public Axis getAxisDetail(String axisId) throws PdcException {
    Axis axis = getPdcManager().getAxisDetail(axisId);
    setCurrentAxis(axis);
    return axis;
  }

  public UsedAxis getUsedAxis(String usedAxisId) throws PdcException {
    if (pdcFieldTemplateManager.isEnabled()) {
      return pdcFieldTemplateManager.getUsedAxis(usedAxisId);
    } else {
      return getPdcManager().getUsedAxis(usedAxisId);
    }
  }

  public List<UsedAxis> getUsedAxisList() throws PdcException {
    if (pdcFieldTemplateManager.isEnabled()) {
      return pdcFieldTemplateManager.getUsedAxisList();
    } else {
      return getPdcManager().getUsedAxisByInstanceId(getComponentId());
    }
  }

  public int addUsedAxis(UsedAxis usedAxis) throws PdcException {
    usedAxis.setAxisId(new Integer(getCurrentAxis().getAxisHeader().getPK().getId()).intValue());
    if (pdcFieldTemplateManager.isEnabled()) {
      pdcFieldTemplateManager.addUsedAxis(usedAxis);
      return 0;
    } else {
      usedAxis.setInstanceId(getComponentId());
      return getPdcManager().addUsedAxis(usedAxis);
    }
  }

  public int updateUsedAxis(UsedAxis usedAxis) throws PdcException {
    if (pdcFieldTemplateManager.isEnabled()) {
      pdcFieldTemplateManager.updateUsedAxis(usedAxis);
      return 0;
    } else {
      usedAxis.setInstanceId(getComponentId());
      usedAxis.setAxisId(new Integer(getCurrentAxis().getAxisHeader().getPK().getId()).intValue());
      return getPdcManager().updateUsedAxis(usedAxis);
    }
  }

  public void deleteUsedAxis(String usedAxisId) throws PdcException {
    if (pdcFieldTemplateManager.isEnabled()) {
      pdcFieldTemplateManager.deleteUsedAxis(usedAxisId);
    } else {
      getPdcManager().deleteUsedAxis(usedAxisId);
    }
  }

}