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

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.StringTokenizer;
import java.util.Vector;

import com.silverpeas.form.DataRecord;
import com.silverpeas.interestCenter.model.InterestCenter;
import com.silverpeas.interestCenter.util.InterestCenterUtil;
import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.silverpeas.thesaurus.model.Jargon;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.security.ComponentSecurity;
import com.stratelia.silverpeas.containerManager.ContainerPeas;
import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;
import com.stratelia.silverpeas.containerManager.ContainerWorkspace;
import com.stratelia.silverpeas.contentManager.ContentPeas;
import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.pdc.control.Pdc;
import com.stratelia.silverpeas.pdc.control.PdcBm;
import com.stratelia.silverpeas.pdc.control.PdcBmImpl;
import com.stratelia.silverpeas.pdc.model.Axis;
import com.stratelia.silverpeas.pdc.model.AxisHeader;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdc.model.SearchAxis;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.SearchCriteria;
import com.stratelia.silverpeas.pdc.model.UsedAxis;
import com.stratelia.silverpeas.pdc.model.Value;
import com.stratelia.silverpeas.pdcPeas.Keys;
import com.stratelia.silverpeas.pdcPeas.SortResults;
import com.stratelia.silverpeas.pdcPeas.SortResultsFactory;
import com.stratelia.silverpeas.pdcPeas.model.GlobalSilverResult;
import com.stratelia.silverpeas.pdcPeas.model.QueryParameters;
import com.stratelia.silverpeas.pdcPeas.vo.AuthorVO;
import com.stratelia.silverpeas.pdcPeas.vo.ComponentVO;
import com.stratelia.silverpeas.pdcPeas.vo.ResultFilterVO;
import com.stratelia.silverpeas.pdcPeas.vo.ResultGroupFilter;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.silverpeas.versioning.model.DocumentPK;
import com.stratelia.silverpeas.versioning.model.DocumentVersion;
import com.stratelia.silverpeas.versioning.util.VersioningUtil;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBm;
import com.stratelia.webactiv.searchEngine.control.ejb.SearchEngineBmHome;
import com.stratelia.webactiv.searchEngine.model.AxisFilter;
import com.stratelia.webactiv.searchEngine.model.MatchingIndexEntry;
import com.stratelia.webactiv.searchEngine.model.QueryDescription;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.attachment.ejb.AttachmentPK;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
import com.stratelia.webactiv.util.statistic.control.StatisticBmHome;
import com.stratelia.webactiv.util.statistic.model.StatisticRuntimeException;

public class PdcSearchSessionController extends AbstractComponentSessionController {

  static public final String ALL_SPACES = "*";
  static public final String ALL_COMPONENTS = "*";
  // Container and Content Peas
  private ContainerWorkspace containerWorkspace = null;
  private ContainerPeas containerPeasPDC = null;
  private ContentPeas contentPeasPDC = null;
  private SearchContext searchContext = new SearchContext(); // Current position
  // in PDC
  private QueryParameters queryParameters = null; // Current parameters for
  // plain search
  private VersioningUtil versioningUtil = new VersioningUtil();
  private List<String> componentList = null;
  private String isSecondaryShowed = "NO";
  private boolean showOnlyPertinentAxisAndValues = true;
  private List<GlobalSilverResult> globalSR = new ArrayList<GlobalSilverResult>();
  private List<GlobalSilverResult> filteredSR = new ArrayList<GlobalSilverResult>();
  private int indexOfFirstItemToDisplay = 1;
  private int nbItemsPerPage = -1;
  private Value currentValue = null;
  // pagination de la liste des resultats (search Engine)
  private int indexOfFirstResultToDisplay = 0;
  private String displayParamChoices = null; // All || Res || Req
  private int nbResToDisplay = -1;
  private int sortValue = -1; // 1 || 2 || 3 || 4 || 5
  private String sortOrder = null; // ASC || DESC
  public static final String SORT_ORDER_ASC = "ASC";
  public static final String SORT_ORDER_DESC = "DESC";
  // Activation ou non de la fonction d'export
  private boolean isExportEnabled = false;
  private boolean isRefreshEnabled = false;
  public static final int SEARCH_SIMPLE = 0; // only field query
  public static final int SEARCH_ADVANCED = 1; // Simple + filter
  public static final int SEARCH_EXPERT = 2; // Advanced + pdc
  public static final int SEARCH_XML = 3; // XML
  private int searchType = SEARCH_EXPERT;
  public static final int SEARCH_FULLTEXT = 0;
  public static final int SEARCH_PDC = 1;
  public static final int SEARCH_MIXED = 2;
  private String searchPage = null;
  private String resultPage = null;
  private String resultPageId = null;
  // XML Search Session's objects
  private PublicationTemplateImpl xmlTemplate = null;
  private DataRecord xmlData = null;
  private int searchScope = SEARCH_FULLTEXT;
  private ComponentSecurity componentSecurity = null;
  // field value of XML form used to sort results
  private String xmlFormSortValue = null;
  // keyword used to retrieve the implementation to realize sorting or filtering
  private String sortImplementor = null;

  private int currentResultsDisplay = SHOWRESULTS_ALL;
  public static final int SHOWRESULTS_ALL = 0;
  public static final int SHOWRESULTS_OnlyPDC = 1;

  // spelling word
  private String[] spellingwords = null;

