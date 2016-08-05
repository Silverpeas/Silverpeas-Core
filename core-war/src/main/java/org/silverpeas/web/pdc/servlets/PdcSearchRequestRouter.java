/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.pdc.servlets;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.RecordTemplate;
import org.silverpeas.core.contribution.content.form.form.XmlSearchForm;
import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.web.look.LookHelper;
import org.silverpeas.core.webapi.pdc.AxisValueCriterion;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentPeas;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.contribution.contentcontainer.content.IGlobalSilverContentProcessor;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.index.search.model.ParseException;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.SearchCriteria;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.web.pdc.control.Keys;
import org.silverpeas.web.pdc.control.PdcSearchSessionController;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.pdc.pdc.model.QueryParameters;
import org.silverpeas.web.pdc.vo.ResultFilterVO;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.web.selection.SelectionUsersGroups;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.apache.commons.fileupload.FileItem;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.ScoreComparator;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.WAAttributeValuePair;
import org.silverpeas.core.exception.UtilException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

import static org.silverpeas.core.contribution.contentcontainer.content.IGlobalSilverContentProcessor
    .PROCESSOR_NAME_SUFFIX;

public class PdcSearchRequestRouter extends ComponentRequestRouter<PdcSearchSessionController> {

  private static final long serialVersionUID = 1L;
  private ContentManager contentManager = null;

  public PdcSearchRequestRouter() throws Exception {
    contentManager = new ContentManager();
  }

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

