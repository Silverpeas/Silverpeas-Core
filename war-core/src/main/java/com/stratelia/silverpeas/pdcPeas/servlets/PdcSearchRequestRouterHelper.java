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
package com.stratelia.silverpeas.pdcPeas.servlets;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

import com.silverpeas.interestCenter.model.InterestCenter;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.SearchAxis;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.SearchCriteria;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.pdcPeas.Keys;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController;
import com.stratelia.silverpeas.pdcPeas.model.GlobalSilverResult;
import com.stratelia.silverpeas.pdcPeas.model.QueryParameters;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.UserDetail;

public class PdcSearchRequestRouterHelper {

  public static QueryParameters saveUserChoicesAndSetPdcInfo(
      PdcSearchSessionController pdcSC, HttpServletRequest request,
      boolean setPdcInfo) throws Exception {
    QueryParameters queryParameters = saveUserChoices(pdcSC, request);
    setUserChoices(request, pdcSC);
    setAttributesAdvancedSearch(pdcSC, request, setPdcInfo);
    if (setPdcInfo) {
      setPertinentAxis(pdcSC, request);
      setContext(pdcSC, request);
    }
    return queryParameters;
  }

  public static QueryParameters saveFavoriteRequestAndSetPdcInfo(
      PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    String favoriteRequestId = request.getParameter("iCenterId");
    return saveFavoriteRequestAndSetPdcInfo(pdcSC, request, favoriteRequestId);
  }

  public static QueryParameters saveFavoriteRequestAndSetPdcInfo(
      PdcSearchSessionController pdcSC, HttpServletRequest request,
      String favoriteRequestId) throws Exception {
    // this parameter is for Back Button on result page
    String urlToRedirect = request.getParameter("urlToRedirect");
    request.setAttribute("urlToRedirect", urlToRedirect);
    // load settings of selected Interest center
    InterestCenter ic = pdcSC.loadICenter(favoriteRequestId);
    QueryParameters queryParameters = saveFavoriteRequest(pdcSC, ic);
    setUserChoices(request, pdcSC);
    setAttributesAdvancedSearch(pdcSC, request, true);
    setPertinentAxis(pdcSC, request);
    setContext(pdcSC, request);

    return queryParameters;
  }

  public static QueryParameters saveFavoriteRequest(
      PdcSearchSessionController pdcSC, InterestCenter favoriteRequest)
      throws Exception {
    String query = favoriteRequest.getQuery();
    String spaceId = favoriteRequest.getWorkSpaceID();
    String componentId = favoriteRequest.getPeasID();
    String authorSearch = favoriteRequest.getAuthorID();
    Date afterdate = favoriteRequest.getAfterDate();
    Date beforedate = favoriteRequest.getBeforeDate();

    if (spaceId != null) {
      spaceId = spaceId.trim();
    }
    if (componentId != null) {
      componentId = componentId.trim();
    }
    if (authorSearch != null) {
      authorSearch = authorSearch.trim();
    }

    SilverTrace.debug("pdcPeas","PdcPeasRequestRouterHelper.saveFavoriteRequest()",
        "root.MSG_GEN_PARAM_VALUE", "authorSearch = " + authorSearch);
    QueryParameters queryParameters = pdcSC.getQueryParameters();
    queryParameters.setKeywords(query);
    queryParameters.setSpaceId(spaceId);
    queryParameters.setInstanceId(componentId);
    queryParameters.setCreatorId(authorSearch);
    queryParameters.setAfterDate(afterdate);
    queryParameters.setBeforeDate(beforedate);

    return queryParameters;
  }

