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

import java.rmi.NoSuchObjectException;

import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import com.silverpeas.jobSearchPeas.SearchResult;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdcPeas.model.QueryParameters;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.AdminController;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.Domain;
import com.stratelia.webactiv.beans.admin.Group;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.exception.SilverpeasException;
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

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Class declaration
 * @author Cécile Bonin
 */
public class JobSearchPeasSessionController extends AbstractComponentSessionController {
  
  private AdminController myAdminController = null;
  private PublicationBm publicationBm = null;
  private NodeBm nodeBm = null;
  private SearchEngineBm searchBm = null;
  private String searchField = null;
  private String category = null;
  private List<SearchResult> listResult = null;
  
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
    if (publicationBm == null) {
      try {
        PublicationBmHome publicationBmHome =
            (PublicationBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.PUBLICATIONBM_EJBHOME,
            PublicationBmHome.class);
        publicationBm = publicationBmHome.create();
      } catch (Exception e) {
        throw new PublicationRuntimeException("JobSearchPeasSessionController.getPublicationBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return publicationBm;
  }
  
  /**
   * @return
   */
  private NodeBm getNodeBm() {
    if (nodeBm == null) {
      try {
        NodeBmHome nodeBmHome =
            (NodeBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.NODEBM_EJBHOME, NodeBmHome.class);
        nodeBm = nodeBmHome.create();
      } catch (Exception e) {
        throw new PublicationRuntimeException("JobSearchPeasSessionController.getNodeBm()",
            SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
      }
    }
    return nodeBm;
  }
  
  /**
   * @return
   * @throws PdcException
   */
  private SearchEngineBm getSearchEngineBm() throws PdcException {
    if (searchBm == null) {
      try {
        SearchEngineBmHome searchBmHome = (SearchEngineBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.SEARCHBM_EJBHOME,
            SearchEngineBmHome.class);
        searchBm = searchBmHome.create();
      } catch (Exception e) {
        throw new PdcException(
            "JobSearchPeasSessionController.getSearchEngineBm()",
            SilverpeasException.ERROR, "pdcPeas.EX_CANT_GET_SEARCH_ENGINE", e);
      }
    }
    return searchBm;
  }
  
  /**
   * @return
   */
  public String getSearchField() {
    return searchField;
  }

  /**
   * @param searchField
   */
  public void setSearchField(String searchField) {
    this.searchField = searchField;
  }

  /**
   * @return
   */
  public String getCategory() {
    return category;
  }


  /**
   * @param category
   */
  public void setCategory(String category) {
    this.category = category;
  }


  /**
   * @return
   */
  public List<SearchResult> getListResult() {
    return listResult;
  }


  /**
   * @param listResult
   */
  public void setListResult(List<SearchResult> listResult) {
    this.listResult = listResult;
  }


  /**
   * @param spaceId
   * @return
   */
  private String getPathSpace(String spaceId) {
    String emplacement = "";
    try {
      //Espace > Sous-espace
      List<SpaceInstLight> spaceList = getAdminController().getPathToSpace(spaceId, false);
      boolean first = true;
      for (SpaceInstLight space : spaceList) {
        if(!first) {
          emplacement += " > ";
        }
        emplacement += space.getName(getLanguage());
        first = false;
      }
    } catch (Exception e) {
      SilverTrace.warn("admin", "JobSearchPeasSessionController.getPathSpace()",
          "admin.CANT_GET_SPACE_PATH", "spaceId = " + spaceId, e);
    }
    return emplacement;
  }
  
  /**
   * @param searchField
   * @return
   */
  private List<SearchResult> searchResultSpaceId(String searchField) {
    List<SearchResult> listResult = new ArrayList<SearchResult>(); 
    SpaceInstLight spaceInstLight = getAdminController().getSpaceInstLight(searchField);
    if(spaceInstLight != null) {
      String nom = spaceInstLight.getName(getLanguage());
      String desc = spaceInstLight.getDescription();
      Date dateCrea = spaceInstLight.getCreateDate();
      String nomCrea = getAdminController().getUserDetail(Integer.toString(spaceInstLight.getCreatedBy())).getDisplayedName();
      List<String> listEmplacement = new ArrayList<String>();
      String emplacement = getPathSpace(searchField);
      listEmplacement.add(emplacement);
      
      String url = "";
      if(spaceInstLight.isRoot()) {
        url = "openSpace('"+spaceInstLight.getFullId()+"')";
      } else {
        SpaceInstLight rootSpaceInstLight = spaceInstLight;
        while(! rootSpaceInstLight.isRoot()) {
          String fatherId = rootSpaceInstLight.getFatherId();
          rootSpaceInstLight = getAdminController().getSpaceInstLight(fatherId);
        }
        url = "openSubSpace('"+rootSpaceInstLight.getFullId()+"', '"+spaceInstLight.getFullId()+"')";
      }
     
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
   * @throws PdcException 
   * @throws RemoteException 
   * @throws ParseException 
   */
  private List<SearchResult> searchEngineResultSpace(String searchField) throws PdcException {
    List<SearchResult> listSearchResult = new ArrayList<SearchResult>(); 
    MatchingIndexEntry[] plainSearchResults = null;
    QueryDescription query = null;
    try {
      QueryParameters queryParameters = new QueryParameters(getLanguage());
      queryParameters.setKeywords(searchField);
      
      query = queryParameters.getQueryDescription(getUserId(), "*");
      query.addSpaceComponentPair(null, "Spaces");

      getSearchEngineBm().search(query);
      plainSearchResults = getSearchEngineBm().getRange(0,
          getSearchEngineBm().getResultLength());
      
      String nomCrea = null;
      String objectId = null;
      String spaceId = null;
      String emplacement = null;
      List<String> listEmplacement = null;
      SpaceInstLight spaceInstLight = null;
      SpaceInstLight rootSpaceInstLight = null;
      String url = null;
      for (MatchingIndexEntry result : plainSearchResults) {

        nomCrea = getAdminController().getUserDetail(result.getCreationUser()).getDisplayedName();
        
        objectId = result.getObjectId(); // WA3
        spaceId = objectId.substring(2);
        
        url = "";
        spaceInstLight = getAdminController().getSpaceInstLight(spaceId);
        if(spaceInstLight != null) {
          listEmplacement = new ArrayList<String>();
          emplacement = getPathSpace(spaceId);
          listEmplacement.add(emplacement);
          if(spaceInstLight.isRoot()) {
            url = "openSpace('"+spaceInstLight.getFullId()+"')";
          } else {
            rootSpaceInstLight = spaceInstLight;
            while(! rootSpaceInstLight.isRoot()) {
              String fatherId = rootSpaceInstLight.getFatherId();
              rootSpaceInstLight = getAdminController().getSpaceInstLight(fatherId);
            }
            url = "openSubSpace('"+rootSpaceInstLight.getFullId()+"', '"+spaceInstLight.getFullId()+"')";
          }
          
          SearchResult searchResult = new SearchResult();
          searchResult.setName(result.getTitle(getLanguage()));
          searchResult.setDesc(result.getPreview(getLanguage()));
          searchResult.setCreaDate(DateUtil.parse(result.getCreationDate(), "yyyy/MM/dd"));
          searchResult.setCreaName(nomCrea);
          searchResult.setPath(listEmplacement);
          searchResult.setUrl(url);
          listSearchResult.add(searchResult);
        }
      }
    } catch (NoSuchObjectException nsoe) {
      // an error occurs on searchEngine statefull EJB
      // interface is not null but the EJB is !
      // so we set interface to null and we launch again de search.
      searchBm = null;
      listSearchResult = searchEngineResultSpace(searchField);
    } catch (Exception e) {
      throw new PdcException(
          "JobSearchPeasSessionController.searchEngineResultSpace",
          SilverpeasException.ERROR, "pdcPeas.EX_CANT_GET_SEARCH_ENGINE", e);
    }
    return listSearchResult;
    
  }
  
  /**
   * @param searchField
   * @return
   * @throws ParseException 
   * @throws PdcException 
   * @throws RemoteException 
   */
  private List<SearchResult> searchResultSpace(String searchField) throws PdcException {
    //id espace
    List<SearchResult> listResult = searchResultSpaceId(searchField);
      
    //nom espace
    List<SearchResult> listSearchResult = searchEngineResultSpace(searchField);
    
    //fusion des 2 listes
    for(SearchResult searchResult : listSearchResult) {
      listResult.add(searchResult);
    }
    return listResult;
  }
  
  /**
   * @param componentId
   * @return
   */
  private String getPathComponent(String componentId) {
    String emplacement = "";
    //Espace > Sous-espace
    List<SpaceInstLight> spaceList = getAdminController().getPathToComponent(componentId);
    boolean first = true;
    for (SpaceInstLight space : spaceList) {
      if (!first) {
        emplacement += " > ";
      }
      emplacement += space.getName(getLanguage());
      first = false;
    }
    return emplacement;
  }
  
  /**
   * @param searchField
   * @return
   */
  private List<SearchResult> searchResultComponentId(String searchField) {
    List<SearchResult> listResult = new ArrayList<SearchResult>(); 
    ComponentInstLight componentInstLight = getAdminController().getComponentInstLight(searchField);
    if(componentInstLight != null) {
      String nom = componentInstLight.getLabel(getLanguage());
      String desc = componentInstLight.getDescription(getLanguage());
      Date dateCrea = componentInstLight.getCreateDate();
      String nomCrea = getAdminController().getUserDetail(Integer.toString(componentInstLight.getCreatedBy())).getDisplayedName();
      List<String> listEmplacement = new ArrayList<String>();
      String emplacement = getPathComponent(searchField);
      listEmplacement.add(emplacement);
      
      String url = "openComponent('"+componentInstLight.getId()+"')";
     
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
   * @throws PdcException 
   * @throws RemoteException 
   * @throws ParseException 
   */
  private List<SearchResult> searchEngineResultComponent(String searchField) throws PdcException {
    List<SearchResult> listSearchResult = new ArrayList<SearchResult>(); 
    MatchingIndexEntry[] plainSearchResults = null;
    QueryDescription query = null;
    try {
      QueryParameters queryParameters = new QueryParameters(getLanguage());
      queryParameters.setKeywords(searchField);
     
      query = queryParameters.getQueryDescription(getUserId(), "*");
      query.addSpaceComponentPair(null, "Components");

      getSearchEngineBm().search(query);
      plainSearchResults = getSearchEngineBm().getRange(0,
          getSearchEngineBm().getResultLength());
      
      MatchingIndexEntry result = null;
      String creationDate = null;
      String nomCrea = null;
      String componentId = null;
      String emplacement = null;
      List<String> listEmplacement = null;
      String url = null;
      for (int i = 0; i < plainSearchResults.length; i++) {
        result = plainSearchResults[i];

        creationDate = result.getCreationDate();
        nomCrea = getAdminController().getUserDetail(result.getCreationUser()).getDisplayedName();
        
        componentId = result.getObjectId();
        listEmplacement = new ArrayList<String>();
        emplacement = getPathComponent(componentId);
        listEmplacement.add(emplacement);
        
        url = "openComponent('"+componentId+"')";
        
        SearchResult searchResult = new SearchResult();
        searchResult.setName(result.getTitle(getLanguage()));
        searchResult.setDesc(result.getPreview(getLanguage()));
        searchResult.setCreaDate(DateUtil.parse(creationDate, "yyyy/MM/dd"));
        searchResult.setCreaName(nomCrea);
        searchResult.setPath(listEmplacement);
        searchResult.setUrl(url);
        listSearchResult.add(searchResult);
      }
    } catch (NoSuchObjectException nsoe) {
      // an error occurs on searchEngine statefull EJB
      // interface is not null but the EJB is !
      // so we set interface to null and we launch again de search.
      searchBm = null;
      listSearchResult = searchEngineResultComponent(searchField);
    } catch (Exception e) {
      throw new PdcException(
          "JobSearchPeasSessionController.searchEngineResultComponent",
          SilverpeasException.ERROR, "pdcPeas.EX_CANT_GET_SEARCH_ENGINE", e);
    }
    return listSearchResult;
    
  }
    
  /**
   * @param searchField
   * @return
   * @throws PdcException 
   */
  private List<SearchResult> searchResultService(String searchField) throws PdcException {
    //id service
    List<SearchResult> listResult = searchResultComponentId(searchField);
     
    //nom service
    List<SearchResult> listSearchResult = searchEngineResultComponent(searchField);
   
    //fusion des 2 listes
    for(SearchResult searchResult : listSearchResult) {
      listResult.add(searchResult);
    }
    return listResult;
  }

  
  /**
   * @param searchField
   * @return
   * @throws RemoteException 
   */
  private List<SearchResult> searchResultPublication(String searchField) throws RemoteException {
    List<SearchResult> listResult = new ArrayList<SearchResult>(); 
    
    PublicationDetail publication = null;
    PublicationPK pubPK = null;
    
    //id publication
    try {
      Integer.parseInt(searchField);
      pubPK = new PublicationPK(searchField);
      publication = getPublicationBm().getDetail(pubPK);
    } catch(NumberFormatException e) {
      publication = null;
    }
    
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
          ArrayList<NodeDetail> pathTab = new ArrayList<NodeDetail>(path);
          Collections.reverse(pathTab);
          Iterator<NodeDetail> itNode = pathTab.iterator();
          while (itNode.hasNext()) {
            NodeDetail nodeDetail = itNode.next();
            emplacement += nodeDetail.getName(getLanguage()) + " > ";
          }
          emplacement = emplacement.substring(0, emplacement.length() - 3);
          listEmplacement.add(emplacement);
        }
      }
      
      String url = "openPublication('"+URLManager.getSimpleURL(URLManager.URL_PUBLI, pubPK.getId())+"')";
      
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
   * @param group
   * @return
   */
  private String getPathGroup(Group group) {
    String emplacement = "";
    String groupId = group.getId();
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
    List<String> groupList = getAdminController().getPathToGroup(groupId);
    for (String elementGroupId : groupList) {
      emplacement += " > "+ getAdminController().getGroupName(elementGroupId);
    }
    return emplacement;
  }
  
  /**
   * @param searchField
   * @return
   */
  private List<SearchResult> searchResultGroupId(String searchField) {
    List<SearchResult> listResult = new ArrayList<SearchResult>(); 
    Group group = getAdminController().getGroupById(searchField);
    if(group != null && group.getId() != null) {
      String nom = group.getName();
      String desc = group.getDescription();
      Date dateCrea = null;
      String nomCrea = "";
      List<String> listEmplacement = new ArrayList<String>();
      String emplacement = getPathGroup(group);
      listEmplacement.add(emplacement);  
      
      String url = "openGroup('"+group.getId()+"')";
     
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
   * @throws PdcException 
   * @throws RemoteException 
   * @throws ParseException 
   */
  private List<SearchResult> searchEngineResultGroup(String searchField) throws PdcException {
    List<SearchResult> listSearchResult = new ArrayList<SearchResult>(); 
    MatchingIndexEntry[] plainSearchResults = null;
    QueryDescription query = null;
    try {
      QueryParameters queryParameters = new QueryParameters(getLanguage());
      queryParameters.setKeywords(searchField);
     
      query = queryParameters.getQueryDescription(getUserId(), "*");
      query.addSpaceComponentPair(null, "groups");

      getSearchEngineBm().search(query);
      plainSearchResults = getSearchEngineBm().getRange(0,
          getSearchEngineBm().getResultLength());
      
      MatchingIndexEntry result = null;
      String groupId = null;
      Group group = null;
      String emplacement = null;
      List<String> listEmplacement = null;
      String url = null;
      for (int i = 0; i < plainSearchResults.length; i++) {
        result = plainSearchResults[i];

        groupId = result.getObjectId();
        group = getAdminController().getGroupById(groupId);
        listEmplacement = new ArrayList<String>();
        emplacement = getPathGroup(group);
        listEmplacement.add(emplacement);
        
        url = "openGroup('"+groupId+"')";
        
        SearchResult searchResult = new SearchResult();
        searchResult.setName(result.getTitle(getLanguage()));
        searchResult.setDesc(result.getPreview(getLanguage()));
        searchResult.setCreaDate(null);
        searchResult.setCreaName("");
        searchResult.setPath(listEmplacement);
        searchResult.setUrl(url);
        listSearchResult.add(searchResult);
      }
    } catch (NoSuchObjectException nsoe) {
      // an error occurs on searchEngine statefull EJB
      // interface is not null but the EJB is !
      // so we set interface to null and we launch again de search.
      searchBm = null;
      listSearchResult = searchEngineResultGroup(searchField);
    } catch (Exception e) {
      throw new PdcException(
          "JobSearchPeasSessionController.searchEngineResultComponent",
          SilverpeasException.ERROR, "pdcPeas.EX_CANT_GET_SEARCH_ENGINE", e);
    }
    return listSearchResult;
    
  }
  
  /**
   * @param searchField
   * @return
   * @throws PdcException 
   */
  private List<SearchResult> searchResultGroup(String searchField) throws PdcException {
    //id group
    List<SearchResult> listResult = searchResultGroupId(searchField);
     
    //nom group
    List<SearchResult> listSearchResult = searchEngineResultGroup(searchField);
   
    //fusion des 2 listes
    for(SearchResult searchResult : listSearchResult) {
      listResult.add(searchResult);
    }
    return listResult;
  }

  
  /**
   * @param user
   * @return
   */
  private List<String> getListPathUser(UserDetail user) {
    List<String> listEmplacement = new ArrayList<String>();
    String emplacement = "";
    String userId = user.getId();
    
    //groupe(s) d'appartenance
    String[] groupIds = getAdminController().getDirectGroupsIdsOfUser(userId);
    if (groupIds != null && groupIds.length > 0) {
      for (int iGrp = 0; iGrp < groupIds.length; iGrp++) {
        Group group = getOrganizationController().getGroup(groupIds[iGrp]);
        
        String domainId = group.getDomainId();
        if(domainId == null) {
          domainId = "-1";
        }
        Domain domain = getAdminController().getDomain(domainId);
        emplacement = "";
        //nom du domaine
        if("-1".equals(domainId)) {//domaine mixte
          emplacement += getString("JSP.domainMixt");  
        } else {
          emplacement += domain.getName();
        }
        
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
      
      String domainId = user.getDomainId();
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
      
      listEmplacement.add(emplacement);
    }
    return listEmplacement;
  }
  
  /**
   * @param searchField
   * @param listResult
   */
  private List<SearchResult> searchResultUserId(String searchField) {
    List<SearchResult> listResult = new ArrayList<SearchResult>(); 
    UserDetail user = getAdminController().getUserDetail(searchField);
    if(user != null) {
      String nom = user.getDisplayedName();
      String desc = user.geteMail();
      Date dateCrea = null;
      String nomCrea = "";
      List<String> listEmplacement = getListPathUser(user);
      
      String url = "openUser('"+user.getId()+"')";
     
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
   * @throws PdcException 
   * @throws RemoteException 
   * @throws ParseException 
   */
  private List<SearchResult> searchEngineResultUser(String searchField) throws PdcException {
    List<SearchResult> listSearchResult = new ArrayList<SearchResult>(); 
    MatchingIndexEntry[] plainSearchResults = null;
    QueryDescription query = null;
    try {
      QueryParameters queryParameters = new QueryParameters(getLanguage());
      queryParameters.setKeywords(searchField);
     
      query = queryParameters.getQueryDescription(getUserId(), "*");
      query.addSpaceComponentPair(null, "users");

      getSearchEngineBm().search(query);
      plainSearchResults = getSearchEngineBm().getRange(0,
          getSearchEngineBm().getResultLength());
      
      MatchingIndexEntry result = null;
      String userId = null;
      UserDetail user = null;
      List<String> listEmplacement = null;
      String url = null;
      for (int i = 0; i < plainSearchResults.length; i++) {
        result = plainSearchResults[i];

        userId = result.getObjectId();
        user = getAdminController().getUserDetail(userId);
        listEmplacement = getListPathUser(user);
        
        url = "openUser('"+userId+"')";
        
        SearchResult searchResult = new SearchResult();
        searchResult.setName(result.getTitle(getLanguage()));
        searchResult.setDesc(result.getPreview(getLanguage()));
        searchResult.setCreaDate(null);
        searchResult.setCreaName("");
        searchResult.setPath(listEmplacement);
        searchResult.setUrl(url);
        listSearchResult.add(searchResult);
      }
    } catch (NoSuchObjectException nsoe) {
      // an error occurs on searchEngine statefull EJB
      // interface is not null but the EJB is !
      // so we set interface to null and we launch again de search.
      searchBm = null;
      listSearchResult = searchEngineResultUser(searchField);
    } catch (Exception e) {
      throw new PdcException(
          "JobSearchPeasSessionController.searchEngineResultUser",
          SilverpeasException.ERROR, "pdcPeas.EX_CANT_GET_SEARCH_ENGINE", e);
    }
    return listSearchResult;
    
  }
  
  /**
   * @param searchField
   * @return
   * @throws PdcException 
   */
  private List<SearchResult> searchResultUser(String searchField) throws PdcException {
    //id user
    List<SearchResult> listResult = searchResultUserId(searchField);
     
    //nom user
    List<SearchResult> listSearchResult = searchEngineResultUser(searchField);
   
    //fusion des 2 listes
    for(SearchResult searchResult : listSearchResult) {
      listResult.add(searchResult);
    }
    return listResult;
  }

  /**
   * @param searchField
   * @param category
   * @return
   * @throws RemoteException 
   * @throws PdcException 
   */
  public List<SearchResult> searchResult(String searchField, String category) throws RemoteException, PdcException {
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