    String destination = "";
    // controller to inform the request
    try {
      if (function.startsWith("GlobalViewArbo")) {
        // USED ONLY IN GLOBAL MODE -- The user wants to collapse or uncollapse a value

        InterestCentersHelper.putSelectedInterestCenterId(request);

        PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, true);
        viewArbo(pdcSC, request);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if (function.startsWith("GlobalAddCriteria")) {
        // USED ONLY IN GLOBAL MODE -- the user add a criteria into the SearchContext.

        addCriteria(pdcSC, request);

        InterestCentersHelper.putSelectedInterestCenterId(request);

        PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, true);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if (function.startsWith("GlobalDeleteCriteria")) {
        // USED ONLY IN GLOBAL MODE -- the user deletes a criteria from the SearchContext.

        InterestCentersHelper.putSelectedInterestCenterId(request);

        deleteCriteria(pdcSC, request);

        PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, true);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if (function.startsWith("GlobalModifyCriteria")) {
        // USED ONLY IN GLOBAL MODE -- the user modifies a criteria from the SearchContext.

        InterestCentersHelper.putSelectedInterestCenterId(request);

        modifyCriteria(pdcSC, request);

        PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, true);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if (function.startsWith("GlobalView")) {
        // the user comes from the link "Advanced Search" of the TopBar.jsp

        pdcSC.setSearchType(PdcSearchSessionController.SEARCH_EXPERT);

        destination = doGlobalView(pdcSC, request);
      } else if (function.startsWith("ChangeSearchType")) {
        boolean setAdvancedSearchItems = processChangeSearchType(function, pdcSC, request);

        if (StringUtil.getBooleanValue(request.getParameter("ResetPDCContext"))) {
          // remove PDC search context
          pdcSC.removeAllCriterias();
        }

        destination = doGlobalView(pdcSC, request, false, setAdvancedSearchItems);
      } else if (function.equals("ResetPDCContext")) {
        // remove PDC search context
        pdcSC.removeAllCriterias();

        boolean setAdvancedSearchItems = true;
        if (StringUtil.getBooleanValue(request.getParameter("FromPDCFrame"))) {
          // Exclusive case to display pertinent classification axis in PDC frame
          // Advanced search items are useless in this case
          setAdvancedSearchItems = false;
        }
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
      } else if (function.equals("Pagination")) {
        processSelection(pdcSC, request);

        String index = request.getParameter("Index");
        pdcSC.setIndexOfFirstResultToDisplay(index);
        setDefaultDataToNavigation(request, pdcSC);

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

        setDefaultDataToNavigation(request, pdcSC);

        destination = getDestinationForResults(pdcSC);
      } else if (function.startsWith("AdvancedSearch")) {
        String mode = request.getParameter("mode");
        if ("clear".equals(mode)) {
          clearUserChoices(pdcSC);
          pdcSC.resetResultPage();
          pdcSC.resetResultPageId();
          pdcSC.resetSearchPage();
          pdcSC.resetSearchPageId();
        }
        processChangeSearchType(function, pdcSC, request);

        // Display classic result page or only PDC result page
        String showResults = request.getParameter("ShowResults");
        pdcSC.setCurrentResultsDisplay(showResults);

        pdcSC.setResultPage(request.getParameter("ResultPage"));
        pdcSC.setResultPageId(request.getParameter("ResultPageId"));
        pdcSC.setXmlFormSortValue(request.getParameter("SortResXForm"));
        pdcSC.setSortImplemtor(request.getParameter("sortImp"));

        String searchType = request.getParameter("searchType");
        if (searchType != null && !"".equals(searchType)) {
          if ("Normal".equals(searchType)) {
            pdcSC.setSearchType(PdcSearchSessionController.SEARCH_SIMPLE);
          } else {
            pdcSC.setSearchType(Integer.parseInt(searchType));
          }
        }

        pdcSC.setSelectedSilverContents(new ArrayList<>());
        // This is the main function of global search
        boolean pdcUsedDuringSearch = false;
        // recupere les parametres (Only for a global search in advanced mode)
        String icId = request.getParameter("icId");
        QueryParameters searchParameters;
        if (icId != null) {
          searchParameters =
              PdcSearchRequestRouterHelper.
              saveFavoriteRequestAndSetPdcInfo(pdcSC, request, icId);
        } else {
          searchParameters =
              PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, false);
        }

        // Filters by the axis' values on the PdC the content to seek should be positioned.
        String axisValues = request.getParameter("AxisValueCouples");
        List<AxisValueCriterion> axisValueCriteria = AxisValueCriterion.fromFlattenedAxisValues(
            axisValues);
        for (AxisValueCriterion anAxisValueCriterion : axisValueCriteria) {
          pdcSC.getSearchContext().addCriteria(anAxisValueCriterion);
        }

        // Optional. Managing direct search on one axis.
        String axisId = request.getParameter("AxisId");
        String valueId = request.getParameter("ValueId"); // looks like /0/2/
        if (StringUtil.isDefined(axisId) && StringUtil.isDefined(valueId)) {
          SearchCriteria criteria = new SearchCriteria(Integer.parseInt(axisId), valueId);
          pdcSC.getSearchContext().addCriteria(criteria);
        }

        if (pdcSC.getSearchContext() != null && !pdcSC.getSearchContext().isEmpty()) {
          pdcUsedDuringSearch = true;
        }

        List<Integer> alSilverContentIds = null;
        if (pdcUsedDuringSearch) {
          // the search context is not empty. We have to search all silvercontentIds according to
          // query settings
          alSilverContentIds = searchAllSilverContentId(pdcSC, searchParameters);
        }
        // the query string contains something
        if (searchParameters.isDefined()
            || (StringUtil.isDefined(searchParameters.getSpaceId()) && !pdcUsedDuringSearch)
            || pdcSC.isDataTypeDefined()) {
          // We have to search objects from classical search and merge it eventually with result
          // from PDC
          MatchingIndexEntry[] ie;
          try {
            ie = pdcSC.search(); // launch the classical research
          } catch (ParseException pex) {
            ie = new MatchingIndexEntry[0];
            request.setAttribute("parseException", "pdcPeas.badRequest");
          }

          if (pdcUsedDuringSearch) {
            pdcSC.setSearchScope(PdcSearchSessionController.SEARCH_MIXED);

            // We retain only objects which are presents in the both search result list
            MatchingIndexEntry[] result = mixedSearch(ie, alSilverContentIds);

            // filtre les résultats affichables
            pdcSC.processResultsToDisplay(result);
          } else {
            pdcSC.setSearchScope(PdcSearchSessionController.SEARCH_FULLTEXT);

            // filtre les résultats affichables
            pdcSC.processResultsToDisplay(ie);
          }

        } else {
          pdcSC.setSearchScope(PdcSearchSessionController.SEARCH_PDC);

          // get the list of silvercontents according to the list of silvercontent ids
          List<GlobalSilverContent> alSilverContents = pdcSearchOnly(alSilverContentIds, pdcSC);

          pdcSC.setResults(alSilverContents);
          pdcSC.processResultsToDisplay(alSilverContents);
        }
        if (StringUtil.isDefined(pdcSC.getResultPage())
            && !pdcSC.getResultPage().equals("globalResult")
            && !pdcSC.getResultPage().equals("pdaResult.jsp")) {
          PdcSearchRequestRouterHelper.processItemsPagination(function, pdcSC, request);
        } else {
          setDefaultDataToNavigation(request, pdcSC);
        }
        destination = getDestinationForResults(pdcSC);
      } else if (function.equals("LastResults")) {

        setDefaultDataToNavigation(request, pdcSC);

        destination = "/pdcPeas/jsp/globalResult.jsp";
      } else if (function.equals("XMLSearchViewTemplate")) {
        String templateFileName = request.getParameter("xmlSearchSelectedForm");

        pdcSC.setXmlTemplate(templateFileName);

        destination = doGlobalView(pdcSC, request);
      } else if (function.equals("XMLRestrictSearch")) {
        PdcSearchRequestRouterHelper.saveUserChoices(pdcSC, request);

        destination = doGlobalView(pdcSC, request);
      } else if (function.equals("XMLSearch")) {
        pdcSC.getQueryParameters().clearXmlQuery();

        List<FileItem> items = getRequestItems(request);

        String title = getParameterValue(items, "TitleNotInXMLForm");
        pdcSC.getQueryParameters().setXmlTitle(title);

        PublicationTemplateImpl template = pdcSC.getXmlTemplate();

        // build a dataRecord object storing user's entries
        RecordTemplate searchTemplate = template.getSearchTemplate();
        DataRecord data = searchTemplate.getEmptyRecord();

        PagesContext context =
            new PagesContext("XMLSearchForm", "2", pdcSC.getLanguage(), pdcSC.getUserId());

        XmlSearchForm searchForm = (XmlSearchForm) template.getSearchForm();
        searchForm.update(items, data, context);

        // xmlQuery is in the data object, store it into session
        pdcSC.setXmlData(data);

        // build the xmlSubQuery according to the dataRecord object
        String templateFileName = template.getFileName();
        String templateName = templateFileName.substring(0, templateFileName.lastIndexOf("."));
        String[] fieldNames = searchTemplate.getFieldNames();
        for (int f = 0; f < fieldNames.length; f++) {
          String fieldName = fieldNames[f];
          Field field = data.getField(fieldName);
          String fieldValue = field.getStringValue();
          if (fieldValue != null && fieldValue.trim().length() > 0) {
            String fieldQuery = fieldValue.trim().replaceAll("##", " AND ");
            pdcSC.getQueryParameters().addXmlSubQuery(templateName + "$$" + fieldName, fieldQuery);
          }
        }

        // launch the search
        MatchingIndexEntry[] ie = pdcSC.search();

        pdcSC.setSearchScope(PdcSearchSessionController.SEARCH_XML);

        pdcSC.processResultsToDisplay(ie);

        setDefaultDataToNavigation(request, pdcSC);

        destination = "/pdcPeas/jsp/globalResult.jsp";
      } else if (function.startsWith("ActivateThesaurus")
          || function.startsWith("DesactivateThesaurus")
          || function.startsWith("GlobalActivateThesaurus")
          || function.startsWith("GlobalDesactivateThesaurus")) {

        destination = processThesaurusActions(function, pdcSC, request);
      } else if (function.startsWith("ToUserPanel")) {// utilisation de userPanel et userPanelPeas
        try {
          destination = pdcSC.initUserPanel();
        } catch (Exception e) {
          SilverTrace.warn("pdcPeas", "PdcPeasRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
      } else if (function.startsWith("FromUserPanel")) {// récupération des valeurs de userPanel
        // par userPanelPeas
        Selection sel = pdcSC.getSelection();
        // Get user selected in User Panel
        String[] userIds = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), null);
        if (userIds.length != 0) {
          UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(userIds);
          if (userDetails != null) {
            request.setAttribute("UserDetail", pdcSC.getUserDetail(userDetails[0].getId()));
          }
        }
        destination = "/pdcPeas/jsp/refreshFromUserSelect.jsp";
        // destination = doGlobalView(pdcSC, request);
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
        if ("clear".equals(mode)) {
          clearUserChoices(pdcSC);
          pdcSC.resetResultPage();
          pdcSC.resetResultPageId();
          pdcSC.resetSearchPage();
          pdcSC.resetSearchPageId();
        }
        pdcSC.setResultPage(request.getParameter("ResultPage"));
        pdcSC.setResultPageId(request.getParameter("ResultPageId"));
        String searchType = request.getParameter("searchType");
        if (searchType != null && !"".equals(searchType)) {
          if ("Normal".equals(searchType)) {
            pdcSC.setSearchType(PdcSearchSessionController.SEARCH_SIMPLE);
          } else {
            pdcSC.setSearchType(Integer.parseInt(searchType));
          }
        } else {
          pdcSC.setSearchType(PdcSearchSessionController.SEARCH_EXPERT);
        }
        pdcSC.setSelectedSilverContents(new ArrayList<>());
        // Use pdc search only if user has selected an axis value
        boolean pdcUsedDuringSearch = false;
        String listAxis = request.getParameter("listAxis");
        // Reset current search context.
        pdcSC.getSearchContext().clearCriterias();

        // Check PDC search context
        if (StringUtil.isDefined(listAxis)) {
          pdcUsedDuringSearch = true;
          // Initialize search context
          String[] arrayAxis = listAxis.split(",\\s*");
          for (String curAxis : arrayAxis) {
            pdcSC.getSearchContext().addCriteria(new SearchCriteria(Integer.parseInt(curAxis
                .substring("Axis".length(), curAxis.indexOf('='))), curAxis.substring(curAxis
                .indexOf('=') + 1)));
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

        if (pdcSC.getSearchContext() != null && !pdcSC.getSearchContext().isEmpty()) {
          pdcUsedDuringSearch = true;
        }
        List<Integer> alSilverContentIds = null;
        if (pdcUsedDuringSearch) {
          // the search context is not empty. We have to search all silvercontentIds according to
          // query settings
          alSilverContentIds = searchAllSilverContentId(pdcSC, searchParameters);
        }
        // the query string contains something
        if (searchParameters.isDefined()
            || (StringUtil.isDefined(searchParameters.getSpaceId()) && !pdcUsedDuringSearch)) {
          // We have to search objects from classical search and merge it eventually with result
          // from PDC
          MatchingIndexEntry[] ie = pdcSC.search(); // launch the classical research

          if (pdcUsedDuringSearch) {
            pdcSC.setSearchScope(PdcSearchSessionController.SEARCH_MIXED);
            // We retain only objects which are presents in the both search result list
            MatchingIndexEntry[] result = mixedSearch(ie, alSilverContentIds);
            // filtre les résultats affichables
            pdcSC.processResultsToDisplay(result);
          } else {
            pdcSC.setSearchScope(PdcSearchSessionController.SEARCH_FULLTEXT);
            // filtre les résultats affichables
            pdcSC.processResultsToDisplay(ie);
          }

        } else {
          pdcSC.setSearchScope(PdcSearchSessionController.SEARCH_PDC);
          // get the list of silvercontents according to the list of silvercontent ids
          List<GlobalSilverContent> alSilverContents = pdcSearchOnly(alSilverContentIds, pdcSC);
          pdcSC.setResults(alSilverContents);
          pdcSC.processResultsToDisplay(alSilverContents);
        }
        if (StringUtil.isDefined(pdcSC.getResultPage())
            && !pdcSC.getResultPage().equals("globalResult")
            && !pdcSC.getResultPage().equals("pdaResult.jsp")) {
          PdcSearchRequestRouterHelper.processItemsPagination(function, pdcSC, request);
        } else {
          setDefaultDataToNavigation(request, pdcSC);
        }
        destination = getDestinationForResults(pdcSC);
      } else if (function.startsWith("FilterSearchResult")) {// This function allow group filtering
        // result on globalResult page
        // Retrieve filter parameter
        initSearchFilter(request, pdcSC);
        setDefaultDataToNavigation(request, pdcSC);
        destination = getDestinationForResults(pdcSC);
      } else {
        destination = "/pdcPeas/jsp/" + function;
      }
      ThesaurusHelper.setJargonInfoInRequest(pdcSC, request, pdcSC.getActiveThesaurus());
    } catch (Exception e) {
      SilverTrace.error("pdcPeas", "PdcSearchRequestRouter.getDestination",
          "pdcPeas.EX_GET_DESTINATION_ERROR", "", e);
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
    return destination;
  }

  /**
   * Initialize search result filter object from request
   *
   * @param request the HTTPServletRequest
   * @param pdcSC the pdcSearchSessionController
   * @return a new ResultFilterVO which contains data information
   */
  private ResultFilterVO initSearchFilter(HttpServletRequest request,
      PdcSearchSessionController pdcSC) {
    String userId = request.getParameter("authorFilter");
    String instanceId = request.getParameter("componentFilter");
    String datatype = request.getParameter("datatypeFilter");
    String filetype = request.getParameter("filetypeFilter");

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

    // check form field facets
    for (String facetId : pdcSC.getFieldFacets().keySet()) {
      String param = request.getParameter(facetId + "Filter");
      if (StringUtil.isDefined(param)) {
        filter.addFormFieldSelectedFacetEntry(facetId, param);
      }
    }

    pdcSC.setIndexOfFirstResultToDisplay("0");
    pdcSC.setSelectedFacetEntries(filter);

    return filter;
  }

  private List<FileItem> getRequestItems(HttpServletRequest request) throws UtilException {
    return HttpRequest.decorate(request).getFileItems();
  }

  private String getParameterValue(List<FileItem> items, String parameterName) {
    Iterator<FileItem> iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = iter.next();
      if (item.isFormField() && parameterName.equals(item.getFieldName())) {
        return item.getString();
      }
    }
    return null;
  }