  public static QueryParameters saveUserChoices(
      PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    // build information for the home jsp for the advancedsearch plain text
    // We get user choices about advanced search and store it in the
    // PdcSearchSessionController
    String query = request.getParameter("query");

    QueryParameters queryParameters = pdcSC.getQueryParameters();
    queryParameters.setKeywords(query);

    if (pdcSC.getSearchType() >= PdcSearchSessionController.SEARCH_ADVANCED) {
      queryParameters.setSpaceId(request.getParameter("spaces"));
      queryParameters.setInstanceId(request.getParameter("componentSearch"));
      queryParameters.setCreatorId(request.getParameter("authorSearch"));
      queryParameters.setAfterDate(request.getParameter("createafterdate"));
      queryParameters.setBeforeDate(request.getParameter("createbeforedate"));
      queryParameters.setAfterUpdateDate(request.getParameter("updateafterdate"));
      queryParameters.setBeforeUpdateDate(request.getParameter("updatebeforedate"));
    }

    String paramNbResToDisplay = request.getParameter("nbRes");
    if (paramNbResToDisplay != null) {
      int nbResToDisplay = Integer.parseInt(paramNbResToDisplay);
      pdcSC.setNbResToDisplay(nbResToDisplay);
    }
    String paramSortRes = request.getParameter("sortRes");
    if (paramSortRes != null) {
      int sortRes = Integer.parseInt(paramSortRes);
      pdcSC.setSortValue(sortRes);
    }
    String paramSortOrder = request.getParameter("sortOrder");   
    if (paramSortOrder != null) {
      pdcSC.setSortOrder(paramSortOrder);
    }
    String paramSortResFieldXForm = request.getParameter(Keys.RequestSortXformField.value());
    if (StringUtil.isDefined(paramSortResFieldXForm)) {
      pdcSC.setXmlFormSortValue(paramSortResFieldXForm);
    }
    else {
      pdcSC.setXmlFormSortValue(null);
    }
    String sortImplementor = request.getParameter(Keys.RequestSortImplementor.value());
    if (StringUtil.isDefined(sortImplementor)) {
      pdcSC.setSortImplemtor(sortImplementor);
    }
    else {
      pdcSC.setSortImplemtor(null);
    }
    return queryParameters;
  }

  /**
   * Get user choices from the PdcSearchSessionController and put it in the Request
   * @param request 
   * @param pdcSC
   * @throws Exception 
   */
  public static void setUserChoices(HttpServletRequest request, PdcSearchSessionController pdcSC) 
      throws Exception {
    QueryParameters queryParameters = pdcSC.getQueryParameters();
    if (queryParameters != null) {
      String authorSearch = queryParameters.getCreatorId();
      // travail sur l'auteur
      if (authorSearch != null) {
        UserDetail userDetail = pdcSC.getOrganizationController().getUserDetail(authorSearch);
        queryParameters.setCreatorDetail(userDetail);
      }
      request.setAttribute("QueryParameters", queryParameters);
    }
    request.setAttribute("DisplayParamChoices", pdcSC.getDisplayParamChoices());
    request.setAttribute("ChoiceNbResToDisplay", pdcSC.getListChoiceNbResToDisplay());
    request.setAttribute("NbResToDisplay", Integer.valueOf(pdcSC.getNbResToDisplay()));
    request.setAttribute("SortValue", Integer.valueOf(pdcSC.getSortValue()));
    request.setAttribute("SortOrder", pdcSC.getSortOrder());

    // List of user favorite requests
    List favoriteRequests = buildICentersList(pdcSC);
    String requestSelected = request.getParameter("iCenterId");
    request.setAttribute("RequestList", favoriteRequests);
    if (requestSelected != null) {
      request.setAttribute("RequestSelected", requestSelected);
    }

    String showAllAxis = request.getParameter("showNotOnlyPertinentAxisAndValues");
    if ("true".equals(showAllAxis)) {
      pdcSC.setShowOnlyPertinentAxisAndValues(false);
      request.setAttribute("showAllAxis", "true");
    } else {
      pdcSC.setShowOnlyPertinentAxisAndValues(true);
    }
    request.setAttribute("synonyms", pdcSC.getSynonyms());
    // put search type
    request.setAttribute("SearchType", Integer.valueOf(pdcSC.getSearchType()));
  }

  /**
   * set attributes into the request Attributes are build by information about both
   * sessioncontroller
   * @param pdcSC 
   * @param setSpacesAndComponents
   * @throws Exception
   */
  public static void setAttributesAdvancedSearch(
      PdcSearchSessionController pdcSC, HttpServletRequest request,
      boolean setSpacesAndComponents) throws Exception {
    String selectedSpace = null;
    String selectedComponent = null;

    QueryParameters queryParameters = pdcSC.getQueryParameters();
    if (queryParameters != null) {
      selectedSpace = queryParameters.getSpaceId();
      selectedComponent = queryParameters.getInstanceId();
    }

    SilverTrace.info("pdcPeas",
        "PdcPeasRequestRouterHelper.setAttributesAdvancedSearch()",
        "root.MSG_GEN_PARAM_VALUE", "selectedSpace = " + selectedSpace);
    SilverTrace.info("pdcPeas",
        "PdcPeasRequestRouterHelper.setAttributesAdvancedSearch()",
        "root.MSG_GEN_PARAM_VALUE", "selectedComponent = " + selectedComponent);

    if (setSpacesAndComponents) {
      request.setAttribute("SpaceList", pdcSC.getAllowedSpaces());

      if (selectedSpace != null) {
        request.setAttribute("ComponentList", pdcSC.getAllowedComponents(selectedSpace));
      }
    }

    if (!pdcSC.isSelectionActivated()) {
      pdcSC.buildComponentListWhereToSearch(selectedSpace, selectedComponent);
    }

    // The selection is active ?
    request.setAttribute("ActiveSelection", Boolean.valueOf(pdcSC.isSelectionActivated()));
  }

