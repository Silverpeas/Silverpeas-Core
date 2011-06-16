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
package com.silverpeas.jobSearchPeas.control;

import java.rmi.RemoteException;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.silverpeas.jobSearchPeas.SearchResult;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;
import com.stratelia.webactiv.util.publication.model.PublicationRuntimeException;
import com.stratelia.webactiv.util.node.control.NodeBm;
import com.stratelia.webactiv.util.node.control.NodeBmHome;
import com.stratelia.webactiv.util.node.model.NodeDetail;
import com.stratelia.webactiv.util.node.model.NodePK;

/**
 * Class declaration
 * @author Cécile Bonin
 */
public class JobSearchPeasSessionController extends AbstractComponentSessionController {
  
  private AdminController myAdminController = null;
  
  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   * @see
   */
  public JobSearchPeasSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(
        mainSessionCtrl,
        componentContext,
        "com.silverpeas.jobSearchPeas.multilang.jobSearchPeasBundle");
    setComponentRootName(URLManager.CMP_JOBSEARCHPEAS);
  }

  
  /**
   * @return
   */
  public AdminController getAdminController() {
    if (myAdminController == null) {
      myAdminController = new AdminController(getUserId());
    }
    return myAdminController;
  }
  
  /**
   * @return
   */
  private PublicationBm getPublicationBm() {
    PublicationBm publicationBm = null;
    try {
      PublicationBmHome publicationBmHome =
          (PublicationBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
          PublicationBmHome.class);
      publicationBm = publicationBmHome.create();
    } catch (Exception e) {
      throw new PublicationRuntimeException("JobSearchPeasSessionController.getPublicationBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return publicationBm;
  }
  
  /**
   * @return
   */
  private NodeBm getNodeBm() {
    NodeBm nodeBm = null;
    try {
      NodeBmHome nodeBmHome =
          (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
      nodeBm = nodeBmHome.create();
    } catch (Exception e) {
      throw new PublicationRuntimeException("JobSearchPeasSessionController.getNodeBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return nodeBm;
  }
  
  /**
   * @param searchField
   * @return
   */
  private List<SearchResult> searchResultSpace(String searchField) {
    List<SearchResult> listResult = new ArrayList<SearchResult>(); 
    
    //id espace
    SpaceInstLight spaceInstLight = getAdminController().getSpaceInstLight(searchField);
    if(spaceInstLight != null) {
      String nom = spaceInstLight.getName(getLanguage());
      String desc = spaceInstLight.getDescription();
      Date dateCrea = spaceInstLight.getCreateDate();
      String nomCrea = getAdminController().getUserDetail(Integer.toString(spaceInstLight.getCreatedBy())).getDisplayedName();
      List<String> listEmplacement = new ArrayList<String>();
      String emplacement = "";
      //Espace > Sous-espace
      List<SpaceInstLight> spaceList = getAdminController().getPathToSpace(searchField, true);
      boolean first = true;
      for (SpaceInstLight space : spaceList) {
        if(!first) {
          emplacement += " > ";
        }
        emplacement += space.getName(getLanguage());
        first = false;
      }
      listEmplacement.add(emplacement);
      
      String url = "";
     
      SearchResult searchResult = new SearchResult();
      searchResult.setName(nom);
      searchResult.setDesc(desc);
      searchResult.setCreaDate(dateCrea);
      searchResult.setCreaName(nomCrea);
      searchResult.setPath(listEmplacement);
      searchResult.setUrl(url);
      listResult.add(searchResult);
    }
    
    //nom espace
    
    
    return listResult;

  }
  
  /**
   * @param searchField
   * @return
   */
  private List<SearchResult> searchResultService(String searchField) {
    List<SearchResult> listResult = new ArrayList<SearchResult>(); 
    
    //id service
    ComponentInstLight componentInstLight = getAdminController().getComponentInstLight(searchField);
    if(componentInstLight != null) {
      String nom = componentInstLight.getLabel(getLanguage());
      String desc = componentInstLight.getDescription(getLanguage());
      Date dateCrea = componentInstLight.getCreateDate();
      String nomCrea = getAdminController().getUserDetail(Integer.toString(componentInstLight.getCreatedBy())).getDisplayedName();
      List<String> listEmplacement = new ArrayList<String>();
      String emplacement = "";
      //Espace > Sous-espace
      List<SpaceInstLight> spaceList = getAdminController().getPathToComponent(searchField);
      for (SpaceInstLight space : spaceList) {
        emplacement += space.getName(getLanguage()) + " > ";
      }
      //Composant
      emplacement += getAdminController().getComponentInstLight(searchField).getLabel(getLanguage());
      listEmplacement.add(emplacement);
      
      String url = "";
      //RjobStartPagePeas/jsp/OpenComponent?ComponentId='+componentId;
     
      SearchResult searchResult = new SearchResult();
      searchResult.setName(nom);
      searchResult.setDesc(desc);
      searchResult.setCreaDate(dateCrea);
      searchResult.setCreaName(nomCrea);
      searchResult.setPath(listEmplacement);
      searchResult.setUrl(url);
      listResult.add(searchResult);
    }
    
    //nom service
    
    
    return listResult;

  }
  
  /**
   * @param searchField
   * @return
   * @throws RemoteException 
   */
  private List<SearchResult> searchResultPublication(String searchField) throws RemoteException {
    List<SearchResult> listResult = new ArrayList<SearchResult>(); 
    
    //id publication
    PublicationPK pubPK = new PublicationPK(searchField);
    PublicationDetail publication = getPublicationBm().getDetail(pubPK);
    
    if(publication != null) {
      String nom = publication.getName(getLanguage());
      String desc = publication.getDescription(getLanguage());
      Date dateCrea = publication.getCreationDate();
      String creaId = publication.getCreatorId();
      String nomCrea = getAdminController().getUserDetail(creaId).getDisplayedName();
      pubPK = publication.getPK();
      String instanceId = pubPK.getInstanceId();
      List<String> listEmplacement = new ArrayList<String>();
      String emplacementEspaceComposant = "";
      //Espace > Sous-espace
      List<SpaceInstLight> spaceList = getAdminController().getPathToComponent(instanceId);
      for (SpaceInstLight space : spaceList) {
        emplacementEspaceComposant += space.getName(getLanguage()) + " > ";
      }
      
      //Composant
      emplacementEspaceComposant += getAdminController().getComponentInstLight(instanceId).getLabel(getLanguage()) + " > ";
      
      //Theme / Sous-theme
      Collection<NodePK> fatherPKs = getPublicationBm().getAllFatherPK(pubPK);
      if (fatherPKs != null) {
        Iterator<NodePK> it = fatherPKs.iterator();
        while (it.hasNext()) {
          String emplacement = emplacementEspaceComposant;
          NodePK pk = it.next();
          Collection<NodeDetail> path = getNodeBm().getAnotherPath(pk);
          Iterator<NodeDetail> itNode = path.iterator();
          while (itNode.hasNext()) {
            NodeDetail nodeDetail = itNode.next();
            emplacement += nodeDetail.getName(getLanguage()) + " > ";
          }
          emplacement = emplacement.substring(0, emplacement.length() - 3);
          listEmplacement.add(emplacement);
        }
      }
      
      String url = publication.getURL();
     
      SearchResult searchResult = new SearchResult();
      searchResult.setName(nom);
      searchResult.setDesc(desc);
      searchResult.setCreaDate(dateCrea);
      searchResult.setCreaName(nomCrea);
      searchResult.setPath(listEmplacement);
      searchResult.setUrl(url);
      listResult.add(searchResult);
    }
    return listResult;
  }
  
  /**
   * @param searchField
   * @return
   */
  private List<SearchResult> searchResultGroup(String searchField) {
    List<SearchResult> listResult = new ArrayList<SearchResult>(); 
    
    //id group
    Group group = getAdminController().getGroupById(searchField);
    if(group != null) {
      String nom = group.getName();
      String desc = group.getDescription();
      Date dateCrea = null;
      String nomCrea = "";
      List<String> listEmplacement = new ArrayList<String>();
      String emplacement = "";
      String domainId = group.getDomainId();
      if(domainId == null) {
        domainId = "-1";
      }
      Domain domain = getAdminController().getDomain(domainId);
      //nom du domaine
      if("-1".equals(domainId)) {//domaine mixte
        emplacement += getString("JSP.domainMixt");  
      } else {
        emplacement += domain.getName();
      }
      //nom du(des) groupe(s) pères
      List<String> groupList = getAdminController().getPathToGroup(searchField);
      for (String elementGroupId : groupList) {
        emplacement += " > "+ getAdminController().getGroupName(elementGroupId);
      }
      //nom du groupe
      emplacement += " > "+ group.getName();
      listEmplacement.add(emplacement);  
      
      String url = "/RjobDomainPeas/jsp/groupOpen?groupId="+searchField;
     
      SearchResult searchResult = new SearchResult();
      searchResult.setName(nom);
      searchResult.setDesc(desc);
      searchResult.setCreaDate(dateCrea);
      searchResult.setCreaName(nomCrea);
      searchResult.setPath(listEmplacement);
      searchResult.setUrl(url);
      listResult.add(searchResult);
    }
    
    //nom group
    
    
    return listResult;

  }
  
  /**
   * @param searchField
   * @return
   */
  private List<SearchResult> searchResultUser(String searchField) {
    List<SearchResult> listResult = new ArrayList<SearchResult>(); 
    
    //id user
    UserDetail user = getAdminController().getUserDetail(searchField);
    if(user != null) {
      String nom = user.getDisplayedName();
      String desc = user.geteMail();
      Date dateCrea = null;
      String nomCrea = "";
      List<String> listEmplacement = new ArrayList<String>();
      String emplacementDomaine = "";
      String domainId = user.getDomainId();
      if(domainId == null) {
        domainId = "-1";
      }
      Domain domain = getAdminController().getDomain(domainId);
      //nom du domaine
      if("-1".equals(domainId)) {//domaine mixte
        emplacementDomaine += getString("JSP.domainMixt");  
      } else {
        emplacementDomaine += domain.getName();
      }
      //groupe(s) d'appartenance
      String emplacement = emplacementDomaine;
      String[] groupIds = getAdminController().getDirectGroupsIdsOfUser(searchField);
      if (groupIds != null && groupIds.length > 0) {
        for (int iGrp = 0; iGrp < groupIds.length; iGrp++) {
          Group group = getOrganizationController().getGroup(groupIds[iGrp]);
          emplacement = emplacementDomaine;
          //nom du(des) groupe(s) pères
          List<String> groupList = getAdminController().getPathToGroup(groupIds[iGrp]);
          for (String elementGroupId : groupList) {
            emplacement += " > "+ getAdminController().getGroupName(elementGroupId);
          }
          //nom du groupe
          emplacement += " > "+ group.getName();
          listEmplacement.add(emplacement);
        }
      } else {
        listEmplacement.add(emplacement);
      }
      
      String url = "/RjobDomainPeas/jsp/groupOpen?groupId="+searchField;
     
      SearchResult searchResult = new SearchResult();
      searchResult.setName(nom);
      searchResult.setDesc(desc);
      searchResult.setCreaDate(dateCrea);
      searchResult.setCreaName(nomCrea);
      searchResult.setPath(listEmplacement);
      searchResult.setUrl(url);
      listResult.add(searchResult);
    }
    
    //nom user
    
    
    return listResult;

  }

  /**
   * @param searchField
   * @param category
   * @return
   * @throws RemoteException 
   */
  public List<SearchResult> searchResult(String searchField, String category) throws RemoteException {
    if("space".equals(category)) {
      return searchResultSpace(searchField);
    } else if("service".equals(category)) {
      return searchResultService(searchField);
    } else if ("publication".equals(category)) {
      return searchResultPublication(searchField);
    } else if ("group".equals(category)) {
      return searchResultGroup(searchField);
    } else if ("user".equals(category)) {
      return searchResultUser(searchField);
    }
    return null;
  }
}