  private List<WAAttributeValuePair> getItemPks(List<GlobalSilverResult> listGR) {
    List<WAAttributeValuePair> itemPKs = new ArrayList<>();
    Iterator<GlobalSilverResult> itListGR = listGR.iterator();
    while (itListGR.hasNext()) {
      GlobalSilverResult gb = itListGR.next();
      itemPKs.add(new WAAttributeValuePair(gb.getId(), gb.getInstanceId()));
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
    if ("clear".equals(mode)) {
      clearUserChoices(pdcSC);
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
      if (!StringUtil.getBooleanValue(request.getParameter("FromPDCFrame"))) {
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
      PagesContext context = new PagesContext("XMLSearchForm", "2", pdcSC.getLanguage(), false,
          "useless", pdcSC.getUserId());
      context.setBorderPrinted(false);
      request.setAttribute("context", context);
    }

    // put search type
    request.setAttribute("SearchType", Integer.valueOf(pdcSC.getSearchType()));
    return getDestinationDuringSearch(pdcSC, request);
  }

  private void initializePdcAxis(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    PdcSearchRequestRouterHelper.setPertinentAxis(pdcSC, request);
    PdcSearchRequestRouterHelper.setContext(pdcSC, request);

    ThesaurusHelper.initializeJargon(pdcSC);
    ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);
  }

  private void viewArbo(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    String axisId = request.getParameter("AxisId");
    String valueId = PdcSearchRequestRouterHelper.getLastValueOf(request.getParameter("ValueId"));

    setDaughtersToRequest(pdcSC, request, axisId, valueId);
  }

  private void addCriteria(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    String axisId = request.getParameter("AxisId");
    String valueId = request.getParameter("ValueId");

    // construction de l'objet SearchCriteria
    SearchCriteria searchCriteria = new SearchCriteria(Integer.parseInt(axisId), valueId);
    pdcSC.addCriteriaToSearchContext(searchCriteria); // travail sur le contexte courant
  }

  /*
   * Remove axis from the user search context
   */
  private void deleteCriteria(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    String axesId = request.getParameter("Ids"); // get ids of selected criteria

    String oneAxisId = "";
    // get all ids and remove corresponding criteria into the SearchCriteria
    StringTokenizer st = new StringTokenizer(axesId, ",");
    while (st.hasMoreTokens()) {
      oneAxisId = st.nextToken();
      pdcSC.removeCriteriaFromSearchContext(oneAxisId);
    }
  }

  private void modifyCriteria(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    String axisId = request.getParameter("AxisId");
    String path = request.getParameter("ValueId");
    String valueId = PdcSearchRequestRouterHelper.getLastValueOf(path);

    pdcSC.removeCriteriaFromSearchContext(new SearchCriteria(Integer.parseInt(axisId), path));

    // il faut tester si la valueId est le rootId ou non
    // en effet, s'il s'agit d'un rootId, alors il ne faut pas
    // afficher l'arborescence mais uniquement l'axe
    // le rootId est de la forme /0 et la valueId est de la forme /0/1/
    if (path.endsWith("/")) {
      // il s'agit d'un valeur
      setDaughtersToRequest(pdcSC, request, axisId, valueId);
    }
  }

  private void buildSearchContext(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    // on prepare le chemin complet pour l'affichage dans le cadre du contexte
    SearchContext searchContext = pdcSC.getSearchContext();
    List<SearchCriteria> c = searchContext.getCriterias();
    List<List> pathCriteria = new ArrayList<>(c.size());
    if (c.size() > 0) {
      for (int i = 0; i < c.size(); i++) {
        SearchCriteria sc = c.get(i);

        int searchAxisId = sc.getAxisId();
        String searchValue = PdcSearchRequestRouterHelper.getLastValueOf(sc.getValue());
        // on créait un axis
        AxisHeader axis = pdcSC.getAxisHeader(String.valueOf(searchAxisId));

        String treeId = null;
        if (axis != null) {
          treeId = String.valueOf(axis.getRootId());
        }

        List<Value> fullPath = new ArrayList<>();
        if (searchValue != null && treeId != null) {
          fullPath = pdcSC.getFullPath(searchValue, treeId);
        }

        pathCriteria.add(fullPath);
      }
    }
    request.setAttribute("PathCriteria", pathCriteria);
    // on ajoute le contexte de recherche
    request.setAttribute("SearchContext", searchContext);
  }

  /*
   * Put, in the request, the pertinent axis, the search context and the full path of each criteria
   */
  private void buildContextAndPertinentAxis(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception {
    // Put the pertinent axis in the request
    PdcSearchRequestRouterHelper.setPertinentAxis(pdcSC, request);

    // Put the search context and the full path of each criteria in the request
    buildSearchContext(pdcSC, request);
  }

  /**
   * Builds a list of daughters value to see an arborescence and set it into the request.
   *
   * @param pdcSC - the PdcSearchSessionController object
   * @param request - the HttpServletRequest object
   * @param axisId - the id of the axis
   * @param valueId - the id of the value
   */
  private void setDaughtersToRequest(PdcSearchSessionController pdcSC, HttpServletRequest request,
      String axisId, String valueId) throws Exception {
    // on cherche a savoir si l'on doit montrer des valeurs filles
    List<Value> daughters = null;
    if (StringUtil.isDefined(axisId)) {
      // 1er depilage de l'axe ?
      if (!StringUtil.isDefined(valueId)) {
        daughters = pdcSC.getFirstLevelAxisValues(axisId);
      } else {
        daughters = pdcSC.getDaughterValues(axisId, valueId);
      }
    }
    // on passe la liste contenant les filles dans la requete
    if (daughters != null) {
      request.setAttribute("Daughters", daughters);
      request.setAttribute("SelectedAxis", axisId);
      request.setAttribute("SelectedValue", valueId);
    }
  }

  /**
   * Cette méthode permet de mettre dans la request et dans le sessionController les données utiles
   * à la navigation. permettra de naviguer à l'aide des boutons précédent et suivant
   *
   * @param request - HttpServletRequest pour donner l'information à la globalResult.jsp
   */
  private void setDefaultDataToNavigation(HttpServletRequest request,
      PdcSearchSessionController pdcSC) throws Exception {

    ResultFilterVO filter = pdcSC.getSelectedFacetEntries();

    request.setAttribute("Keywords", pdcSC.getQueryParameters().getKeywords());

    request.setAttribute("IndexOfFirstResult", Integer.valueOf(pdcSC.
        getIndexOfFirstResultToDisplay()));
    request.setAttribute("RefreshEnabled", Boolean.valueOf(pdcSC.isRefreshEnabled()));
    request.setAttribute("ExternalSearchEnabled", Boolean.valueOf(pdcSC.isEnableExternalSearch()));

    request.setAttribute("Results", pdcSC.getSortedResultsToDisplay(pdcSC.getSortValue(), pdcSC.
        getSortOrder(), pdcSC.getXmlFormSortValue(), pdcSC.getSortImplemtor(), filter));
    request.setAttribute("UserId", pdcSC.getUserId());

    // Add result group filter data
    request.setAttribute("ResultGroup", pdcSC.getResultGroupFilter());

    request.setAttribute("NbTotalResults", Integer.valueOf(pdcSC.getTotalResults()));
    request.setAttribute("PertinenceVisible", Boolean.valueOf(pdcSC.isPertinenceVisible()));

    request.setAttribute("DisplayParamChoices", pdcSC.getDisplayParamChoices());
    request.setAttribute("ChoiceNbResToDisplay", pdcSC.getListChoiceNbResToDisplay());
    request.setAttribute("NbResToDisplay", Integer.valueOf(pdcSC.getNbResToDisplay()));
    request.setAttribute("SortValue", Integer.valueOf(pdcSC.getSortValue()));
    request.setAttribute("SortOrder", pdcSC.getSortOrder());

    // spelling words
    request.setAttribute("spellingWords", pdcSC.getSpellingwords());

    request.setAttribute("ResultsDisplay", Integer.valueOf(pdcSC.getCurrentResultsDisplay()));
    request.setAttribute("ResultPageId", pdcSC.getResultPageId());
    request.setAttribute("XmlFormSortValue", pdcSC.getXmlFormSortValue());
    request.setAttribute("sortImp", pdcSC.getSortImplemtor());

    setTabsInfoIntoRequest(pdcSC, request);
  }

  /**
   * Cette methode retourne uniquement la liste contenant les silvercontent (Recherche PDC pure)
   *
   * @param alSilverContentIds - la liste de silvercontentId
   * @return la liste des silvercontents
   */
  private List<GlobalSilverContent> pdcSearchOnly(List<Integer> alSilverContentIds,
      PdcSearchSessionController pdcSC)
      throws Exception {

    List<GlobalSilverContent> alSilverContents = new ArrayList<>();
    if (alSilverContentIds == null || alSilverContentIds.isEmpty()) {
      return alSilverContents;
    }

    // la recherche PDC à des résultats. La liste qui contient les silverContentId n'est pas vide
    // recherche des componentId a partir de silverContentId
    // attention cette methode ne fonctionne que si l'on classe un document dans son instance.
    List<String> alInstanceIds = new ArrayList<>();
    // on récupère la liste de instance contenant tous les documents
    alInstanceIds = contentManager.getInstanceId(alSilverContentIds);


    // une fois la liste des instanceId définie, on parcourt cette liste pour en retirer les
    // SilverContentIds
    // propre à chaque instanceId.
    // Pb si entre temps, un utilisateur dé-instancie un job'Peas

    for (int j = 0; j < alInstanceIds.size(); j++) {
      String instanceId = alInstanceIds.get(j);
      ContentPeas contentP = contentManager.getContentPeas(instanceId);

      // On récupère tous les silverContentId d'un instanceId
      List<Integer> allSilverContentIds = contentManager.getSilverContentIdByInstanceId(instanceId);

      // une fois les SilverContentId de l'instanceId récupérés, on ne garde que ceux qui sont
      // dans la liste résultat (alSilverContentIds).
      allSilverContentIds.retainAll(alSilverContentIds);

      if (contentP != null) {
        // we are going to search only SilverContent of this instanceId
        ContentInterface contentInterface = contentP.getContentInterface();
        List<SilverContentInterface> silverContentTempo = contentInterface.getSilverContentById(
            allSilverContentIds, instanceId, pdcSC.getUserId(), contentP.getUserRoles());

        if (silverContentTempo != null) {
          alSilverContents.addAll(transformSilverContentsToGlobalSilverContents(silverContentTempo,
              instanceId, pdcSC));
        }
      }
    }



    return alSilverContents;
  }

  private List<GlobalSilverContent> transformSilverContentsToGlobalSilverContents(
      List<SilverContentInterface> silverContentTempo, String instanceId,
      PdcSearchSessionController pdcSC)
      throws Exception {
    List<GlobalSilverContent> alSilverContents = new ArrayList<>(silverContentTempo.size());
    String contentProcessorPrefixId = "default";
    if (instanceId.startsWith("gallery")) {
      contentProcessorPrefixId = "gallery";
    }
    IGlobalSilverContentProcessor processor =
        ServiceProvider.getService(contentProcessorPrefixId + PROCESSOR_NAME_SUFFIX);

    for (SilverContentInterface sci : silverContentTempo) {
      UserDetail creatorDetail = pdcSC.getOrganisationController().getUserDetail(sci.getCreatorId());

      GlobalSilverContent gsc =
          processor.getGlobalSilverContent(sci, creatorDetail, pdcSC.getLocation(instanceId));

      alSilverContents.add(gsc);
    }
    return alSilverContents;
  }

  /**
   * Cette méthode est appellée uniquement lorsque l'utilisateur fait une recherche combinant la
   * recherche classique à la recherche PDC. Elle récupère dans une liste tous les documents se
   * trouvant positionnés dans le PDC de ie. Puis elle ne garde, que les elements communs entre les
   * deux listes - celle créée et celle des silvercontentid. Ensuite, elle récupère les
   * MatchinIndexEntry correspondant aux résultats du tri des listes.
   *
   * @param ie - le tableau de MatchingIndexEntry trouvé par la recherche classique
   * @return le nouveau tableau de MatchingIndexEntry.
   */
  private MatchingIndexEntry[] mixedSearch(MatchingIndexEntry[] ie, List<Integer> alSilverContentIds)
      throws Exception {

    // On créait une liste triée d'indexEntry
    SortedSet<Integer> basicSearchList = new TreeSet<>();
    List<String> docFeature = new ArrayList<>();
    for (int i = 0; ie != null && i < ie.length; i++) {
      String instanceId = ie[i].getComponent(); // recupere l'instanceId
      String objectId = ie[i].getObjectId(); // recupere l'id du document
      docFeature.add(objectId);
      docFeature.add(instanceId);
    }
    try {
      // on récupère le silverContentId à partir de la recherche classique
      basicSearchList = contentManager.getSilverContentId(docFeature);
    } catch (Exception e) {

    }

    // ne garde que les objets communs aux 2 listes basicSearchList - alSilverContentIds
    // en effet, la liste resultante du PDC n'est pas la meme que celle
    // élaborée à partir de la recherche classique
    if (alSilverContentIds != null) {
      basicSearchList.retainAll(alSilverContentIds);
    }

    // la liste basicSearchList ne contient maintenant que les silverContentIds des documents
    // trouvés
    // mais ces documents sont également dans le tableau résultat de la recherche classique
    // il faut donc créer un tableau de MatchingIndexEntry pour afficher le resultat
    List<MatchingIndexEntry> result = new ArrayList<>();

    if (basicSearchList != null && basicSearchList.size() > 0) {
      // la liste contient bien des résultats
      Iterator<Integer> it = basicSearchList.iterator();
      for (; it.hasNext();) {
        int contentId = it.next().intValue(); // on récupère le silverContentId de la
        // liste
        // on récupère l'internalContentId car nous en avons besoin pour la construction d'un
        // matchingIndexEntry
        String internalContentId = contentManager.getInternalContentId(contentId);
        MatchingIndexEntry mie = getMatchingIndexEntry(ie, internalContentId);
        if (mie != null) {
          result.add(mie);
        }
      }
    }

    Collections.sort(result, ScoreComparator.comparator);
    return result.toArray(new MatchingIndexEntry[0]);
  }

  /*
   * cette méthode retourne tous les SilverContentId des documents qui sont classés
   * @param pdcSC - le PdcSearchSessionController pour travailler avec le containerPeasPDC
   * @param searchParameters - les parametres de la recherche (auteur, dates)
   * @return la liste des SilverContentId permettant par la suite de faire une recherche sur les
   * SilverContent's (PDC pure) ou MatchingIndexEntry (Classic/mixte)
   */
  private List<Integer> searchAllSilverContentId(PdcSearchSessionController pdcSC,
      QueryParameters searchParameters) throws Exception {
    List<String> alComponentIds = pdcSC.getCurrentComponentIds();
    // We get silvercontentids according to the search context, author, components and dates
    String afterDate = DateUtil.date2SQLDate(searchParameters.getAfterDate());
    String beforeDate = DateUtil.date2SQLDate(searchParameters.getBeforeDate());
    return new GlobalPdcManager().findSilverContentIdByPosition(pdcSC.getSearchContext(),
        alComponentIds, searchParameters.getCreatorId(), afterDate, beforeDate);
  }

  private void clearUserChoices(PdcSearchSessionController pdcSC) {
    pdcSC.clearQueryParameters();
    pdcSC.removeAllCriterias();
    pdcSC.setSortOrder(PdcSearchSessionController.SORT_ORDER_DESC);
    pdcSC.setSortValue(1);
    pdcSC.getQueryParameters().setXmlTitle(null);
    pdcSC.clearXmlTemplateAndData();
    pdcSC.setDataType(PdcSearchSessionController.ALL_DATA_TYPE);
    pdcSC.setSelectedFacetEntries(null);
  }

  /**
   * Dans un tableau de MatchingIndexEntry, on recherche l'objet MatchingIndexEntry qui a comme
   * objectId l'internalContentId
   */
  private MatchingIndexEntry getMatchingIndexEntry(MatchingIndexEntry[] ie, String internalContentId)
      throws Exception {
    MatchingIndexEntry res = null;
    for (int i = 0; i < ie.length; i++) {
      // on parcourt le tableau résultats de la recherche classique
      // et on retourne le MatchingIndexEntry correspondant à l'internalContentId
      if ((ie[i].getObjectId()).equals(internalContentId)) {
        res = ie[i];
        break;
      }
    }

    return res;
  }

  private void processSelection(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    // get the selected object ids
    String selectedObjectIds = request.getParameter("selectedIds");

    // extract the selected objects from the results
    List<GlobalSilverResult> silverContents = pdcSC.getResultsToDisplay();
    String objectId = null;
    List<GlobalSilverResult> selectedSilverContents = pdcSC.getSelectedSilverContents();
    if (selectedSilverContents == null) {
      selectedSilverContents = new ArrayList<>();
    }
    for (int i = 0; i < silverContents.size(); i++) {
      GlobalSilverResult gsr = silverContents.get(i);
      objectId = gsr.getId() + "-" + gsr.getInstanceId();
      if (selectedObjectIds.indexOf(objectId) != -1 && !selectedSilverContents.contains(gsr)) {
        // the silverContent is in the selected objects list
        selectedSilverContents.add(gsr);
      } else if (selectedObjectIds.indexOf(objectId) == -1) {
        selectedSilverContents.remove(gsr);
      }
    }

    // memorize the selected silverContents
    pdcSC.setSelectedSilverContents(selectedSilverContents);

    // Ajout d un traitement spécifique pour le cas de l export: je ne change pas la
    // mécanique existante car je crains la régression.
    String notSelectedObjectIds = request.getParameter("notSelectedIds");
    if (selectedObjectIds != null && selectedObjectIds.length() != 0) {
      for (int i = 0; i < silverContents.size(); i++) {
        GlobalSilverResult gsr = silverContents.get(i);
        objectId = gsr.getId() + "-" + gsr.getInstanceId();
        if (selectedObjectIds.indexOf(objectId) != -1) {
          // the silverContent is in the selected objects list
          gsr.setSelected(true);
        }
        if (notSelectedObjectIds.indexOf(objectId) != -1) {
          // the silverContent is in the selected objects list
          gsr.setSelected(false);
        }
      }
    }
  }

  public String processThesaurusActions(String function, PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception {
    String destination = "";
    if (function.startsWith("GlobalActivateThesaurus")
        || function.startsWith("GlobalDesactivateThesaurus")) {
      ThesaurusHelper
          .setJargonInfoInRequest(pdcSC, request, function.startsWith("GlobalActivateThesaurus"));
      InterestCentersHelper.putSelectedInterestCenterId(request);
      PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, true);
      destination = getDestinationDuringSearch(pdcSC, request);
    }
    return destination;
  }

  private boolean processChangeSearchType(String function, PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception {
    boolean setAdvancedSearchItems = true;
    pdcSC.setSearchPage(request.getParameter("SearchPage"));
    pdcSC.setSearchPageId(request.getParameter("SearchPageId"));
    pdcSC.setResultPage(request.getParameter("ResultPage"));
    pdcSC.setResultPageId(request.getParameter("ResultPageId"));

    if (function.equals("ChangeSearchTypeToSimple")) {
      pdcSC.setSearchType(PdcSearchSessionController.SEARCH_SIMPLE);
    } else if (function.equals("ChangeSearchTypeToAdvanced")) {
      pdcSC.setSearchType(PdcSearchSessionController.SEARCH_ADVANCED);
    } else if (function.equals("ChangeSearchTypeToXml")) {
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
        pdcSC.setSortImplemtor(sortImp);
      } else {
        pdcSC.setSortImplemtor(null);
      }
      String SortResXForm = request.getParameter(Keys.RequestSortXformField.value());
      if (StringUtil.isDefined(templateName)) {
        pdcSC.setXmlFormSortValue(SortResXForm);
      } else {
        pdcSC.setXmlFormSortValue(null);
      }
      String sortOrder = request.getParameter("sortOrder");
      if (StringUtil.isDefined(sortOrder)) {
        pdcSC.setSortOrder(sortOrder);
      } else {
        pdcSC.setSortOrder(PdcSearchSessionController.SORT_ORDER_ASC);
      }

      pdcSC.setSearchType(PdcSearchSessionController.SEARCH_XML);
    } else {
      String spaceId = request.getParameter("spaces");
      String instanceId = request.getParameter("componentSearch");
      pdcSC.getQueryParameters().setSpaceIdAndInstanceId(spaceId, instanceId);

      if (pdcSC.isPlatformUsesPDC()) {
        pdcSC.setSearchType(PdcSearchSessionController.SEARCH_EXPERT);
      } else {
        // PDC is not used, redirect to simple search
        pdcSC.setSearchType(PdcSearchSessionController.SEARCH_ADVANCED);
      }

      if (StringUtil.getBooleanValue(request.getParameter("FromPDCFrame"))) {
        // Exclusive case to display pertinent classification axis in PDC frame
        // Advanced search items are useless in this case
        setAdvancedSearchItems = false;
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
        request.setAttribute("SearchType", Integer.valueOf(pdcSC.getSearchType()));

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
    return "/pdcPeas/jsp/globalResult.jsp";
  }

  private void setTabsInfoIntoRequest(PdcSearchSessionController pdcSC, HttpServletRequest request) {
    request.setAttribute("XmlSearchVisible", Boolean.valueOf(pdcSC.isXmlSearchVisible()));
    request.setAttribute("ExpertSearchVisible", pdcSC.isPlatformUsesPDC());
  }
}
