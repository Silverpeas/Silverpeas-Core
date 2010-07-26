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

package com.stratelia.silverpeas.pdcPeas.control;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.silverpeas.thesaurus.model.Jargon;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.ClassifyPosition;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.PdcRuntimeException;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.beans.admin.ComponentInst;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.beans.admin.SpaceInst;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class PdcClassifySessionController extends AbstractComponentSessionController {
  private int currentSilverObjectId = -1;
  private List<String> currentSilverObjectIds = null;
  private PdcBm pdcBm = null;
  private boolean sendSubscriptions = true;

  private ThesaurusManager thesaurus = new ThesaurusManager();

  // Positions manager in PDC field mode.
  private PdcFieldPositionsManager pdcFieldPositionsManager = new PdcFieldPositionsManager();

  // jargon utilise par l'utilisateur
  private Jargon jargon = null;

  public PdcClassifySessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle,
      String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle);
  }

  private PdcBm getPdcBm() {
    if (pdcBm == null) {
      pdcBm = (PdcBm) new PdcBmImpl();
    }
    return pdcBm;
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
    OrganizationController orga = getOrganizationController();
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
      return getPdcBm().getUsedAxisToClassify(getCurrentComponentId(), getCurrentSilverObjectId());
    }
  }

  public int addPosition(ClassifyPosition position) throws PdcException {
    int result = -1;
    try{
        if (pdcFieldPositionsManager.isEnabled()) {
          pdcFieldPositionsManager.addPosition(position);
        } else {
          if (getCurrentSilverObjectId() != -1) {
            // classical classification = addPosition to one object
            result = getPdcBm().addPosition(getCurrentSilverObjectId(), position,
                getCurrentComponentId(), isSendSubscriptions());
          } else if (getCurrentSilverObjectIds() != null) {
            String silverObjectId = null;
            for (int i = 0; i < getCurrentSilverObjectIds().size(); i++) {
              silverObjectId = (String) getCurrentSilverObjectIds().get(i);
              getPdcBm().addPosition(Integer.parseInt(silverObjectId), position,
                  getCurrentComponentId(), isSendSubscriptions());
            }
          }
        }
    }catch(PdcRuntimeException pe){
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
      return getPdcBm().updatePosition(position, getCurrentComponentId(),
          getCurrentSilverObjectId(), isSendSubscriptions());
    }
  }

  public void deletePosition(int positionId) throws PdcException {
    if (pdcFieldPositionsManager.isEnabled()) {
      pdcFieldPositionsManager.deletePosition(positionId);
    } else {
      getPdcBm().deletePosition(positionId, getCurrentComponentId());
    }
  }

  public void deletePosition(String positionId) throws PdcException {
    deletePosition(new Integer(positionId).intValue());
  }

  public List<ClassifyPosition> getPositions() throws PdcException {
    if (pdcFieldPositionsManager.isEnabled()) {
      return pdcFieldPositionsManager.getPositions();
    } else {
      return getPdcBm().getPositions(getCurrentSilverObjectId(), getCurrentComponentId());
    }
  }

  public List<UsedAxis> getUsedAxis() throws PdcException {
    if (pdcFieldPositionsManager.isEnabled()) {
      return pdcFieldPositionsManager.getUsedAxisList();
    } else {
      return getPdcBm().getUsedAxisByInstanceId(getCurrentComponentId());
    }
  }

  public synchronized boolean getActiveThesaurus() throws PdcException,
      RemoteException {
    try {
      return getPersonalization().getThesaurusStatus();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return getPersonalization().getThesaurusStatus();
    } catch (Exception e) {
      throw new PdcException(
          "PdcClassifySessionController.getActiveThesaurus()",
          SilverpeasException.ERROR, "pdcPeas.EX_CANT_GET_ACTIVE_THESAURUS",
          "", e);
    }
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