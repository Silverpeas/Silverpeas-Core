/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.stratelia.silverpeas.pdcPeas.control;


import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import java.util.List;

public class PdcUtilizationSessionController extends AbstractComponentSessionController {
  
  private static String SETTINGS_FILE = "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasSettings";

  private String currentView = "P";
  private Axis currentAxis = null;
  private PdcBm pdcBm = null;

  // PDC field manager.
  private PdcFieldTemplateManager pdcFieldTemplateManager = new PdcFieldTemplateManager();

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
      OrganizationController orga = getOrganizationController();
      ComponentInst componentInst = orga.getComponentInst(componentId);
      this.context.setCurrentComponentLabel(componentInst.getLabel());
      this.context.setCurrentSpaceId(componentInst.getDomainFatherId());
      SpaceInst spaceInst = orga.getSpaceInstById(getSpaceId());
      this.context.setCurrentSpaceName(spaceInst.getName());
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

  private PdcBm getPdcBm() {
    if (pdcBm == null) {
      pdcBm = (PdcBm) new PdcBmImpl();
    }
    return pdcBm;
  }

  public void setCurrentView(String view) throws PdcException {
    currentView = view;
  }

  public String getCurrentView() throws PdcException {
    return currentView;
  }

  private void setCurrentAxis(Axis axis) throws PdcException {
    currentAxis = axis;
  }

  public Axis getCurrentAxis() throws PdcException {
    return currentAxis;
  }

  public PdcFieldTemplateManager getPdcFieldTemplateManager() {
    return pdcFieldTemplateManager;
  }

  public List<AxisHeader> getPrimaryAxis() throws PdcException {
    return getPdcBm().getAxisByType("P");
  }

  public List<AxisHeader> getSecondaryAxis() throws PdcException {
    return getPdcBm().getAxisByType("S");
  }

  public List<AxisHeader> getAxis() throws PdcException {
    return getPdcBm().getAxisByType(getCurrentView());
  }

  public Axis getAxisDetail(String axisId) throws PdcException {
    Axis axis = getPdcBm().getAxisDetail(axisId);
    setCurrentAxis(axis);
    return axis;
  }

  public UsedAxis getUsedAxis(String usedAxisId) throws PdcException {
    if (pdcFieldTemplateManager.isEnabled()) {
      return pdcFieldTemplateManager.getUsedAxis(usedAxisId);
    } else {
      return getPdcBm().getUsedAxis(usedAxisId);
    }
  }

  public List<UsedAxis> getUsedAxisList() throws PdcException {
    if (pdcFieldTemplateManager.isEnabled()) {
      return pdcFieldTemplateManager.getUsedAxisList();
    } else {
      return getPdcBm().getUsedAxisByInstanceId(getComponentId());
    }
  }

  public int addUsedAxis(UsedAxis usedAxis) throws PdcException {
    usedAxis.setAxisId(new Integer(getCurrentAxis().getAxisHeader().getPK().getId()).intValue());
    if (pdcFieldTemplateManager.isEnabled()) {
      pdcFieldTemplateManager.addUsedAxis(usedAxis);
      return 0;
    } else {
      usedAxis.setInstanceId(getComponentId());
      return getPdcBm().addUsedAxis(usedAxis);
    }
  }

  public int updateUsedAxis(UsedAxis usedAxis) throws PdcException {
    if (pdcFieldTemplateManager.isEnabled()) {
      pdcFieldTemplateManager.updateUsedAxis(usedAxis);
      return 0;
    } else {
      usedAxis.setInstanceId(getComponentId());
      usedAxis.setAxisId(new Integer(getCurrentAxis().getAxisHeader().getPK().getId()).intValue());
      return getPdcBm().updateUsedAxis(usedAxis);
    }
  }

  public void deleteUsedAxis(String usedAxisId) throws PdcException {
    if (pdcFieldTemplateManager.isEnabled()) {
      pdcFieldTemplateManager.deleteUsedAxis(usedAxisId);
    } else {
      getPdcBm().deleteUsedAxis(usedAxisId);
    }
  }

}