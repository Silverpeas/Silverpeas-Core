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

import org.silverpeas.core.pdc.PdcServiceProvider;
import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;
import org.silverpeas.core.pdc.thesaurus.service.ThesaurusManager;
import org.silverpeas.core.pdc.thesaurus.model.Jargon;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.PdcRuntimeException;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.exception.SilverpeasException;

import java.util.ArrayList;
import java.util.List;

public class PdcClassifySessionController extends AbstractComponentSessionController {
  private int currentSilverObjectId = -1;
  private List<String> currentSilverObjectIds = null;
  private PdcManager pdcManager = PdcServiceProvider.getPdcManager();
  private boolean sendSubscriptions = true;

  private ThesaurusManager thesaurus = PdcServiceProvider.getThesaurusManager();

  // Positions manager in PDC field mode.
  private PdcFieldPositionsManager pdcFieldPositionsManager =
      ServiceProvider.getService(PdcFieldPositionsManager.class);

  // jargon utilise par l'utilisateur
  private Jargon jargon = null;

  public PdcClassifySessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle,
      String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle);
  }

  private PdcManager getPdcManager() {
    return pdcManager;
  }

  public void setCurrentSilverObjectId(String silverObjectId) {
    currentSilverObjectId = new Integer(silverObjectId).intValue();
  }

  public void setCurrentSilverObjectId(int silverObjectId) {
    currentSilverObjectId = silverObjectId;
  }

  public int getCurrentSilverObjectId() throws PdcException {
    return currentSilverObjectId;
  }

  public void addCurrentSilverObjectId(String silverObjectId) {
    if (currentSilverObjectIds == null) {
      currentSilverObjectIds = new ArrayList<String>();
    }
    currentSilverObjectIds.add(silverObjectId);
  }

  public List<String> getCurrentSilverObjectIds() throws PdcException {
    return currentSilverObjectIds;
  }

  public void clearCurrentSilverObjectIds() {
    if (currentSilverObjectIds != null) {
      currentSilverObjectIds.clear();
    }
  }

  public void setCurrentComponentId(String componentId) {
    OrganizationController orga = getOrganisationController();
    ComponentInst componentInst = orga.getComponentInst(componentId);
    String currentSpaceId = componentInst.getDomainFatherId();
    SpaceInst spaceInst = orga.getSpaceInstById(currentSpaceId);
    this.context.setCurrentComponentId(componentId);
    this.context.setCurrentComponentLabel(componentInst.getLabel());
    this.context.setCurrentComponentName(componentInst.getName());
    this.context.setCurrentSpaceName(spaceInst.getName());
  }

  public String getCurrentComponentId() {
    return this.getComponentId();
  }

  public String getCurrentComponentName() {
    return this.getComponentName();
  }

  public List<UsedAxis> getUsedAxisToClassify() throws PdcException {
    if (pdcFieldPositionsManager.isEnabled()) {
      return pdcFieldPositionsManager.getUsedAxisList();
    } else {
      return getPdcManager().getUsedAxisToClassify(getCurrentComponentId(), getCurrentSilverObjectId());
    }
  }

  public int addPosition(ClassifyPosition position) throws PdcException {
    int result = -1;
    try {
      if (pdcFieldPositionsManager.isEnabled()) {
        pdcFieldPositionsManager.addPosition(position);
      } else {
        if (getCurrentSilverObjectId() != -1) {
          // classical classification = addPosition to one object
          result = getPdcManager().addPosition(getCurrentSilverObjectId(), position,
              getCurrentComponentId(), isSendSubscriptions());
        } else if (getCurrentSilverObjectIds() != null) {
          String silverObjectId = null;
          for (int i = 0; i < getCurrentSilverObjectIds().size(); i++) {
            silverObjectId = (String) getCurrentSilverObjectIds().get(i);
            getPdcManager().addPosition(Integer.parseInt(silverObjectId), position,
                getCurrentComponentId(), isSendSubscriptions());
          }
        }
      }
    } catch (PdcRuntimeException pe) {
      throw new PdcException(
          "PdcClassifySessionController.addPosition()",
          SilverpeasException.ERROR, pe.getMessage(),
          pe);
    }
    return result;
  }

  public int updatePosition(ClassifyPosition position) throws PdcException {
    if (pdcFieldPositionsManager.isEnabled()) {
      return pdcFieldPositionsManager.updatePosition(position);
    } else {
      return getPdcManager().updatePosition(position, getCurrentComponentId(),
          getCurrentSilverObjectId(), isSendSubscriptions());
    }
  }

  public void deletePosition(int positionId) throws PdcException {
    if (pdcFieldPositionsManager.isEnabled()) {
      pdcFieldPositionsManager.deletePosition(positionId);
    } else {
      getPdcManager().deletePosition(positionId, getCurrentComponentId());
    }
  }

  public void deletePosition(String positionId) throws PdcException {
    deletePosition(new Integer(positionId).intValue());
  }

  public List<ClassifyPosition> getPositions() throws PdcException {
    if (pdcFieldPositionsManager.isEnabled()) {
      return pdcFieldPositionsManager.getPositions();
    } else {
      return getPdcManager().getPositions(getCurrentSilverObjectId(), getCurrentComponentId());
    }
  }

  public List<UsedAxis> getUsedAxis() throws PdcException {
    if (pdcFieldPositionsManager.isEnabled()) {
      return pdcFieldPositionsManager.getUsedAxisList();
    } else {
      return getPdcManager().getUsedAxisByInstanceId(getCurrentComponentId());
    }
  }

  public synchronized boolean getActiveThesaurus() throws PdcException {
    return getPersonalization().isThesaurusEnabled();
  }

  public void initializeJargon() throws PdcException {
    try {
      Jargon theJargon = thesaurus.getJargon(getUserId());
      this.jargon = theJargon;
    } catch (ThesaurusException e) {
      throw new PdcException("PdcClassifySessionController.initializeJargon",
          SilverpeasException.ERROR, "pdcPeas.EX_CANT_INITIALIZE_JARGON", "", e);
    }
  }

  public Jargon getJargon() {
    return this.jargon;
  }

  public boolean isSendSubscriptions() {
    return sendSubscriptions;
  }

  public void setSendSubscriptions(boolean sendSubscriptions) {
    this.sendSubscriptions = sendSubscriptions;
  }

  public PdcFieldPositionsManager getPdcFieldPositionsManager() {
    return pdcFieldPositionsManager;
  }

}