  public PdcSearchSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle,
      String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle,
        "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasSettings");

    isExportEnabled = isExportLicenseOK();
    isRefreshEnabled = "true".equals(getSettings().getString("EnableRefresh"));

    try {
      isThesaurusEnableByUser = getActiveThesaurusByUser();
    } catch (Exception e) {
      isThesaurusEnableByUser = false;
    }

    searchContext.setUserId(getUserId());
  }

  public void setContainerWorkspace(ContainerWorkspace givenContainerWorkspace) {
    containerWorkspace = givenContainerWorkspace;
  }

  public ContainerWorkspace getContainerWorkspace() {
    return containerWorkspace;
  }

  public ContainerPositionInterface getContainerPosition() {
    return searchContext;
  }

  public SearchContext getSearchContext() {
    return this.searchContext;
  }

  /******************************************************************************************************************/
  /**
   * PDC search methods (via DomainsBar) /
   * @throws Exception
   ******************************************************************************************************************/
  public void setPDCResults(List<GlobalSilverContent> globalSilverContents) throws Exception {
    indexOfFirstItemToDisplay = 0;
    processResultsToDisplay(globalSilverContents);
  }

  public int getNbItemsPerPage() {
    if (nbItemsPerPage == -1) {
      nbItemsPerPage = new Integer(getSettings().getString("NbItemsParPage", "20")).intValue();
    }
    return nbItemsPerPage;
  }

  public int getIndexOfFirstItemToDisplay() {
    return indexOfFirstItemToDisplay;
  }

  public void setIndexOfFirstItemToDisplay(String index) {
    this.indexOfFirstItemToDisplay = new Integer(index).intValue();
  }

  /******************************************************************************************************************/
  /**
   * plain search methods /
   ******************************************************************************************************************/
  public void setResults(List<GlobalSilverContent> globalSilverResults) {
    indexOfFirstResultToDisplay = 0;
    setLastResults(globalSilverResults);
  }

  public int getIndexOfFirstResultToDisplay() {
    return indexOfFirstResultToDisplay;
  }

  public void setIndexOfFirstResultToDisplay(String index) {
    this.indexOfFirstResultToDisplay = new Integer(index).intValue();
  }

  public String getDisplayParamChoices() {
    if (displayParamChoices == null) {
      displayParamChoices = getSettings().getString("DisplayParamChoices", "All");
    }
    return displayParamChoices;
  }

  public List<String> getListChoiceNbResToDisplay() {
    List<String> choiceNbResToDisplay = new ArrayList<String>();
    StringTokenizer st = new StringTokenizer(getSettings().getString("ChoiceNbResToDisplay"), ",");
    while (st.hasMoreTokens()) {
      String choice = st.nextToken();
      choiceNbResToDisplay.add(choice);
    }
    return choiceNbResToDisplay;
  }

  public int getNbResToDisplay() {
    if (nbResToDisplay == -1) {
      nbResToDisplay = Integer.parseInt(getListChoiceNbResToDisplay().get(0));
    }
    return nbResToDisplay;
  }

  public void setNbResToDisplay(int nbResToDisplay) {
    this.nbResToDisplay = nbResToDisplay;
  }

  public String getSortOrder() {
    if (sortOrder == null) {
      sortOrder = PdcSearchSessionController.SORT_ORDER_DESC;
    }
    return sortOrder;
  }

  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }

  public int getSortValue() {
    if (sortValue == -1) {
      sortValue = 1;
    }
    return sortValue;
  }

  public void setSortValue(int sortValue) {
    this.sortValue = sortValue;
  }

  public void setQueryParameters(QueryParameters query) {
    this.queryParameters = query;
  }

  public QueryParameters getQueryParameters() {
    if (this.queryParameters == null) {
      this.queryParameters = new QueryParameters(getLanguage());
    }
    return this.queryParameters;
  }

  public void clearQueryParameters() {
    if (queryParameters != null) {
      queryParameters.clear();
    }
  }

  public MatchingIndexEntry[] search() throws ParseException {
    SilverTrace.info("pdcPeas", "PdcSearchSessionController.search()",
        "root.MSG_GEN_ENTER_METHOD");
    MatchingIndexEntry[] plainSearchResults = null;
    QueryDescription query = null;
    try {
      // spelling words initialization
      getSearchEngineBm().setSpellingWords(null);
      spellingwords = null;
      if (getQueryParameters() != null
          &&
          (getQueryParameters().isDefined() || getQueryParameters().getXmlQuery() != null || StringUtil
              .isDefined(getQueryParameters().getSpaceId()))) {
        query = getQueryParameters().getQueryDescription(
            getUserId(), "*");

        if (componentList == null) {
          buildComponentListWhereToSearch(null, null);
        }

        for (int i = 0; i < componentList.size(); i++) {
          query.addComponent((String) componentList.get(i));
        }

        if (getQueryParameters().getSpaceId() == null) {
          // c'est une recherche globale, on cherche si le pdc et les composants
          // personnels.
          query.addSpaceComponentPair(null, "user@" + getUserId()
              + "_mailService");
          query.addSpaceComponentPair(null, "user@" + getUserId() + "_todo");
          query.addSpaceComponentPair(null, "user@" + getUserId() + "_agenda");
          query.addSpaceComponentPair(null, "pdc");
          // pour retrouver les espaces et les composants
          query.addSpaceComponentPair(null, "Spaces");
          query.addSpaceComponentPair(null, "Components");
        } else {
          // used for search by space without keywords
          query.setSearchBySpace(true);
        }

        SilverTrace.info("pdcPeas", "PdcSearchSessionController.search()",
            "root.MSG_GEN_PARAM_VALUE", "# component = "
            + query.getSpaceComponentPairSet().size());

        String originalQuery = query.getQuery();
        query.setQuery(getSynonymsQueryString(originalQuery));

        getSearchEngineBm().search(query);
        plainSearchResults = getSearchEngineBm().getRange(0,
            getSearchEngineBm().getResultLength());
        // spelling words
        if (getSettings().getBoolean("enableWordSpelling", false)) {
          spellingwords = getSearchEngineBm().getSpellingWords();
        }

      }
    } catch (NoSuchObjectException nsoe) {
      // an error occurs on searchEngine statefull EJB
      // interface is not null but the EJB is !
      // so we set interface to null and we launch again de search.
      searchEngine = null;
      plainSearchResults = search();
    } catch (Exception e) {
      String keyword = "";
      if (query != null) {
        keyword = query.getQuery();
      }
      SilverTrace.info("pdcPeas", "PdcSearchSessionController.search()",
          "pdcPeas.EX_CAN_SEARCH_QUERY", "query : " + keyword, e);
    }
    SilverTrace.info("pdcPeas", "PdcSearchSessionController.search()",
        "root.MSG_GEN_EXIT_METHOD");

    return plainSearchResults;
  }

  public boolean isMatchingIndexEntryAvailable(MatchingIndexEntry mie) {
    String componentId = mie.getComponent();
    if (componentId.startsWith("kmelia")) {
      try {
        return getSecurityIntf().isObjectAvailable(componentId, getUserId(),
            mie.getObjectId(), mie.getObjectType());
      } catch (Exception e) {
        SilverTrace.info("pdcPeas",
            "PdcSearchSessionController.isMatchingIndexEntryAvailable()",
            "pdcPeas.EX_CAN_SEARCH_QUERY", "componentId = " + componentId
            + ", objectId = " + mie.getObjectId() + ", objectType = "
            + mie.getObjectType(), e);
      }
    }
    // contrôle des droits sur les espaces et les composants
    String objectType = mie.getObjectType();
    if (objectType.equals("Space")) {
      return getOrganizationController().isSpaceAvailable(mie.getObjectId(),
          getUserId());
    }
    if (objectType.equals("Component")) {
      return getOrganizationController().isComponentAvailable(
          mie.getObjectId(), getUserId());
    }

    return true;
  }

  private ComponentSecurity getSecurityIntf() throws Exception {
    if (componentSecurity == null) {
      componentSecurity = (ComponentSecurity) Class.forName(
          "com.stratelia.webactiv.kmelia.KmeliaSecurity").newInstance();
    }

    return componentSecurity;
  }

  public List<GlobalSilverResult> getResultsToDisplay() throws Exception {
    return getSortedResultsToDisplay(getSortValue(), getSortOrder(), getXmlFormSortValue(),
        getSortImplemtor(), null);
  }

  /**
   * Build the list of result group filter from current global search result
   * @return new ResultGroupFilter object which contains all data to filter result
   */
  public ResultGroupFilter getResultGroupFilter() {
    // Return object declaration
    ResultGroupFilter res = new ResultGroupFilter();

    // Retrieve current list of results
    List<GlobalSilverResult> results = getFilteredSR();

    Map<String, AuthorVO> authorsMap = new HashMap<String, AuthorVO>();
    Map<String, ComponentVO> componentsMap = new HashMap<String, ComponentVO>();

    if (results != null) {
      // Retrieve the black list component (we don't need to filter data on it)
      List<String> blackList = getFacetBlackList();

      // Loop or each result
      for (GlobalSilverResult result : results) {
        String authorName = result.getCreatorName();
        String authorId = result.getUserId();
        AuthorVO curAuthor = new AuthorVO(authorName, authorId);
        String instanceId = result.getInstanceId();
        String location = result.getLocation();
        String type = result.getType();

        if (StringUtil.isDefined(authorId)) {
          if (!authorsMap.containsKey(authorId)) {
            authorsMap.put(authorId, curAuthor);
          } else {
            authorsMap.get(authorId).setNbElt(authorsMap.get(authorId).getNbElt() + 1);
          }
        }
        if (!blackList.contains(type) && StringUtil.isDefined(location)) {
          if (!componentsMap.containsKey(instanceId)) {
            componentsMap.put(instanceId, new ComponentVO(location.substring(location.lastIndexOf(
                '/') + 1), instanceId));
          } else {
            componentsMap.get(instanceId).setNbElt(componentsMap.get(instanceId).getNbElt() + 1);
          }
        }
      }
    }
    List<AuthorVO> authors = new ArrayList<AuthorVO>(authorsMap.values());
    Collections.sort(authors, new Comparator<AuthorVO>() {

      @Override
      public int compare(AuthorVO o1, AuthorVO o2) {
        return o2.getNbElt() - o1.getNbElt();
      }
    });

    List<ComponentVO> components = new ArrayList<ComponentVO>(componentsMap.values());
    Collections.sort(components, new Comparator<ComponentVO>() {

      @Override
      public int compare(ComponentVO o1, ComponentVO o2) {
        return o2.getNbElt() - o1.getNbElt();
      }
    });

    // Fill result filter with current result values
    res.setAuthors(authors);
    res.setComponents(components);
    return res;
  }

  public List<GlobalSilverResult> processResultsToDisplay(MatchingIndexEntry[] indexEntries)
      throws Exception {
    // Tous les résultats
    List<GlobalSilverResult> results =
        matchingIndexEntries2GlobalSilverResults(filterMatchingIndexEntries(indexEntries));
    setGlobalSR(results);
    return results;
  }

  public List<GlobalSilverResult> processResultsToDisplay(List<GlobalSilverContent> silverContents)
      throws Exception {
    // Tous les résultats
    List<GlobalSilverResult> results = globalSilverContents2GlobalSilverResults(silverContents);
    setGlobalSR(results);

    // case of PDC results : pertinence sort is not applicable
    // sort by updateDate desc
    setSortValue(5);
    setSortOrder(SORT_ORDER_DESC);
    return results;
  }

  public List<GlobalSilverResult> getSortedResultsToDisplay(int sortValue, String sortOrder,
      String xmlFormSortValue, String sortType, ResultFilterVO filter)
      throws Exception {
    List<GlobalSilverResult> sortedResultsToDisplay = new ArrayList<GlobalSilverResult>();

    // Tous les résultats
    List<GlobalSilverResult> results = getGlobalSR();

    if (results != null && getSelectedSilverContents() != null) {
      GlobalSilverResult result = null;
      for (int i = 0; i < results.size(); i++) {
        result = results.get(i);
        if (getSelectedSilverContents().contains(result)) {
          result.setSelected(true);
        } else {
          result.setSelected(false);
        }
      }
    }

    // Check if we need to filter or not result
    List<GlobalSilverResult> sortedResults = new ArrayList<GlobalSilverResult>();
    // Tri de tous les résultats
    // Gets a SortResult implementation to realize the sorting and/or filtering results
    SortResults sortResults = SortResultsFactory.getSortResults(sortType);
    String sortValString = null;
    // determines which value used for sort value
    if (StringUtil.isDefined(xmlFormSortValue)) {
      sortValString = xmlFormSortValue;
    } else {
      sortValString = Integer.toString(sortValue);
    }
    // realizes the sort
    if (sortValue == 7) {
      setPopularityToResults();
    }
    sortedResults = sortResults.execute(results, sortOrder, sortValString, getLanguage());
    if (filter != null) {
      // Check Author filter
      sortedResultsToDisplay = filterResult(filter, sortedResults);
    } else {
      // Put the full result list in session
      setGlobalSR(sortedResults);

      // get the part of results to display
      sortedResultsToDisplay = sortedResults.subList(
          getIndexOfFirstResultToDisplay(), getLastIndexToDisplay());
    }

    return sortedResultsToDisplay;
  }

  private void setPopularityToResults() {
    List<GlobalSilverResult> results = getGlobalSR();
    StatisticBm statisticBm = getStatisticBm();
    ForeignPK pk = new ForeignPK("unknown");
    for (GlobalSilverResult result : results) {
      if (isPopularityCompliant(result)) {
        pk.setComponentName(result.getInstanceId());
        pk.setId(result.getId());
        try {
          int nbAccess = statisticBm.getCount(pk, 1, "Publication");
          result.setHits(nbAccess);
        } catch (RemoteException e) {
          SilverTrace.error("pdcPeas", "PdcSearchSessionController.setPopularityToResults()",
              "root.EX_CANT_GET_REMOTE_OBJECT", "pk = " + pk.toString(), e);
        }
      }
    }
  }

  private boolean isPopularityCompliant(GlobalSilverResult gsr) {
    if (gsr != null &&
        (StringUtil.isDefined(gsr.getInstanceId()) && (gsr.getInstanceId().startsWith("kmelia") ||
        gsr.getInstanceId().startsWith("kmax") || gsr
        .getInstanceId().startsWith("toolbox"))) &&
        ("Publication".equals(gsr.getType()) || (StringUtil.isDefined(gsr.getURL()) && gsr.getURL()
        .indexOf("Publication") != -1))) {
      return true;
    }
    return false;
  }

  public StatisticBm getStatisticBm() {
    try {
      StatisticBmHome statisticHome =
          (StatisticBmHome) EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME,
          StatisticBmHome.class);
      StatisticBm statisticBm = statisticHome.create();
      return statisticBm;
    } catch (Exception e) {
      throw new StatisticRuntimeException("PdcSearchSessionController.getStatisticBm()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * This method filter current array of global silver result with filter parameters
   * @param filter
   * @param listGSR
   * @param sortedResults
   * @return list of GlobalSilverResult to display
   */
  private List<GlobalSilverResult> filterResult(ResultFilterVO filter,
      List<GlobalSilverResult> listGSR) {
    List<GlobalSilverResult> sortedResults = new ArrayList<GlobalSilverResult>();
    List<GlobalSilverResult> sortedResultsToDisplay;
    String authorFilter = filter.getAuthorId();
    boolean filterAuthor = false;
    if (StringUtil.isDefined(authorFilter)) {
      filterAuthor = true;
    }

    // Check Component filter
    String componentFilter = filter.getComponentId();
    boolean filterComponent = false;
    if (StringUtil.isDefined(componentFilter)) {
      filterComponent = true;
    }

    List<String> blackList = getFacetBlackList();

    for (GlobalSilverResult gsResult : listGSR) {
      if (!blackList.contains(gsResult.getType())) {
        if (filterAuthor && gsResult.getUserId().equals(authorFilter)
            && filterComponent && gsResult.getInstanceId().equals(componentFilter)) {
          sortedResults.add(gsResult);
        } else if (filterAuthor && gsResult.getUserId().equals(authorFilter)
            && !filterComponent) {
          sortedResults.add(gsResult);
        } else if (!filterAuthor
            && filterComponent && gsResult.getInstanceId().equals(componentFilter)) {
          sortedResults.add(gsResult);
        }
      }
    }

    // Put the full result list in session
    setFilteredSR(sortedResults);

    // Retrieve last index to display
    int start = getIndexOfFirstResultToDisplay();
    if (start > sortedResults.size()) {
      start = 0;
    }
    int end = start + getNbResToDisplay();
    if (end > sortedResults.size() - 1) {
      end = sortedResults.size();
    }
    sortedResultsToDisplay = sortedResults.subList(start, end);
    return sortedResultsToDisplay;
  }

  private List<String> getFacetBlackList() {
    String sBlackList = getSettings().getString("searchengine.facet.component.blacklist", "");
    List<String> blackList = new ArrayList<String>();
    if (StringUtil.isDefined(sBlackList)) {
      blackList = Arrays.asList(sBlackList.split(",\\s*"));
    }
    return blackList;
  }

  private void setExtraInfoToResultsToDisplay(List<GlobalSilverResult> results) {
    String titleLink = "";
    String downloadLink = "";
    String resultType = "";
    String underLink = "";
    String componentId = null;
    String m_sContext = GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL");

    // activate the mark as read functionality on results list
    String markAsReadJS = "";
    boolean isEnableMarkAsRead = getSettings().getBoolean("enableMarkAsRead", false);

    GlobalSilverResult result = null;
    MatchingIndexEntry indexEntry = null;
    for (int r = 0; r < results.size(); r++) {
      result = results.get(r);
      result.setResultId(r);
      indexEntry = result.getIndexEntry();
      if (indexEntry != null) {
        resultType = indexEntry.getObjectType();
        if (!StringUtil.isDefined(resultType)) {
          resultType = "";
        }
      }
      componentId = result.getInstanceId();
      downloadLink = null;
      // create the url part to activate the mark as read functionality
      if (isEnableMarkAsRead) {
        markAsReadJS = "markAsRead('" + r + "');";
      }
      if (resultType.equals("Versioning")) {
        // Added to be compliant with old indexing method
        resultType = "Publication";
      }

      if (resultType.startsWith("Attachment")) {
        if (!componentId.startsWith("webPages")) {
          try {
            downloadLink = getAttachmentUrl(indexEntry.getObjectType(), indexEntry.
                getComponent());
          } catch (Exception e) {
            SilverTrace.error("pdcPeas",
                "searchEngineSessionController.setExtraInfoToResultsToDisplay()",
                "pdcPeas.MSG_CANT_GET_DOWNLOAD_LINK", e);
          }
          underLink = getUrl(m_sContext, indexEntry);
          int iStart = underLink.indexOf("Attachment");
          int iEnd = underLink.indexOf("&", iStart);
          underLink = underLink.substring(0, iStart) + "Publication"
              + underLink.substring(iEnd, underLink.length());
          titleLink = "javascript:" + markAsReadJS + " window.open('"
              + EncodeHelper.javaStringToJsString(downloadLink)
              + "');jumpToComponent('" + componentId
              + "');document.location.href='"
              + EncodeHelper.javaStringToJsString(underLink)
              + "&FileOpened=1';";
        } else {
          ComponentInstLight componentInst = getOrganizationController().getComponentInstLight(
              componentId);
          if (componentInst != null) {
            String title = componentInst.getLabel(getLanguage());
            result.setTitle(title);
            result.setType("Wysiwyg");

            titleLink = getUrl(m_sContext, indexEntry);
          }
        }
      } else if (resultType.startsWith("Versioning")) {
        try {
          downloadLink = getVersioningUrl(resultType.substring(10), componentId);
        } catch (Exception e) {
          SilverTrace.error("pdcPeas",
              "searchEngineSessionController.setExtraInfoToResultsToDisplay()",
              "pdcPeas.MSG_CANT_GET_DOWNLOAD_LINK", e);
        }
        underLink = getUrl(m_sContext, indexEntry);
        int iStart = underLink.indexOf("Versioning");
        int iEnd = underLink.indexOf("&", iStart);
        underLink = underLink.substring(0, iStart) + "Publication"
            + underLink.substring(iEnd, underLink.length());
        titleLink = "javascript:" + markAsReadJS + " window.open('"
            + EncodeHelper.javaStringToJsString(downloadLink)
            + "');jumpToComponent('" + componentId
            + "');document.location.href='"
            + EncodeHelper.javaStringToJsString(underLink) + "&FileOpened=1';";
      } else if (resultType.equals("LinkedFile")) {
        // open the linked file inside a popup window
        downloadLink =
            FileServerUtils.getUrl(indexEntry.getTitle(), indexEntry.getObjectId(),
            AttachmentController.
            getMimeType(indexEntry.getTitle()));
        // window opener is reloaded on the main page of the component
        underLink = m_sContext + URLManager.getURL("useless", componentId)
            + "Main";
        titleLink = "javascript:" + markAsReadJS + " window.open('"
            + EncodeHelper.javaStringToJsString(downloadLink)
            + "');jumpToComponent('" + componentId
            + "');document.location.href='"
            + EncodeHelper.javaStringToJsString(underLink) + "';";
      } else if (resultType.equals("TreeNode")) {
        // the PDC uses this type of object.
        // window.opener is not reloaded.
        // the glossary is opened in popup mode.
        String objectId = indexEntry.getObjectId();
        String treeId = objectId.substring(objectId.indexOf("_") + 1, objectId.length());
        String valueId = objectId.substring(0, objectId.indexOf("_"));
        String uniqueId = treeId + "_" + valueId;
        SilverTrace.warn("pdcPeas", "PdcSearchRequestRouter.buildResultList()",
            "root.MSG_GEN_PARAM_VALUE", "uniqueId= " + uniqueId);
        titleLink = "javascript:" + markAsReadJS + " openGlossary('" + uniqueId + "');";
      } else if (resultType.equals("Space")) {
        // retour sur l'espace
        String spaceId = indexEntry.getObjectId();
        titleLink = "javascript:" + markAsReadJS + " goToSpace('" + spaceId
            + "');document.location.href='"
            + URLManager.getSimpleURL(URLManager.URL_SPACE, spaceId) + "';";
      } else if (resultType.equals("Component")) {
        // retour sur le composant
        componentId = indexEntry.getObjectId();
        underLink = URLManager.getSimpleURL(URLManager.URL_COMPONENT,
            componentId);
        titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId
            + "');document.location.href='" + underLink + "';";
      } else if (componentId.startsWith("user@")) {
        titleLink = getUrl(m_sContext, componentId, null, indexEntry.getPageAndParams());
      } else {
        titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId
            + "');";
        if (indexEntry != null) {
          titleLink += "document.location.href='" + getUrl(m_sContext, indexEntry) + "';";
        } else {
          titleLink +=
              "document.location.href='" +
              getUrl(m_sContext, componentId, "useless", result.getURL()) + "';";
        }
      }

      result.setTitleLink(titleLink);
      if (StringUtil.isDefined(downloadLink)) {
        result.setDownloadLink(downloadLink);
      }

      if (isExportEnabled()) {
        result.setExportable(isCompliantResult(result));
      }
    }
  }

  public int getTotalResults() {
    if (!getFilteredSR().isEmpty()) {
      return getFilteredSR().size();
    }
    return getGlobalSR().size();
  }

  public int getTotalFilteredResults() {
    return getFilteredSR().size();
  }

  private int getLastIndexToDisplay() {
    int end = getIndexOfFirstResultToDisplay() + getNbResToDisplay();

    if (end > getTotalResults() - 1) {
      end = getTotalResults();
    }
    return end;
  }

  /**
   * Cette methode construit un tableau contenant toutes les informations utiles à la construction
   * de la JSP resultat
   * @param results - un tableau de MatchingIndexEntry
   * @return un tableau contenant les informations relatives aux parametres d'entrée
   */
  private List<GlobalSilverResult> matchingIndexEntries2GlobalSilverResults(
      List<MatchingIndexEntry> matchingIndexEntries) throws Exception {
    SilverTrace.info(
        "pdcPeas",
        "PdcSearchSessionController.matchingIndexEntries2GlobalSilverResults()",
        "root.MSG_GEN_ENTER_METHOD");

    if (matchingIndexEntries == null || matchingIndexEntries.size() == 0) {
      return new ArrayList<GlobalSilverResult>();
    }

    String title = null;
    String place = null;

    LinkedList<String> returnedObjects = new LinkedList<String>();
    Hashtable<String, String> places = null;

    List<GlobalSilverResult> results = new ArrayList<GlobalSilverResult>();

    String componentId = null;
    MatchingIndexEntry result = null;
    for (int i = 0; i < matchingIndexEntries.size(); i++) {
      result = matchingIndexEntries.get(i);

      // reinitialisation
      title = result.getTitle();
      componentId = result.getComponent();

      GlobalSilverResult gsr = new GlobalSilverResult(result);

      SilverTrace.info(
          "pdcPeas",
          "PdcSearchSessionController.matchingIndexEntries2GlobalSilverResults()",
          "root.MSG_GEN_PARAM_VALUE", "title= " + title);

      // WARNING : LINE BELOW HAS BEEN ADDED TO NOT SHOW WYSIWYG ALONE IN SEARCH
      // RESULT PAGE
      if (title.endsWith("wysiwyg.txt")
          && (componentId.startsWith("kmelia") || componentId.startsWith("kmax"))) {
        continue;
      }

      // Added by NEY - 22/01/2004
      // Some explanations to lines below
      // If a publication have got the word "truck" in its title and an
      // associated wysiwyg which content the same word
      // The search engine will return 2 same lines (One for the publication and
      // the other for the wysiwyg)
      // Following lines filters one and only one line. The choice between both
      // lines is not important.
      if ("Wysiwyg".equals(result.getObjectType())) {
        // We must search if the eventual associated Publication have not been
        // already added to the result
        String objectIdAndObjectType = result.getObjectId() + "&&Publication&&"
            + result.getComponent();
        if (returnedObjects.contains(objectIdAndObjectType)) {
          // the Publication have already been added
          continue;
        } else {
          objectIdAndObjectType = result.getObjectId() + "&&Wysiwyg&&"
              + result.getComponent();
          returnedObjects.add(objectIdAndObjectType);
        }
      } else if ("Publication".equals(result.getObjectType())) {
        // We must search if the eventual associated Wysiwyg have not been
        // already added to the result
        String objectIdAndObjectType = result.getObjectId() + "&&Wysiwyg&&"
            + result.getComponent();
        if (returnedObjects.contains(objectIdAndObjectType)) {
          // the Wysiwyg have already been added
          continue;
        } else {
          objectIdAndObjectType = result.getObjectId() + "&&Publication&&"
              + result.getComponent();
          returnedObjects.add(objectIdAndObjectType);
        }
      }

      // preparation sur l'emplacement du document
      if (componentId.startsWith("user@")) {
        UserDetail user = getOrganizationController().getUserDetail(
            componentId.substring(5, componentId.indexOf("_")));
        String component = componentId.substring(componentId.indexOf("_") + 1);
        place = user.getDisplayedName() + " / " + component;
      } else if (componentId.equals("pdc")) {
        place = getString("pdcPeas.pdc");
      } else {
        if (places == null) {
          places = new Hashtable<String, String>();
        }

        place = places.get(componentId);

        if (place == null) {
          ComponentInstLight componentInst = getOrganizationController().getComponentInstLight(
              componentId);
          if (componentInst != null) {
            place = getSpaceLabel(componentInst.getDomainFatherId()) + " / "
                + componentInst.getLabel(getLanguage());
            places.put(componentId, place);
          }
        }
      }

      gsr.setLocation(place);

      String userId = result.getCreationUser();
      gsr.setCreatorName(getCompleteUserName(userId));

      results.add(gsr);
    }
    if (places != null) {
      places.clear();
    }
    return results;
  }

  private List<GlobalSilverResult> globalSilverContents2GlobalSilverResults(
      List<GlobalSilverContent> globalSilverContents) throws Exception {
    if (globalSilverContents == null || globalSilverContents.isEmpty()) {
      return new ArrayList<GlobalSilverResult>();
    }
    List<GlobalSilverResult> results = new ArrayList<GlobalSilverResult>();
    GlobalSilverContent gsc = null;
    GlobalSilverResult gsr = null;
    // String encodedURL = null;
    for (int i = 0; i < globalSilverContents.size(); i++) {
      gsc = globalSilverContents.get(i);
      gsr = new GlobalSilverResult(gsc);
      // encodedURL = URLEncoder.encode(gsc.getURL(), "UTF-8");
      // gsr.setTitleLink("javaScript:submitContent('" + encodedURL + "','"
      // + gsc.getInstanceId() + "')");
      String userId = gsc.getUserId();
      gsr.setCreatorName(getCompleteUserName(userId));

      if (isExportEnabled()) {
        gsr.setExportable(isCompliantResult(gsr));
      }

      results.add(gsr);
    }
    return results;
  }

  private String getCompleteUserName(String userId) {
    UserDetail user = getOrganizationController().getUserDetail(userId);
    if (user != null) {
      return user.getDisplayedName();
    }
    return "";
  }

  private String getAttachmentUrl(String objectType, String componentId)
      throws Exception {
    String id = objectType.substring(10); // object type is Attachment1245 or
    // Attachment1245_en
    String language = I18NHelper.defaultLanguage;
    if (id != null && id.indexOf("_") != -1) {
      // extract attachmentId and language
      language = id.substring(id.indexOf("_") + 1, id.length());
      id = id.substring(0, id.indexOf("_"));
    }

    AttachmentPK attachmentPK = new AttachmentPK(id, "useless", componentId);
    AttachmentDetail attachmentDetail = AttachmentController.searchAttachmentByPK(attachmentPK);

    String urlAttachment = attachmentDetail.getAttachmentURL(language);

    // Utilisation de l'API Acrobat Reader pour ouvrir le document PDF en mode
    // recherche (paramètre 'search')
    // Transmet au PDF la requête tapée par l'utilisateur via l'URL d'accès
    // http://partners.adobe.com/public/developer/en/acrobat/sdk/pdf/pdf_creation_apis_and_specs/PDFOpenParameters.pdf
    if (queryParameters != null) {
      String keywords = queryParameters.getKeywords();
      if (keywords != null && keywords.trim().length() > 0
          && "application/pdf".equals(attachmentDetail.getType(language))) {
        // Suppression des éventuelles quotes (ne sont pas acceptées)
        if (keywords.startsWith("\"")) {
          keywords = keywords.substring(1);
        }
        if (keywords.endsWith("\"")) {
          keywords = keywords.substring(0, keywords.length() - 1);
        }

        // Ajout du paramètre search à la fin de l'URL
        urlAttachment += "#search=%22" + keywords + "%22";
      }
    }

    return FileServerUtils.getApplicationContext() + urlAttachment;
  }

  private String getVersioningUrl(String documentId, String componentId)
      throws Exception {
    SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.getVersioningUrl",
        "root.MSG_GEN_PARAM_VALUE", "documentId = " + documentId
        + ", componentId = " + componentId);

    DocumentVersion version = versioningUtil.getLastPublicVersion(new DocumentPK(
        new Integer(documentId).intValue(), "useless", componentId));

    String urlVersioning = versioningUtil.getDocumentVersionURL(componentId,
        version.getLogicalName(), documentId, version.getPk().getId());

    return FileServerUtils.getApplicationContext() + urlVersioning;
  }

  /******************************************************************************************************************/
  /**
   * search from DomainsBar methods /
   ******************************************************************************************************************/
  public void setCurrentValue(Value value) {
    this.currentValue = value;
  }

  public Value getCurrentValue() {
    return this.currentValue;
  }

  public String getCurrentComponentId() throws PdcException {
    return getComponentId();
  }

  public boolean isShowOnlyPertinentAxisAndValues() {
    return showOnlyPertinentAxisAndValues;
  }

  public void setShowOnlyPertinentAxisAndValues(
      boolean showOnlyPertinentAxisAndValues) {
    this.showOnlyPertinentAxisAndValues = showOnlyPertinentAxisAndValues;
  }

  public void setCurrentComponentIds(List<String> componentList) {
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.setCurrentComponentIds()",
        "root.MSG_GEN_ENTER_METHOD", "# componentId = " + componentList.size());
    String componentId = null;
    for (int i = 0; componentList != null && i < componentList.size(); i++) {
      componentId = componentList.get(i);
      SilverTrace.debug("pdcPeas",
          "PdcSearchSessionController.setCurrentComponentIds()",
          "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);
    }
    this.componentList = componentList;
  }

  public List<String> getCurrentComponentIds() {
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.getCurrentComponentIds()",
        "root.MSG_GEN_ENTER_METHOD");
    String componentId = null;
    for (int i = 0; componentList != null && i < componentList.size(); i++) {
      componentId = componentList.get(i);
      SilverTrace.info("pdcPeas",
          "PdcSearchSessionController.getCurrentComponentIds()",
          "root.MSG_GEN_PARAM_VALUE", "componentId = " + componentId);
    }
    return this.componentList;
  }

  public void setSecondaryAxis(String secondAxis) {
    if (secondAxis.equals("NO")) {
      this.isSecondaryShowed = "NO";
    } else {
      this.isSecondaryShowed = "YES";
    }
  }

  public String getSecondaryAxis() {
    return this.isSecondaryShowed;
  }

  public List<AxisHeader> getAllAxis() throws PdcException {
    return getPdcBm().getAxis();
  }

  public List<AxisHeader> getPrimaryAxis() throws PdcException {
    return getPdcBm().getAxisByType("P");
  }

  public List<SearchAxis> getAxis(String viewType) throws PdcException {
    return getAxis(viewType, new AxisFilter());
  }

  public List<SearchAxis> getAxis(String viewType, AxisFilter filter) throws PdcException {
    if (componentList == null || componentList.size() == 0) {
      if (StringUtil.isDefined(getCurrentComponentId())) {
        return getPdcBm().getPertinentAxisByInstanceId(searchContext, viewType,
            getCurrentComponentId());
      }
      return new ArrayList<SearchAxis>();
    } else {
      if (isShowOnlyPertinentAxisAndValues()) {
        return getPdcBm().getPertinentAxisByInstanceIds(searchContext,
            viewType, componentList);
      } else {
        // we get all axis (pertinent or not) from a type P or S
        List<AxisHeader> axis = getPdcBm().getAxisByType(viewType);
        // we have to transform all axis (AxisHeader) into SearchAxis to make
        // the display into jsp transparent
        return transformAxisHeadersIntoSearchAxis(axis);
      }
    }
  }

  private List<SearchAxis> transformAxisHeadersIntoSearchAxis(List<AxisHeader> axis) {
    ArrayList<SearchAxis> transformedAxis = new ArrayList<SearchAxis>();
    AxisHeader ah;
    SearchAxis sa;
    try {
      for (int i = 0; i < axis.size(); i++) {
        ah = (AxisHeader) axis.get(i);
        sa = new SearchAxis(Integer.parseInt(ah.getPK().getId()), 0);
        // sa.setAxisName(ah.getName());
        sa.setAxis(ah);
        sa.setAxisRootId(Integer.parseInt(
            getPdcBm().getRoot(ah.getPK().getId()).getValuePK().getId()));
        sa.setNbObjects(1);
        transformedAxis.add(sa);
      }
    } catch (Exception e) {
    }
    return transformedAxis;
  }

  public List<Value> getDaughterValues(String axisId, String valueId)
      throws PdcException {
    return getDaughterValues(axisId, valueId, new AxisFilter());
  }

  public List<Value> getDaughterValues(String axisId, String valueId, AxisFilter filter)
      throws PdcException {
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.getDaughterValues()",
        "root.MSG_GEN_ENTER_METHOD", "axisId = " + axisId + ", valueId = "
        + valueId);
    List<Value> values = null;
    if (componentList == null || componentList.size() == 0) {
      values = getPdcBm().getPertinentDaughterValuesByInstanceId(searchContext,
          axisId, valueId, getCurrentComponentId());
    } else {
      if (isShowOnlyPertinentAxisAndValues()) {
        values = getPdcBm().getPertinentDaughterValuesByInstanceIds(
            searchContext, axisId, valueId, componentList);
      } else {
        values = setNBNumbersToOne(getPdcBm().getDaughters(axisId, valueId));
      }
    }
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.getDaughterValues()",
        "root.MSG_GEN_EXIT_METHOD");
    return values;
  }

  private List<Value> setNBNumbersToOne(List<Value> values) {
    for (int i = 0; i < values.size(); i++) {
      Value value = values.get(i);
      value.setNbObjects(1);
    }
    return values;
  }

  public List<Value> getFirstLevelAxisValues(String axisId) throws PdcException {
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.getFirstLevelAxisValues()",
        "root.MSG_GEN_ENTER_METHOD", "axisId = " + axisId);
    List<Value> result = null;
    if (componentList == null || componentList.size() == 0) {
      result = getPdcBm().getFirstLevelAxisValuesByInstanceId(searchContext,
          axisId, getCurrentComponentId());
    } else {
      result = getPdcBm().getFirstLevelAxisValuesByInstanceIds(searchContext,
          axisId, componentList);
    }
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.getFirstLevelAxisValues()",
        "root.MSG_GEN_EXIT_METHOD", "axisId = " + axisId);
    return result;
  }

  public SearchContext addCriteriaToSearchContext(SearchCriteria criteria) {
    this.searchContext.addCriteria(criteria);
    return getSearchContext();
  }

  public SearchContext removeCriteriaFromSearchContext(SearchCriteria criteria) {
    this.searchContext.removeCriteria(criteria);
    return getSearchContext();
  }

  public SearchContext removeCriteriaFromSearchContext(String axisId) {
    this.searchContext.removeCriteria(Integer.parseInt(axisId));
    return getSearchContext();
  }

  public void removeAllCriterias() {
    this.searchContext.clearCriterias();
  }

  public Axis getAxisDetail(String axisId) throws PdcException {
    return getAxisDetail(axisId, new AxisFilter());
  }

  public Axis getAxisDetail(String axisId, AxisFilter filter)
      throws PdcException {
    Axis axis = getPdcBm().getAxisDetail(axisId, filter);
    return axis;
  }

  public AxisHeader getAxisHeader(String axisId) throws PdcException {
    AxisHeader axisHeader = getPdcBm().getAxisHeader(axisId);
    return axisHeader;
  }

  public List getFullPath(String valueId, String treeId) throws PdcException {
    return getPdcBm().getFullPath(valueId, treeId);
  }

  public Value getAxisValue(String valueId, String treeId) throws PdcException {
    return getPdcBm().getAxisValue(valueId, treeId);
  }

  public Value getValue(String axisId, String valueId) throws PdcException {
    return getPdcBm().getValue(axisId, valueId);
  }

  public void setContainerPeas(ContainerPeas containerGivenPeasPDC) {
    containerPeasPDC = containerGivenPeasPDC;
  }

  public ContainerPeas getContainerPeas() {
    return containerPeasPDC;
  }

  public void setContentPeas(ContentPeas contentGivenPeasPDC) {
    contentPeasPDC = contentGivenPeasPDC;
  }

  public ContentPeas getContentPeas() {
    return contentPeasPDC;
  }

  /******************************************************************************************************************/
  /**
   * searchAndSelect methods /
   ******************************************************************************************************************/
  private boolean activeSelection = false;
  private Pdc m_pdc = null;

  public Pdc getPdc() {
    if (m_pdc == null) {
      m_pdc = new Pdc();
    }

    return m_pdc;
  }

  public void setSelectionActivated(boolean isSelectionActivated) {
    this.activeSelection = isSelectionActivated;
  }

  public boolean isSelectionActivated() {
    return this.activeSelection;
  }

  public void setSelectedSilverContents(List<GlobalSilverResult> silverContents) {
    getPdc().setSelectedSilverContents(silverContents);
  }

  public List<GlobalSilverResult> getSelectedSilverContents() {
    return getPdc().getSelectedSilverContents();
  }

  public List<String> getInstanceIdsFromComponentName(String componentName) {
    CompoSpace[] compoIds = getOrganizationController().getCompoForUser(
        getUserId(), componentName);
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.getInstanceIdsFromComponentName",
        "root.MSG_GEN_PARAM_VALUE", "compoIds = " + compoIds.toString());
    List<String> instanceIds = new ArrayList<String>();
    for (int i = 0; i < compoIds.length; i++) {
      instanceIds.add(((CompoSpace) compoIds[i]).getComponentId());
    }
    return instanceIds;
  }

  /******************************************************************************************************************/
  /**
   * Thesaurus methods /
   ******************************************************************************************************************/
  private ThesaurusManager thesaurus = new ThesaurusManager();
  private boolean activeThesaurus = false; // thesaurus actif
  private Jargon jargon = null;// jargon utilisé par l'utilisateur
  private Map<String, Collection<String>> synonyms = new HashMap<String, Collection<String>>();
  private static final int QUOTE_CHAR = new Integer('"').intValue();
  private static String[] KEYWORDS = null;
  private boolean isThesaurusEnableByUser = false;

  private synchronized boolean getActiveThesaurusByUser() throws PdcException, RemoteException {
    try {
      return getPersonalization().getThesaurusStatus();
    } catch (NoSuchObjectException nsoe) {
      initPersonalization();
      return getPersonalization().getThesaurusStatus();
    } catch (Exception e) {
      throw new PdcException("PdcSearchSessionController.getActiveThesaurus()",
          SilverpeasException.ERROR, "pdcPeas.EX_CANT_GET_ACTIVE_THESAURUS", "", e);
    }
  }

  public boolean getActiveThesaurus() throws PdcException, RemoteException {
    return this.isThesaurusEnableByUser;
  }

  public void initializeJargon() throws PdcException {
    try {
      Jargon theJargon = thesaurus.getJargon(getUserId());
      this.jargon = theJargon;
    } catch (ThesaurusException e) {
      throw new PdcException("PdcSearchSessionController.initializeJargon",
          SilverpeasException.ERROR, "pdcPeas.EX_CANT_INITIALIZE_JARGON", "", e);
    }
  }

  public Jargon getJargon() {
    return this.jargon;
  }

  private String getSynonymsQueryString(String queryString) {
    String synonymsQueryString = "";
    if (queryString == null || queryString.equals("") || !activeThesaurus) {
      synonymsQueryString = queryString;
    } else {
      StreamTokenizer st = new StreamTokenizer(new StringReader(queryString));
      st.resetSyntax();
      st.lowerCaseMode(true);
      st.wordChars('\u0000', '\u00FF');
      st.quoteChar('"');
      st.ordinaryChar('+');
      st.ordinaryChar('-');
      st.ordinaryChar(')');
      st.ordinaryChar('(');
      st.ordinaryChar(' ');
      try {
        String synonymsString = "";
        while (st.nextToken() != StreamTokenizer.TT_EOF) {
          String word = "";
          String specialChar = "";
          if ((st.ttype == StreamTokenizer.TT_WORD) || (st.ttype == QUOTE_CHAR)) {
            word = st.sval;
          } else {
            specialChar = String.valueOf((char) st.ttype);
          }
          if (!word.equals("")) {
            if (!isKeyword(word)) {
              synonymsString += "(\"" + word + "\"";
              Collection synonyms = getSynonym(word);
              Iterator it = synonyms.iterator();
              while (it.hasNext()) {
                String synonym = (String) it.next();
                synonymsString += " " + "\"" + synonym + "\"";
              }
              synonymsString += ")";
            } else // and or
            {
              synonymsString += word;
            }
          }
          if (!specialChar.equals("")) {
            synonymsString += specialChar;
          }
        }
        synonymsQueryString = synonymsString;
      } catch (IOException e) {
        throw new PdcPeasRuntimeException(
            "PdcSearchSessionController.setSynonymsQueryString",
            SilverpeasException.ERROR, "pdcPeas.EX_GET_SYNONYMS", e);
      }
    }
    return synonymsQueryString;
  }

  private Collection<String> getSynonym(String mot) {
    if (synonyms.containsKey(mot)) {
      return synonyms.get(mot);
    } else {
      try {
        Collection<String> synos = new ThesaurusManager().getSynonyms(mot, getUserId());
        synonyms.put(mot, synos);
        return synos;
      } catch (ThesaurusException e) {
        throw new PdcPeasRuntimeException(
            "PdcSearchSessionController.getSynonym", SilverpeasException.ERROR,
            "pdcPeas.EX_GET_SYNONYMS", e);
      }
    }
  }

  public Map<String, Collection<String>> getSynonyms() {
    if (activeThesaurus) {
      return synonyms;
    } else {
      return new HashMap<String, Collection<String>>();
    }
  }

  private boolean isKeyword(String mot) {
    String[] keyWords = getStopWords();
    for (int i = 0; i < keyWords.length; i++) {
      if (mot.toLowerCase().equals(keyWords[i].toLowerCase())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns an array of words which are not usually usefull for searching.
   */
  private String[] getStopWords() {
    if (KEYWORDS == null) {
      try {
        List<String> wordList = new ArrayList<String>();
        ResourceLocator resource = new ResourceLocator(
            "com.stratelia.webactiv.util.indexEngine.StopWords", getLanguage());
        Enumeration<String> stopWord = resource.getKeys();
        while (stopWord.hasMoreElements()) {
          wordList.add(stopWord.nextElement());
        }
        KEYWORDS = (String[]) wordList.toArray(new String[wordList.size()]);
      } catch (MissingResourceException e) {
        SilverTrace.warn("pdcPeas", "PdcSearchSessionController",
            "pdcPeas.MSG_MISSING_STOPWORDS_DEFINITION");
        return new String[0];
      }
    }
    return KEYWORDS;
  }

  /******************************************************************************************************************/
  /**
   * Glossary methods /
   ******************************************************************************************************************/
  private String showAllAxisInGlossary = null;
  private List<Value> axis_result = null;

  public boolean showAllAxisInGlossary() {
    if (showAllAxisInGlossary == null) {
      showAllAxisInGlossary = getSettings().getString("glossaryShowAllAxis");
    }
    return showAllAxisInGlossary.equals("true");
  }

  public MatchingIndexEntry[] glossarySearch(String query) throws PdcException {
    MatchingIndexEntry[] glossaryResults = null;
    if (query != null) {
      QueryDescription queryDescription = new QueryDescription(query);
      queryDescription.addSpaceComponentPair("transverse", "pdc");
      try {
        getSearchEngineBm().search(queryDescription);
        glossaryResults = getSearchEngineBm().getRange(0,
            getSearchEngineBm().getResultLength());
      } catch (NoSuchObjectException nsoe) {
        // an error occurs on searchEngine statefull EJB
        // interface is not null but the EJB is !
        // so we set interface to null and we launch again de search.
        searchEngine = null;
        glossaryResults = glossarySearch(query);
      } catch (Exception e) {
        throw new PdcException("PdcSearchSessionController.glossarySearch()",
            SilverpeasException.ERROR, "pdcPeas.EX_CAN_SEARCH_QUERY",
            "query : " + queryDescription.getQuery(), e);
      }
    }
    return glossaryResults;
  }

  public List<Value> getAxisValuesByFilter(String filter_by_name,
      String filter_by_description, boolean search_in_daughters,
      String instanceId) throws PdcException {
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.getAxisValuesByFilter",
        "root.MSG_GEN_PARAM_VALUE", "filter_by_name = " + filter_by_name
        + ", filter_by_description = " + filter_by_description
        + ", search_in_daughters = " + search_in_daughters);
    AxisFilter filter;
    if (filter_by_name != null && !"".equals(filter_by_name)) {
      filter_by_name = filter_by_name.replace('*', '%');
      filter = new AxisFilter(AxisFilter.NAME, filter_by_name);
    } else {
      filter = new AxisFilter();
    }

    if (filter_by_description != null && !"".equals(filter_by_description)) {
      filter_by_description = filter_by_description.replace('*', '%');
      filter.addCondition(AxisFilter.DESCRIPTION, filter_by_description);
    }

    List<AxisHeader> allAxis = new ArrayList<AxisHeader>();
    if (instanceId == null) {
      allAxis = getAllAxis();
    } else {
      allAxis = getUsedAxisHeaderByInstanceId(instanceId);
    }

    List<Value> axises = new ArrayList<Value>();

    for (int i = 0; i < allAxis.size(); i++) {
      AxisHeader sa = allAxis.get(i);
      String rootId = String.valueOf(sa.getRootId());
      List<Value> daughters = getPdcBm().getFilteredAxisValues(rootId, filter);
      axises.addAll(daughters);
    }
    return axises;
  }

  public List<AxisHeader> getUsedAxisHeaderByInstanceId(String instanceId)
      throws PdcException {
    List<UsedAxis> usedAxisList = getPdcBm().getUsedAxisByInstanceId(instanceId);
    AxisHeader axisHeader = null;
    UsedAxis usedAxis = null;
    String axisId = null;
    List<AxisHeader> allAxis = new ArrayList<AxisHeader>();
    // get all AxisHeader corresponding to usedAxis for this instance
    for (int i = 0; i < usedAxisList.size(); i++) {
      usedAxis = usedAxisList.get(i);
      axisId = new Integer(usedAxis.getAxisId()).toString();
      axisHeader = getAxisHeader(axisId);
      allAxis.add(axisHeader);
    }
    return allAxis;
  }

  public List<String> getUsedTreeIds(String instanceId) throws PdcException {
    List<AxisHeader> usedAxisHeaders = getUsedAxisHeaderByInstanceId(instanceId);
    List<String> usedTreeIds = new ArrayList<String>();
    AxisHeader axisHeader = null;
    String treeId = null;
    for (int i = 0; i < usedAxisHeaders.size(); i++) {
      axisHeader = usedAxisHeaders.get(i);
      if (axisHeader != null) {
        treeId = Integer.toString(axisHeader.getRootId());
        usedTreeIds.add(treeId);
      }
    }
    return usedTreeIds;
  }

  public Value getAxisValueAndFullPath(String valueId, String treeId)
      throws PdcException {
    Value value = getPdcBm().getAxisValue(valueId, treeId);
    if (value != null) {
      value.setPathValues(getFullPath(valueId, treeId));
    }
    return value;
  }

  public List<Axis> getUsedAxisByAComponentInstance(String instanceId)
      throws PdcException {
    List<UsedAxis> usedAxisList = getPdcBm().getUsedAxisByInstanceId(instanceId);
    List<Axis> axisList = new ArrayList<Axis>();
    UsedAxis usedAxis = null;
    for (int i = 0; i < usedAxisList.size(); i++) {
      usedAxis = usedAxisList.get(i);
      axisList.add(getAxisDetail(new Integer(usedAxis.getAxisId()).toString()));
    }
    return axisList;
  }

  public void setAxisResult(List<Value> result) {
    axis_result = result;
  }

  public List<Value> getAxisResult() {
    return axis_result;
  }

  /******************************************************************************************************************/
  /**
   * Interest Center methods /
   ******************************************************************************************************************/
  public int saveICenter(InterestCenter ic) throws PdcException {
    try {
      int userId = Integer.parseInt(getUserId());
      ic.setOwnerID(userId);
      return (new InterestCenterUtil()).createIC(ic);
    } catch (Exception e) {
      throw new PdcPeasRuntimeException(
          "PdcSearchSessionController.saveICenter", SilverpeasException.ERROR,
          "pdcPeas.EX_SAVE_IC", e);
    }
  }

  public ArrayList getICenters() {
    try {
      int id = Integer.parseInt(getUserId());
      return (new InterestCenterUtil()).getICByUserId(id);
    } catch (Exception e) {
      throw new PdcPeasRuntimeException(
          "PdcSearchSessionController.getICenters", SilverpeasException.ERROR,
          "pdcPeas.EX_GET_IC", e);
    }
  }

  public InterestCenter loadICenter(String icId) throws PdcException {
    try {
      int id = Integer.parseInt(icId);
      InterestCenter ic = (new InterestCenterUtil()).getICByID(id);
      getSearchContext().clearCriterias();
      List<SearchCriteria> criterias = ic.getPdcContext();
      for (SearchCriteria criteria : criterias) {
        searchContext.addCriteria(criteria);
      }
      return ic;
    } catch (Exception e) {
      throw new PdcPeasRuntimeException(
          "PdcSearchSessionController.loadICenter", SilverpeasException.ERROR,
          "pdcPeas.EX_LOAD_IC", e);
    }
  }

  /******************************************************************************************************************/
  /**
   * PDC Subscriptions methods /
   ******************************************************************************************************************/
  private PDCSubscription pdcSubscription;

  public PDCSubscription getPDCSubscription() {
    return pdcSubscription;
  }

  public void setPDCSubscription(PDCSubscription subscription) {
    this.pdcSubscription = subscription;
  }

  /******************************************************************************************************************/
  /**
   * UserPanel methods /
   ******************************************************************************************************************/
  public String initUserPanel() throws RemoteException {
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL");
    String hostSpaceName = getString("pdcPeas.SearchPage");
    String hostUrl = m_context + "/RpdcSearch/jsp/FromUserPanel";

    Selection sel = getSelection();
    sel.resetAll();
    sel.setHostSpaceName(hostSpaceName);
    sel.setGoBackURL(hostUrl);
    sel.setCancelURL(hostUrl);
    sel.setMultiSelect(false);
    sel.setPopupMode(false);
    sel.setSetSelectable(false);

    PairObject hostComponentName = new PairObject(getComponentLabel(), null);
    sel.setHostPath(null);
    sel.setHostComponentName(hostComponentName);
    sel.setFirstPage(Selection.FIRST_PAGE_BROWSE);

    return Selection.getSelectionURL(Selection.TYPE_USERS_GROUPS);
  }

  /*********************************************************************************************/
  /** SearchEngine primitives **/
  /*********************************************************************************************/
  public void buildComponentListWhereToSearch(String space, String component) {
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.buildComponentListWhereToSearch()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + space + ", component = "
        + component);

    componentList = new ArrayList<String>();

    if (space == null) {
      String[] allowedComponentIds = getUserAvailComponentIds();
      // Il n'y a pas de restriction sur un espace particulier
      for (int i = 0; i < allowedComponentIds.length; i++) {
        if (isSearchable(allowedComponentIds[i])) {
          componentList.add(allowedComponentIds[i]);
        }
      }
    } else {
      if (component == null) {
        // Restriction sur un espace. La recherche doit avoir lieu
        String[] asAvailCompoForCurUser = getOrganizationController().getAvailCompoIds(space,
            getUserId());
        for (int nI = 0; nI < asAvailCompoForCurUser.length; nI++) {
          if (isSearchable(asAvailCompoForCurUser[nI])) {
            componentList.add(asAvailCompoForCurUser[nI]);
          }
        }
      } else {
        componentList.add(component);
      }
    }
  }

  /**
   * This method allow user to search over multiple component selection
   * @param space
   * @param components a list of selected components
   */
  public void buildCustomComponentListWhereToSearch(String space, List<String> components) {
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.buildComponentListWhereToSearch()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + space + ", component = "
        + components);

    componentList = new ArrayList<String>();

    if (space == null) {
      String[] allowedComponentIds = getUserAvailComponentIds();
      // Il n'y a pas de restriction sur un espace particulier
      for (int i = 0; i < allowedComponentIds.length; i++) {
        if (isSearchable(allowedComponentIds[i])) {
          componentList.add(allowedComponentIds[i]);
        }
      }
    } else {
      if (components == null) {
        // Restriction sur un espace. La recherche doit avoir lieu
        String[] asAvailCompoForCurUser = getOrganizationController().getAvailCompoIds(space,
            getUserId());
        for (int nI = 0; nI < asAvailCompoForCurUser.length; nI++) {
          if (isSearchable(asAvailCompoForCurUser[nI])) {
            componentList.add(asAvailCompoForCurUser[nI]);
          }
        }
      } else {
        for (String component : components) {
          componentList.add(component);
        }
      }
    }
  }

  private boolean isSearchable(String componentId) {
    if (componentId.startsWith("silverCrawler")
        || componentId.startsWith("gallery")
        || componentId.startsWith("kmelia")) {
      boolean isPrivateSearch = "yes".equalsIgnoreCase(getOrganizationController().
          getComponentParameterValue(componentId, "privateSearch"));
      if (isPrivateSearch) {
        return false;
      } else {
        return true;
      }
    } else {
      return true;
    }
  }

  /**
   * Returns the list of allowed spaces/domains for the current user.
   */
  public List<SpaceInstLight> getAllowedSpaces() {
    List<SpaceInstLight> allowed = new ArrayList<SpaceInstLight>();

    String[] spaceIds = getOrganizationController().getAllSpaceIds(getUserId());

    // add each shared domains
    for (int nI = 0; nI < spaceIds.length; nI++) {
      String spaceId = spaceIds[nI];
      SpaceInstLight spaceInst = getOrganizationController().getSpaceInstLightById(spaceId);
      if (spaceInst != null && spaceInst.getFatherId().equals("0")) {
        allowed.add(spaceInst);

        String[] subSpaces = getOrganizationController().getAllSubSpaceIds(
            spaceId, getUserId());
        SpaceInstLight subSpaceInst = null;
        for (int j = 0; j < subSpaces.length; j++) {
          subSpaceInst = getOrganizationController().getSpaceInstLightById(
              subSpaces[j]);
          if (subSpaceInst != null) {
            allowed.add(subSpaceInst);
          }
        }
      }
    }
    return allowed;
  }

  /**
   * Returns the list of allowed components for the current user in the given space/domain.
   */
  public List<ComponentInstLight> getAllowedComponents(String space) {
    List<ComponentInstLight> allowedList = new ArrayList<ComponentInstLight>();
    if (space != null) {
      String[] asAvailCompoForCurUser = getOrganizationController().getAvailCompoIds(space,
          getUserId());
      for (int nI = 0; nI < asAvailCompoForCurUser.length; nI++) {
        ComponentInstLight componentInst = getOrganizationController().getComponentInstLight(
            asAvailCompoForCurUser[nI]);

        if (componentInst != null) {
          allowedList.add(componentInst);
        }
      }
    }
    return allowedList;
  }

  /**
   * Returns the label of the given domain/space
   */
  public String getSpaceLabel(String spaceId) {
    SpaceInstLight spaceInst = getOrganizationController().getSpaceInstLightById(spaceId);
    if (spaceInst != null) {
      return spaceInst.getName(getLanguage());
    } else {
      return spaceId;
    }
  }

  /**
   * Returns the label of the given component
   */
  public String getComponentLabel(String spaceId, String componentId) {
    ComponentInstLight componentInst = null;
    try {
      if (!spaceId.startsWith("user@") && !spaceId.equals("transverse")) {
        componentInst = getOrganizationController().getComponentInstLight(
            componentId);
      }
    } catch (Exception e) {
      SilverTrace.warn("pdcPeas",
          "searchEngineSessionController.getComponentLabel()",
          "pdcPeas.EXE_PREFIX_NULL", "spaceId= " + spaceId);
    }

    if (componentInst != null) {
      if (componentInst.getLabel(getLanguage()).length() > 0) {
        return componentInst.getLabel(getLanguage());
      } else {
        return componentInst.getName();
      }
    } else {
      return componentId;
    }
  }

  /*********************************************************************************************/
  /** AskOnce methods **/
  /*********************************************************************************************/
  private Vector<String[]> searchDomains = null; // All the domains available for search

  /**
   * Get the search domains available for search The search domains are contained in a Vector of
   * array of 3 String (String[0]=domain name, String[1]=domain url page, String[2]=internal Id)
   */
  public Vector<String[]> getSearchDomains() {
    if (searchDomains == null) {
      setSearchDomains();
    }

    return searchDomains;
  }

  /**
   * Set the search domains available for search The search domains are contained in a Vector of
   * array of 3 String (String[0]=domain name, String[1]=domain url page, String[2]=internal Id)
   */
  public void setSearchDomains() {
    ResourceLocator resource = null;
    Vector<String[]> domains = new Vector<String[]>();

    try {
      resource = new ResourceLocator(
          "com.stratelia.silverpeas.pdcPeas.settings.domains", "");
      if (resource != null) {
        int i = 1;
        boolean stop = false;
        while (!stop) {
          // Retrieve url that will be called if domain is selected
          String url = resource.getString("domain" + i + ".url", null);
          if (url == null) {
            stop = true;
          } else {
            // Retrieve localized domain's name
            String name = resource.getString("domain" + i + ".name_"
                + getLanguage(), null);

            // Retrieve domain internal id (used for specific treatment in
            // requestrouter)
            String id = resource.getString("domain" + i + ".id", null);

            // Retrieve default domain's name, if localized one not found
            if (name == null) {
              name = resource.getString("domain" + i + ".name");
            }

            // build an array and store it in Vector
            String[] domain = new String[3];
            domain[0] = name;
            domain[1] = url;
            domain[2] = id;
            domains.add(domain);
          }
          i++;
        }
      }
    } catch (MissingResourceException e) {
      SilverTrace.warn("pdcPeas",
          "searchEngineSessionController.setSearchDomains()",
          "pdcPeas.MSG_CANT_FIND_DOMAIN_PROPERTIES", e);
    }

    searchDomains = domains;
  }

  /*********************************************************************************************/
  /** Date primitives **/
  /*********************************************************************************************/
  public String getUrl(String urlBase, MatchingIndexEntry indexEntry) {
    return getUrl(urlBase, indexEntry.getComponent(), indexEntry.getParams(),
        indexEntry.getPageAndParams());
  }

  public String getUrl(String urlBase, String componentId, String params,
      String pageAndParams) {
    if (isNewComposant(componentId)) {
      ComponentInstLight componentInst = getOrganizationController().getComponentInstLight(
          componentId);
      return urlBase
          + URLManager.getNewComponentURL(componentInst.getDomainFatherId(),
          componentId)
          + params
          + URLManager.getEndURL(componentInst.getDomainFatherId(), componentId);
    } else {
      return urlBase + URLManager.getURL(null, componentId) + pageAndParams;
    }
  }

  private boolean isNewComposant(String componentId) {
    if (componentId.startsWith("whitePages")
        || componentId.startsWith("trucsAstuc")
        || componentId.startsWith("incidents")
        || componentId.startsWith("documentation")) {
      return true;
    } else {
      return false;
    }
  }

  private boolean isExportLicenseOK() {
    ResourceLocator resource = new ResourceLocator("license.license", "");
    String code = resource.getString("export");

    boolean validSequence = true;
    String serial = "643957685";
    try {
      for (int i = 0; i < 9 && validSequence; i++) {
        String groupe = code.substring(i * 3, i * 3 + 3);
        int total = 0;
        for (int j = 0; j < groupe.length(); j++) {
          String valeur = groupe.substring(j, j + 1);
          total += new Integer(valeur).intValue();
        }
        if (total != new Integer(serial.substring(i * 1, i * 1 + 1)).intValue()) {
          validSequence = false;
        }
      }
    } catch (Exception e) {
      validSequence = false;
    }
    return validSequence;
  }

  /*********************************************************************************************/
  /** Business objects primitives **/
  /*********************************************************************************************/
  private PdcBm pdcBm = null; // To retrieve items from PDC
  private SearchEngineBm searchEngine = null; // To retrieve items using

  // searchEngine
  private PdcBm getPdcBm() {
    if (pdcBm == null) {
      pdcBm = (PdcBm) new PdcBmImpl();
    }
    return pdcBm;
  }

  private SearchEngineBm getSearchEngineBm() throws PdcException {
    if (searchEngine == null) {
      try {
        SearchEngineBmHome home = (SearchEngineBmHome) EJBUtilitaire.getEJBObjectRef(
            JNDINames.SEARCHBM_EJBHOME,
            SearchEngineBmHome.class);
        searchEngine = home.create();
      } catch (Exception e) {
        throw new PdcException(
            "PdcSearchSessionController.getSearchEngineBm()",
            SilverpeasException.ERROR, "pdcPeas.EX_CANT_GET_SEARCH_ENGINE", e);
      }
    }
    return searchEngine;
  }

  /**
   * @return true if export is enabled
   */
  public boolean isExportEnabled() {
    return isExportEnabled;
  }

  /**
   * @return type of search (Simple, Advanced or Expert)
   */
  public int getSearchType() {
    return searchType;
  }

  /**
   * @param i - type of the current search
   */
  public void setSearchType(int i) {
    searchType = i;
  }

  private boolean isCompliantResult(GlobalSilverResult result) {
    String instanceId = result.getInstanceId();
    String type = result.getType();
    if (instanceId != null) {
      if (instanceId.startsWith("kmelia") || instanceId.startsWith("toolbox")) {
        if (!type.equals("Node")) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Cette methode construit un tableau contenant toutes les informations utiles à la construction
   * de la JSP resultat
   * @param results - un tableau de MatchingIndexEntry
   * @return un tableau contenant les informations relatives aux parametres d'entrée
   */
  private List<MatchingIndexEntry> filterMatchingIndexEntries(
      MatchingIndexEntry[] matchingIndexEntries) {
    if (matchingIndexEntries == null || matchingIndexEntries.length == 0) {
      return new ArrayList<MatchingIndexEntry>();
    }

    String title = "";
    String componentId = null;

    List<MatchingIndexEntry> results = new ArrayList<MatchingIndexEntry>();

    MatchingIndexEntry result = null;

    try {
      getSecurityIntf().enableCache();
    } catch (Exception e) {
      SilverTrace.info("pdcPeas",
          "PdcSearchSessionController.filterMatchingIndexEntries()",
          "pdcPeas.EX_CAN_SEARCH_QUERY", e);
    }

    for (int i = 0; i < matchingIndexEntries.length; i++) {
      result = matchingIndexEntries[i];

      // reinitialisation
      title = result.getTitle();
      componentId = result.getComponent();

      if (!isMatchingIndexEntryAvailable(result)) {
        continue;
      }

      // WARNING : LINE BELOW HAS BEEN ADDED TO NOT SHOW WYSIWYG ALONE IN SEARCH
      // RESULT PAGE
      if (title.endsWith("wysiwyg.txt")
          && (componentId.startsWith("kmelia") || componentId.startsWith("kmax"))) {
        continue;
      }

      results.add(result);
    }

    try {
      getSecurityIntf().disableCache();
    } catch (Exception e) {
      SilverTrace.info("pdcPeas",
          "PdcSearchSessionController.filterMatchingIndexEntries()",
          "pdcPeas.EX_CAN_SEARCH_QUERY", e);
    }

    return results;
  }

  /**
   * @return
   */
  public int getSearchScope() {
    return searchScope;
  }

  /**
   * @param i
   */
  public void setSearchScope(int i) {
    setIndexOfFirstResultToDisplay("0");
    searchScope = i;
  }

  public boolean isRefreshEnabled() {
    return isRefreshEnabled;
  }

  public boolean isXmlSearchVisible() {
    return "true".equals(getSettings().getString("XmlSearchVisible"));
  }

  public boolean isPertinenceVisible() {
    return getSettings().getBoolean("PertinenceVisible", true);
  }

  public PublicationTemplateImpl getXmlTemplate() {
    return xmlTemplate;
  }

  public PublicationTemplateImpl setXmlTemplate(String fileName)
      throws PdcPeasRuntimeException {
    PublicationTemplateImpl template = null;
    try {
      template = (PublicationTemplateImpl) PublicationTemplateManager.loadPublicationTemplate(
          fileName);
      this.xmlTemplate = template;
    } catch (PublicationTemplateException e) {
      throw new PdcPeasRuntimeException(
          "PdcSearchSessionController.setXmlTemplate()",
          SilverpeasException.ERROR, "pdcPeas.CANT_LOAD_TEMPLATE", e);
    }
    return template;
  }

  public DataRecord getXmlData() {
    return xmlData;
  }

  public void setXmlData(DataRecord xmlData) {
    this.xmlData = xmlData;
  }

  public String getSearchPage() {
    return searchPage;
  }

  public void setSearchPage(String searchPage) {
    if (StringUtil.isDefined(searchPage)) {
      this.searchPage = searchPage;
    } else {
      resetSearchPage();
    }
  }

  public void resetSearchPage() {
    searchPage = null;
  }

  public String getResultPage() {
    return resultPage;
  }

  public void setResultPage(String resultPage) {
    if (StringUtil.isDefined(resultPage)) {
      this.resultPage = resultPage;
    } else {
      resetResultPage();
    }
  }

  public void resetResultPage() {
    resultPage = null;
  }
  
  public String getResultPageId() {
    return resultPageId;
  }

  public void setResultPageId(String resultPageId) {
    if (StringUtil.isDefined(resultPageId)) {
      this.resultPageId = resultPageId;
    } else {
      resetResultPageId();
    }
  }

  public void resetResultPageId() {
    resultPageId = null;
  }

  /**
   * gets suggestions or spelling words if a search doesn't return satisfying results. A minimal
   * score trigger the suggestions search (0.5 by default)
   * @return array that contains suggestions.
   */
  public String[] getSpellingwords() {
    return spellingwords;
  }

  /**
   * @return the list of current global silver result
   */
  public List<GlobalSilverResult> getGlobalSR() {
    return globalSR;
  }

  /**
   * Set the list of current global silver result
   * @param globalSR : the current list of result
   */
  public void setGlobalSR(List<GlobalSilverResult> globalSR) {
    setExtraInfoToResultsToDisplay(globalSR);
    this.globalSR = globalSR;
    clearFilteredSR();
  }

  public void setFilteredSR(List<GlobalSilverResult> filteredSR) {
    this.filteredSR = filteredSR;
  }

  public List<GlobalSilverResult> getFilteredSR() {
    if (filteredSR.isEmpty()) {
      // filtered list has not been processed yet
      return globalSR;
    }
    return filteredSR;
  }

  public void clearFilteredSR() {
    filteredSR.clear();
  }

  public int getCurrentResultsDisplay() {
    return currentResultsDisplay;
  }

  public void setCurrentResultsDisplay(String param) {
    if (!StringUtil.isDefined(param)) {
      param = "0";
    }
    setCurrentResultsDisplay(Integer.parseInt(param));
  }

  public void setCurrentResultsDisplay(int currentResultsDisplay) {
    this.currentResultsDisplay = currentResultsDisplay;
  }

  /**
   * Gets the XML form field used to realize sorting
   * @return the xmlFormSortValue
   */
  public String getXmlFormSortValue() {
    return xmlFormSortValue;
  }

  /**
   * Sets the XML form field used to realize sorting
   * @param xmlFormSortValue the xmlFormSortValue to set
   */
  public void setXmlFormSortValue(String xmlFormSortValue) {
    this.xmlFormSortValue = xmlFormSortValue;
  }

  /**
   * Gets the keyword to retreive the implementation class name to realize sorting or filtering
   * @return the sortImplemtor
   */
  public String getSortImplemtor() {
    if (sortImplementor == null) {
      return Keys.defaultImplementor.value();
    }
    return sortImplementor;
  }

  /**
   * @param sortImplemtor the sortImplemtor to set
   */
  public void setSortImplemtor(String sortImplementor) {
    this.sortImplementor = sortImplementor;
  }
}
