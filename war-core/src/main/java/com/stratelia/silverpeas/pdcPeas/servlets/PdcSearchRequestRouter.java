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
package com.stratelia.silverpeas.pdcPeas.servlets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.RecordTemplate;
import com.silverpeas.form.form.XmlSearchForm;
import com.silverpeas.jcrutil.BasicDaoFactory;
import com.silverpeas.look.LookHelper;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.silverpeas.containerManager.ContainerInterface;
import com.stratelia.silverpeas.containerManager.ContainerManager;
import com.stratelia.silverpeas.containerManager.ContainerManagerException;
import com.stratelia.silverpeas.containerManager.ContainerPeas;
import com.stratelia.silverpeas.containerManager.ContainerWorkspace;
import com.stratelia.silverpeas.containerManager.URLIcone;
import com.stratelia.silverpeas.contentManager.ContentInterface;
import com.stratelia.silverpeas.contentManager.ContentManager;
import com.stratelia.silverpeas.contentManager.ContentManagerException;
import com.stratelia.silverpeas.contentManager.ContentPeas;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.contentManager.IGlobalSilverContentProcessor;
import com.stratelia.silverpeas.contentManager.SilverContentInterface;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.ContainerContextImpl;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.SearchCriteria;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.pdcPeas.control.GoogleTabsUtil;
import com.stratelia.silverpeas.pdcPeas.control.Keys;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController;
import com.stratelia.silverpeas.pdcPeas.model.GlobalSilverResult;
import com.stratelia.silverpeas.pdcPeas.model.QueryParameters;
import com.stratelia.silverpeas.pdcPeas.vo.ResultFilterVO;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.selection.SelectionUsersGroups;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.ScoreComparator;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.WAAttributeValuePair;
import com.stratelia.webactiv.util.exception.UtilException;

public class PdcSearchRequestRouter extends ComponentRequestRouter {

  private static final long serialVersionUID = 1L;
  private ContainerManager containerManager = null;
  private ContentManager contentManager = null;
  private ContainerPeas containerPeasPDC = null;
  private ContentPeas contentPeasPDC = null;

  public PdcSearchRequestRouter() throws Exception {
    containerManager = new ContainerManager();
    contentManager = new ContentManager();
  }