  /**
   * put in the request the primary axis and eventually the secondary axis accroding to search
   * context
   * @param pdcSC
   * @param request 
   * @throws Exception
   */
  public static void setPertinentAxis(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception {
    SilverTrace.info("pdcPeas",
        "PdcPeasRequestRouterHelper.setPertinentAxis()",
        "root.MSG_GEN_ENTER_METHOD");

    String showSecondarySearchAxis = request.getParameter("ShowSndSearchAxis");

    // does the user want to see secondary axis ?
    if (showSecondarySearchAxis == null) {
      showSecondarySearchAxis = pdcSC.getSecondaryAxis();
    } else {
      pdcSC.setSecondaryAxis(showSecondarySearchAxis);
    }

    SearchAxis axis = null;

    SilverTrace.info("pdcPeas",
        "PdcPeasRequestRouterHelper.setAttributesAdvancedSearch()",
        "root.MSG_GEN_PARAM_VALUE", "avant getAxis(P)");

    // we get primary and eventually secondary axis
    List primarySearchAxis = pdcSC.getAxis("P");
    for (int p = 0; p < primarySearchAxis.size(); p++) {
      axis = (SearchAxis) primarySearchAxis.get(p);
      axis.setValues(pdcSC.getDaughterValues(
          Integer.toString(axis.getAxisId()), "0"));
    }

    SilverTrace.info("pdcPeas",
        "PdcPeasRequestRouterHelper.setAttributesAdvancedSearch()",
        "root.MSG_GEN_PARAM_VALUE", "avant getAxis(S)");
    List secondarySearchAxis = null;
    if ("YES".equals(showSecondarySearchAxis)) {
      // user wants to see secondary axis
      secondarySearchAxis = pdcSC.getAxis("S");
      for (int s = 0; s < secondarySearchAxis.size(); s++) {
        axis = (SearchAxis) secondarySearchAxis.get(s);
        axis.setValues(pdcSC.getDaughterValues(Integer.toString(axis.getAxisId()), "0"));
      }
    }

    // We set axis into the request
    request.setAttribute("ShowPrimaryAxis", primarySearchAxis);
    request.setAttribute("ShowSecondaryAxis", secondarySearchAxis);
    request.setAttribute("ShowSndSearchAxis", pdcSC.getSecondaryAxis());

    SilverTrace.info("pdcPeas",
        "PdcPeasRequestRouterHelper.setPertinentAxis()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * put in the request the primary axis and eventually the secondary axis accroding to search
   * context
   * @param pdcSC
   * @param request
   * @throws Exception 
   */
  public static void setContext(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception {
    SilverTrace.info("pdcPeas", "PdcPeasRequestRouterHelper.setContext()",
        "root.MSG_GEN_ENTER_METHOD");

    // on retire du searchcontext tous les criteres qui ne sont pas dans
    // l'espace choisi par l'utilisateur.
    // Dans ce cas, on retire de la list de searchContext, le critere de
    // recherche.
    SearchContext searchContext = pdcSC.getSearchContext();
    @SuppressWarnings("unchecked")
    List<SearchAxis> primaryAxis = (List<SearchAxis>) request.getAttribute("ShowPrimaryAxis");
    @SuppressWarnings("unchecked")
    List<SearchAxis> secondaryAxis = (List<SearchAxis>) request.getAttribute("ShowSecondaryAxis");
    boolean isExistInPrimaryAxis = false;
    boolean isExistInSecondaryAxis = false;
    SearchAxis sa = null;

    List<SearchCriteria> c = searchContext.getCriterias();
    Axis axis = null;
    int searchAxisId;
    String searchValue = "";
    String treeId = "";
    List<List<Value>> pathCriteria = new ArrayList<List<Value>>(c.size());
    if (!c.isEmpty()) {
      for(SearchCriteria sc : c) {
        searchAxisId = sc.getAxisId();
        if (primaryAxis != null) {
          // on parcourt la liste des axes primaires
          // si l'on trouve un axisId de searchCriteria = axisId de l'axe
          // primaire alors on le laisse
          // dans le searchCriteria sinon on le supprime
          for (int j = 0 ; j < primaryAxis.size(); j++) {
            sa = primaryAxis.get(j);
            if (searchAxisId == sa.getAxisId()) {
              isExistInPrimaryAxis = true;
              break;
            }
          }

        }
        if ((!isExistInPrimaryAxis) && (secondaryAxis != null)) {
          // on parcourt la liste des axes secondaires
          // si l'on trouve un axisId de searchCriteria = axisId de l'axe
          // secondaire alors on l'enleve
          for (int j = 0; j < secondaryAxis.size(); j++) {
            sa = secondaryAxis.get(j);
            if (searchAxisId == sa.getAxisId()) {
              isExistInSecondaryAxis = true;
              break;
            }
          }
        }

        if (isExistInSecondaryAxis || isExistInPrimaryAxis) {
          searchValue = getLastValueOf(sc.getValue());
          // on creait un axis
          axis = pdcSC.getAxisDetail(String.valueOf(searchAxisId));
          treeId = String.valueOf(axis.getAxisHeader().getRootId());
          @SuppressWarnings("unchecked")
          List<Value> fullPath = pdcSC.getFullPath(searchValue, treeId);
          pathCriteria.add(fullPath);
        }
      }
    }
    request.setAttribute("PathCriteria", pathCriteria);
    // on ajoute le contexte de recherche
    request.setAttribute("SearchContext", searchContext);
    SilverTrace.info("pdcPeas", "PdcPeasRequestRouterHelper.setContext()",
        "root.MSG_GEN_EXIT_METHOD");
  }

  public static String getLastValueOf(String path) {
    // cherche l'id de la valeur
    // valeur de la forme /0/1/2/  
    String newValueId = path;
    int len = path.length();
    path = path.substring(0, len - 1); // on retire le dernier slash

    if ("/".equals(path)) {
      newValueId = newValueId.substring(1);// on retire le slash
    } else {
      int lastIdx = path.lastIndexOf('/');
      newValueId = path.substring(lastIdx + 1);
    }
    return newValueId;
  }

  private static List buildICentersList(PdcSearchSessionController pdcSC) {
    return pdcSC.getICenters();
  }

  public static void processItemsPagination(String function,
      PdcSearchSessionController pdcSC, HttpServletRequest request) {
    String index = request.getParameter("Index");
    if (StringUtil.isDefined(index)) {
      pdcSC.setIndexOfFirstItemToDisplay(index);
    }
    request.setAttribute("NbItemsPerPage", Integer.valueOf(pdcSC.getNbItemsPerPage()));
    request.setAttribute("FirstItemIndex", Integer.valueOf(pdcSC.getIndexOfFirstItemToDisplay()));

    Value value = pdcSC.getCurrentValue();
    request.setAttribute("SelectedValue", value);
  }

  public static void processSearchDomains(PdcSearchSessionController pdcSC,
      HttpServletRequest request, String currentDomain) {
    // Set search domains
    request.setAttribute("searchDomains", pdcSC.getSearchDomains());
    request.setAttribute("currentSearchDomainId", currentDomain);
  }

  /**
   * Checks the list of result and marks a result as read
   * @param pdcSC PdcSearchSessionController object
   * @param request HttpRequest object
   */
  public static void markResultAsRead(PdcSearchSessionController pdcSC,
      HttpServletRequest request) {
    String sId = request.getParameter("id");
    if (StringUtils.isNotEmpty(sId)) {
      try {
        int resultId = Integer.parseInt(sId);
        List<GlobalSilverResult> results = pdcSC.getGlobalSR();
        for (GlobalSilverResult result : results) {
          if (result.getResultId() == resultId) {
            result.setHasRead(true);
          }
        }
      } catch (Exception e) {
        SilverTrace.error("pdcPeas", "PdcSearchRequestRouterHelper.markResultAsRead",
            "pdcPeas.ERROR_WHEN_MARKING_RESULT", "resultId = " + sId);
      }
    }

  }

  private PdcSearchRequestRouterHelper() {
  }
}
