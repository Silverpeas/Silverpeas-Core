/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
package com.stratelia.silverpeas.pdcPeas.control;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;

import org.silverpeas.attachment.AttachmentServiceFactory;
import org.silverpeas.attachment.model.SimpleDocument;
import org.silverpeas.attachment.model.SimpleDocumentPK;
import org.silverpeas.search.PlainSearchResult;
import org.silverpeas.search.SearchEngineFactory;
import org.silverpeas.search.searchEngine.model.AxisFilter;
import org.silverpeas.search.searchEngine.model.MatchingIndexEntry;
import org.silverpeas.search.searchEngine.model.QueryDescription;
import org.silverpeas.viewer.ViewerFactory;

import com.silverpeas.form.DataRecord;
import com.silverpeas.form.Field;
import com.silverpeas.form.FieldDisplayer;
import com.silverpeas.form.FieldTemplate;
import com.silverpeas.form.FormException;
import com.silverpeas.form.PagesContext;
import com.silverpeas.form.TypeManager;
import com.silverpeas.form.fieldType.TextFieldImpl;
import com.silverpeas.interestCenter.model.InterestCenter;
import com.silverpeas.interestCenter.util.InterestCenterUtil;
import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.silverpeas.publicationTemplate.PublicationTemplate;
import com.silverpeas.publicationTemplate.PublicationTemplateException;
import com.silverpeas.publicationTemplate.PublicationTemplateImpl;
import com.silverpeas.publicationTemplate.PublicationTemplateManager;
import com.silverpeas.thesaurus.ThesaurusException;
import com.silverpeas.thesaurus.control.ThesaurusManager;
import com.silverpeas.thesaurus.model.Jargon;
import com.silverpeas.util.EncodeHelper;
import com.silverpeas.util.FileUtil;
import com.silverpeas.util.ForeignPK;
import com.silverpeas.util.MimeTypes;
import com.silverpeas.util.StringUtil;
import com.silverpeas.util.i18n.I18NHelper;
import com.silverpeas.util.security.ComponentSecurity;