  @Override
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new PdcSearchSessionController(mainSessionCtrl, componentContext,
        "com.stratelia.silverpeas.pdcPeas.multilang.pdcBundle",
        "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasIcons");
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
   * @param function The entering request function (ex : "Main.jsp")
   * @param componentSC The component Session Control, build and initialised.
   * @param request The entering request. The request rooter need it to get parameters
   * @return The complete destination URL for a forward (ex
   * :"/notificationUser/jsp/notificationUser.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, ComponentSessionController componentSC,
      HttpServletRequest request) {
    SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.getDestination()",
        "root.MSG_GEN_PARAM_VALUE", " Function=" + function);
    String destination = "";
    PdcSearchSessionController pdcSC = (PdcSearchSessionController) componentSC; // get the session
    // controller to inform the request
    try {
      PdcSubscriptionHelper.init(pdcSC, request);
      if (function.startsWith("PDCSubscription") || function.startsWith("addSubscription")
          || function.startsWith("updateSubscription")) {
        // Processing of the Pdc subscriptions actions
        destination = processPDCSubscriptionActions(function, pdcSC, request);

      } else if (function.startsWith("ToSearchToSelect")
          || function.startsWith("ValidateSelectedObjects")) {

        // Processing of the Pdc selection actions
        destination = processPDCSelectionActions(function, pdcSC, request);

      } else if (function.startsWith("AxisTree") || function.startsWith("searchInit")
          || function.startsWith("searchResult")) {

        // Processing of the Pdc glossary actions
        destination = processPDCGlossaryActions(function, pdcSC, request);

      } else if (function.startsWith("Main")) {
        // Function used only by components which use the PDC as the container (whitePages,
        // questionReply, filebox+ components based on)
        // Init all the informations concerning the container/content stuff
        this.initContainerContentInfo(pdcSC, false, null);
        // Put the containerWorkspace into the request
        request.setAttribute("containerWorkspace", pdcSC.getContainerWorkspace());
        request.setAttribute("ComponentId", pdcSC.getComponentId());

        // Put the pertinent axis, the search context and the full path of each criteria in the
        // request
        buildContextAndPertinentAxis(pdcSC, request);

        // Put the jargon corresponding to the user
        ThesaurusHelper.initializeJargon(pdcSC);
        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        // create the new destination
        destination = "/pdcPeas/jsp/searchContextInComponent.jsp";
      } else if (function.startsWith("SearchView")) {
        // ONLY USE FOR LOCAL SEARCH
        // Get the SilverContents to display
        ContainerInterface containerInterface = containerPeasPDC.getContainerInterface();

        List<String> alComponentIds = new ArrayList<String>();
        // if we are in selection mode, we get silverContent from all available instances of the
        // specific component
        if (pdcSC.isSelectionActivated()) {
          alComponentIds.addAll(pdcSC.getCurrentComponentIds());
        } else {
          alComponentIds.add(pdcSC.getComponentId());
        }

        // we search all silverContent ids according to the search context and the component
        // instance list
        List<Integer> alSilverContentIds =
            containerInterface.findSilverContentIdByPosition(pdcSC.getContainerPosition(),
                alComponentIds);
        SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.getDestination", "SearchView",
            "alSilverContentIds = " + alSilverContentIds.toString());

        ContentInterface contentInterface = contentPeasPDC.getContentInterface();

        // If we are cooming from the globalSearch, we have to init the ContainerWorkspace
        // ONLY USE FOR LOCAL SEARCH
        if (pdcSC.getContainerWorkspace() == null) {
          // Init all the informations concerning the container/content stuff
          // for the selected component
          this.initContainerContentInfo(pdcSC, false, pdcSC.getComponentId());
        }

        // According to the finded silvercontentIds, we get the corresponding silverContent objects
        List<SilverContentInterface> alSilverContents = contentInterface.getSilverContentById(
            alSilverContentIds, pdcSC.getComponentId(), pdcSC.getUserId(), pdcSC.
                getContainerWorkspace().getContentUserRoles());
        pdcSC.getContainerWorkspace().setSilverContents(alSilverContents);
        // Put the containerWorkspace int the request
        request.setAttribute("containerWorkspace", pdcSC.getContainerWorkspace());
        request.setAttribute("ComponentId", pdcSC.getComponentId());
        // Put the search context in the request
        buildSearchContext(pdcSC, request);
        // Put the jargon corresponding to the user
        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);
        // create the new destination
        destination = "/pdcPeas/jsp/searchResult.jsp";
      } else if (function.startsWith("ContentForward")) {
        // ONLY USE FOR LOCAL SEARCH

        // Get the destination. It corresponds to the url page of the silverContent
        destination = request.getParameter("contentURL");

        // Compute the URL to forward to the content
        String sURLContent =
            URLManager.getURL(contentPeasPDC.getSessionControlBeanName(), pdcSC.getSpaceId(), pdcSC
                .
                getComponentId());
        destination = sURLContent + destination;
        SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.getDestination", "",
            "Container forwarding to: " + destination);

        // Put the containerContext in the request
        String sURLContainer =
            URLManager.getURL(containerPeasPDC.getSessionControlBeanName(), pdcSC.getSpaceId(),
                pdcSC.getComponentId());
        ContainerContextImpl containerContext = new ContainerContextImpl();
        containerContext.setContainerInstanceId(containerManager.getContainerInstanceId(pdcSC.
            getComponentId()));
        containerContext.setReturnURL(sURLContainer + containerPeasPDC.getReturnURL());
        containerContext.setClassifyURLIcone(containerPeasPDC.getClassifyURLIcone());
        containerContext.setContainerPositionInterface(pdcSC.getContainerPosition());
        containerContext.setContainerPeas(containerPeasPDC);

        // Put the containerWorkspace int the request
        request.setAttribute("ContainerContext", containerContext);
      } else if (function.startsWith("GlobalContentForward")) {
        // Get the destination. It corresponds to the url page of the silverContent
        destination = request.getParameter("contentURL");

        String componentId = request.getParameter("componentId");
        String spaceId = null;

        // Compute the URL to forward to the content
        ContentPeas contentP = contentManager.getContentPeas(componentId);

        String sURLContent = null;
        if (contentP == null) {
          sURLContent = URLManager.getURL(spaceId, componentId);
        } else {
          sURLContent =
              URLManager.getURL(contentP.getSessionControlBeanName(), spaceId, componentId);
        }

        request.setAttribute("ToURL", sURLContent + destination);
        SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.getDestination", "",
            "GlobalContentForward - Container forwarding to: redirectToComponent.jsp, ToURL = "
                + sURLContent + destination);

        destination = "/pdcPeas/jsp/redirectToComponent.jsp";

        if (contentP != null) {
          this.initContainerContentInfo(pdcSC, true, componentId);
        }

        // Put the containerContext in the request
        String sURLContainer =
            URLManager.getURL(containerPeasPDC.getSessionControlBeanName(), spaceId, componentId);
        ContainerContextImpl containerContext = new ContainerContextImpl();
        containerContext.setContainerInstanceId(containerManager
            .getContainerInstanceId(componentId));
        containerContext.setReturnURL(sURLContainer + containerPeasPDC.getReturnURL());
        containerContext.setClassifyURLIcone(containerPeasPDC.getClassifyURLIcone());
        containerContext.setContainerPositionInterface(pdcSC.getContainerPosition());
        containerContext.setContainerPeas(containerPeasPDC);

        SilverTrace.info("pdcPeas", "PdcSearchRequestRouteur.GlobalContentForward",
            "root.MSG_GEN_PARAM_VALUE", "sURLContainer = " + sURLContainer);
        SilverTrace.info("pdcPeas", "PdcSearchRequestRouteur.GlobalContentForward",
            "root.MSG_GEN_PARAM_VALUE", "containerPeasPDC.getReturnURL() = "
                + containerPeasPDC.getReturnURL());

        // Put the containerWorkspace in the request
        request.setAttribute("containerWorkspace", pdcSC.getContainerWorkspace());
        request.setAttribute("ComponentId", pdcSC.getComponentId());
        request.setAttribute("ContainerContext", containerContext);
      } else if (function.startsWith("ViewContext")) {

        // Put the containerWorkspace int the request
        request.setAttribute("containerWorkspace", pdcSC.getContainerWorkspace());
        request.setAttribute("ComponentId", pdcSC.getComponentId());

        buildContextAndPertinentAxis(pdcSC, request);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        destination = "/pdcPeas/jsp/searchContextInComponent.jsp";
      } else if (function.startsWith("ViewArbo")) {
        // USED ONLY IN LOCAL MODE -- The user wants to collapse or uncollapse a value

        // Put the containerWorkspace int the request
        request.setAttribute("containerWorkspace", pdcSC.getContainerWorkspace());
        request.setAttribute("ComponentId", pdcSC.getComponentId());

        // Put the daughters into the request
        viewArbo(pdcSC, request);

        // Put the pertinent axis, the search context and the full path of each criteria in the
        // request
        buildContextAndPertinentAxis(pdcSC, request);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        destination = "/pdcPeas/jsp/searchContextInComponent.jsp";

      } else if (function.startsWith("GlobalViewArbo")) {
        // USED ONLY IN GLOBAL MODE -- The user wants to collapse or uncollapse a value

        InterestCentersHelper.putSelectedInterestCenterId(request);

        PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, true);
        viewArbo(pdcSC, request);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        PdcSearchRequestRouterHelper.processSearchDomains(pdcSC, request, "SILVERPEAS");

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if (function.startsWith("AddCriteria")) {
        // USED ONLY IN LOCAL MODE -- the user add a criteria into the SearchContext.

        // Put the containerWorkspace int the request
        request.setAttribute("containerWorkspace", pdcSC.getContainerWorkspace());
        request.setAttribute("ComponentId", pdcSC.getComponentId());

        addCriteria(pdcSC, request);

        buildContextAndPertinentAxis(pdcSC, request);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        destination = "/pdcPeas/jsp/searchContextInComponent.jsp";
      } else if (function.startsWith("GlobalAddCriteria")) {
        // USED ONLY IN GLOBAL MODE -- the user add a criteria into the SearchContext.

        addCriteria(pdcSC, request);

        InterestCentersHelper.putSelectedInterestCenterId(request);

        PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, true);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        PdcSearchRequestRouterHelper.processSearchDomains(pdcSC, request, "SILVERPEAS");

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if (function.startsWith("DeleteCriteria")) {
        // USED ONLY IN LOCAL MODE -- the user deletes a criteria from the SearchContext.

        // Put the containerWorkspace int the request
        request.setAttribute("containerWorkspace", pdcSC.getContainerWorkspace());
        request.setAttribute("ComponentId", pdcSC.getComponentId());

        deleteCriteria(pdcSC, request);
        buildContextAndPertinentAxis(pdcSC, request);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        destination = "/pdcPeas/jsp/searchContextInComponent.jsp";
      } else if (function.startsWith("GlobalDeleteCriteria")) {
        // USED ONLY IN GLOBAL MODE -- the user deletes a criteria from the SearchContext.

        InterestCentersHelper.putSelectedInterestCenterId(request);

        deleteCriteria(pdcSC, request);

        PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, true);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        PdcSearchRequestRouterHelper.processSearchDomains(pdcSC, request, "SILVERPEAS");

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if (function.startsWith("ModifyCriteria")) {
        // USED ONLY IN LOCAL MODE -- the user modifies a criteria from the SearchContext.

        // Put the containerWorkspace int the request
        request.setAttribute("containerWorkspace", pdcSC.getContainerWorkspace());
        request.setAttribute("ComponentId", pdcSC.getComponentId());

        modifyCriteria(pdcSC, request);

        buildContextAndPertinentAxis(pdcSC, request);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        destination = "/pdcPeas/jsp/searchContextInComponent.jsp";
      } else if (function.startsWith("GlobalModifyCriteria")) {
        // USED ONLY IN GLOBAL MODE -- the user modifies a criteria from the SearchContext.

        InterestCentersHelper.putSelectedInterestCenterId(request);

        modifyCriteria(pdcSC, request);

        PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, true);

        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        PdcSearchRequestRouterHelper.processSearchDomains(pdcSC, request, "SILVERPEAS");

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if (function.startsWith("GlobalView")) {
        // the user comes from the link "Advanced Search" of the TopBar.jsp

        pdcSC.setSearchType(PdcSearchSessionController.SEARCH_EXPERT);

        destination = doGlobalView(pdcSC, request);
      } else if (function.equals("DisplayPDC")) {
        String componentId = request.getParameter("ComponentId");

        request.setAttribute("ComponentId", componentId);

        destination = "/pdcPeas/jsp/pdcInComponent.jsp";
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

        this.initContainerContentInfo(pdcSC, true, null);
        pdcSC.setContainerPeas(containerPeasPDC);

        ThesaurusHelper.initializeJargon(pdcSC);
        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        PdcSearchRequestRouterHelper.processSearchDomains(pdcSC, request, "SILVERPEAS");

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if (function.startsWith("ViewAdvancedSearch")) {

        InterestCentersHelper.putSelectedInterestCenterId(request);
        InterestCentersHelper.processICenterSaving(pdcSC, request);

        PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, true);

        ThesaurusHelper.initializeJargon(pdcSC);
        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);

        PdcSearchRequestRouterHelper.processSearchDomains(pdcSC, request, "SILVERPEAS");

        destination = getDestinationDuringSearch(pdcSC, request);
      } else if (function.equals("Pagination")) {
        // Initialize searchFilter
        ResultFilterVO filter = initSearchFilter(request, pdcSC);
        processPDCSelectionActions("ValidateSelectedObjects", pdcSC, request);

        String index = request.getParameter("Index");
        pdcSC.setIndexOfFirstResultToDisplay(index);
        setDefaultDataToNavigation(request, pdcSC, filter);

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

        ResultFilterVO filter = initSearchFilter(request, pdcSC);
        setDefaultDataToNavigation(request, pdcSC, filter);

        destination = getDestinationForResults(pdcSC);
      } else if (function.startsWith("AdvancedSearch")) {
        SilverTrace.debug("pdcPeas", "PdcPeasRequestRouter.AdvancedSearch",
            "root.MSG_GEN_ENTER_METHOD");

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

        pdcSC.setSelectedSilverContents(new ArrayList<GlobalSilverResult>());
        // This is the main function of global search
        boolean pdcUsedDuringSearch = false;
        // recupere les parametres (Only for a global search in advanced mode)
        String icId = request.getParameter("icId");
        QueryParameters searchParameters = null;
        if (icId != null) {
          searchParameters =
              PdcSearchRequestRouterHelper.saveFavoriteRequestAndSetPdcInfo(pdcSC, request, icId);
        } else {
          searchParameters =
              PdcSearchRequestRouterHelper.saveUserChoicesAndSetPdcInfo(pdcSC, request, false);
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

        if (containerPeasPDC == null) {
          this.initContainerContentInfo(pdcSC, true, null);
          pdcSC.setContainerPeas(containerPeasPDC);
        }

        List<Integer> alSilverContentIds = null;
        if (pdcUsedDuringSearch) {
          // the search context is not empty. We have to search all silvercontentIds according to
          // query settings
          alSilverContentIds = searchAllSilverContentId(pdcSC, searchParameters);
        }
        SilverTrace.debug("pdcPeas", "PdcPeasRequestRouter.AdvancedSearch",
            "root.MSG_GEN_PARAM_VALUE", "avant search");
        // the query string contains something
        if (searchParameters.isDefined()
            || (StringUtil.isDefined(searchParameters.getSpaceId()) && !pdcUsedDuringSearch)
            || pdcSC.isDataTypeDefined()) {
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
        SilverTrace.debug("pdcPeas", "PdcPeasRequestRouter.AdvancedSearch",
            "root.MSG_GEN_PARAM_VALUE", "après search");

        if (StringUtil.isDefined(pdcSC.getResultPage())
            && !pdcSC.getResultPage().equals("globalResult")
            && !pdcSC.getResultPage().equals("pdaResult.jsp")) {
          PdcSearchRequestRouterHelper.processItemsPagination(function, pdcSC, request);
        } else {
          ResultFilterVO filter = initSearchFilter(request, pdcSC);
          setDefaultDataToNavigation(request, pdcSC, filter);
        }

        // destination = "/pdcPeas/jsp/globalResult.jsp";
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
        String fieldValue = "";
        String fieldName = "";
        String fieldQuery = "";
        Field field = null;
        for (int f = 0; f < fieldNames.length; f++) {
          fieldName = fieldNames[f];
          field = data.getField(fieldName);
          fieldValue = field.getStringValue();
          if (fieldValue != null && fieldValue.trim().length() > 0) {
            fieldQuery = fieldValue.trim().replaceAll("##", " AND ");
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

        PdcSearchRequestRouterHelper.processSearchDomains(pdcSC, request, "SILVERPEAS");

        destination = processThesaurusActions(function, pdcSC, request);
      } else if (function.startsWith("SpecificDomainView")) {
        // To do a search in a domain that is not Silverpeas
        try {
          String domainId = request.getParameter("searchDomainId");
          if (domainId == null) {
            destination = getDestination("GlobalView", componentSC, request);
          } else {
            // request.setAttribute("domains", pdcSC.getDomains());
            PdcSearchRequestRouterHelper.processSearchDomains(pdcSC, request, domainId);
            destination = getDomainSearchPage(pdcSC.getSearchDomains(), domainId);
            if (destination == null) {
              destination = getDestination("GlobalView", componentSC, request);
            }
          }
        } catch (Exception e) {
          SilverTrace.error("pdcPeas", "PdcPeasRequestRouter.getDestination()",
              "root.MSG_ERR_CALCULATE_SEARCHFORM", e);
          destination = getDestination("GlobalView", componentSC, request);
        }
      } else if (function.startsWith("ToUserPanel")) {// utilisation de userPanel et userPanelPeas
        try {
          destination = pdcSC.initUserPanel();
        } catch (Exception e) {
          SilverTrace.warn("pdcPeas", "PdcPeasRequestRouter.getDestination()",
              "root.EX_USERPANEL_FAILED", "function = " + function, e);
        }
        SilverTrace.debug("pdcPeas", "PdcPeasRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "ToUserPanel: function = " + function + "=> destination="
                + destination);
      } else if (function.startsWith("FromUserPanel")) {// récupération des valeurs de userPanel
        // par userPanelPeas
        SilverTrace.debug("pdcPeas", "PdcPeasRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "FromUserPanel:");
        Selection sel = pdcSC.getSelection();
        // Get user selected in User Panel
        String[] userIds = SelectionUsersGroups.getDistinctUserIds(sel.getSelectedElements(), null);
        SilverTrace.debug("pdcPeas", "PdcPeasRequestRouter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "userIds:" + userIds.toString());
        if (userIds.length != 0) {
          SilverTrace.debug("pdcPeas", "PdcPeasRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "userIds.length():" + userIds.length);

          UserDetail[] userDetails = SelectionUsersGroups.getUserDetails(userIds);
          SilverTrace.debug("pdcPeas", "PdcPeasRequestRouter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "userDetails:" + userDetails.toString());
          if (userDetails != null) {
            SilverTrace.debug("pdcPeas", "PdcPeasRequestRouter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "userDetails[0].getId():" + userDetails[0].getId());
            request.setAttribute("UserDetail", pdcSC.getUserDetail(userDetails[0].getId()));
          }
        }
        destination = "/pdcPeas/jsp/refreshFromUserSelect.jsp";
        // destination = doGlobalView(pdcSC, request);
      } else if (function.startsWith("ExportPublications")) {
        processPDCSelectionActions("ValidateSelectedObjects", pdcSC, request);

        // build an exploitable list by importExportPeas
        List<WAAttributeValuePair> selectedResultsWa =
            getItemPks(pdcSC.getSelectedSilverContents());
        request.setAttribute("selectedResultsWa", selectedResultsWa);

        // jump to importExportPeas
        destination = "/RimportExportPeas/jsp/ExportItems";
      } else if (function.startsWith("ExportAttachementsToPDF")) {
        processPDCSelectionActions("ValidateSelectedObjects", pdcSC, request);
        // build an exploitable list by importExportPeas
        List<WAAttributeValuePair> selectedResultsWa =
            getItemPks(pdcSC.getSelectedSilverContents());
        request.setAttribute("selectedResultsWa", selectedResultsWa);
        // jump to importExportPeas
        destination = "/RimportExportPeas/jsp/ExportPDF";
      } else if ("ViewWebTab".equals(function)) {
        String id = request.getParameter("Id");

        request.setAttribute("WebTabId", id);
        request.setAttribute("WebTabs", GoogleTabsUtil.getTabs());
        request.setAttribute("Keywords", pdcSC.getQueryParameters().getKeywords());
        request.setAttribute("XmlSearchVisible", Boolean.valueOf(pdcSC.isXmlSearchVisible()));
        destination = "/pdcPeas/jsp/webTab.jsp";
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
        pdcSC.setSelectedSilverContents(new ArrayList<GlobalSilverResult>());
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
            pdcSC.getSearchContext().addCriteria(
                new SearchCriteria(Integer.parseInt(curAxis.substring("Axis".length(), curAxis.
                    indexOf('='))), curAxis.substring(curAxis.indexOf('=') + 1)));
          }
        }

        // Initialize query parameters
        QueryParameters searchParameters = pdcSC.getQueryParameters();
        searchParameters.setKeywords(query);
        String curSpaceId = request.getParameter("spaces");
        if (!StringUtil.isDefined(curSpaceId)) {
          curSpaceId = null;
        }
        searchParameters.setSpaceId(curSpaceId);
        String strComponentIds = request.getParameter("componentSearch");
        List<String> componentIds = null;
        if (StringUtil.isDefined(strComponentIds)) {
          componentIds = Arrays.asList(strComponentIds.split(",\\s*"));
        }
        searchParameters.setInstanceId(strComponentIds);
        pdcSC.buildCustomComponentListWhereToSearch(curSpaceId, componentIds);

        if (pdcSC.getSearchContext() != null && !pdcSC.getSearchContext().isEmpty()) {
          pdcUsedDuringSearch = true;
        }
        if (containerPeasPDC == null) {
          this.initContainerContentInfo(pdcSC, true, null);
          pdcSC.setContainerPeas(containerPeasPDC);
        }
        List<Integer> alSilverContentIds = null;
        if (pdcUsedDuringSearch) {
          // the search context is not empty. We have to search all silvercontentIds according to
          // query settings
          alSilverContentIds = searchAllSilverContentId(pdcSC, searchParameters);
        }
        SilverTrace.debug("pdcPeas", "PdcPeasRequestRouter.AdvancedSearch",
            "root.MSG_GEN_PARAM_VALUE", "avant search");
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
        SilverTrace.debug("pdcPeas", "PdcPeasRequestRouter.AdvancedSearch",
            "root.MSG_GEN_PARAM_VALUE", "après search");

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
        ResultFilterVO filter = initSearchFilter(request, pdcSC);
        setDefaultDataToNavigation(request, pdcSC, filter);
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
   * @param request the HTTPServletRequest
   * @param pdcSC the pdcSearchSessionController
   * @return a new ResultFilterVO which contains data information
   */
  private ResultFilterVO initSearchFilter(HttpServletRequest request,
      PdcSearchSessionController pdcSC) {
    String userId = request.getParameter("authorFilter");
    String instanceId = request.getParameter("componentFilter");
    ResultFilterVO filter = null;

    // Check filter values
    if (StringUtil.isDefined(userId) || StringUtil.isDefined(instanceId)) {
      filter = new ResultFilterVO();
      if (StringUtil.isDefined(userId)) {
        filter.setAuthorId(userId);
      }
      if (StringUtil.isDefined(instanceId)) {
        filter.setComponentId(instanceId);
      }
      String changerFilter = request.getParameter("changeFilter");
      if (StringUtil.isDefined(changerFilter)) {
        pdcSC.setIndexOfFirstResultToDisplay("0");
      }
    }
    return filter;
  }

  private List<FileItem> getRequestItems(HttpServletRequest request) throws UtilException {
    return FileUploadUtil.parseRequest(request);
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
    List<WAAttributeValuePair> itemPKs = new ArrayList<WAAttributeValuePair>();
    Iterator<GlobalSilverResult> itListGR = listGR.iterator();
    while (itListGR.hasNext()) {
      GlobalSilverResult gb = itListGR.next();
      itemPKs.add(new WAAttributeValuePair(gb.getId(), gb.getInstanceId()));
    }
    return itemPKs;
  }

  private String doGlobalView(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception, PdcException, ContentManagerException {
    return doGlobalView(pdcSC, request, true, true);
  }

  private String doGlobalView(PdcSearchSessionController pdcSC, HttpServletRequest request,
      boolean saveUserChoice, boolean setAdvancedSearchItems)
      throws Exception, PdcException, ContentManagerException {
    this.initContainerContentInfo(pdcSC, true, null);
    pdcSC.setContainerPeas(containerPeasPDC);

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
      LookHelper helper = (LookHelper) session.getAttribute(LookHelper.SESSION_ATT);
      if (!StringUtil.getBooleanValue(request.getParameter("FromPDCFrame"))) {
        // Context is different of PDC frame, always process PDC axis
        initializePdcAxis(pdcSC, request);
      } else {
        if (helper.isDisplayPDCInHomePage() ||
            (!helper.isDisplayPDCInHomePage() && StringUtil.isDefined(pdcSC.getQueryParameters()
                .getSpaceId()))) {
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

    PdcSearchRequestRouterHelper.processSearchDomains(pdcSC, request, "SILVERPEAS");
    // put search type
    request.setAttribute("SearchType", Integer.valueOf(pdcSC.getSearchType()));
    request.setAttribute("XmlSearchVisible", Boolean.valueOf(pdcSC.isXmlSearchVisible()));
    return getDestinationDuringSearch(pdcSC, request);
  }

  private void initializePdcAxis(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception, PdcException {
    PdcSearchRequestRouterHelper.setPertinentAxis(pdcSC, request);
    PdcSearchRequestRouterHelper.setContext(pdcSC, request);

    ThesaurusHelper.initializeJargon(pdcSC);
    ThesaurusHelper.setJargonInfoInRequest(pdcSC, request);
  }

  /**
   * Return the url corresponding to the given domain Id
   * @param searchDomains the domains defined in domains.properties
   * @param domainId the search domain id
   * @return the destination page url
   */
  private String getDomainSearchPage(Vector<String[]> searchDomains, String domainId) {
    String[] domainDetails = null;
    for (int i = 0; searchDomains != null && i < searchDomains.size(); i++) {
      domainDetails = searchDomains.get(i);
      if (domainDetails[2].equals(domainId)) {
        return domainDetails[1];
      }
    }

    return null;
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

  // Init all the informations concerning the container/content stuff
  private void initContainerContentInfo(PdcSearchSessionController pdcSC, boolean bOnlyContainer,
      String componentId) throws ContainerManagerException, ContentManagerException {
    // Create the manager objects
    containerManager = new ContainerManager();
    contentManager = new ContentManager();

    // With the global advanced search, we need to know the componentId
    if (componentId == null) {
      componentId = pdcSC.getComponentId();
    }
    // Get the containerPeas
    if (bOnlyContainer) {
      containerPeasPDC = containerManager.getContainerPeasByType("containerPDC");
    } else {
      containerPeasPDC = containerManager.getContainerPeas(componentId);
    }

    // Get the contentPeas
    if (!bOnlyContainer) {
      contentPeasPDC = contentManager.getContentPeas(componentId);
    }

    // Normally we would have to do componentSC.getContainerRoles() and
    // componentSC.getContentRoles();
    // Work around (hard coded)
    // Get the user generic roles
    // WHEN it would be instanciable !! String[] asUserGenericRoles = componentSC.getUserRoles();
    // Instead
    String[] asUserGenericRoles = pdcSC.getUserRoles();
    List<String> asUserContainerRoles = new ArrayList<String>();
    for (int nI = 0; nI < asUserGenericRoles.length; nI++) {
      if (asUserGenericRoles[nI].equals("user")) {
        asUserContainerRoles.add("containerPDC_user");
      }
      if (asUserGenericRoles[nI].equals("admin")) {
        asUserContainerRoles.add("containerPDC_admin");
      }
    }

    List<String> asUserContentRoles = new ArrayList<String>();
    if (!bOnlyContainer) {
      if (contentPeasPDC.getType().equals("fileBoxPlus")
          || contentPeasPDC.getType().equals("whitePages")
          || contentPeasPDC.getType().equals("questionReply")) {
        for (int nI = 0; nI < asUserGenericRoles.length; nI++) {
          if (asUserGenericRoles[nI].equals("user")) {
            asUserContentRoles.add("user");
          }
          if (asUserGenericRoles[nI].equals("admin")) {
            asUserContentRoles.add("admin");
          }
          if (asUserGenericRoles[nI].equals("publisher")) {
            asUserContentRoles.add("publisher");
          }
          if (asUserGenericRoles[nI].equals("writer")) {
            asUserContentRoles.add("writer");
          }
        }
      }
    }

    // Get the content URLIcones
    List<URLIcone> auContentURLIcones = null;
    if (!bOnlyContainer) {
      auContentURLIcones =
          contentManager.getContentURLIcones(contentPeasPDC.getType(), asUserContentRoles);
    }

    // Build the Container Workspace
    ContainerWorkspace containerWorkspace = new ContainerWorkspace();
    containerWorkspace.setContainerUserRoles(asUserContainerRoles);
    if (!bOnlyContainer) {
      containerWorkspace.setContentUserRoles(asUserContentRoles);
      containerWorkspace.setContentURLIcones(auContentURLIcones);
    }
    // Put it in the session controller
    pdcSC.setContainerWorkspace(containerWorkspace);
  }

  private void buildSearchContext(PdcSearchSessionController pdcSC, HttpServletRequest request)
      throws Exception {
    // on prepare le chemin complet pour l'affichage dans le cadre du contexte
    SearchContext searchContext = pdcSC.getSearchContext();
    List<SearchCriteria> c = searchContext.getCriterias();
    List<List> pathCriteria = new ArrayList<List>(c.size());
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

        List<Value> fullPath = new ArrayList<Value>();
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
   * @param request - HttpServletRequest pour donner l'information à la globalResult.jsp
   * @param len - la taille du tableau/liste résultat
   */
  private void setDefaultDataToNavigation(HttpServletRequest request,
      PdcSearchSessionController pdcSC) throws Exception {
    setDefaultDataToNavigation(request, pdcSC, null);
  }

  private void setDefaultDataToNavigation(HttpServletRequest request,
      PdcSearchSessionController pdcSC, ResultFilterVO filter) throws Exception {

    request.setAttribute("Keywords", pdcSC.getQueryParameters().getKeywords());

    request.setAttribute("IndexOfFirstResult", Integer.valueOf(pdcSC
        .getIndexOfFirstResultToDisplay()));
    request.setAttribute("ExportEnabled", Boolean.valueOf(pdcSC.isExportEnabled()));
    request.setAttribute("RefreshEnabled", Boolean.valueOf(pdcSC.isRefreshEnabled()));
    request.setAttribute("ExternalSearchEnabled", Boolean.valueOf(pdcSC.isEnableExternalSearch()));

    request.setAttribute("Results", pdcSC.getSortedResultsToDisplay(pdcSC.getSortValue(), pdcSC.
        getSortOrder(), pdcSC.getXmlFormSortValue(), pdcSC.getSortImplemtor(), filter));
    request.setAttribute("UserId", pdcSC.getUserId());

    if (filter != null) {
      // Add filtered data
      request.setAttribute("FilteredUserId", filter.getAuthorId());
      request.setAttribute("FilteredComponentId", filter.getComponentId());
    }

    // Add result group filter data
    request.setAttribute("ResultGroup", pdcSC.getResultGroupFilter());

    request.setAttribute("NbTotalResults", Integer.valueOf(pdcSC.getTotalResults()));
    request.setAttribute("XmlSearchVisible", Boolean.valueOf(pdcSC.isXmlSearchVisible()));
    request.setAttribute("PertinenceVisible", Boolean.valueOf(pdcSC.isPertinenceVisible()));

    request.setAttribute("DisplayParamChoices", pdcSC.getDisplayParamChoices());
    request.setAttribute("ChoiceNbResToDisplay", pdcSC.getListChoiceNbResToDisplay());
    request.setAttribute("NbResToDisplay", Integer.valueOf(pdcSC.getNbResToDisplay()));
    request.setAttribute("SortValue", Integer.valueOf(pdcSC.getSortValue()));
    request.setAttribute("SortOrder", pdcSC.getSortOrder());
    request.setAttribute("WebTabs", GoogleTabsUtil.getTabs());

    // spelling words
    request.setAttribute("spellingWords", pdcSC.getSpellingwords());

    request.setAttribute("ResultsDisplay", Integer.valueOf(pdcSC.getCurrentResultsDisplay()));
    request.setAttribute("ResultPageId", pdcSC.getResultPageId());
    request.setAttribute("XmlFormSortValue", pdcSC.getXmlFormSortValue());
    request.setAttribute("sortImp", pdcSC.getSortImplemtor());
  }

  /**
   * Cette methode retourne uniquement la liste contenant les silvercontent (Recherche PDC pure)
   * @param alSilverContentIds - la liste de silvercontentId
   * @return la liste des silvercontents
   */
  private List<GlobalSilverContent> pdcSearchOnly(List<Integer> alSilverContentIds,
      PdcSearchSessionController pdcSC)
      throws Exception {
    SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.pdcSearchOnly", "root.MSG_GEN_PARAM_VALUE",
        "alSilverContentIds = " + alSilverContentIds);
    List<GlobalSilverContent> alSilverContents = new ArrayList<GlobalSilverContent>();
    if (alSilverContentIds == null || alSilverContentIds.isEmpty()) {
      return alSilverContents;
    }

    // la recherche PDC à des résultats. La liste qui contient les silverContentId n'est pas vide
    // recherche des componentId a partir de silverContentId
    // attention cette methode ne fonctionne que si l'on classe un document dans son instance.
    ContentPeas contentP = null;
    String instanceId = "";
    List<String> alInstanceIds = new ArrayList<String>();
    // on récupère la liste de instance contenant tous les documents
    alInstanceIds = contentManager.getInstanceId(alSilverContentIds);
    SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.pdcSearchOnly", "root.MSG_GEN_PARAM_VALUE",
        "alInstanceIds = " + alInstanceIds);

    // une fois la liste des instanceId définie, on parcourt cette liste pour en retirer les
    // SilverContentIds
    // propre à chaque instanceId.
    // Pb si entre temps, un utilisateur dé-instancie un job'Peas
    List<Integer> allSilverContentIds = new ArrayList<Integer>();

    for (int j = 0; j < alInstanceIds.size(); j++) {
      instanceId = alInstanceIds.get(j);
      contentP = contentManager.getContentPeas(instanceId);

      // On récupère tous les silverContentId d'un instanceId
      allSilverContentIds = contentManager.getSilverContentIdByInstanceId(instanceId);
      SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.pdcSearchOnly",
          "root.MSG_GEN_PARAM_VALUE", "allSilverContentIds = " + allSilverContentIds
              + " in instance " + instanceId);

      // une fois les SilverContentId de l'instanceId récupérés, on ne garde que ceux qui sont
      // dans la liste résultat (alSilverContentIds).
      allSilverContentIds.retainAll(alSilverContentIds);

      List<SilverContentInterface> silverContentTempo = null;
      if (contentP != null) {
        // we are going to search only SilverContent of this instanceId
        ContentInterface contentInterface = contentP.getContentInterface();
        silverContentTempo = contentInterface.getSilverContentById(allSilverContentIds, instanceId,
            pdcSC.getUserId(), contentP.getUserRoles());

        if (silverContentTempo != null) {
          alSilverContents.addAll(transformSilverContentsToGlobalSilverContents(silverContentTempo,
              instanceId, pdcSC));
        }
      }
    }
    SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.pdcSearchOnly", "root.MSG_GEN_PARAM_VALUE",
        "silverContent size= " + alSilverContents.size());
    SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.pdcSearchOnly", "root.MSG_GEN_PARAM_VALUE",
        "alSilverContentIds = " + alSilverContentIds.toString());

    return alSilverContents;
  }

  private List<GlobalSilverContent> transformSilverContentsToGlobalSilverContents(
      List<SilverContentInterface> silverContentTempo, String instanceId,
      PdcSearchSessionController pdcSC)
      throws Exception {
    List<GlobalSilverContent> alSilverContents = new ArrayList<GlobalSilverContent>();
    SilverContentInterface sci = null;
    UserDetail creatorDetail = null;
    GlobalSilverContent gsc = null;
    String contentProcessorId = "default";
    if (instanceId.startsWith("gallery")) {
      contentProcessorId = "gallery";
    }
    IGlobalSilverContentProcessor processor =
        (IGlobalSilverContentProcessor) BasicDaoFactory.getBean(contentProcessorId);

    for (int i = 0; i < silverContentTempo.size(); i++) {
      sci = silverContentTempo.get(i);
      creatorDetail = pdcSC.getOrganizationController().getUserDetail(sci.getCreatorId());

      gsc = processor.getGlobalSilverContent(sci, creatorDetail, getLocation(instanceId, pdcSC));

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
   * @param ie - le tableau de MatchingIndexEntry trouvé par la recherche classique
   * @return le nouveau tableau de MatchingIndexEntry.
   */
  private MatchingIndexEntry[] mixedSearch(MatchingIndexEntry[] ie, List<Integer> alSilverContentIds)
      throws Exception {

    // On créait une liste triée d'indexEntry
    SortedSet<Integer> basicSearchList = new TreeSet<Integer>();
    String instanceId = "";
    String objectId = "";
    List<String> docFeature = new ArrayList<String>();
    for (int i = 0; ie != null && i < ie.length; i++) {
      instanceId = ie[i].getComponent(); // recupere l'instanceId
      objectId = ie[i].getObjectId(); // recupere l'id du document
      docFeature.add(objectId);
      docFeature.add(instanceId);
    }
    try {
      // on récupère le silverContentId à partir de la recherche classique
      basicSearchList = contentManager.getSilverContentId(docFeature);
    } catch (Exception e) {
      SilverTrace
          .info("pdcPeas", "PdcSearchRequestRouteur.mixedSearch", "root.MSG_GEN_EXIT_METHOD");
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
    List<MatchingIndexEntry> result = new ArrayList<MatchingIndexEntry>();

    if (basicSearchList != null && basicSearchList.size() > 0) {
      // la liste contient bien des résultats
      Iterator<Integer> it = basicSearchList.iterator();
      int contentId;
      MatchingIndexEntry mie = null;
      String internalContentId = "";
      for (; it.hasNext();) {
        contentId = it.next().intValue(); // on récupère le silverContentId de la
        // liste
        // on récupère l'internalContentId car nous en avons besoin pour la construction d'un
        // matchingIndexEntry
        internalContentId = contentManager.getInternalContentId(contentId);
        mie = getMatchingIndexEntry(ie, internalContentId);
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
    ContainerInterface containerInterface = getContainerInterface(pdcSC);
    List<String> alComponentIds = pdcSC.getCurrentComponentIds();
    // We get silvercontentids according to the search context, author, components and dates
    String afterDate = DateUtil.date2SQLDate(searchParameters.getAfterDate(), pdcSC.getLanguage());
    String beforeDate =
        DateUtil.date2SQLDate(searchParameters.getBeforeDate(), pdcSC.getLanguage());
    return containerInterface.findSilverContentIdByPosition(pdcSC.getContainerPosition(),
        alComponentIds, searchParameters.getCreatorId(), afterDate, beforeDate);
  }

  private ContainerInterface getContainerInterface(PdcSearchSessionController pdcSC)
      throws Exception {
    containerPeasPDC = pdcSC.getContainerPeas();
    if (containerPeasPDC == null) {
      this.initContainerContentInfo(pdcSC, true, null);
      pdcSC.setContainerPeas(containerPeasPDC);
    }
    return containerPeasPDC.getContainerInterface();
  }

  /**
   * Retourne l'emplacement du document "PDC"
   * @param instanceId - l'id de l'instance
   * @return l'emplacement du document
   */
  private String getLocation(String instanceId, PdcSearchSessionController pdcSC) throws Exception {
    String spaceId = getSpaceId(instanceId, pdcSC); // recherche l'espace contenant l'instanceId
    return pdcSC.getSpaceLabel(spaceId) + " / " + pdcSC.getComponentLabel(spaceId, instanceId);
  }

  private String getSpaceId(String componentId, PdcSearchSessionController pdcSC) throws Exception {
    // recherche PDC Uniquement
    String spaceId = "";
    ComponentInstLight componentInst =
        pdcSC.getOrganizationController().getComponentInstLight(componentId);
    if (componentInst != null) {
      spaceId = componentInst.getDomainFatherId();
    }

    return spaceId;
  }

  private void clearUserChoices(PdcSearchSessionController pdcSC) {
    pdcSC.clearQueryParameters();
    pdcSC.removeAllCriterias();
    pdcSC.setSelectionActivated(false);
    pdcSC.setSortOrder(PdcSearchSessionController.SORT_ORDER_DESC);
    pdcSC.setSortValue(1);
    pdcSC.getQueryParameters().setXmlTitle(null);
    pdcSC.clearXmlTemplateAndData();
    pdcSC.setDataType(PdcSearchSessionController.ALL_DATA_TYPE);
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

  private String processPDCSubscriptionActions(String function, PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception {
    String destination = "";

    if (function.startsWith("PDCSubscription")) {
      clearUserChoices(pdcSC);
      PdcSubscriptionHelper.loadSubscription(pdcSC, request);
      destination = doGlobalView(pdcSC, request);
    } else if (function.startsWith("addSubscription")) {
      PdcSubscriptionHelper.addSubscription(pdcSC, request);
      destination = doGlobalView(pdcSC, request);

    } else if (function.startsWith("updateSubscription")) {
      PdcSubscriptionHelper.updateSubscription(pdcSC, request);
      destination = doGlobalView(pdcSC, request);
    }
    return destination;
  }

  private String processPDCSelectionActions(String function, PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception {
    String destination = "";

    if (function.startsWith("ToSearchToSelect")) {

      clearUserChoices(pdcSC);

      // the selection mode is activated
      pdcSC.setSelectionActivated(true);

      // the selection is made on a specific component (kmelia, whitePages...)
      String componentName = request.getParameter("ComponentName");
      // get all available instances of the specific component
      List<String> instanceIds = pdcSC.getInstanceIdsFromComponentName(componentName);
      pdcSC.setCurrentComponentIds(instanceIds);

      // store the url of the page to return
      String returnURL = request.getParameter("ReturnURL");
      pdcSC.getPdc().setURLToReturn(returnURL);

      String selectedAxis1 = request.getParameter("Axis1");
      // For future use
      // String selectedAxis2 = request.getParameter("Axis2");
      // String selectedAxis3 = request.getParameter("Axis3");

      // construction de l'objet SearchCriteria
      if (selectedAxis1 != null && !selectedAxis1.equals("-1")) {
        SearchCriteria searchCriteria =
            new SearchCriteria(Integer.parseInt(selectedAxis1), "/0");
        pdcSC.addCriteriaToSearchContext(searchCriteria); // travail sur le contexte courant
      }

      destination = doGlobalView(pdcSC, request);
    } else if (function.startsWith("ValidateSelectedObjects")) {

      // get the selected object ids
      String selectedObjectIds = request.getParameter("selectedIds");
      SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.getDestination",
          "ValidateSelectedObjects", "selectedObjectIds = " + selectedObjectIds);

      // extract the selected objects from the results
      List<GlobalSilverResult> silverContents = pdcSC.getResultsToDisplay();
      String objectId = null;
      List<GlobalSilverResult> selectedSilverContents = pdcSC.getSelectedSilverContents();
      if (selectedSilverContents == null) {
        selectedSilverContents = new ArrayList<GlobalSilverResult>();
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

      // return to the stored return url
      destination = pdcSC.getPdc().getURLToReturn();
    }
    return destination;
  }

  private String processPDCGlossaryActions(String function, PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception {
    String destination = "";

    if (function.startsWith("AxisTree")) {
      // be careful, we don't care about pertinent axis !
      if (containerPeasPDC == null) {
        this.initContainerContentInfo(pdcSC, true, null);
        pdcSC.setContainerPeas(containerPeasPDC);
      }

      String component_id = request.getParameter("component_id");
      String unique_id = request.getParameter("uniqueId");
      List<Axis> allAxis = new ArrayList<Axis>();
      if (component_id != null && !"".equals(component_id)) {
        // we are in the localResourceLocator search (localResourceLocator to a component instance)
        // only axis used by this instance must be shown
        allAxis.addAll(pdcSC.getUsedAxisByAComponentInstance(component_id));
      } else {
        // we are in the global search
        // All axis and all values must be shown
        component_id = "";

        List<AxisHeader> axisHeaders = null;
        if (pdcSC.showAllAxisInGlossary()) {
          axisHeaders = pdcSC.getAllAxis();
        } else {
          axisHeaders = pdcSC.getPrimaryAxis();
        }

        AxisHeader axisHeader = null;
        Axis axis = null;
        for (int i = 0; i < axisHeaders.size(); i++) {
          axisHeader = axisHeaders.get(i);
          axis = pdcSC.getAxisDetail(axisHeader.getPK().getId());
          allAxis.add(axis);
        }
      }

      request.setAttribute("component_id", component_id);
      request.setAttribute("uniqueId", unique_id);
      request.setAttribute("Axis", allAxis);

      destination = "/pdcPeas/jsp/consultNavigation.jsp";
    } else if (function.startsWith("searchInit")) {
      String component_id = request.getParameter("component_id");
      String unique_id = request.getParameter("uniqueId");

      request.setAttribute("component_id", component_id);
      request.setAttribute("uniqueId", unique_id);
      destination = "/pdcPeas/jsp/consultSearchInit.jsp";
    } else if (function.startsWith("searchResult")) {
      String query = request.getParameter("query");
      String type = request.getParameter("type");
      String component_id = request.getParameter("component_id");
      String unique_id = request.getParameter("uniqueId");

      if (query != null && !"".equals(query)) {
        if (type != null && type.equals("filter")) {
          // We search only axis values beginning with the string entered in the request
          if (!"*".equals(query.substring(query.length() - 1))) {
            query += "*";
          }

          if (component_id != null && !"".equals(component_id)) {
            pdcSC.setAxisResult(pdcSC.getAxisValuesByFilter(query, "", true, component_id));
          } else {
            pdcSC.setAxisResult(pdcSC.getAxisValuesByFilter(query, "", true, null));
          }
        } else {
          // We search pdc values with the help of the search engine
          MatchingIndexEntry[] ie = pdcSC.glossarySearch(query);
          // get results from searchEngine
          // for each result, get corresponding AxisValue
          String valueIdAndTreeId = null;
          int indexOfDelimiter = -1;
          String valueId = null;
          String treeId = null;
          List<Value> values = new ArrayList<Value>();
          Value value = null;
          List<String> usedTreeIds = null;
          for (int i = 0; i < ie.length; i++) {
            MatchingIndexEntry oneResult = ie[i];
            valueIdAndTreeId = oneResult.getObjectId();
            indexOfDelimiter = valueIdAndTreeId.indexOf('_');
            if (indexOfDelimiter != -1) {
              valueId = valueIdAndTreeId.substring(0, indexOfDelimiter);
              treeId = valueIdAndTreeId.substring(indexOfDelimiter + 1, valueIdAndTreeId.length());
              SilverTrace.info("pdcPeas", "PdcSearchRequestRouteur.searchResult",
                  "root.MSG_GEN_PARAM_VALUE", "valueId = " + valueId + ", treeId = " + treeId);
              // get the value and its path from root to value
              value = pdcSC.getAxisValueAndFullPath(valueId, treeId);
              if (component_id != null && !"".equals(component_id)) {
                // check if this value belongs to an axis which is used by the instance
                if (i == 0) {
                  usedTreeIds = pdcSC.getUsedTreeIds(component_id);
                }
                if (usedTreeIds.contains(treeId)) {
                  values.add(value);
                }
              } else {
                values.add(value);
              }
            }
          }
          pdcSC.setAxisResult(values);
        }
      }

      request.setAttribute("Axis", pdcSC.getAxisResult());
      request.setAttribute("component_id", component_id);
      request.setAttribute("uniqueId", unique_id);
      request.setAttribute("query", query);
      request.setAttribute("type", type);

      destination = "/pdcPeas/jsp/consultSearchResult.jsp";
    }

    return destination;
  }

  public String processThesaurusActions(String function, PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws Exception {
    String destination = "";
    if (function.startsWith("ActivateThesaurus") || function.startsWith("DesactivateThesaurus")) {
      if (function.startsWith("ActivateThesaurus")) {
        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request, true);
      } else {
        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request, false);
      }
      // Put the containerWorkspace int the request
      request.setAttribute("containerWorkspace", pdcSC.getContainerWorkspace());
      request.setAttribute("ComponentId", pdcSC.getComponentId());
      buildContextAndPertinentAxis(pdcSC, request);
      destination = "/pdcPeas/jsp/searchContextInComponent.jsp";
    } else if (function.startsWith("GlobalActivateThesaurus")
        || function.startsWith("GlobalDesactivateThesaurus")) {
      if (function.startsWith("GlobalActivateThesaurus")) {
        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request, true);
      } else {
        ThesaurusHelper.setJargonInfoInRequest(pdcSC, request, false);
      }
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
      pdcSC.getQueryParameters().setSpaceId(spaceId);

      String instanceId = request.getParameter("componentSearch");
      pdcSC.getQueryParameters().setInstanceId(instanceId);

      pdcSC.setSearchType(PdcSearchSessionController.SEARCH_EXPERT);

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

    if (pdcSC.getSearchType() == PdcSearchSessionController.SEARCH_XML) {
      request.setAttribute("PageId", pdcSC.getSearchPageId());
      return "/pdcPeas/jsp/globalSearchXML.jsp";
    } else {
      if (StringUtil.isDefined(pdcSC.getSearchPage())) {
        return pdcSC.getSearchPage();
      } else {
        // put search type
        request.setAttribute("SearchType", Integer.valueOf(pdcSC.getSearchType()));
        request.setAttribute("XmlSearchVisible", Boolean.valueOf(pdcSC.isXmlSearchVisible()));
        request.setAttribute("WebTabs", GoogleTabsUtil.getTabs());
        // Add component search type
        request.setAttribute("ComponentSearchType", pdcSC.getSearchTypeConfig());
        return "/pdcPeas/jsp/globalSearch.jsp";
      }
    }
  }

  private String getDestinationForResults(PdcSearchSessionController pdcSC) {
    if (StringUtil.isDefined(pdcSC.getResultPage())) {
      return "/pdcPeas/jsp/" + pdcSC.getResultPage();
    } else {
      return "/pdcPeas/jsp/globalResult.jsp";
    }
  }
}
