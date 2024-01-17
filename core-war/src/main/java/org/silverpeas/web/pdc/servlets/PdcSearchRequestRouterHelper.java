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
package org.silverpeas.web.pdc.servlets;

import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.pdc.interests.model.Interests;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.web.pdc.QueryParameters;
import org.silverpeas.web.pdc.control.Keys;
import org.silverpeas.web.pdc.control.PdcSearchSessionController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class PdcSearchRequestRouterHelper {

  private PdcSearchRequestRouterHelper() {
  }

  /**
   * Retrieve query data from current request and prepare result view.
   *
   * @param pdcSC
   * @param request
   * @param setPdcInfo
   * @return a QueryParameters
   * @throws Exception
   */
  public static QueryParameters saveUserChoicesAndSetPdcInfo(
      PdcSearchSessionController pdcSC, HttpServletRequest request,
      boolean setPdcInfo) {
    QueryParameters queryParameters = saveUserChoices(pdcSC, request);
    setUserChoices(request, pdcSC);
    setAttributesAdvancedSearch(pdcSC, request, setPdcInfo);
    if (setPdcInfo) {
      setPertinentAxis(pdcSC, request);
      setContext(pdcSC, request);
    }
    return queryParameters;
  }

  public static QueryParameters saveFavoriteRequestAndSetPdcInfo(PdcSearchSessionController pdcSC,
      HttpServletRequest request) {
    String favoriteRequestId = request.getParameter("iCenterId");
    return saveFavoriteRequestAndSetPdcInfo(pdcSC, request, favoriteRequestId);
  }

  public static QueryParameters saveFavoriteRequestAndSetPdcInfo(PdcSearchSessionController pdcSC,
      HttpServletRequest request, String favoriteRequestId)  {
    // this parameter is for Back Button on result page
    String urlToRedirect = request.getParameter("urlToRedirect");
    request.setAttribute("urlToRedirect", urlToRedirect);
    // load settings of selected Interest center
    Interests ic = pdcSC.loadICenter(favoriteRequestId);
    QueryParameters queryParameters = saveFavoriteRequest(pdcSC, ic);
    setUserChoices(request, pdcSC);
    setAttributesAdvancedSearch(pdcSC, request, true);
    setPertinentAxis(pdcSC, request);
    setContext(pdcSC, request);

    return queryParameters;
  }

  public static QueryParameters saveFavoriteRequest(PdcSearchSessionController pdcSC,
      Interests favoriteRequest)  {
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

    QueryParameters queryParameters = pdcSC.getQueryParameters();
    queryParameters.setKeywords(query);
    queryParameters.setSpaceIdAndInstanceId(spaceId, componentId);
    queryParameters.setCreatorId(authorSearch);
    queryParameters.setAfterDate(DateUtil.toLocalDate(afterdate));
    queryParameters.setBeforeDate(DateUtil.toLocalDate(beforedate));

    return queryParameters;
  }

  /**
   * Build information for the home jsp for the advancedsearch plain text. We get user choices about
   * advanced search and store it in the PdcSearchSessionController
   *
   * @param pdcSC: the pdcSessionController
   * @param request : the HttpServletRequest
   * @return a QueryParameters from session updated with data from request
   * @throws Exception
   */
  public static QueryParameters saveUserChoices(PdcSearchSessionController pdcSC,
      HttpServletRequest request) {
    final String query = request.getParameter(
        pdcSC.getSearchType() == PdcSearchSessionController.SEARCH_XML ?
            "TitleNotInXMLForm" :
            "query");

    final QueryParameters queryParameters = pdcSC.getQueryParameters();
    queryParameters.setKeywords(query);

    if (pdcSC.getSearchType() >= PdcSearchSessionController.SEARCH_ADVANCED) {
      String lang = pdcSC.getLanguage();
      queryParameters.setSpaceIdAndInstanceId(request.getParameter("spaces"),
          request.getParameter("componentSearch"));
      queryParameters.setCreatorId(request.getParameter("authorSearch"));
      queryParameters.setAfterDate(getDateFromRequest("createafterdate", lang, request));
      queryParameters.setBeforeDate(getDateFromRequest("createbeforedate", lang, request));
      queryParameters.setAfterUpdateDate(getDateFromRequest("updateafterdate", lang, request));
      queryParameters.setBeforeUpdateDate(getDateFromRequest("updatebeforedate", lang, request));
      queryParameters.setFolder(request.getParameter(QueryParameters.PARAM_FOLDER));
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
    } else {
      pdcSC.setXmlFormSortValue(null);
    }
    String sortImplementor = request.getParameter(Keys.RequestSortImplementor.value());
    if (StringUtil.isDefined(sortImplementor)) {
      pdcSC.setSortImplemtor(sortImplementor);
    } else {
      pdcSC.setSortImplemtor(null);
    }

    // Set component search type
    pdcSC.setDataType(request.getParameter("dataType"));
    return queryParameters;
  }

  private static LocalDate getDateFromRequest(String name, String language, HttpServletRequest request) {
    String str = request.getParameter(name);
    if (!StringUtil.isDefined(str)) {
      return null;
    }
    return DateUtil.stringToLocalDate(str, language);
  }

  /**
   * Get user choices from the PdcSearchSessionController and put it in the HTTP request. Prepare
   * data that will be used in the result view.
   *
   * @param request
   * @param pdcSC
   * @throws Exception
   */
  public static void setUserChoices(HttpServletRequest request, PdcSearchSessionController pdcSC) {
    QueryParameters queryParameters = pdcSC.getQueryParameters();
    if (queryParameters != null) {
      String authorSearch = queryParameters.getCreatorId();
      // travail sur l'auteur
      if (authorSearch != null) {
        UserDetail userDetail = pdcSC.getOrganisationController().getUserDetail(authorSearch);
        queryParameters.setCreatorDetail(userDetail);
      }
      request.setAttribute("QueryParameters", queryParameters);
    }
    request.setAttribute("DisplayParamChoices", pdcSC.getDisplayParamChoices());
    request.setAttribute("NbResToDisplay", Integer.valueOf(pdcSC.getNbResToDisplay()));
    request.setAttribute("SortValue", Integer.valueOf(pdcSC.getSortValue()));
    request.setAttribute("SortOrder", pdcSC.getSortOrder());
    request.setAttribute("ItemType", pdcSC.getDataType());

    // List of user favorite requests
    List<Interests> favoriteRequests = buildICentersList(pdcSC);
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
    // put search type
    request.setAttribute("SearchType", Integer.valueOf(pdcSC.getSearchType()));
  }

  /**
   * Set attributes into the request in order to prepare data to be displayed. <br> Attributes are
   * build by information which are inside the sessionController
   *
   * @param pdcSC the pdcSessionController
   * @param request HTTP servlet request
   * @param setSpacesAndComponents if false do nothing, else if add SpaceList and ComponentList
   * attributes into the request
   * @throws Exception
   */
  public static void setAttributesAdvancedSearch(
      PdcSearchSessionController pdcSC, HttpServletRequest request,
      boolean setSpacesAndComponents) {
    String selectedSpace = null;
    String selectedComponent = null;

    QueryParameters queryParameters = pdcSC.getQueryParameters();
    if (queryParameters != null) {
      selectedSpace = queryParameters.getSpaceId();
      selectedComponent = queryParameters.getInstanceId();
    }

    request.setAttribute("ExportEnabled", false);

    if (setSpacesAndComponents) {
      request.setAttribute("SpaceList", pdcSC.getAllowedSpaces());

      if (selectedSpace != null) {
        request.setAttribute("ComponentList", pdcSC.getAllowedComponents(selectedSpace));
      }
    }

    pdcSC.buildComponentListWhereToSearch(selectedSpace, selectedComponent);
  }

  /**
   * put in the request the primary axis and eventually the secondary axis accroding to search
   * context
   *
   * @param pdcSC
   * @param request
   */
  public static void setPertinentAxis(PdcSearchSessionController pdcSC,
      HttpServletRequest request) {
    String showSecondarySearchAxis = request.getParameter("ShowSndSearchAxis");

    // does the user want to see secondary axis ?
    if (showSecondarySearchAxis != null) {
      pdcSC.setSecondaryAxis(showSecondarySearchAxis);
    }

    // We set axis into the request
    request.setAttribute("ShowSndSearchAxis", pdcSC.getSecondaryAxis());
  }

  /**
   * put in the request the primary axis and eventually the secondary axis accroding to search
   * context
   *
   * @param pdcSC
   * @param request
   */
  public static void setContext(PdcSearchSessionController pdcSC,
      HttpServletRequest request) {

    // on retire du searchcontext tous les criteres qui ne sont pas dans
    // l'espace choisi par l'utilisateur.
    // Dans ce cas, on retire de la list de searchContext, le critere de
    // recherche.
    SearchContext searchContext = pdcSC.getSearchContext();

    // on ajoute le contexte de recherche
    request.setAttribute("SearchContext", searchContext);
  }

  private static List<Interests> buildICentersList(PdcSearchSessionController pdcSC) {
    return pdcSC.getICenters();
  }

  public static void processItemsPagination(PdcSearchSessionController pdcSC,
      HttpServletRequest request) {
    String index = request.getParameter("Index");
    if (StringUtil.isDefined(index)) {
      pdcSC.setIndexOfFirstItemToDisplay(index);
    }
    request.setAttribute("NbItemsPerPage", pdcSC.getNbItemsPerPage());
    request.setAttribute("FirstItemIndex", pdcSC.getIndexOfFirstItemToDisplay());

    Value value = pdcSC.getCurrentValue();
    request.setAttribute("SelectedValue", value);
  }

  /**
   * Checks the list of result and marks a result as read
   *
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
        SilverLogger.getLogger(PdcSearchRequestRouterHelper.class)
            .error("Error when marking result {0} as read", new String[] {sId}, e);
      }
    }
  }
}