import com.stratelia.silverpeas.classifyEngine.Criteria;
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
import com.stratelia.silverpeas.pdcPeas.model.GlobalSilverResult;
import com.stratelia.silverpeas.pdcPeas.model.QueryParameters;
import com.stratelia.silverpeas.pdcPeas.vo.ExternalSPConfigVO;
import com.stratelia.silverpeas.pdcPeas.vo.Facet;
import com.stratelia.silverpeas.pdcPeas.vo.FacetEntryVO;
import com.stratelia.silverpeas.pdcPeas.vo.ResultFilterVO;
import com.stratelia.silverpeas.pdcPeas.vo.ResultGroupFilter;
import com.stratelia.silverpeas.pdcPeas.vo.SearchTypeConfigurationVO;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.webactiv.beans.admin.CompoSpace;
import com.stratelia.webactiv.beans.admin.ComponentInstLight;
import com.stratelia.webactiv.beans.admin.SpaceInstLight;
import com.stratelia.webactiv.beans.admin.UserDetail;
import com.stratelia.webactiv.beans.admin.indexation.UserIndexation;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.FileServerUtils;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasException;
import com.stratelia.webactiv.util.exception.UtilException;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;
import com.stratelia.webactiv.util.statistic.control.StatisticBm;
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
  private String searchPageId = null;
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
  private List<String> spellingwords = Collections.EMPTY_LIST;
  // Activate external search
  private boolean isEnableExternalSearch = false;
  private List<ExternalSPConfigVO> externalServers = null;
  private String curServerName = null;
  // Component search type
  public static final String ALL_DATA_TYPE = "0";
  private String dataType = ALL_DATA_TYPE;
  private List<SearchTypeConfigurationVO> dataSearchTypes = null;
  // forms fields facets from current results
  private Map<String, Facet> fieldFacets = null;
  // facets entry selected by the user
  private ResultFilterVO selectedFacetEntries = null;
  private boolean platformUsesPDC = false;
  private boolean includeUsers = false;

  public PdcSearchSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle,
      String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle,
        "com.stratelia.silverpeas.pdcPeas.settings.pdcPeasSettings");

    isExportEnabled = isExportLicenseOK();
    isRefreshEnabled = getSettings().getBoolean("EnableRefresh", true);

    try {
      isThesaurusEnableByUser = getActiveThesaurusByUser();
    } catch (Exception e) {
      isThesaurusEnableByUser = false;
    }

    searchContext.setUserId(getUserId());

    // Initialize external search
    isEnableExternalSearch = getSettings().getBoolean("external.search.enable", false);
    getExternalSPConfig();

    try {
      platformUsesPDC = !getPdcBm().getAxis().isEmpty();
    } catch (PdcException e) {
      SilverTrace.info("pdcPeas", "PdcSearchSessionController()", "root.MSG_GEN_ERROR", e);
    }

    includeUsers = getSettings().getBoolean("search.users.included", false);
  }

  /**
   * Retrieve the external silverpeas server configuration from pdcPeasSettings file<br> Using the
   * following keys<br> <ul> <li>external.search.server.CPT.name=ADEF</li>
   * <li>external.search.server.CPT.data.path=D:\\silverpeas\\data</li>
   * <li>external.search.server.CPT.component.filters=kmelia</li>
   * <li>external.search.server.CPT.url=http://monserveur/silverpeas</li> </ul> Where CPT is the
   * number of external servers starting from 1 to N
   */
  private void getExternalSPConfig() {
    if (isEnableExternalSearch) {
      curServerName = getSettings().getString("server.name");
      externalServers = new ArrayList<ExternalSPConfigVO>();
      String prefixKey = "external.search.server.";
      String nameKey = ".name";
      String pathKey = ".data.path";
      String urlKey = ".url";
      String filterKey = ".component.filters";
      int cptSrv = 1;
      String srvName = getSettings().getString(prefixKey + cptSrv + nameKey);
      while (StringUtil.isDefined(srvName)) {
        String path = getSettings().getString(prefixKey + cptSrv + pathKey);
        String url = getSettings().getString(prefixKey + cptSrv + urlKey);
        String components = getSettings().getString(prefixKey + cptSrv + filterKey);
        String[] componentsArray = components.split(",");
        externalServers.add(new ExternalSPConfigVO(srvName, cptSrv, path,
            Arrays.asList(componentsArray), url));
        // Loop increase
        cptSrv++;
        srvName = getSettings().getString(prefixKey + cptSrv + nameKey);
      }
    }
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

  /**
   * ***************************************************************************************************************
   */
  /**
   * PDC search methods (via DomainsBar) /
   *
   * @throws Exception
   * ****************************************************************************************************************
   */
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

  /**
   * ***************************************************************************************************************
   */
  /**
   * plain search methods /
   * ****************************************************************************************************************
   */
  public void setResults(List<GlobalSilverContent> globalSilverResults) {
    indexOfFirstResultToDisplay = 0;
    setLastResults(globalSilverResults);
  }

  public int getIndexOfFirstResultToDisplay() {
    return indexOfFirstResultToDisplay;
  }

  public void setIndexOfFirstResultToDisplay(String index) {
    this.indexOfFirstResultToDisplay = Integer.parseInt(index);
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
      this.queryParameters = new QueryParameters();
    }
    return this.queryParameters;
  }

  public void clearQueryParameters() {
    if (queryParameters != null) {
      queryParameters.clear();
    }
  }

  public MatchingIndexEntry[] search() throws
      org.silverpeas.search.searchEngine.model.ParseException {
    SilverTrace.info("pdcPeas", "PdcSearchSessionController.search()", "root.MSG_GEN_ENTER_METHOD");
    MatchingIndexEntry[] plainSearchResults = null;
    QueryDescription query = null;
    selectedFacetEntries = null;
    try {
      // spelling words initialization
      spellingwords = null;
      if (getQueryParameters() != null && (getQueryParameters().isDefined()
          || getQueryParameters().getXmlQuery() != null
          || StringUtil.isDefined(getQueryParameters().getSpaceId()) || isDataTypeDefined())) {
        query = getQueryParameters().getQueryDescription(getUserId(), "*");
        if (componentList == null) {
          buildComponentListWhereToSearch(null, null);
        }

        for (String curComp : componentList) {
          if (isDataTypeSearch(curComp)) {
            query.addComponent(curComp);
          }
        }

        if (getQueryParameters().getSpaceId() == null && !isDataTypeDefined()) {
          // c'est une recherche globale, on cherche si le pdc et les composants
          // personnels.
          query.addSpaceComponentPair(null, "user@" + getUserId() + "_mailService");
          query.addSpaceComponentPair(null, "user@" + getUserId() + "_todo");
          query.addSpaceComponentPair(null, "user@" + getUserId() + "_agenda");
          query.addSpaceComponentPair(null, "pdc");
          // pour retrouver les espaces et les composants
          query.addSpaceComponentPair(null, "Spaces");
          query.addSpaceComponentPair(null, "Components");
          if (includeUsers) {
            query.addSpaceComponentPair(null, "users");
          }
        } else if (getQueryParameters().getSpaceId() != null) {
          // used for search by space without keywords
          query.setSearchBySpace(true);
        } else if (isDataTypeDefined()) {
          // used for search by component type without keywords
          query.setSearchByComponentType(true);
        }

        // Add external components into QueryDescription
        addExternalComponents(query);

        SilverTrace.info("pdcPeas", "PdcSearchSessionController.search()",
            "root.MSG_GEN_PARAM_VALUE", "# component = " + query.getSpaceComponentPairSet().size());

        String originalQuery = query.getQuery();
        query.setQuery(getSynonymsQueryString(originalQuery));

        PlainSearchResult searchResult = SearchEngineFactory.getSearchEngine().search(query);
        plainSearchResults = searchResult.getEntries().toArray(new MatchingIndexEntry[searchResult.
            getEntries().size()]);
        // spelling words
        if (getSettings().getBoolean("enableWordSpelling", false)) {
          spellingwords = searchResult.getSpellingWords();
        }

      }
    } catch (ParseException e) {
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

  /**
   * Main method to add external components to a query description object
   *
   * @param query the query description used to build Lucene query
   */
  private void addExternalComponents(QueryDescription query) {
    if (isEnableExternalSearch && !query.isSearchBySpace()) {
      for (ExternalSPConfigVO extServerCfg : this.externalServers) {
        // Loop on each directory in order to add all the external components
        List<String> filteredComponents = extServerCfg.getComponents();
        if (filteredComponents != null && !filteredComponents.isEmpty()) {
          browseExternalServerDirectory(query, extServerCfg);
        }
      }
    }
  }

  /**
   * This method retrieve the list of subfolders from external server configuration path. Then it
   * filters each directory using the external list of authorized components.
   *
   * @param query the QueryDescription where adding new ExternalComponent search
   * @param extServerCfg the external server configuration read from properties file
   */
  private void browseExternalServerDirectory(QueryDescription query, ExternalSPConfigVO extServerCfg) {
    Collection<File> listDir = new ArrayList<File>();
    try {
      listDir =
          FileFolderManager.getAllSubFolder(extServerCfg.getDataPath() + File.separator
          + "index");
    } catch (UtilException e) {
      SilverTrace.error("pdcPeas", "PdcSearchSessionController.browseExternalServerDirectory()",
          "I/O exception folder = " + extServerCfg.getDataPath() + File.separator + "index", e);
    }
    if (!listDir.isEmpty()) {
      for (File file : listDir) {
        filterExternalComponents(query, extServerCfg, file);
      }
    }
  }

  /**
   * Filter the list of external component for Lucene search purpose
   *
   * @param query : the query description used to build lucene query
   * @param extServerCfg : the external server configuration
   * @param file : current external directory
   */
  private void filterExternalComponents(QueryDescription query, ExternalSPConfigVO extServerCfg,
      File file) {
    String fileName = file.getName();
    List<String> filteredComponents = extServerCfg.getComponents();
    for (String authorizedComp : filteredComponents) {
      if (fileName.indexOf(authorizedComp) >= 0 && isDataTypeSearch(fileName)) {
        query.addExternalComponents(extServerCfg.getName(), fileName, extServerCfg.getDataPath(),
            extServerCfg.getUrl());
      }
    }
  }

  public boolean isMatchingIndexEntryAvailable(MatchingIndexEntry mie) {
    // Do not filter and check external components
    if (isEnableExternalSearch && isExternalComponent(mie.getServerName())) {
      // Fitler only Publication and Node data
      String objectType = mie.getObjectType();
      if ("Versioning".equals(objectType) || "Publication".equals(objectType)
          || "Node".equals(objectType)) {
        return true;
      } else {
        return false;
      }
    }

    String componentId = mie.getComponent();
    if (componentId.startsWith("kmelia")) {
      try {
        return getSecurityIntf().isObjectAvailable(componentId, getUserId(),
            mie.getObjectId(), mie.getObjectType());
      } catch (Exception e) {
        SilverTrace.info("pdcPeas", "PdcSearchSessionController.isMatchingIndexEntryAvailable()",
            "pdcPeas.EX_CAN_SEARCH_QUERY", "componentId = " + componentId + ", objectId = "
            + mie.getObjectId() + ", objectType = " + mie.getObjectType(), e);
      }
    }
    // contrôle des droits sur les espaces et les composants
    String objectType = mie.getObjectType();
    if ("Space".equals(objectType)) {
      // check if space is allowed to current user
      return getOrganisationController().isSpaceAvailable(mie.getObjectId(), getUserId());
    } else if ("Component".equals(objectType)) {
      // check if component is allowed to current user
      return getOrganisationController().isComponentAvailable(mie.getObjectId(), getUserId());
    } else if (UserIndexation.OBJECT_TYPE.equals(objectType)
        && GeneralPropertiesManager.getDomainVisibility() != GeneralPropertiesManager.DVIS_ALL) {
      // visibility between domains is limited, check found user domain against current user domain
      String userId = mie.getObjectId();
      UserDetail userFound = getUserDetail(userId);
      if (GeneralPropertiesManager.getDomainVisibility() == GeneralPropertiesManager.DVIS_ONE) {
        if ("0".equals(getUserDetail().getDomainId())) {
          // current user of default domain can see all users
          return true;
        } else {
          // current user of other domains can see only users of his domain
          return userFound.getDomainId().equals(getUserDetail().getDomainId());
        }
      } else if (GeneralPropertiesManager.getDomainVisibility()
          == GeneralPropertiesManager.DVIS_EACH) {
        // user found must be in same domain of current user
        return userFound.getDomainId().equals(getUserDetail().getDomainId());
      }
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
   *
   * @return new ResultGroupFilter object which contains all data to filter result
   */
  public ResultGroupFilter getResultGroupFilter() {
    // Return object declaration
    ResultGroupFilter res = new ResultGroupFilter();

    // Retrieve current list of results
    List<GlobalSilverResult> results = getFilteredSR();

    Facet authorFacet = new Facet("author", getString("pdcPeas.facet.author"));
    Facet componentFacet = new Facet("component", getString("pdcPeas.facet.service"));
    Facet dataTypeFacet = new Facet("datatype", getString("pdcPeas.facet.datatype"));

    // key is the fieldName
    Map<String, Facet> fieldFacetsMap = new HashMap<String, Facet>();

    if (results != null) {
      // Retrieve the black list component (we don't need to filter data on it)
      List<String> blackList = getFacetBlackList();

      // Loop on each result
      for (GlobalSilverResult result : results) {
        if (isEnableExternalSearch) {
          if (result.getIndexEntry() != null) {
            String extSrvName = result.getIndexEntry().getServerName();
            if ((StringUtil.isDefined(extSrvName) && isExternalComponent(extSrvName))
                || !StringUtil.isDefined(extSrvName)) {
              continue;
            }
          }
        }

        // manage "author" facet
        processFacetAuthor(authorFacet, result);

        // manage "component" facet
        processFacetComponent(componentFacet, result, blackList);

        // manage "datatype" facet
        processFacetDatatype(dataTypeFacet, result);

        // manage forms fields facets
        processFacetsFormField(fieldFacetsMap, result);
      }
    }

    // Fill result filter with current result values
    res.setAuthorFacet(authorFacet);
    res.setComponentFacet(componentFacet);
    res.setDatatypeFacet(dataTypeFacet);
    res.setFormFieldFacets(new ArrayList<Facet>(fieldFacetsMap.values()));

    // sort facets entries descending
    res.sortFacetsEntries();

    this.fieldFacets = fieldFacetsMap;

    return res;
  }

  private void processFacetAuthor(Facet facet, GlobalSilverResult result) {
    String authorName = result.getCreatorName();
    String authorId = result.getUserId();
    if (StringUtil.isDefined(authorId) && StringUtil.isDefined(authorName)) {
      FacetEntryVO facetEntry = new FacetEntryVO(authorName, authorId);
      if (getSelectedFacetEntries() != null) {
        if (authorId.equals(getSelectedFacetEntries().getAuthorId())) {
          facetEntry.setSelected(true);
        }
      }
      facet.addEntry(facetEntry);
    }
  }

  private void processFacetDatatype(Facet facet, GlobalSilverResult result) {
    String instanceId = result.getInstanceId();
    String type = result.getType();
    if (StringUtil.isDefined(type)) {
      SearchTypeConfigurationVO searchType = getSearchType(instanceId, type);
      if (searchType != null) {
        FacetEntryVO facetEntry = new FacetEntryVO(searchType.getName(), String.valueOf(searchType.
            getConfigId()));
        if (getSelectedFacetEntries() != null) {
          if (String.valueOf(searchType.getConfigId()).equals(getSelectedFacetEntries().
              getDatatype())) {
            facetEntry.setSelected(true);
          }
        }
        facet.addEntry(facetEntry);
      }
    }
  }

  private void processFacetComponent(Facet facet, GlobalSilverResult result, List<String> blackList) {
    String instanceId = result.getInstanceId();
    String location = result.getLocation();
    String type = result.getType();
    if (!blackList.contains(type) && StringUtil.isDefined(location)) {
      String appLocation = location.substring(location.lastIndexOf('/') + 1);
      FacetEntryVO facetEntry = new FacetEntryVO(appLocation, instanceId);
      if (getSelectedFacetEntries() != null) {
        if (instanceId.equals(getSelectedFacetEntries().getComponentId())) {
          facetEntry.setSelected(true);
        }
      }
      facet.addEntry(facetEntry);
    }
  }

  private void processFacetsFormField(Map<String, Facet> fieldFacetsMap, GlobalSilverResult result) {
    Map<String, String> fieldsForFacets = result.getFormFieldsForFacets();
    if (fieldsForFacets != null && !fieldsForFacets.isEmpty()) {
      // there is at least one field used to generate a facet
      Set<String> facetIds = fieldsForFacets.keySet();
      for (String facetId : facetIds) {
        String[] splitted = getFormNameAndFieldName(facetId);
        String formName = splitted[0];
        String fieldName = splitted[1];
        if (!isFieldStillAFacet(formName, fieldName)) {
          // this field is no more a facet
          continue;
        }
        Facet facet = null;
        if (!fieldFacetsMap.containsKey(facetId)) {
          // new facet, adding it to result list
          try {
            facet = new Facet(facetId, getFieldLabel(formName, splitted[1]));
          } catch (Exception e) {
            SilverTrace.error("pdcPeas", "PdcSearchSessionController.processFacetsFormField()",
                "pdcPeas.CANT_GET_FACET_LABEL", e);
          }
          fieldFacetsMap.put(facetId, facet);
        } else {
          // facet already initialized
          facet = fieldFacetsMap.get(facetId);
        }
        if (facet != null) {
          String fieldValueKey = fieldsForFacets.get(facetId);
          String fieldValueLabel = fieldValueKey;
          try {
            fieldValueLabel = getFieldValue(formName, fieldName, fieldValueKey);
          } catch (Exception e) {
            SilverTrace.error("pdcPeas", "PdcSearchSessionController.processFacetsFormField()",
                "pdcPeas.CANT_GET_FACET_ENTRY_LABEL", e);
          }
          FacetEntryVO entry = new FacetEntryVO(fieldValueLabel, fieldValueKey);
          // check if this entry have been selected
          if (getSelectedFacetEntries() != null) {
            String selectedEntry =
                getSelectedFacetEntries().getFormFieldSelectedFacetEntry(facet.getId());
            if (StringUtil.isDefined(selectedEntry) && selectedEntry.equals(fieldValueKey)) {
              entry.setSelected(true);
            }
          }
          facet.addEntry(entry);
        }
      }
    }
  }

  public Map<String, Facet> getFieldFacets() {
    return fieldFacets;
  }

  private String getFieldLabel(String formName, String fieldName)
      throws PublicationTemplateException, FormException {
    PublicationTemplate form =
        PublicationTemplateManager.getInstance().loadPublicationTemplate(formName);
    return form.getRecordTemplate().getFieldTemplate(fieldName).getLabel(getLanguage());
  }

  private String getFieldValue(String formName, String fieldName, String fieldValue)
      throws PublicationTemplateException, FormException {
    PublicationTemplate form =
        PublicationTemplateManager.getInstance().loadPublicationTemplate(formName);
    FieldTemplate fieldTemplate = form.getRecordTemplate().getFieldTemplate(fieldName);
    FieldDisplayer fieldDisplayer =
        TypeManager.getInstance().getDisplayer(fieldTemplate.getTypeName(), "simpletext");
    StringWriter sw = new StringWriter();
    PrintWriter out = new PrintWriter(sw);
    PagesContext pageContext = new PagesContext();
    pageContext.setLanguage(getLanguage());
    Field field = new TextFieldImpl();
    field.setValue(fieldValue);
    fieldDisplayer.display(out, field, fieldTemplate, pageContext);
    return sw.toString();
  }

  private String[] getFormNameAndFieldName(String compressedFieldName) {
    String formName = compressedFieldName.substring(0, compressedFieldName.indexOf("$$"));
    if (!formName.endsWith(".xml")) {
      formName += ".xml";
    }
    String fieldName = compressedFieldName.substring(compressedFieldName.indexOf("$$") + 2);
    return new String[]{formName, fieldName};
  }

  private boolean isFieldStillAFacet(String formName, String fieldName) {
    PublicationTemplate form;
    try {
      form = PublicationTemplateManager.getInstance().loadPublicationTemplate(formName);
      return form.getFieldsForFacets().contains(fieldName);
    } catch (PublicationTemplateException e) {
      SilverTrace.error("pdcPeas", "PdcSearchSessionController.isFieldStillAFacet()",
          "pdcPeas.CANT_GET_FIELDS_FOR_FACETS", e);
    }
    return false;
  }

  public List<GlobalSilverResult> processResultsToDisplay(MatchingIndexEntry[] indexEntries)
      throws Exception {
    // Tous les résultats
    List<GlobalSilverResult> results =
        matchingIndexEntries2GlobalSilverResults(filterMatchingIndexEntries(indexEntries));
    setGlobalSR(results, true);
    return results;
  }

  public List<GlobalSilverResult> processResultsToDisplay(List<GlobalSilverContent> silverContents)
      throws Exception {
    // Tous les résultats
    List<GlobalSilverResult> results = globalSilverContents2GlobalSilverResults(silverContents);
    setGlobalSR(results, true);

    // case of PDC results : pertinence sort is not applicable
    // sort by updateDate desc
    setSortValue(5);
    setSortOrder(SORT_ORDER_DESC);
    return results;
  }

  public List<GlobalSilverResult> getSortedResultsToDisplay(int sortValue, String sortOrder,
      String xmlFormSortValue, String sortType, ResultFilterVO filter) {

    // Tous les résultats
    List<GlobalSilverResult> results = getGlobalSR();

    if (results != null && getSelectedSilverContents() != null) {
      for (int i = 0; i < results.size(); i++) {
        GlobalSilverResult result = results.get(i);
        if (getSelectedSilverContents().contains(result)) {
          result.setSelected(true);
        } else {
          result.setSelected(false);
        }
      }
    }

    // Tri de tous les résultats
    // Gets a SortResult implementation to realize the sorting and/or filtering results
    SortResults sortResults = SortResultsFactory.getSortResults(sortType);
    sortResults.setPdcSearchSessionController(this);
    String sortValString;
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
    List<GlobalSilverResult> sortedResults = sortResults.execute(results, sortOrder, sortValString,
        getLanguage());
    if (filter != null && !filter.isEmpty()) {
      // Check Author filter
      return filterResult(filter, sortedResults);
    } else {
      // Put the full result list in session
      setGlobalSR(sortedResults, false);

      // get the part of results to display
      return sortedResults.subList(getIndexOfFirstResultToDisplay(), getLastIndexToDisplay());
    }
  }

  private void setPopularityToResults() {
    List<GlobalSilverResult> results = getGlobalSR();
    StatisticBm statisticBm = getStatisticBm();
    ForeignPK pk = new ForeignPK("unknown");
    for (GlobalSilverResult result : results) {
      if (isPopularityCompliant(result)) {
        pk.setComponentName(result.getInstanceId());
        pk.setId(result.getId());
        int nbAccess = statisticBm.getCount(pk, 1, "Publication");
        result.setHits(nbAccess);
      }
    }
  }

  private boolean isPopularityCompliant(GlobalSilverResult gsr) {
    return (gsr != null && (StringUtil.isDefined(gsr.getInstanceId()) && (gsr.getInstanceId().
        startsWith("kmelia") || gsr.getInstanceId().startsWith("kmax") || gsr.getInstanceId().
        startsWith("toolbox"))) && ("Publication".equals(gsr.getType()) || (StringUtil.isDefined(
        gsr.getURL()) && gsr.getURL().indexOf("Publication") != -1)));
  }

  public StatisticBm getStatisticBm() {
    try {
      return EJBUtilitaire.getEJBObjectRef(JNDINames.STATISTICBM_EJBHOME, StatisticBm.class);
    } catch (Exception e) {
      throw new StatisticRuntimeException("PdcSearchSessionController.getStatisticBm()",
          SilverpeasException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * This method filter current array of global silver result with filter parameters
   *
   * @param filter
   * @param listGSR
   * @return list of GlobalSilverResult to display
   */
  private List<GlobalSilverResult> filterResult(ResultFilterVO filter,
      List<GlobalSilverResult> listGSR) {
    List<GlobalSilverResult> sortedResults = new ArrayList<GlobalSilverResult>();
    List<GlobalSilverResult> sortedResultsToDisplay;

    SilverTrace.debug("pdcPeas", "PdcSearchSessionController.filterResult",
        "root.MSG_GEN_ENTER_METHOD", "filter = " + filter.toString());

    String authorFilter = filter.getAuthorId();
    boolean filterAuthor = StringUtil.isDefined(authorFilter);

    // Check Component filter
    String componentFilter = filter.getComponentId();
    boolean filterComponent = StringUtil.isDefined(componentFilter);

    String datatypeFilter = filter.getDatatype();
    boolean filterDatatype = StringUtil.isDefined(datatypeFilter);

    boolean filterFormFields = !filter.isSelectedFormFieldFacetsEmpty();

    List<String> blackList = getFacetBlackList();

    for (GlobalSilverResult gsResult : listGSR) {
      if (!blackList.contains(gsResult.getType())) {
        String gsrUserId = gsResult.getUserId();
        String gsrInstanceId = gsResult.getInstanceId();
        boolean visible = true;

        // check author facet
        if (filterAuthor && !gsrUserId.equals(authorFilter)) {
          visible = false;
        }

        // check component facet
        if (visible && filterComponent && !gsrInstanceId.equals(componentFilter)) {
          visible = false;
        }

        // check datatype facet
        if (visible && filterDatatype) {
          SearchTypeConfigurationVO searchType = getSearchType(gsrInstanceId, gsResult.getType());
          if (searchType == null || !datatypeFilter.equals(String.valueOf(searchType.getConfigId()))) {
            visible = false;
          }
        }

        // check form field facets
        Map<String, String> gsrFormFieldsForFacets = gsResult.getFormFieldsForFacets();
        if (visible && filterFormFields) {
          if (gsrFormFieldsForFacets == null) {
            // search result does not contain stored form fields
            visible = false;
          } else {
            if (!gsrFormFieldsForFacets.isEmpty()) {
              Map<String, String> selectedFacetEntries = filter.getFormFieldSelectedFacetEntries();
              for (Map.Entry<String, String> facet : selectedFacetEntries.entrySet()) {
                // get stored value relative to given facet
                String resultFieldValue = gsrFormFieldsForFacets.get(facet.getKey());
                SilverTrace.debug("pdcPeas", "PdcSearchSessionController.filterResult",
                    "root.MSG_GEN_PARAM_VALUE", "For '" + gsResult.getName() + "' and facet '"
                    + facet.getKey() + "', result stored " + resultFieldValue);
                // visible if stored value is equals to selected facet entry
                visible = facet.getValue().equalsIgnoreCase(resultFieldValue);
              }
            }
          }
        }

        if (visible) {
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

  /**
   * Enhance information from result before sending them to the view
   *
   * @param results a list of GlobalSilverResult to enhance
   */
  private void setExtraInfoToResultsToDisplay(List<GlobalSilverResult> results) {
    String titleLink = "";
    String downloadLink = "";
    String resultType = "";
    String underLink;
    // activate the mark as read functionality on results list
    String markAsReadJS = "";
    boolean isEnableMarkAsRead = getSettings().getBoolean("enableMarkAsRead", false);

    for (int r = 0; r < results.size(); r++) {
      GlobalSilverResult result = results.get(r);
      result.setResultId(r);
      MatchingIndexEntry indexEntry = result.getIndexEntry();
      if (indexEntry != null) {
        resultType = indexEntry.getObjectType();
        if (!StringUtil.isDefined(resultType)) {
          resultType = "";
        }
      }
      String componentId = result.getInstanceId();
      downloadLink = null;
      // create the url part to activate the mark as read functionality
      if (isEnableMarkAsRead) {
        markAsReadJS = "markAsRead('" + r + "');";
      }
      if ("Versioning".equals(resultType)) {
        // Added to be compliant with old indexing method
        resultType = "Publication";
      }

      // Declare if it's an internal server search
      boolean isInternalSearch = true;

      // Add only when External Search is enabled
      // Check if external search exists
      if (isEnableExternalSearch && indexEntry != null) {
        // build external search url location
        String serverName = indexEntry.getServerName();

        if (StringUtil.isDefined(serverName) && isExternalComponent(serverName)) {
          isInternalSearch = false;
          titleLink = buildExternalServerURL(resultType, markAsReadJS, indexEntry, serverName);
        } else if (!StringUtil.isDefined(serverName)) {
          isInternalSearch = false;
          titleLink = "javascript:showExternalSearchError();";
        }
      }

      if (isInternalSearch) {
        if (resultType.startsWith("Attachment")) {
          if (!componentId.startsWith("webPages")) {
            try {
              downloadLink =
                  getAttachmentUrl(indexEntry.getObjectType(), indexEntry.getComponent(), result);
            } catch (Exception e) {
              SilverTrace.warn("pdcPeas",
                  "searchEngineSessionController.setExtraInfoToResultsToDisplay()",
                  "pdcPeas.MSG_CANT_GET_DOWNLOAD_LINK", e);
            }
            underLink = getUrl(URLManager.getApplicationURL(), indexEntry);
            int iStart = underLink.indexOf("Attachment");
            int iEnd = underLink.indexOf('&', iStart);
            underLink = underLink.substring(0, iStart) + "Publication" + underLink.substring(iEnd,
                underLink.length());
            StringBuilder titleLinkBuilder = new StringBuilder(256);
            titleLinkBuilder.append("javascript:").append(markAsReadJS).append(" window.open('").
                append(EncodeHelper.javaStringToJsString(downloadLink)).append(
                "');jumpToComponent('").append(componentId).
                append("');document.location.href='").append(EncodeHelper.javaStringToJsString(
                underLink)).append("&FileOpened=1';");
            titleLink = titleLinkBuilder.toString();
          } else {
            ComponentInstLight componentInst = getOrganisationController().getComponentInstLight(
                componentId);
            if (componentInst != null) {
              String title = componentInst.getLabel(getLanguage());
              result.setTitle(title);
              result.setType("Wysiwyg");
              underLink = URLManager.getSimpleURL(URLManager.URL_COMPONENT, componentId);
              titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId
                  + "');document.location.href='" + underLink + "';";
            }
          }
        } else if (resultType.startsWith("Versioning")) {
          try {
            downloadLink = getVersioningUrl(resultType.substring(10), componentId, result);
          } catch (Exception e) {
            SilverTrace.error("pdcPeas",
                "searchEngineSessionController.setExtraInfoToResultsToDisplay()",
                "pdcPeas.MSG_CANT_GET_DOWNLOAD_LINK", e);
          }
          underLink = getUrl(URLManager.getApplicationURL(), indexEntry);
          int iStart = underLink.indexOf("Versioning");
          int iEnd = underLink.indexOf('&', iStart);
          underLink = underLink.substring(0, iStart) + "Publication" + underLink.substring(iEnd,
              underLink.length());
          titleLink = buildTitleLink(markAsReadJS, downloadLink, componentId, underLink, true);
        } else if (resultType.equals("LinkedFile")) {
          // open the linked file inside a popup window
          downloadLink =
              FileServerUtils.getUrl(indexEntry.getTitle(), indexEntry.getObjectId(),
              FileUtil.getMimeType(indexEntry.getTitle()));
          // window opener is reloaded on the main page of the component
          underLink = URLManager.getApplicationURL() + URLManager.getURL("useless", componentId)
              + "Main";
          titleLink = buildTitleLink(markAsReadJS, downloadLink, componentId, underLink, false);
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
          titleLink = URLManager.getApplicationURL() + URLManager.getURL(resultType) + indexEntry
              .getPageAndParams();
        } else if (UserIndexation.OBJECT_TYPE.equals(resultType)) {
          UserDetail userDetail = getUserDetail(indexEntry.getPK().getObjectId());
          if (userDetail != null) {
            result.setThumbnailURL(userDetail.getAvatar());
          }
          titleLink = "javascript:" + markAsReadJS + " viewUserProfile('" + indexEntry.getPK().
              getObjectId() + "');";
        } else {
          titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId
              + "');";
          if (indexEntry != null) {
            titleLink += "document.location.href='" + getUrl(URLManager.getApplicationURL(),
                indexEntry) + "';";
          } else {
            titleLink +=
                "document.location.href='"
                + getUrl(URLManager.getApplicationURL(), componentId, result.getURL())
                + "';";
          }
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

  /**
   * Only called when isEnableExternalSearch is activated. Build an external link using Silverpeas
   * permalink
   *
   * @see URLManager.getSimpleURL
   * @param resultType the result type
   * @param markAsReadJS javascript string to mark this result as read
   * @param indexEntry the current indexEntry
   * @param serverName the server name string
   * @return a string which represents an external server URL
   */
  private String buildExternalServerURL(String resultType, String markAsReadJS,
      MatchingIndexEntry indexEntry, String serverName) {
    StringBuilder extURLSB = new StringBuilder();
    for (ExternalSPConfigVO extSrv : externalServers) {
      if (serverName.equalsIgnoreCase(extSrv.getName())) {
        extURLSB.append("javascript:").append(markAsReadJS).append(" ");
        extURLSB.append("window.open('").append(extSrv.getUrl());
        // Retrieve the URLManager type
        int type = 0;
        String objectId = "";
        String compId = indexEntry.getComponent();
        if ("Publication".equals(resultType)) {
          // exemple http://server/silverpeas/Publication/ID_PUBLI
          type = URLManager.URL_PUBLI;
          objectId = indexEntry.getObjectId();
        } else if ("Node".equals(resultType)) {
          // exemple http://server/silverpeas/Topic/ID_TOPIC?ComponentId=ID_COMPONENT
          type = URLManager.URL_TOPIC;
          objectId = indexEntry.getObjectId();
        } else if ("File".equals(resultType)) {
          // exemple http://server/silverpeas/File/ID_FILE
          type = URLManager.URL_FILE;
          objectId = indexEntry.getObjectId();
        }
        extURLSB.append(URLManager.getSimpleURL(type, objectId, compId, false));
        extURLSB.append("','").append(extSrv.getName()).append("');void 0;");

      }
    }
    return extURLSB.toString();
  }

  private String buildTitleLink(String markAsReadJS, String downloadLink, String componentId,
      String underLink, boolean openFile) {
    StringBuilder titleLinkBuilder = new StringBuilder(256);
    titleLinkBuilder.append("javascript:").append(markAsReadJS).append(" window.open('");
    titleLinkBuilder.append(EncodeHelper.javaStringToJsString(downloadLink));
    titleLinkBuilder.append("');jumpToComponent('").append(componentId);
    titleLinkBuilder.append("');document.location.href='");
    titleLinkBuilder.append(EncodeHelper.javaStringToJsString(underLink));
    if (openFile) {
      titleLinkBuilder.append("&FileOpened=1';");
    }
    return titleLinkBuilder.toString();
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
   *
   * @param matchingIndexEntries - un tableau de MatchingIndexEntry
   * @return un tableau contenant les informations relatives aux parametres d'entrée
   */
  private List<GlobalSilverResult> matchingIndexEntries2GlobalSilverResults(
      List<MatchingIndexEntry> matchingIndexEntries) throws Exception {
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.matchingIndexEntries2GlobalSilverResults()",
        "root.MSG_GEN_ENTER_METHOD");
    if (matchingIndexEntries == null || matchingIndexEntries.isEmpty()) {
      return new ArrayList<GlobalSilverResult>();
    }

    // Initialize loop variables

    LinkedList<String> returnedObjects = new LinkedList<String>();
    Map<String, String> places = null;
    List<GlobalSilverResult> results = new ArrayList<GlobalSilverResult>();

    // Retrieve list of object type filter
    List<String> objectTypeFilter = getListObjectTypeFilter();
    for (MatchingIndexEntry result : matchingIndexEntries) {
      boolean processThisResult = processResult(result, objectTypeFilter);

      if (processThisResult) {
        // reinitialisation
        String title = result.getTitle();
        String componentId = result.getComponent();

        GlobalSilverResult gsr = new GlobalSilverResult(result);

        SilverTrace.info("pdcPeas",
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

        // Check if it's an external search before searching components information
        String place;
        if (isExternalComponent(result.getServerName())) {
          place = getString("pdcPeas.external.search.label") + " ";
          place += getExternalServerLabel(result.getServerName());
        } else {
          // preparation sur l'emplacement du document
          if (componentId.startsWith("user@")) {
            UserDetail user = getOrganisationController().getUserDetail(
                componentId.substring(5, componentId.indexOf("_")));
            String component = componentId.substring(componentId.indexOf("_") + 1);
            place = user.getDisplayedName() + " / " + component;
          } else if (componentId.equals("pdc")) {
            place = getString("pdcPeas.pdc");
          } else if (componentId.equals("users")) {
            place = "";
          } else {
            if (places == null) {
              places = new HashMap<String, String>();
            }
            place = places.get(componentId);
            if (place == null) {
              ComponentInstLight componentInst = getOrganisationController().getComponentInstLight(
                  componentId);
              if (componentInst != null) {
                place = getSpaceLabel(componentInst.getDomainFatherId()) + " / "
                    + componentInst.getLabel(getLanguage());
                places.put(componentId, place);
              }
            }
          }
          String userId = result.getCreationUser();
          gsr.setCreatorName(getCompleteUserName(userId));
        }
        gsr.setLocation(place);
        results.add(gsr);
      }
    }
    if (places != null) {
      places.clear();
    }
    return results;
  }

  /**
   * @return list of current object type filter if exists, null else if
   */
  private List<String> getListObjectTypeFilter() {
    // Retrieve object type filter
    List<String> objectTypeFilter = null;
    if (!PdcSearchSessionController.ALL_DATA_TYPE.equals(this.dataType)) {
      for (SearchTypeConfigurationVO configVO : this.dataSearchTypes) {
        if (configVO.getConfigId() == Integer.parseInt(getDataType())) {
          return configVO.getTypes();
        }
      }
    }
    return objectTypeFilter;
  }

  /**
   * @param result the MatchingIndexEntry to process
   * @param objectTypeFilter the list of objectTypeFilter string
   * @return true if we process this result and add the GlobalSilverResult to the result list
   */
  private boolean processResult(MatchingIndexEntry result, List<String> objectTypeFilter) {
    // Default loop variable
    boolean processThisResult = true;

    // Check if we filter this object type or not before doing any data processing
    if (objectTypeFilter != null && objectTypeFilter.size() > 0) {
      // If object type filter is defined, change processThisResult default value.
      processThisResult = false;
      for (String objType : objectTypeFilter) {
        if (result.getObjectType().equalsIgnoreCase(objType)) {
          processThisResult = true;
        }
      }
    }
    return processThisResult;
  }

  /**
   * @param serverName the server name
   * @return true if it's an external component, false else if
   */
  private boolean isExternalComponent(String serverName) {
    if (StringUtil.isDefined(curServerName) && !curServerName.equalsIgnoreCase(serverName)) {
      return true;
    }
    return false;
  }

  /**
   * @param serverName
   * @return the server label
   */
  private String getExternalServerLabel(String serverName) {
    String srvLabel = "";
    boolean srvFound = false;
    if (StringUtil.isDefined(serverName)) {
      for (ExternalSPConfigVO extSrv : externalServers) {
        if (serverName.equalsIgnoreCase(extSrv.getName())) {
          srvLabel = getString("external.search.server." + extSrv.getConfigOrder() + ".label");
          srvFound = true;
        }
      }
    }
    if (!srvFound) {
      srvLabel = getString("pdcPeas.external.search.unknown");
    }
    return srvLabel;
  }

  /**
   * Converts a MatchingIndexEntry to a GlobalSilverResult, mainly duplicate code from
   * matchingIndexEntries2GlobalSilverResults, needs a Silverpeas Guru to refactor the two methods
   *
   * @param matchingIndexEntry
   * @return GlobalSilverResult or null if the MatchingIndexEntry was null
   * @throws Exception
   */
  public GlobalSilverResult matchingIndexEntry2GlobalSilverResult(
      MatchingIndexEntry matchingIndexEntry) {

    if (matchingIndexEntry == null) {
      return null;
    }

    // reinitialisation
    String title = matchingIndexEntry.getTitle();
    String componentId = matchingIndexEntry.getComponent();
    String location = null;

    GlobalSilverResult gsr = new GlobalSilverResult(matchingIndexEntry);

    SilverTrace.info(
        "pdcPeas",
        "PdcSearchSessionController.matchingIndexEntry2GlobalSilverResult()",
        "root.MSG_GEN_PARAM_VALUE", "title= " + title);

    // preparation sur l'emplacement du document
    if (componentId.startsWith("user@")) {
      UserDetail user = getOrganisationController().getUserDetail(
          componentId.substring(5, componentId.indexOf("_")));
      String component = componentId.substring(componentId.indexOf("_") + 1);
      location = user.getDisplayedName() + " / " + component;
    } else if (componentId.equals("pdc")) {
      location = getString("pdcPeas.pdc");
    } else if (componentId.equals("users")) {
      location = "";
    } else {
      ComponentInstLight componentInst = getOrganisationController().getComponentInstLight(
          componentId);
      if (componentInst != null) {
        location = getSpaceLabel(componentInst.getDomainFatherId()) + " / "
            + componentInst.getLabel(getLanguage());
      }
    }

    gsr.setLocation(location);

    String userId = matchingIndexEntry.getCreationUser();
    gsr.setCreatorName(getCompleteUserName(userId));

    return gsr;
  }

  private List<GlobalSilverResult> globalSilverContents2GlobalSilverResults(
      List<GlobalSilverContent> globalSilverContents) throws Exception {
    if (globalSilverContents == null || globalSilverContents.isEmpty()) {
      return new ArrayList<GlobalSilverResult>();
    }
    List<GlobalSilverResult> results = new ArrayList<GlobalSilverResult>();
    for (int i = 0; i < globalSilverContents.size(); i++) {
      GlobalSilverContent gsc = globalSilverContents.get(i);
      GlobalSilverResult gsr = new GlobalSilverResult(gsc);
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
    UserDetail user = getOrganisationController().getUserDetail(userId);
    if (user != null) {
      return user.getDisplayedName();
    }
    return "";
  }

  private String getAttachmentUrl(String objectType, String componentId, GlobalSilverResult gsr)
      throws Exception {
    String id = objectType.substring(10); // object type is Attachment1245 or
    // Attachment1245_en
    String language = I18NHelper.defaultLanguage;
    if (id != null && id.indexOf('_') != -1) {
      // extract attachmentId and language
      language = id.substring(id.indexOf('_') + 1, id.length());
      id = id.substring(0, id.indexOf('_'));
    }

    SimpleDocumentPK documentPk = new SimpleDocumentPK(id, componentId);
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService()
        .searchDocumentById(documentPk, language);

    // check if attachment is previewable and viewable
    File attachmentFile = new File(document.getAttachmentPath());
    boolean previewable = ViewerFactory.getPreviewService().isPreviewable(attachmentFile);
    boolean viewable = ViewerFactory.getViewService().isViewable(attachmentFile);

    gsr.setPreviewable(previewable);
    gsr.setViewable(viewable);
    gsr.setAttachmentId(id);
    gsr.setVersioned(false);

    String urlAttachment = document.getAttachmentURL();

    // Utilisation de l'API Acrobat Reader pour ouvrir le document PDF en mode
    // recherche (paramètre 'search')
    // Transmet au PDF la requête tapée par l'utilisateur via l'URL d'accès
    // http://partners.adobe.com/public/developer/en/acrobat/sdk/pdf/pdf_creation_apis_and_specs/PDFOpenParameters.pdf
    if (queryParameters != null) {
      String keywords = queryParameters.getKeywords();
      if (keywords != null && keywords.trim().length() > 0
          && MimeTypes.PDF_MIME_TYPE.equals(document.getContentType())) {
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

  private String getVersioningUrl(String documentId, String componentId, GlobalSilverResult gsr)
      throws Exception {
    SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.getVersioningUrl",
        "root.MSG_GEN_PARAM_VALUE", "documentId = " + documentId + ", componentId = " + componentId);
    SimpleDocument document = AttachmentServiceFactory.getAttachmentService()
        .searchDocumentById(new SimpleDocumentPK(documentId, componentId), null);
    SimpleDocument version = document.getLastPublicVersion();

    if (version != null) {
      // check if attachment is previewable and viewable
      File file = new File(version.getAttachmentPath());
      boolean previewable = ViewerFactory.getPreviewService().isPreviewable(file);
      boolean viewable = ViewerFactory.getViewService().isViewable(file);

      gsr.setPreviewable(previewable);
      gsr.setViewable(viewable);
      gsr.setAttachmentId(documentId);
      gsr.setVersioned(true);

      // process download link
      return FileServerUtils.getApplicationContext() + document.getAttachmentURL();
    }
    return null;
  }

  /**
   * ***************************************************************************************************************
   */
  /**
   * search from DomainsBar methods /
   * ****************************************************************************************************************
   */
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
    SilverTrace.info("pdcPeas", "PdcSearchSessionController.getCurrentComponentIds()",
        "root.MSG_GEN_ENTER_METHOD");
    for (int i = 0; componentList != null && i < componentList.size(); i++) {
      String componentId = componentList.get(i);
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
    if (componentList == null || componentList.isEmpty()) {
      if (StringUtil.isDefined(getCurrentComponentId())) {
        return getPdcBm().getPertinentAxisByInstanceId(searchContext, viewType,
            getCurrentComponentId());
      }
      return new ArrayList<SearchAxis>();
    } else {
      // we get all axis (pertinent or not) from a type P or S
      List<AxisHeader> axis = getPdcBm().getAxisByType(viewType);
      // we have to transform all axis (AxisHeader) into SearchAxis to make
      // the display into jsp transparent
      return transformAxisHeadersIntoSearchAxis(axis);
    }
  }

  private List<SearchAxis> transformAxisHeadersIntoSearchAxis(List<AxisHeader> axis) {
    ArrayList<SearchAxis> transformedAxis = new ArrayList<SearchAxis>();
    try {
      for (int i = 0; i < axis.size(); i++) {
        AxisHeader ah = axis.get(i);
        SearchAxis sa = new SearchAxis(Integer.parseInt(ah.getPK().getId()), 0);
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
    List<Value> values;
    if (componentList == null || componentList.isEmpty()) {
      values = getPdcBm().getPertinentDaughterValuesByInstanceId(searchContext,
          axisId, valueId, getCurrentComponentId());
    } else {
      if (isShowOnlyPertinentAxisAndValues()) {
        values = getPdcBm().getPertinentDaughterValuesByInstanceIds(
            searchContext, axisId, valueId, getCopyOfInstanceIds());
      } else {
        values = setNBNumbersToOne(getPdcBm().getDaughters(axisId, valueId));
      }
    }
    SilverTrace.info("pdcPeas", "PdcSearchSessionController.getDaughterValues()",
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
    SilverTrace.info("pdcPeas", "PdcSearchSessionController.getFirstLevelAxisValues()",
        "root.MSG_GEN_ENTER_METHOD", "axisId = " + axisId);
    List<Value> result;
    if (componentList == null || componentList.isEmpty()) {
      result = getPdcBm().getFirstLevelAxisValuesByInstanceId(searchContext,
          axisId, getCurrentComponentId());
    } else {
      result = getPdcBm().getFirstLevelAxisValuesByInstanceIds(searchContext,
          axisId, getCopyOfInstanceIds());
    }
    SilverTrace.info("pdcPeas", "PdcSearchSessionController.getFirstLevelAxisValues()",
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

  public List<Value> getFullPath(String valueId, String treeId) throws PdcException {
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
  /**
   * ***************************************************************************************************************
   */
  /**
   * searchAndSelect methods /
   * ****************************************************************************************************************
   */
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
    CompoSpace[] compoIds = getOrganisationController().getCompoForUser(getUserId(), componentName);
    SilverTrace.info("pdcPeas", "PdcSearchSessionController.getInstanceIdsFromComponentName",
        "root.MSG_GEN_PARAM_VALUE", "compoIds = " + compoIds.toString());
    List<String> instanceIds = new ArrayList<String>();
    for (CompoSpace component : compoIds) {
      instanceIds.add(component.getComponentId());
    }
    return instanceIds;
  }
  /**
   * ***************************************************************************************************************
   */
  /**
   * Thesaurus methods /
   * ****************************************************************************************************************
   */
  private ThesaurusManager thesaurus = new ThesaurusManager();
  private boolean activeThesaurus = false; // thesaurus actif
  private Jargon jargon = null;// jargon utilisé par l'utilisateur
  private Map<String, Collection<String>> synonyms = new HashMap<String, Collection<String>>();
  private static final int QUOTE_CHAR = new Integer('"').intValue();
  private static String[] KEYWORDS = null;
  private boolean isThesaurusEnableByUser = false;

  private boolean getActiveThesaurusByUser() throws PdcException {
    return getPersonalization().isThesaurusEnabled();
  }

  public boolean getActiveThesaurus() throws PdcException {
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
    String header = "";
    if (queryString == null || queryString.equals("") || !isThesaurusEnableByUser) {
      synonymsQueryString = queryString;
    } else {
      StreamTokenizer st = new StreamTokenizer(new StringReader(queryString));
      st.resetSyntax();
      st.lowerCaseMode(true);
      st.wordChars('\u0000', '\u00FF');
      st.quoteChar('"');
      st.ordinaryChar(')');
      st.ordinaryChar('(');
      st.ordinaryChar(' ');
      try {
        StringBuilder synonymsString = new StringBuilder("");
        while (st.nextToken() != StreamTokenizer.TT_EOF) {
          String word = "";
          String specialChar = "";
          if ((st.ttype == StreamTokenizer.TT_WORD) || (st.ttype == QUOTE_CHAR)) {
            word = st.sval;
          } else {
            specialChar = String.valueOf((char) st.ttype);
          }
          if (!word.isEmpty()) {
            // Check that it's not a determiner or a lucene specific characters
            if (!isKeyword(word)
                && !(word.indexOf('*') >= 0 || word.indexOf('?') >= 0 || word.indexOf(':') >= 0
                || word.indexOf('+') >= 0 || word.indexOf('-') >= 0)) {
              if (word.indexOf(':') != -1) {
                header = word.substring(0, word.indexOf(':') + 1);
                word = word.substring(word.indexOf(':') + 1, word.length());
              }

              synonymsString.append("(\"").append(word).append("\"");
              Collection<String> wordSynonyms = getSynonym(word);
              for (String synonym : wordSynonyms) {
                synonymsString.append(" OR " + "\"").append(synonym).append("\"");
              }
              synonymsString.append(")");
            } else {
              synonymsString.append(word);
            }
          }
          if (!specialChar.isEmpty()) {
            synonymsString.append(specialChar);
          }
        }
        synonymsString.insert(0, header);
        synonymsQueryString = synonymsString.toString();
        SilverTrace.info("pdcPeas",
            "PdcSearchSessionController.getSynonymsQueryString",
            "root.MSG_GEN_PARAM_VALUE",
            "queryString = " + queryString + ", with synonyms: " + synonymsQueryString);
      } catch (IOException e) {
        throw new PdcPeasRuntimeException("PdcSearchSessionController.setSynonymsQueryString",
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
    }
    return new HashMap<String, Collection<String>>();
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
            "org.silverpeas.search.indexEngine.StopWords", getLanguage());
        Enumeration<String> stopWord = resource.getKeys();
        while (stopWord.hasMoreElements()) {
          wordList.add(stopWord.nextElement());
        }
        KEYWORDS = wordList.toArray(new String[wordList.size()]);
      } catch (MissingResourceException e) {
        SilverTrace.warn("pdcPeas", "PdcSearchSessionController",
            "pdcPeas.MSG_MISSING_STOPWORDS_DEFINITION");
        return new String[0];
      }
    }
    return KEYWORDS;
  }
  /**
   * ***************************************************************************************************************
   */
  /**
   * Glossary methods /
   * ****************************************************************************************************************
   */
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
        PlainSearchResult result = SearchEngineFactory.getSearchEngine().search(queryDescription);
        glossaryResults = result.getEntries().toArray(new MatchingIndexEntry[result.getEntries().
            size()]);
      } catch (org.silverpeas.search.searchEngine.model.ParseException e) {
        throw new PdcException("PdcSearchSessionController.glossarySearch()",
            SilverpeasException.ERROR, "pdcPeas.EX_CAN_SEARCH_QUERY", "query : "
            + queryDescription.getQuery(), e);
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

    List<AxisHeader> allAxis;
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
    List<AxisHeader> allAxis = new ArrayList<AxisHeader>(usedAxisList.size());
    // get all AxisHeader corresponding to usedAxis for this instance
    for (UsedAxis usedAxis : usedAxisList) {
      String axisId = new Integer(usedAxis.getAxisId()).toString();
      AxisHeader axisHeader = getAxisHeader(axisId);
      allAxis.add(axisHeader);
    }
    return allAxis;
  }

  public List<String> getUsedTreeIds(String instanceId) throws PdcException {
    List<AxisHeader> usedAxisHeaders = getUsedAxisHeaderByInstanceId(instanceId);
    List<String> usedTreeIds = new ArrayList<String>(usedAxisHeaders.size());
    for (int i = 0; i < usedAxisHeaders.size(); i++) {
      AxisHeader axisHeader = usedAxisHeaders.get(i);
      if (axisHeader != null) {
        String treeId = Integer.toString(axisHeader.getRootId());
        usedTreeIds.add(treeId);
      }
    }
    return usedTreeIds;
  }

  public Value getAxisValueAndFullPath(String valueId, String treeId) throws PdcException {
    Value value = getPdcBm().getAxisValue(valueId, treeId);
    if (value != null) {
      value.setPathValues(getFullPath(valueId, treeId));
    }
    return value;
  }

  public List<Axis> getUsedAxisByAComponentInstance(String instanceId)
      throws PdcException {
    List<UsedAxis> usedAxisList = getPdcBm().getUsedAxisByInstanceId(instanceId);
    List<Axis> axisList = new ArrayList<Axis>(usedAxisList.size());
    for (UsedAxis usedAxis : usedAxisList) {
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

  /**
   * ***************************************************************************************************************
   */
  /**
   * Interest Center methods /
   * ****************************************************************************************************************
   */
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

  public List<InterestCenter> getICenters() {
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
      List<Criteria> criterias = ic.getPdcContext();
      for (Criteria criteria : criterias) {
        searchContext.addCriteria(new SearchCriteria(criteria.getAxisId(), criteria.getValue()));
      }
      return ic;
    } catch (Exception e) {
      throw new PdcPeasRuntimeException(
          "PdcSearchSessionController.loadICenter", SilverpeasException.ERROR,
          "pdcPeas.EX_LOAD_IC", e);
    }
  }
  /**
   * ***************************************************************************************************************
   */
  /**
   * PDC Subscriptions methods /
   * ****************************************************************************************************************
   */
  private PDCSubscription pdcSubscription;

  public PDCSubscription getPDCSubscription() {
    return pdcSubscription;
  }

  public void setPDCSubscription(PDCSubscription subscription) {
    this.pdcSubscription = subscription;
  }

  /**
   * ***************************************************************************************************************
   */
  /**
   * UserPanel methods /
   * ****************************************************************************************************************
   */
  public String initUserPanel() throws RemoteException {
    String m_context = URLManager.getApplicationURL();
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

  /**
   * ******************************************************************************************
   */
  /**
   * SearchEngine primitives *
   */
  /**
   * ******************************************************************************************
   */
  public void buildComponentListWhereToSearch(String space, String component) {
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.buildComponentListWhereToSearch()",
        "root.MSG_GEN_ENTER_METHOD", "space = " + space + ", component = "
        + component);

    componentList = new ArrayList<String>();

    if (space == null) {
      // Il n'y a pas de restriction sur un espace particulier
      String[] allowedComponentIds = getUserAvailComponentIds();
      List<String> excludedComponentIds = getComponentsExcludedFromGlobalSearch();
      for (int i = 0; i < allowedComponentIds.length; i++) {
        if (isSearchable(allowedComponentIds[i], excludedComponentIds)) {
          componentList.add(allowedComponentIds[i]);
        }
      }
    } else {
      if (component == null) {
        // Restriction sur un espace. La recherche doit avoir lieu
        String[] asAvailCompoForCurUser = getOrganisationController().getAvailCompoIds(space,
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

  private List<String> getComponentsExcludedFromGlobalSearch() {
    List<String> excluded = new ArrayList<String>();

    // exclude all components of all excluded spaces
    List<String> spaces = getItemsExcludedFromGlobalSearch("SpacesExcludedFromGlobalSearch");
    for (String space : spaces) {
      String[] availableComponentIds =
          getOrganisationController().getAvailCompoIds(space, getUserId());
      excluded.addAll(Arrays.asList(availableComponentIds));
    }

    // exclude components explicitly excluded
    List<String> components =
        getItemsExcludedFromGlobalSearch("ComponentsExcludedFromGlobalSearch");
    excluded.addAll(components);

    return excluded;
  }

  private List<String> getItemsExcludedFromGlobalSearch(String parameterName) {
    List<String> items = new ArrayList<String>();
    String param = getSettings().getString(parameterName);
    if (StringUtil.isDefined(param)) {
      StringTokenizer tokenizer = new StringTokenizer(param, ",");
      while (tokenizer.hasMoreTokens()) {
        items.add(tokenizer.nextToken());
      }
    }
    return items;
  }

  /**
   * This method allow user to search over multiple component selection
   *
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
        String[] asAvailCompoForCurUser = getOrganisationController().getAvailCompoIds(space,
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
    return isSearchable(componentId, null);
  }

  private boolean isSearchable(String componentId, List<String> exclusionList) {
    if (exclusionList != null && !exclusionList.isEmpty() && exclusionList.contains(componentId)) {
      return false;
    }
    if (componentId.startsWith("silverCrawler")
        || componentId.startsWith("gallery")
        || componentId.startsWith("kmelia")) {
      boolean isPrivateSearch = "yes".equalsIgnoreCase(getOrganisationController().
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
    return getOrganisationController().getSpaceTreeview(getUserId());
  }

  /**
   * Returns the list of allowed components for the current user in the given space/domain.
   */
  public List<ComponentInstLight> getAllowedComponents(String space) {
    List<ComponentInstLight> allowedList = new ArrayList<ComponentInstLight>();
    if (space != null) {
      String[] asAvailCompoForCurUser = getOrganisationController().getAvailCompoIds(space,
          getUserId());
      for (int nI = 0; nI < asAvailCompoForCurUser.length; nI++) {
        ComponentInstLight componentInst = getOrganisationController().getComponentInstLight(
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
    SpaceInstLight spaceInst = getOrganisationController().getSpaceInstLightById(spaceId);
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
        componentInst = getOrganisationController().getComponentInstLight(
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
  /**
   * ******************************************************************************************
   */
  /**
   * AskOnce methods *
   */
  /**
   * ******************************************************************************************
   */
  private List<String[]> searchDomains = null; // All the domains available for search

  /**
   * Get the search domains available for search The search domains are contained in a Vector of
   * array of 3 String (String[0]=domain name, String[1]=domain url page, String[2]=internal Id)
   */
  public List<String[]> getSearchDomains() {
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
    List<String[]> domains = new ArrayList<String[]>();

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

  /**
   * ******************************************************************************************
   */
  /**
   * Date primitives *
   */
  /**
   * ******************************************************************************************
   */
  public String getUrl(String urlBase, MatchingIndexEntry indexEntry) {
    return getUrl(urlBase, indexEntry.getComponent(), indexEntry.getPageAndParams());
  }

  public String getUrl(String urlBase, String componentId, String pageAndParams) {
    String url = urlBase + URLManager.getURL(null, componentId) + pageAndParams;
    if (url.contains("?")) {
      url += "&From=Search";
    } else {
      url += "?From=Search";
    }
    return url;
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
  /**
   * ******************************************************************************************
   */
  /**
   * Business objects primitives *
   */
  /**
   * ******************************************************************************************
   */
  private PdcBm pdcBm = null; // To retrieve items from PDC

  // searchEngine
  private PdcBm getPdcBm() {
    if (pdcBm == null) {
      pdcBm = (PdcBm) new PdcBmImpl();
    }
    return pdcBm;
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
   *
   * @param matchingIndexEntries - un tableau de MatchingIndexEntry
   * @return un tableau contenant les informations relatives aux parametres d'entrée
   */
  private List<MatchingIndexEntry> filterMatchingIndexEntries(
      MatchingIndexEntry[] matchingIndexEntries) {
    if (matchingIndexEntries == null || matchingIndexEntries.length == 0) {
      return new ArrayList<MatchingIndexEntry>();
    }
    List<MatchingIndexEntry> results =
        new ArrayList<MatchingIndexEntry>(matchingIndexEntries.length);
    try {
      getSecurityIntf().enableCache();
    } catch (Exception e) {
      SilverTrace.info("pdcPeas",
          "PdcSearchSessionController.filterMatchingIndexEntries()",
          "pdcPeas.EX_CAN_SEARCH_QUERY", e);
    }

    for (MatchingIndexEntry result : matchingIndexEntries) {
      // reinitialisation
      String title = result.getTitle();
      String componentId = result.getComponent();

      if (!isMatchingIndexEntryAvailable(result)) {
        continue;
      }

      // WARNING : LINE BELOW HAS BEEN ADDED TO NOT SHOW WYSIWYG ALONE IN SEARCH
      // RESULT PAGE
      if (title.endsWith("wysiwyg.txt") && (componentId.startsWith("kmelia")
          || componentId.startsWith("kmax"))) {
        continue;
      }

      results.add(result);
    }

    try {
      getSecurityIntf().disableCache();
    } catch (Exception e) {
      SilverTrace.info("pdcPeas", "PdcSearchSessionController.filterMatchingIndexEntries()",
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
    clearXmlTemplateAndData(); // init xml template data
    PublicationTemplateImpl template = null;
    try {
      template = (PublicationTemplateImpl) PublicationTemplateManager.getInstance().
          loadPublicationTemplate(fileName);
      this.xmlTemplate = template;
    } catch (PublicationTemplateException e) {
      throw new PdcPeasRuntimeException(
          "PdcSearchSessionController.setXmlTemplate()",
          SilverpeasException.ERROR, "pdcPeas.CANT_LOAD_TEMPLATE", e);
    }
    return template;
  }

  public void clearXmlTemplateAndData() {
    xmlTemplate = null;
    xmlData = null;
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

  public String getSearchPageId() {
    return searchPageId;
  }

  public void setSearchPageId(String searchPageId) {
    if (StringUtil.isDefined(searchPageId)) {
      this.searchPageId = searchPageId;
    } else {
      resetSearchPageId();
    }
  }

  public void resetSearchPageId() {
    searchPageId = null;
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
   *
   * @return array that contains suggestions.
   */
  public List<String> getSpellingwords() {
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
   *
   * @param globalSR : the current list of result
   */
  private void setGlobalSR(List<GlobalSilverResult> globalSR, boolean setExtraInfo) {
    if (setExtraInfo) {
      setExtraInfoToResultsToDisplay(globalSR);
    }
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
   *
   * @return the xmlFormSortValue
   */
  public String getXmlFormSortValue() {
    return xmlFormSortValue;
  }

  /**
   * Sets the XML form field used to realize sorting
   *
   * @param xmlFormSortValue the xmlFormSortValue to set
   */
  public void setXmlFormSortValue(String xmlFormSortValue) {
    this.xmlFormSortValue = xmlFormSortValue;
  }

  /**
   * Gets the keyword to retreive the implementation class name to realize sorting or filtering
   *
   * @return the sortImplemtor
   */
  public String getSortImplemtor() {
    if (sortImplementor == null) {
      return Keys.defaultImplementor.value();
    }
    return sortImplementor;
  }

  /**
   * @param sortImplementor the sortImplementor to set
   */
  public void setSortImplemtor(String sortImplementor) {
    this.sortImplementor = sortImplementor;
  }

  /**
   * @return the isEnableExternalSearch attribute
   */
  public boolean isEnableExternalSearch() {
    return isEnableExternalSearch;
  }

  private List<String> getCopyOfInstanceIds() {
    return new ArrayList<String>(componentList);
  }

  /**
   * Retrieve configuration from properties file
   *
   * @return a list of search type configuration value object
   */
  public List<SearchTypeConfigurationVO> getSearchTypeConfig() {
    if (dataSearchTypes == null) {
      List<SearchTypeConfigurationVO> configs = new ArrayList<SearchTypeConfigurationVO>();

      int cpt = 1;
      String postConfigKey = "search.type.";
      String componentsValue = getSettings().getString(postConfigKey + cpt + ".components", "");
      while (StringUtil.isDefined(componentsValue)) {
        String typesValue = getSettings().getString(postConfigKey + cpt + ".types", "");
        String nameValue = getString(postConfigKey + cpt + ".label");

        List<String> listComponents = Arrays.asList(componentsValue.split(","));
        List<String> listTypes = new ArrayList<String>();
        if (StringUtil.isDefined(typesValue)) {
          listTypes = Arrays.asList(typesValue.split(","));
        }
        configs.add(new SearchTypeConfigurationVO(cpt, nameValue, listComponents, listTypes));

        // Loop variable update
        cpt++;
        componentsValue = getSettings().getString(postConfigKey + cpt + ".components", "");
      }
      dataSearchTypes = configs;
    }
    return dataSearchTypes;
  }

  private SearchTypeConfigurationVO getSearchType(String componentId, String type) {
    ComponentInstLight component = getOrganisationController().getComponentInstLight(componentId);
    if (component != null) {
      for (SearchTypeConfigurationVO searchType : getSearchTypeConfig()) {
        if (searchType.getComponents().contains(component.getName())) {
          if (searchType.getTypes().isEmpty() || searchType.getTypes().contains(type)) {
            return searchType;
          }
        }
      }
    }
    return null;
  }

  /**
   * @return the dataType search
   */
  public String getDataType() {
    if (!StringUtil.isDefined(dataType)) {
      dataType = ALL_DATA_TYPE;
    }
    return dataType;
  }

  /**
   * @param dataType the dataType search to set
   */
  public void setDataType(String dataType) {
    this.dataType = dataType;
  }

  public boolean isDataTypeDefined() {
    return !ALL_DATA_TYPE.equals(getDataType());
  }

  /**
   * Add restriction on advanced search data type
   *
   * @param curComp the current component identifier
   * @return true if search engine must search through this component, false else if
   */
  public boolean isDataTypeSearch(String curComp) {
    boolean searchOn = false;
    if (isDataTypeDefined()) {
      List<SearchTypeConfigurationVO> configs = getSearchTypeConfig();
      for (SearchTypeConfigurationVO searchTypeConfigurationVO : configs) {
        if (searchTypeConfigurationVO.getConfigId() == Integer.parseInt(getDataType())) {
          List<String> components = searchTypeConfigurationVO.getComponents();
          for (String authorizedComponent : components) {
            if (curComp.startsWith(authorizedComponent)) {
              return true;
            }
          }
        }
      }
    } else {
      searchOn = true;
    }
    return searchOn;
  }

  public void setSelectedFacetEntries(ResultFilterVO selectedFacetEntries) {
    this.selectedFacetEntries = selectedFacetEntries;
  }

  public ResultFilterVO getSelectedFacetEntries() {
    return selectedFacetEntries;
  }

  public boolean isPlatformUsesPDC() {
    return platformUsesPDC;
  }
}
