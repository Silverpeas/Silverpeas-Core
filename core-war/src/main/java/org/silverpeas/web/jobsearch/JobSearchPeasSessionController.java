/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.jobsearch;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.domain.model.Domain;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.Group;
import org.silverpeas.core.admin.user.model.GroupDetail;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.publication.service.PublicationService;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.node.model.NodePath;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.web.pdc.QueryParameters;

import javax.annotation.Nonnull;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class declaration
 *
* @author Cécile Bonin
 */
public class JobSearchPeasSessionController extends AbstractComponentSessionController {

  private static final String JSP_DOMAIN_MIXT = "JSP.domainMixt";
  private AdminController myAdminController = ServiceProvider.getService(AdminController.class);
  private PublicationService publicationService = PublicationService.get();
  private NodeService nodeService = NodeService.get();
  private String searchField = null;
  private String category = null;
  private List<SearchResult> listResult = null;

  /**
   *
   * Standard Session Controller Constructeur
   *
   * @param mainSessionCtrl The user's profile
   * @param componentContext The component's profile
   */
  public JobSearchPeasSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "org.silverpeas.jobSearchPeas.multilang.jobSearchPeasBundle");
    setComponentRootName(URLUtil.CMP_JOBSEARCHPEAS);
  }

  /**
   * @return
   */
  public AdminController getAdminController() {
    return myAdminController;
  }

  /**
   * @return
   */
  private PublicationService getPublicationService() {
    return publicationService;
  }

  /**
   * @return
   */
  private NodeService getNodeService() {
    return nodeService;
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
    StringBuilder emplacement = new StringBuilder("");
    try {
      //Espace > Sous-espace
      List<SpaceInstLight> spaceList = getAdminController().getPathToSpace(spaceId, false);
      boolean first = true;
      for (SpaceInstLight space : spaceList) {
        if (!first) {
          emplacement.append(" > ");
        }
        emplacement.append(space.getName(getLanguage()));
        first = false;
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return emplacement.toString();
  }

  /**
   * @param spaceId
   * @return
   */
  private List<SearchResult> searchResultSpaceId(String spaceId) {

    List<SearchResult> result = new ArrayList<>();
    SpaceInstLight spaceInstLight = getAdminController().getSpaceInstLight(spaceId);
    if (null != spaceInstLight) {
      String nom = spaceInstLight.getName(getLanguage());
      String desc = spaceInstLight.getDescription();
      Date dateCrea = spaceInstLight.getCreationDate();
      String nomCrea = getUserName(spaceInstLight.getCreatedBy());
      List<String> listEmplacement = new ArrayList<>();
      String emplacement = getPathSpace(spaceId);
      listEmplacement.add(emplacement);

      String url;
      if (spaceInstLight.isRoot()) {
        url = "openSpace('" + spaceInstLight.getId() + "')";
      } else {
        SpaceInstLight rootSpaceInstLight = spaceInstLight;
        while (!rootSpaceInstLight.isRoot()) {
          String fatherId = rootSpaceInstLight.getFatherId();
          rootSpaceInstLight = getAdminController().getSpaceInstLight(fatherId);
        }
        url = "openSubSpace('" + rootSpaceInstLight.getId() + "', '" + spaceInstLight.
            getId() + "')";
      }

      SearchResult searchResult = new SearchResult();
      searchResult.setName(nom);
      searchResult.setDesc(desc);
      searchResult.setCreaDate(dateCrea);
      searchResult.setCreaName(nomCrea);
      searchResult.setPath(listEmplacement);
      searchResult.setUrl(url);
      result.add(searchResult);
    }
    return result;
  }

  /**
   * @param searchField
   * @return
   * @throws PdcException
   */
  private List<SearchResult> searchEngineResultSpace(String searchField) throws PdcException {
    List<SearchResult> listSearchResult = new ArrayList<>();
    try {
      QueryDescription query = initQuery(searchField);
      query.addComponent("Spaces");
      List<MatchingIndexEntry> plainSearchResults =
          SearchEngineProvider.getSearchEngine().search(query).getEntries();
      for (MatchingIndexEntry result : plainSearchResults) {
        String nomCrea = getUserName(Integer.parseInt(result.getCreationUser()));

        String spaceId = result.getObjectId();

        SpaceInstLight spaceInstLight = getAdminController().getSpaceInstLight(spaceId);
        if (null != spaceInstLight) {
          List<String> listEmplacement = new ArrayList<>();
          String emplacement = getPathSpace(spaceId);
          listEmplacement.add(emplacement);
          SearchResult searchResult = new SearchResult();
          searchResult.setName(result.getTitle(getLanguage()));
          searchResult.setDesc(result.getPreview(getLanguage()));
          searchResult.setCreaDate(DateUtil.parseFromLucene(result.getCreationDate()));
          searchResult.setCreaName(nomCrea);
          searchResult.setPath(listEmplacement);
          if (spaceInstLight.isRoot()) {
            searchResult.setUrl("openSpace('" + spaceInstLight.getId() + "')");
          } else {
            SpaceInstLight rootSpaceInstLight = spaceInstLight;
            while (!rootSpaceInstLight.isRoot()) {
              String fatherId = rootSpaceInstLight.getFatherId();
              rootSpaceInstLight = getAdminController().getSpaceInstLight(fatherId);
            }
            searchResult.setUrl("openSubSpace('" + rootSpaceInstLight.getId() + "', '"
                + spaceInstLight.getId() + "')");
          }
          listSearchResult.add(searchResult);
        }
      }
    } catch (ParseException e) {
      throw new PdcException(e);
    }
    return listSearchResult;

  }

  /**
   * @param searchField
   * @return
   * @throws PdcException
   */
  private List<SearchResult> searchResultSpace(String searchField) throws PdcException {
    //id espace
    List<SearchResult> result = searchResultSpaceId(searchField);
    //nom espace
    List<SearchResult> listSearchResult = searchEngineResultSpace(searchField);
    //fusion des 2 listes
    for (SearchResult searchResult : listSearchResult) {
      result.add(searchResult);
    }
    return result;
  }

  /**
   * @param componentId
   * @return
   */
  private String getPathComponent(String componentId) {
    StringBuilder emplacement = new StringBuilder("");
    //Espace > Sous-espace
    List<SpaceInstLight> spaceList = getAdminController().getPathToComponent(componentId);
    boolean first = true;
    for (SpaceInstLight space : spaceList) {
      if (!first) {
        emplacement.append(" > ");
      }
      emplacement.append(space.getName(getLanguage()));
      first = false;
    }
    return emplacement.toString();
  }

  /**
   * @param componentId
   * @return
   */
  private List<SearchResult> searchResultComponentId(String componentId) {

    List<SearchResult> result = new ArrayList<>();
    ComponentInstLight componentInstLight = getAdminController().getComponentInstLight(componentId);
    if (null != componentInstLight) {
      String nom = componentInstLight.getLabel(getLanguage());
      String desc = componentInstLight.getDescription(getLanguage());
      Date dateCrea = componentInstLight.getCreationDate();
      String nomCrea = getUserName(componentInstLight.getCreatedBy());
      List<String> listEmplacement = new ArrayList<>();
      String emplacement = getPathComponent(componentId);
      listEmplacement.add(emplacement);

      String url = "openComponent('" + componentInstLight.getId() + "')";

      SearchResult searchResult = new SearchResult();
      searchResult.setName(nom);
      searchResult.setDesc(desc);
      searchResult.setCreaDate(dateCrea);
      searchResult.setCreaName(nomCrea);
      searchResult.setPath(listEmplacement);
      searchResult.setUrl(url);
      result.add(searchResult);
    }
    return result;
  }

  private String getUserName(int userId) {
    if (userId != -1) {
      UserDetail user = getAdminController().getUserDetail(Integer.toString(userId));
      if (user != null) {
        return user.getDisplayedName();
      }
    }
    return "";
  }

  /**
   * @param searchField
   * @return
   * @throws PdcException
   */
  private List<SearchResult> searchEngineResultComponent(String searchField) throws PdcException {
    List<SearchResult> listSearchResult = new ArrayList<>();

    try {
      QueryDescription query = initQuery(searchField);
      query.addComponent("Components");
      List<MatchingIndexEntry> plainSearchResults =
          SearchEngineProvider.getSearchEngine().search(query).getEntries();

      for (MatchingIndexEntry result : plainSearchResults) {
        String creationDate = result.getCreationDate();
        String nomCrea = getUserName(Integer.parseInt(result.getCreationUser()));
        String componentId = result.getObjectId();
        List<String> listEmplacement = new ArrayList<>();
        String emplacement = getPathComponent(componentId);
        listEmplacement.add(emplacement);
        SearchResult searchResult = new SearchResult();
        searchResult.setName(result.getTitle(getLanguage()));
        searchResult.setDesc(result.getPreview(getLanguage()));
        searchResult.setCreaDate(DateUtil.parseFromLucene(creationDate));
        searchResult.setCreaName(nomCrea);
        searchResult.setPath(listEmplacement);
        searchResult.setUrl("openComponent('" + componentId + "')");
        listSearchResult.add(searchResult);
      }
    } catch (ParseException e) {
      throw new PdcException(e);
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
    List<SearchResult> result = searchResultComponentId(searchField);
    //nom service
    List<SearchResult> listSearchResult = searchEngineResultComponent(searchField);
    //fusion des 2 listes
    for (SearchResult searchResult : listSearchResult) {
      result.add(searchResult);
    }
    return result;
  }

  /**
   * @param searchField
   * @return
   * @throws RemoteException
   */
  private List<SearchResult> searchResultPublication(String searchField) {
    List<SearchResult> result = new ArrayList<>();

    PublicationDetail publication;
    //id publication
    try {
      Integer.parseInt(searchField);
      PublicationPK pubPK = new PublicationPK(searchField);
      publication = getPublicationService().getDetail(pubPK);
    } catch (NumberFormatException e) {
      publication = null;
    }

    if (null != publication) {
      final String nom = publication.getName(getLanguage());
      final String desc = publication.getDescription(getLanguage());
      final Date dateCrea = publication.getCreationDate();
      final String creaId = publication.getCreatorId();
      final String nomCrea = getUserName(Integer.parseInt(creaId));
      final PublicationPK pubPK = publication.getPK();
      final List<String> paths = getPublicationService().getAllLocations(pubPK).stream()
          .map(l -> {
            final NodePath path = getNodeService().getPath(l);
            String formattedPath = path.format(getLanguage(), true);
            if (l.isAlias()) {
              formattedPath += " (" + getString("GML.alias") + ")";
            }
            return formattedPath;
          })
          .collect(Collectors.toList());
      SearchResult searchResult = new SearchResult();
      searchResult.setName(nom);
      searchResult.setDesc(desc);
      searchResult.setCreaDate(dateCrea);
      searchResult.setCreaName(nomCrea);
      searchResult.setPath(paths);
      searchResult.setUrl("openPublication('" + URLUtil.getSimpleURL(URLUtil.URL_PUBLI, pubPK.getId()) + "')");
      result.add(searchResult);
    }
    return result;
  }

  /**
   * @param group
   * @return
   */
  private String getPathGroup(Group group) {
    final String groupId = group.getId();
    final StringBuilder emplacement = setDomainInPath(group.getDomainId());
    //nom du(des) groupe(s) pères
    final List<String> groupList = getAdminController().getPathToGroup(groupId);
    for (String elementGroupId : groupList) {
      emplacement.append(" > ").append(getAdminController().getGroupName(elementGroupId));
    }
    return emplacement.toString();
  }

  @Nonnull
  private StringBuilder setDomainInPath(final String domainId) {
    StringBuilder emplacement = new StringBuilder();
    final String theDomainId = domainId == null ? "-1" : domainId;
    final Domain domain = getAdminController().getDomain(theDomainId);
    if (domain.isMixedOne()) {
      emplacement.append(getString(JSP_DOMAIN_MIXT));
    } else {
      emplacement.append(domain.getName());
    }
    return emplacement;
  }

  /**
   * @param searchField
   * @return
   */
  private List<SearchResult> searchResultGroupId(String searchField) {
    List<SearchResult> result = new ArrayList<>();
    Group group = getAdminController().getGroupById(searchField);
    if (null != group && null != group.getId()) {
      String nom = group.getName();
      String desc = group.getDescription();
      List<String> listEmplacement = new ArrayList<>();
      String emplacement = getPathGroup(group);
      listEmplacement.add(emplacement);

      String url = "openGroup('" + group.getId() + "')";

      SearchResult searchResult = new SearchResult();
      searchResult.setName(nom);
      searchResult.setDesc(desc);
      searchResult.setCreaName("");
      searchResult.setPath(listEmplacement);
      searchResult.setUrl(url);
      result.add(searchResult);
    }
    return result;
  }

  /**
   * @param searchField
   * @return
   * @throws PdcException
   */
  private List<SearchResult> searchEngineResultGroup(String searchField) throws PdcException {
    List<SearchResult> listSearchResult = new ArrayList<>();
    try {
      QueryDescription query = initQuery(searchField);
      query.addComponent("groups");

      List<MatchingIndexEntry> plainSearchResults =
          SearchEngineProvider.getSearchEngine().search(query).getEntries();

      for (MatchingIndexEntry result : plainSearchResults) {
        String groupId = result.getObjectId();
        Group group = getAdminController().getGroupById(groupId);
        List<String> listEmplacement = new ArrayList<>();
        String emplacement = getPathGroup(group);
        listEmplacement.add(emplacement);

        String url = "openGroup('" + groupId + "')";

        SearchResult searchResult = new SearchResult();
        searchResult.setName(result.getTitle(getLanguage()));
        searchResult.setDesc(result.getPreview(getLanguage()));
        searchResult.setCreaName("");
        searchResult.setPath(listEmplacement);
        searchResult.setUrl(url);
        listSearchResult.add(searchResult);
      }
    } catch (ParseException e) {
      throw new PdcException(e);
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
    List<SearchResult> result = searchResultGroupId(searchField);
    //nom group
    List<SearchResult> listSearchResult = searchEngineResultGroup(searchField);
    //fusion des 2 listes
    for (SearchResult searchResult : listSearchResult) {
      result.add(searchResult);
    }
    return result;
  }

  private List<String> getListPathUser(UserDetail user) {
    List<String> listEmplacement = new ArrayList<>();
    String userId = user.getId();

    //groupe(s) d'appartenance
    List<GroupDetail> groups = getAdminController().getDirectGroupsOfUser(userId);
    if (!groups.isEmpty()) {
      for (Group group : groups) {
        final String domainId = group.getDomainId() == null ? "-1" : group.getDomainId();
        final Domain domain = getAdminController().getDomain(domainId);
        final StringBuilder emplacement = new StringBuilder();
        //nom du domaine
        if (domain.isMixedOne()) {
          emplacement.append(getString(JSP_DOMAIN_MIXT));
        } else {
          emplacement.append(domain.getName());
        }

        //nom du(des) groupe(s) pères
        List<String> groupList = getAdminController().getPathToGroup(group.getId());
        for (String elementGroupId : groupList) {
          emplacement.append(" > ").append(getAdminController().getGroupName(elementGroupId));
        }
        //nom du groupe
        emplacement.append(" > ").append(group.getName());
        listEmplacement.add(emplacement.toString());
      }
    } else {
      final StringBuilder emplacement = setDomainInPath(user.getDomainId());
      listEmplacement.add(emplacement.toString());
    }
    return listEmplacement;
  }

  /**
   * @param searchField
   * @return
   */
  private List<SearchResult> searchResultUserId(String searchField) {
    List<SearchResult> result = new ArrayList<>();
    UserDetail user = getAdminController().getUserDetail(searchField);
    if (null != user) {
      String nom = user.getDisplayedName();
      String desc = user.getEmailAddress();
      List<String> listEmplacement = getListPathUser(user);

      String url = "openUser('" + user.getId() + "')";

      SearchResult searchResult = new SearchResult();
      searchResult.setName(nom);
      searchResult.setDesc(desc);
      searchResult.setCreaName("");
      searchResult.setPath(listEmplacement);
      searchResult.setUrl(url);
      result.add(searchResult);
    }

    return result;
  }

  /**
   * @param searchField
   * @return
   * @throws PdcException
   */
  private List<SearchResult> searchEngineResultUser(String searchField) throws PdcException {
    List<SearchResult> listSearchResult = new ArrayList<>();
    try {
      QueryDescription query = initQuery(searchField);
      query.addComponent("users");

      List<MatchingIndexEntry> plainSearchResults =
          SearchEngineProvider.getSearchEngine().search(query).getEntries();

      for (MatchingIndexEntry result : plainSearchResults) {
        String userId = result.getObjectId();
        UserDetail user = getAdminController().getUserDetail(userId);
        List<String> listEmplacement = getListPathUser(user);

        String url = "openUser('" + userId + "')";

        SearchResult searchResult = new SearchResult();
        searchResult.setName(result.getTitle(getLanguage()));
        searchResult.setDesc(result.getPreview(getLanguage()));
        searchResult.setCreaName("");
        searchResult.setPath(listEmplacement);
        searchResult.setUrl(url);
        listSearchResult.add(searchResult);
      }
    } catch (ParseException e) {
      throw new PdcException(e);
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
    List<SearchResult> result = searchResultUserId(searchField);
    //nom user
    List<SearchResult> listSearchResult = searchEngineResultUser(searchField);
    //fusion des 2 listes
    for (SearchResult searchResult : listSearchResult) {
      result.add(searchResult);
    }
    return result;
  }

  /**
   * @param searchField
   * @param category
   * @return
   * @throws PdcException
   */
  public List<SearchResult> searchResult(String searchField, String category) throws PdcException {
    if ("space".equals(category)) {
      return searchResultSpace(searchField);
    } else if ("service".equals(category)) {
      return searchResultService(searchField);
    } else if ("publication".equals(category)) {
      return searchResultPublication(searchField);
    } else if ("group".equals(category)) {
      return searchResultGroup(searchField);
    } else if ("user".equals(category)) {
      return searchResultUser(searchField);
    }
    return Collections.emptyList();
  }

  private QueryDescription initQuery(String userQuery) {
    QueryParameters queryParameters = new QueryParameters();
    queryParameters.setKeywords(userQuery);

    QueryDescription query = queryParameters.getQueryDescription(getUserId(), "*");
    query.setAdminScope(true);
    return query;
  }
}