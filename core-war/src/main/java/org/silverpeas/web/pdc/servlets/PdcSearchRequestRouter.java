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

import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.pdc.pdc.model.SearchCriteria;
import org.silverpeas.kernel.util.StringUtil;
import org.silverpeas.core.util.WAAttributeValuePair;
import org.silverpeas.kernel.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.web.pdc.QueryParameters;
import org.silverpeas.web.pdc.control.Keys;
import org.silverpeas.web.pdc.control.PdcSearchSessionController;
import org.silverpeas.web.pdc.vo.ResultFilterVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class PdcSearchRequestRouter extends ComponentRequestRouter<PdcSearchSessionController> {

  private static final long serialVersionUID = 1L;
  private static final String CLEAR_MODE = "clear";
  private static final String GLOBAL_RESULT_DEST = "/pdcPeas/jsp/globalResult.jsp";
  private static final String RESULT_PAGE_ID = "ResultPageId";
  private static final String RESULT_PAGE = "ResultPage";

  @Override
  public PdcSearchSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new PdcSearchSessionController(mainSessionCtrl, componentContext,
        "org.silverpeas.pdcPeas.multilang.pdcBundle",
        "org.silverpeas.pdcPeas.settings.pdcPeasIcons");
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for notificationUser, returns
   * "notificationUser"
   */
  @Override
  public String getSessionControlBeanName() {
    return "pdcSearch";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param pdcSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex
   * :"/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, PdcSearchSessionController pdcSC,
      HttpRequest request) {

    String destination;
    // controller to inform the request
    try {
      if (function.startsWith("GlobalView")) {
        // the user comes from the link "Advanced Search" of the TopBar.jsp

        pdcSC.setSearchType(PdcSearchSessionController.SEARCH_EXPERT);

        destination = doGlobalView(pdcSC, request);
      } else if (function.startsWith("ChangeSearchType")) {
        boolean setAdvancedSearchItems = processChangeSearchType(function, pdcSC, request);

        destination = doGlobalView(pdcSC, request, false, setAdvancedSearchItems);
      } else if (function.startsWith("LoadAdvancedSearch")) {
        pdcSC.setSearchType(PdcSearchSessionController.SEARCH_EXPERT);

        PdcSearchRequestRouterHelper.saveFavoriteRequestAndSetPdcInfo(pdcSC, request);

        ThesaurusHelper.initializeJargon(pdcSC);
        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if (function.startsWith("ViewAdvancedSearch")) {

        InterestCentersHelper.putSelectedInterestCenterId(request);
        InterestCentersHelper.processICenterSaving(pdcSC, request);

        PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, true);

        ThesaurusHelper.initializeJargon(pdcSC);
        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if ("Pagination".equals(function)) {
        processSelection(pdcSC, request);

        String index = request.getParameter("Index");
        pdcSC.setIndexOfFirstResultToDisplay(Integer.parseInt(index));

        String nbItemsPerPage = request.getParameter("NbItemsPerPage");
        if (StringUtil.isInteger(nbItemsPerPage)) {
          pdcSC.setNbResToDisplay(Integer.parseInt(nbItemsPerPage));
        }

        setDefaultDataToNavigation(false, request, pdcSC);

        destination = getDestinationForResults(pdcSC);
      } else if ("SortResults".equals(function)) {

        String paramNbResToDisplay = request.getParameter("nbRes");
        if (StringUtil.isDefined(paramNbResToDisplay)) {
          int nbResToDisplay = Integer.parseInt(paramNbResToDisplay);
          pdcSC.setNbResToDisplay(nbResToDisplay);
        }
        String paramSortRes = request.getParameter("sortRes");
        if (StringUtil.isDefined(paramSortRes)) {
          int sortRes = Integer.parseInt(paramSortRes);
          pdcSC.setSortValue(sortRes);
        }
        String paramSortOrder = request.getParameter("sortOrder");
        if (StringUtil.isDefined(paramSortOrder)) {
          pdcSC.setSortOrder(paramSortOrder);
        }

        setDefaultDataToNavigation(true, request, pdcSC);

        destination = getDestinationForResults(pdcSC);
      } else if (function.startsWith("AdvancedSearch")) {
        String mode = request.getParameter("mode");
        if (CLEAR_MODE.equals(mode) || pdcSC.getXmlTemplate() != null) {
          pdcSC.clearSearchParameters(true);
        }
        processChangeSearchType(function, pdcSC, request);

        // Display classic result page or only PDC result page
        String showResults = request.getParameter("ShowResults");
        pdcSC.setCurrentResultsDisplay(showResults);

        pdcSC.setResultPage(request.getParameter(RESULT_PAGE));
        pdcSC.setResultPageId(request.getParameter(RESULT_PAGE_ID));
        pdcSC.setXmlFormSortValue(request.getParameter("SortResXForm"));
        pdcSC.setSortImplementor(request.getParameter("sortImp"));

        String searchType = request.getParameter("searchType");
        if (searchType != null && !searchType.isEmpty()) {
          if ("Normal".equals(searchType)) {
            pdcSC.setSearchType(PdcSearchSessionController.SEARCH_SIMPLE);
          } else {
            pdcSC.setSearchType(Integer.parseInt(searchType));
          }
        }

        pdcSC.setSelectedSilverContents(new ArrayList<>());
        // This is the main function of global search
        // recupere les parametres (Only for a global search in advanced mode)
        String icId = request.getParameter("icId");
        if (icId != null) {
          PdcSearchRequestRouterHelper.saveFavoriteRequestAndSetPdcInfo(pdcSC, request, icId);
        } else {
          PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, false);
        }

        // Filters by the axis' values on the PdC the content to seek should be positioned.
        String axisValues = request.getParameter("AxisValueCouples");

        // Optional. Managing direct search on one axis.
        String axisId = request.getParameter("AxisId");
        // looks like /0/2/
        String valueId = request.getParameter("ValueId");
        if (StringUtil.isDefined(axisId) && StringUtil.isDefined(valueId)) {
          SearchCriteria criteria = new SearchCriteria(Integer.parseInt(axisId), valueId);
          pdcSC.getSearchContext().addCriteria(criteria);
        }

        pdcSC.search(axisValues);

        if (StringUtil.isDefined(pdcSC.getResultPage())
            && !"globalResult".equals(pdcSC.getResultPage())) {
          PdcSearchRequestRouterHelper.processItemsPagination(pdcSC, request);
        } else {
          setDefaultDataToNavigation(true, request, pdcSC);
        }
        destination = getDestinationForResults(pdcSC);
      } else if ("LastResults".equals(function)) {

        setDefaultDataToNavigation(false, request, pdcSC);

        destination = GLOBAL_RESULT_DEST;
      } else if ("XMLSearchViewTemplate".equals(function)) {
        String templateFileName = request.getParameter("xmlSearchSelectedForm");

        pdcSC.setXmlTemplate(templateFileName);

        destination = doGlobalView(pdcSC, request);
      } else if ("XMLRestrictSearch".equals(function)) {
        PdcSearchRequestRouterHelper.saveUserChoices(pdcSC, request);

        destination = doGlobalView(pdcSC, request);
      } else if ("XMLDirectSearch".equals(function)) {
        pdcSC.clearSearchParameters(true);
        String templateFileName = request.getParameter("xmlSearchSelectedForm");

        String templateName = templateFileName.substring(0, templateFileName.lastIndexOf('.'));

        // build query
        String fieldParamPrefix = "field_";
        Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
          final String paramName = paramNames.nextElement();
          if (paramName.startsWith(fieldParamPrefix)) {
            String fieldQuery = request.getParameter(paramName);
            String fieldName = paramName.substring(fieldParamPrefix.length());
            pdcSC.getQueryParameters().addXmlSubQuery(templateName + "$$" + fieldName, fieldQuery);
          }
        }

        // launch the search
        pdcSC.search(null);
        pdcSC.setIndexOfFirstResultToDisplay(0);
        setDefaultDataToNavigation(true, request, pdcSC);

        destination = GLOBAL_RESULT_DEST;
      } else if ("XMLSearch".equals(function)) {
        pdcSC.initXMLSearch(request);

        // launch the search
        pdcSC.search(null);
        pdcSC.setIndexOfFirstResultToDisplay(0);
        setDefaultDataToNavigation(true, request, pdcSC);

        destination = GLOBAL_RESULT_DEST;
      } else if (function.startsWith("ToUserPanel")) {
        // utilisation de userPanel et userPanelPeas
        destination = toUserPanel(pdcSC);
      } else if (function.startsWith("FromUserPanel")) {
        // récupération des valeurs de userPanel
        // par userPanelPeas
        Selection sel = pdcSC.getSelection();
        // Get user selected in User Panel
        String[] userIds = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), null);
        if (userIds.length != 0) {
          UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(userIds);
          if (userDetails != null) {
            request.setAttribute("UserDetail", userDetails[0]);
          }
        }
        destination = "/pdcPeas/jsp/refreshFromUserSelect.jsp";
      } else if (function.startsWith("ExportPublications")) {
        processSelection(pdcSC, request);

        // build an exploitable list by importExportPeas
        List<WAAttributeValuePair> selectedResultsWa =
            getItemPks(pdcSC.getSelectedSilverContents());
        request.setAttribute("selectedResultsWa", selectedResultsWa);

        // jump to importExportPeas
        destination = "/RimportExportPeas/jsp/ExportItems";
      } else if (function.startsWith("ExportAttachementsToPDF")) {
        processSelection(pdcSC, request);
        // build an exploitable list by importExportPeas
        List<WAAttributeValuePair> selectedResultsWa =
            getItemPks(pdcSC.getSelectedSilverContents());
        request.setAttribute("selectedResultsWa", selectedResultsWa);
        // jump to importExportPeas
        destination = "/RimportExportPeas/jsp/ExportPDF";
      } else if ("markAsRead".equals(function)) {
        PdcSearchRequestRouterHelper.markResultAsRead(pdcSC, request);
        destination = "/pdcPeas/jsp/blank.html";
      } else if (function.startsWith("CustomLookSearch")) {
        // Specific search which handle FULLTEXT and PDC search
        // Retrieve all request parameters
        String query = request.getParameter("query");
        // TODO implements keywords search instead of full text search
        String mode = request.getParameter("mode");
        if (CLEAR_MODE.equals(mode)) {
          pdcSC.clearSearchParameters(true);

        }
        pdcSC.setResultPage(request.getParameter(RESULT_PAGE));
        pdcSC.setResultPageId(request.getParameter(RESULT_PAGE_ID));
        String searchType = request.getParameter("searchType");
        if (searchType != null && !searchType.isEmpty()) {
          if ("Normal".equals(searchType)) {
            pdcSC.setSearchType(PdcSearchSessionController.SEARCH_SIMPLE);
          } else {
            pdcSC.setSearchType(Integer.parseInt(searchType));
          }
        } else {
          pdcSC.setSearchType(PdcSearchSessionController.SEARCH_EXPERT);
        }

        String listAxis = request.getParameter("listAxis");
        // Reset current search context.
        pdcSC.getSearchContext().clearCriterias();

        // Check PDC search context
        if (StringUtil.isDefined(listAxis)) {
          // Initialize search context
          String[] arrayAxis = listAxis.split(",\\s*");
          for (String curAxis : arrayAxis) {
            pdcSC.getSearchContext().addCriteria(
                new SearchCriteria(Integer.parseInt(curAxis.substring(0, curAxis.indexOf(':'))),
                    curAxis.substring(curAxis.indexOf(':') + 1)));
          }
        }

        // Initialize query parameters
        QueryParameters searchParameters = pdcSC.getQueryParameters();
        searchParameters.setKeywords(query);
        String curSpaceId = request.getParameter("spaces");
        if (!StringUtil.isDefined(curSpaceId)) {
          curSpaceId = null;
        }
        String strComponentIds = request.getParameter("componentSearch");
        List<String> componentIds = null;
        if (StringUtil.isDefined(strComponentIds)) {
          componentIds = Arrays.asList(strComponentIds.split(",\\s*"));
        }
        searchParameters.setSpaceIdAndInstanceId(curSpaceId, strComponentIds);
        pdcSC.buildCustomComponentListWhereToSearch(curSpaceId, componentIds);

        pdcSC.search(listAxis);

        if (StringUtil.isDefined(pdcSC.getResultPage())
            && !pdcSC.getResultPage().equals("globalResult")) {
          PdcSearchRequestRouterHelper.processItemsPagination(pdcSC, request);
        } else {
          setDefaultDataToNavigation(true, request, pdcSC);
        }
        destination = getDestinationForResults(pdcSC);
      } else if (function.startsWith("FilterSearchResult")) {// This function allow group filtering
        // result on globalResult page
        // Retrieve filter parameter
        initSearchFilter(request, pdcSC);
        setDefaultDataToNavigation(false, request, pdcSC);
        destination = getDestinationForResults(pdcSC);
      } else {
        destination = "/pdcPeas/jsp/" + function;
      }
      ThesaurusHelper.setJargonInfoInRequest(pdcSC, request, pdcSC.getActiveThesaurus());
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }

  private String toUserPanel(final PdcSearchSessionController pdcSC) {
    try {
      return pdcSC.initUserPanel();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    return StringUtil.EMPTY;
  }

  /**
   * Indicates if the search has to be done only into PDC.
   * @param request the current request.
   * @return true if the search context concerns only PDC, false otherwise.
   */
  private boolean isOnlyInPdcSearch(final HttpServletRequest request) {
    return StringUtil.getBooleanValue(request.getParameter("FromPDCFrame"));
  }

  /**
   * Initialize search result filter object from request
   *
   * @param request the HTTPServletRequest
   * @param pdcSC the pdcSearchSessionController
   */
  private void initSearchFilter(HttpServletRequest request,
      PdcSearchSessionController pdcSC) {
    String userId = request.getParameter("authorFilter");
    String instanceId = request.getParameter("componentFilter");
    String datatype = request.getParameter("datatypeFilter");
    String filetype = request.getParameter("filetypeFilter");
    String lastUpdate = request.getParameter("lastUpdateFilter");

    ResultFilterVO filter = new ResultFilterVO();

    // Check filter values
    if (StringUtil.isDefined(userId)) {
      filter.setAuthorId(userId);
    }
    if (StringUtil.isDefined(instanceId)) {
      filter.setComponentId(instanceId);
    }
    if (StringUtil.isDefined(datatype)) {
      filter.setDatatype(datatype);
    }
    if (StringUtil.isDefined(filetype)) {
      filter.setFiletype(filetype);
    }
    if (StringUtil.isDefined(lastUpdate)) {
      filter.setLastUpdate(lastUpdate);
    }

    // check form field facets
    for (String facetId : pdcSC.getFieldFacets().keySet()) {
      String param = request.getParameter(facetId + "Filter");
      if (StringUtil.isDefined(param)) {
        filter.addFormFieldSelectedFacetEntry(facetId, param);
      }
    }

    pdcSC.setIndexOfFirstResultToDisplay(0);
    pdcSC.setSelectedFacetEntries(filter);
  }

  private List<WAAttributeValuePair> getItemPks(List<GlobalSilverResult> listGR) {
    List<WAAttributeValuePair> itemPKs = new ArrayList<>();
    for (GlobalSilverResult gb : listGR) {
      final String id = gb.isLinked() ? gb.getLinkedResourceId() : gb.getId();
      itemPKs.add(new WAAttributeValuePair(id, gb.getInstanceId()));
    }
    return itemPKs;
  }

  private String doGlobalView(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    return doGlobalView(pdcSC, request, true, true);
  }

  private String doGlobalView(PdcSearchSessionController pdcSC, HttpServletRequest request,
      boolean saveUserChoice, boolean setAdvancedSearchItems) throws Exception {

    String mode = request.getParameter("mode");
    if (CLEAR_MODE.equals(mode)) {
      pdcSC.clearSearchParameters(false);
    }

    if (saveUserChoice) {
      PdcSearchRequestRouterHelper.saveUserChoices(pdcSC, request);
    }

    if (pdcSC.getSearchType() >= PdcSearchSessionController.SEARCH_ADVANCED) {
      PdcSearchRequestRouterHelper.setUserChoices(request, pdcSC);
      PdcSearchRequestRouterHelper.setAttributesAdvancedSearch(pdcSC, request,
          setAdvancedSearchItems);
    }
    if (pdcSC.getSearchType() == PdcSearchSessionController.SEARCH_EXPERT) {
      HttpSession session = request.getSession(true);
      LookHelper helper = LookHelper.getLookHelper(session);
      if (!isOnlyInPdcSearch(request)) {
        // Context is different of PDC frame, always process PDC axis
        initializePdcAxis(pdcSC, request);
      } else {
        if (helper.isDisplayPDCInHomePage() || (!helper.isDisplayPDCInHomePage() && StringUtil.
            isDefined(pdcSC.getQueryParameters().getSpaceId()))) {
          initializePdcAxis(pdcSC, request);
        }
      }
    }
    if (pdcSC.getSearchType() == PdcSearchSessionController.SEARCH_XML) {
      PublicationTemplateImpl template = pdcSC.getXmlTemplate();
      if (template != null) {
        // A xml search has been done
        request.setAttribute("Template", template);
        DataRecord data = pdcSC.getXmlData();
        if (data == null) {
          data = template.getSearchTemplate().getEmptyRecord();
        }
        request.setAttribute("Data", data);
      }
      // get All Models
      List<PublicationTemplate> templates =
          PublicationTemplateManager.getInstance().getSearchablePublicationTemplates();
      request.setAttribute("XMLForms", templates);
      request.setAttribute("context", pdcSC.getXMLContext());
    }

    // put search type
    request.setAttribute("SearchType", pdcSC.getSearchType());
    return getDestinationDuringSearch(pdcSC, request);
  }

  private void initializePdcAxis(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    PdcSearchRequestRouterHelper.setPertinentAxis(pdcSC, request);
    PdcSearchRequestRouterHelper.setContext(pdcSC, request);

    ThesaurusHelper.initializeJargon(pdcSC);
    ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);
  }

  /**
   * Cette méthode permet de mettre dans la request et dans le sessionController les données utiles
   * à la navigation. permettra de naviguer à l'aide des boutons précédent et suivant
   *
   * @param request - HttpServletRequest pour donner l'information à la globalResult.jsp
   */
  private void setDefaultDataToNavigation(boolean sortResults, HttpServletRequest request,
      PdcSearchSessionController pdcSC) {

    request.setAttribute("Keywords", pdcSC.getQueryParameters().getKeywords());

    request.setAttribute("IndexOfFirstResult", pdcSC.
        getIndexOfFirstResultToDisplay());
    request.setAttribute("RefreshEnabled", pdcSC.isRefreshEnabled());
    request.setAttribute("ExternalSearchEnabled", pdcSC.isEnableExternalSearch());

    request.setAttribute("Results", pdcSC.getSortedResultsToDisplay(sortResults));
    request.setAttribute("UserId", pdcSC.getUserId());

    // Add result group filter data
    request.setAttribute("ResultGroup", pdcSC.getResultGroupFilter());

    request.setAttribute("NbTotalResults", pdcSC.getTotalResults());
    request.setAttribute("PertinenceVisible", pdcSC.isPertinenceVisible());

    request.setAttribute("DisplayParamChoices", pdcSC.getDisplayParamChoices());
    request.setAttribute("NbResToDisplay", pdcSC.getNbResToDisplay());
    request.setAttribute("SortValue", pdcSC.getSortValue());
    request.setAttribute("SortOrder", pdcSC.getSortOrder());

    // spelling words
    request.setAttribute("spellingWords", pdcSC.getSpellingWords());

    request.setAttribute("ResultsDisplay", pdcSC.getCurrentResultsDisplay());
    request.setAttribute(RESULT_PAGE_ID, pdcSC.getResultPageId());
    request.setAttribute("XmlFormSortValue", pdcSC.getXmlFormSortValue());
    request.setAttribute("sortImp", pdcSC.getSortImplementor());

    setTabsInfoIntoRequest(pdcSC, request);
  }

  private void processSelection(PdcSearchSessionController pdcSC, HttpServletRequest request) {
    // get the selected object ids
    String selectedObjectIds = request.getParameter("selectedIds");

    // extract the selected objects from the results
    List<GlobalSilverResult> silverContents = pdcSC.getGlobalSR();
    List<GlobalSilverResult> selectedSilverContents = pdcSC.getSelectedSilverContents();
    if (selectedSilverContents == null) {
      selectedSilverContents = new ArrayList<>();
    }
    for (GlobalSilverResult gsr : silverContents) {
      String id = gsr.isLinked() ? gsr.getLinkedResourceId() : gsr.getId();
      String objectId = id + "-" + gsr.getInstanceId();
      if (selectedObjectIds.contains(objectId) && !selectedSilverContents.contains(gsr)) {
        // the silverContent is in the selected objects list
        selectedSilverContents.add(gsr);
      } else if (!selectedObjectIds.contains(objectId)) {
        selectedSilverContents.remove(gsr);
      }
    }

    // memorize the selected silverContents
    pdcSC.setSelectedSilverContents(selectedSilverContents);

    // Ajout d un traitement spécifique pour le cas de l export: je ne change pas la
    // mécanique existante car je crains la régression.
    String notSelectedObjectIds = request.getParameter("notSelectedIds");
    if (selectedObjectIds != null && !selectedObjectIds.isEmpty()) {
      for (GlobalSilverResult gsr : silverContents) {
        String id = gsr.isLinked() ? gsr.getLinkedResourceId() : gsr.getId();
        String objectId = id + "-" + gsr.getInstanceId();
        if (selectedObjectIds.contains(objectId)) {
          // the silverContent is in the selected objects list
          gsr.setSelected(true);
        }
        if (notSelectedObjectIds.contains(objectId)) {
          // the silverContent is in the selected objects list
          gsr.setSelected(false);
        }
      }
    }
  }

  private boolean processChangeSearchType(String function, PdcSearchSessionController pdcSC,
      HttpServletRequest request) {
    boolean setAdvancedSearchItems = true;
    pdcSC.setSearchPage(request.getParameter("SearchPage"));
    pdcSC.setSearchPageId(request.getParameter("SearchPageId"));
    pdcSC.setResultPage(request.getParameter(RESULT_PAGE));
    pdcSC.setResultPageId(request.getParameter(RESULT_PAGE_ID));

    switch (function) {
      case "ChangeSearchTypeToSimple":
        pdcSC.setSearchType(PdcSearchSessionController.SEARCH_SIMPLE);
        break;
      case "ChangeSearchTypeToAdvanced":
        pdcSC.setSearchType(PdcSearchSessionController.SEARCH_ADVANCED);
        break;
      case "ChangeSearchTypeToXml": {
        // setting predefined values
        String templateName = request.getParameter("Template");
        if (StringUtil.isDefined(templateName)) {
          pdcSC.setXmlTemplate(templateName);
        }

        String spaceId = request.getParameter("SpaceId");
        if (StringUtil.isDefined(spaceId)) {
          pdcSC.getQueryParameters().setSpaceId(spaceId);
        }

        String sortImp = request.getParameter(Keys.RequestSortImplementor.value());
        if (StringUtil.isDefined(templateName)) {
          pdcSC.setSortImplementor(sortImp);
        } else {
          pdcSC.setSortImplementor(null);
        }
        String sortResXForm = request.getParameter(Keys.RequestSortXformField.value());
        if (StringUtil.isDefined(templateName)) {
          pdcSC.setXmlFormSortValue(sortResXForm);
        } else {
          pdcSC.setXmlFormSortValue(null);
        }
        String sortOrder = request.getParameter("sortOrder");
        if (StringUtil.isDefined(sortOrder)) {
          pdcSC.setSortOrder(sortOrder);
        }

        pdcSC.setSearchType(PdcSearchSessionController.SEARCH_XML);
        break;
      }
      default: {
        String spaceId = request.getParameter("spaces");
        String instanceId = request.getParameter("componentSearch");
        pdcSC.getQueryParameters().setSpaceIdAndInstanceId(spaceId, instanceId);

        if (pdcSC.isPlatformUsesPDC()) {
          pdcSC.setSearchType(PdcSearchSessionController.SEARCH_EXPERT);
        } else {
          // PDC is not used, redirect to simple search
          pdcSC.setSearchType(PdcSearchSessionController.SEARCH_ADVANCED);
        }

        if (isOnlyInPdcSearch(request)) {
          // Exclusive case to display pertinent classification axis in PDC frame
          // Advanced search items are useless in this case
          setAdvancedSearchItems = false;
        }
        break;
      }
    }
    return setAdvancedSearchItems;
  }

  private String getDestinationDuringSearch(PdcSearchSessionController pdcSC,
      HttpServletRequest request) {
    pdcSC.setSearchPage(request.getParameter("SearchPage"));
    pdcSC.setSearchPageId(request.getParameter("SearchPageId"));

    setTabsInfoIntoRequest(pdcSC, request);

    if (pdcSC.getSearchType() == PdcSearchSessionController.SEARCH_XML) {
      request.setAttribute("PageId", pdcSC.getSearchPageId());
      return "/pdcPeas/jsp/globalSearchXML.jsp";
    } else {
      if (StringUtil.isDefined(pdcSC.getSearchPage())) {
        return pdcSC.getSearchPage();
      } else {
        // put search type
        request.setAttribute("SearchType", pdcSC.getSearchType());

        // Add component search type
        request.setAttribute("ComponentSearchType", pdcSC.getSearchTypeConfig());
        return "/pdcPeas/jsp/globalSearch.jsp";
      }
    }
  }

  private String getDestinationForResults(PdcSearchSessionController pdcSC) {
    if (StringUtil.isDefined(pdcSC.getResultPage())) {
      return "/pdcPeas/jsp/" + pdcSC.getResultPage();
    }
    return GLOBAL_RESULT_DEST;
  }

  private void setTabsInfoIntoRequest(PdcSearchSessionController pdcSC, HttpServletRequest request) {
    request.setAttribute("XmlSearchVisible", pdcSC.isXmlSearchVisible());
    request.setAttribute("ExpertSearchVisible", pdcSC.isPlatformUsesPDC());
  }
}