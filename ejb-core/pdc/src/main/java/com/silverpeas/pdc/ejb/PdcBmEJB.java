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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.silverpeas.pdc.ejb;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.SessionContext;

import com.stratelia.silverpeas.containerManager.ContainerManagerException;
import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.ContentPeas;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;

public class PdcBmEJB implements javax.ejb.SessionBean {
  com.stratelia.silverpeas.pdc.control.PdcBm pdc = null;
  ContentManager contentManager = null;

  public PdcBmEJB() {
  }

  private com.stratelia.silverpeas.pdc.control.PdcBm getPdcBm() {
    if (pdc == null) {
      pdc = new PdcBmImpl();
    }
    return pdc;
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        System.out.println(e.toString());
      }
    }
    return contentManager;
  }

  public ArrayList findGlobalSilverContents(
      ContainerPositionInterface containerPosition, List componentIds,
      boolean recursiveSearch, boolean visibilitySensitive) {
    ArrayList silverContentIds = new ArrayList();
    try {
      // get the silverContentids classified in the context
      silverContentIds.addAll(getPdcBm()
          .findSilverContentIdByPosition(containerPosition, componentIds,
          recursiveSearch, visibilitySensitive));
    } catch (ContainerManagerException c) {
      throw new PdcBmRuntimeException("PdcBmEJB.findGlobalSilverContents",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", c);
    }

    return (ArrayList) getSilverContentsByIds(silverContentIds);
  }

  public ArrayList findGlobalSilverContents(
      ContainerPositionInterface containerPosition, List componentIds,
      String authorId, String afterDate, String beforeDate,
      boolean recursiveSearch, boolean visibilitySensitive) {
    ArrayList silverContentIds = new ArrayList();
    try {
      // get the silverContentids classified in the context
      silverContentIds.addAll(getPdcBm().findSilverContentIdByPosition(
          containerPosition, componentIds, authorId, afterDate, beforeDate,
          recursiveSearch, visibilitySensitive));
    } catch (ContainerManagerException c) {
      throw new PdcBmRuntimeException("PdcBmEJB.findGlobalSilverContents",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", c);
    }

    return (ArrayList) getSilverContentsByIds(silverContentIds);
  }

  public Value getValue(String axisId, String valueId) throws RemoteException {
    SilverTrace.info("Pdc", "PdcBmEJB.getValue", "root.MSG_GEN_PARAM_VALUE",
        "axisId = " + axisId + ", valueId = " + valueId);
    Value value = null;
    try {
      value = getPdcBm().getValue(axisId, valueId);
    } catch (Exception e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getValue",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    SilverTrace.info("Pdc", "PdcBmEJB.getValue", "root.MSG_GEN_PARAM_VALUE",
        "returned valueId = " + value.getValuePK().getId());
    return value;
  }

  public ArrayList getDaughters(String axisId, String valueId)
      throws RemoteException {
    return (ArrayList) getPdcBm().getDaughters(axisId, valueId);
  }

  public ArrayList getSubAxisValues(String axisId, String valueId)
      throws RemoteException {
    return (ArrayList) getPdcBm().getSubAxisValues(axisId, valueId);
  }

  public int getSilverContentId(String objectId, String componentId) {
    int silverContentId = -1;
    try {
      silverContentId = getContentManager().getSilverContentId(objectId,
          componentId);
    } catch (Exception e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentId",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }

    return silverContentId;
  }

  public ArrayList getPositions(int silverContentId, String componentId) {
    ArrayList positions = null;
    try {
      positions = (ArrayList) getPdcBm().getPositions(silverContentId,
          componentId);
    } catch (PdcException e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getPositions",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return positions;
  }

  public List getSilverContentIds(List docFeatures) {
    ArrayList silverContentIds = null;
    try {
      silverContentIds = (ArrayList) getContentManager().getSilverContentId(
          docFeatures);
    } catch (Exception e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentIds",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return silverContentIds;
  }

  public String getInternalContentId(int silverContentId) {
    String objectId = null;
    try {
      objectId = getContentManager().getInternalContentId(silverContentId);
    } catch (Exception e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getInternalContentId",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return objectId;
  }

  public AxisHeader getAxisHeader(String axisId) {
    AxisHeader axis = null;
    try {
      axis = getPdcBm().getAxisHeader(axisId);
    } catch (Exception e) {
      throw new PdcBmRuntimeException("PdcBmEJB.getAxisHeader",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return axis;
  }

  private List getSilverContentsByIds(List silverContentIds) {
    SilverTrace.info("Pdc", "PdcBmEJB.getSilverContentsByIds",
        "root.MSG_GEN_PARAM_VALUE", "silverContentIds = " + silverContentIds);

    // recherche des componentId a partir de silverContentId
    ContentPeas contentP = null;
    String instanceId = "";
    List alSilverContents = (List) new ArrayList();
    List alInstanceIds = (List) new ArrayList();

    try {
      // on récupère la liste de instance contenant tous les documents
      alInstanceIds = getContentManager().getInstanceId(silverContentIds);
      SilverTrace.info("Pdc", "PdcBmEJB.getSilverContentsByIds",
          "root.MSG_GEN_PARAM_VALUE", "alInstanceIds = " + alInstanceIds);
    } catch (ContentManagerException c) {
      throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentsByIds",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", c);
    }

    instanceId = ""; // reinitialisation

    // une fois la liste des instanceId définie, on parcourt cette liste pour
    // en
    // retirer les SilverContentIds
    // propre à chaque instanceId.
    List allSilverContentIds = (List) new ArrayList();
    List newAlSilverContentIds = (List) new ArrayList();

    for (int j = 0; j < alInstanceIds.size(); j++) {
      instanceId = (String) alInstanceIds.get(j);

      try {
        contentP = getContentManager().getContentPeas(instanceId);

        // On récupère tous les silverContentId d'un instanceId
        allSilverContentIds = getContentManager()
            .getSilverContentIdByInstanceId(instanceId);
        SilverTrace.info("Pdc", "PdcBmEJB.getSilverContentsByIds",
            "root.MSG_GEN_PARAM_VALUE", "allSilverContentIds = "
            + allSilverContentIds + " in instance " + instanceId);
      } catch (ContentManagerException c) {
        throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentsByIds",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT",
            c);
      }

      // une fois les SilverContentId de l'instanceId récupérés, on ne garde
      // que
      // ceux qui sont
      // dans la liste résultat (alSilverContentIds).
      allSilverContentIds.retainAll(silverContentIds);

      List silverContentTempo = null;
      if (contentP != null) {
        try {
          // we are going to search only SilverContent of this instanceId
          ContentInterface contentInterface = (ContentInterface) contentP
              .getContentInterface();

          silverContentTempo = contentInterface.getSilverContentById(
              allSilverContentIds, instanceId, null, new ArrayList());
        } catch (ContentManagerException c) {
          throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentsByIds",
              SilverpeasRuntimeException.ERROR,
              "root.EX_CANT_GET_REMOTE_OBJECT", c);
        } catch (Exception e) {
          throw new PdcBmRuntimeException("PdcBmEJB.getSilverContentsByIds",
              SilverpeasRuntimeException.ERROR,
              "root.EX_CANT_GET_REMOTE_OBJECT", e);
        }

        alSilverContents
            .addAll(transformSilverContentsToGlobalSilverContents(silverContentTempo));
      }
      newAlSilverContentIds.addAll(allSilverContentIds);
    }
    SilverTrace.info("Pdc", "PdcBmEJB.getSilverContentsByIds",
        "root.MSG_GEN_PARAM_VALUE", "silverContent size= "
        + alSilverContents.size());

    // replace old SilverContentId list by the new one, to assure the same order
    silverContentIds.clear();
    silverContentIds.addAll(newAlSilverContentIds);

    SilverTrace.info("Pdc", "PdcBmEJB.getSilverContentsByIds",
        "root.MSG_GEN_PARAM_VALUE", "silverContentIds = "
        + silverContentIds.toString());

    return alSilverContents;
  }

  /*
   * @return a List of GlobalSilverContent
   */
  private List transformSilverContentsToGlobalSilverContents(
      List silverContentTempo) {
    ArrayList silverContents = new ArrayList();
    GlobalSilverContent gsc = null;
    SilverContentInterface sci = null;
    for (int i = 0; i < silverContentTempo.size(); i++) {
      sci = (SilverContentInterface) silverContentTempo.get(i);
      gsc = new GlobalSilverContent(sci, "useless", null, null);
      silverContents.add(gsc);
    }
    return silverContents;
  }

  /* Ejb Methods */

  public void ejbCreate() {
  }

  public void ejbRemove() {
  }

  public void ejbActivate() {
  }

  public void ejbPassivate() {
  }

  public void setSessionContext(SessionContext sc) {
  }

}