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
package org.silverpeas.web.pdc.control;

import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;
import org.silverpeas.core.pdc.interests.model.Interests;
import org.silverpeas.core.pdc.interests.service.InterestsManager;
import org.silverpeas.core.pdc.PdcServiceProvider;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;
import org.silverpeas.core.pdc.thesaurus.service.ThesaurusManager;
import org.silverpeas.core.pdc.thesaurus.model.Jargon;
import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.contribution.contentcontainer.container.ContainerPeas;
import org.silverpeas.core.contribution.contentcontainer.container.ContainerPositionInterface;
import org.silverpeas.core.contribution.contentcontainer.container.ContainerWorkspace;
import org.silverpeas.core.contribution.contentcontainer.content.ContentPeas;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.pdc.pdc.service.Pdc;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.pdc.model.Axis;
import org.silverpeas.core.pdc.pdc.model.AxisHeader;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.SearchAxis;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.SearchCriteria;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.pdc.pdc.model.QueryParameters;
import org.silverpeas.core.security.authorization.ComponentAuthorization;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.file.FileUtil;
import org.silverpeas.web.pdc.vo.ExternalSPConfigVO;
import org.silverpeas.web.pdc.vo.Facet;
import org.silverpeas.web.pdc.vo.FacetEntryVO;
import org.silverpeas.web.pdc.vo.ResultFilterVO;
import org.silverpeas.web.pdc.vo.ResultGroupFilter;
import org.silverpeas.web.pdc.vo.SearchTypeConfigurationVO;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.core.admin.component.model.CompoSpace;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.domain.model.DomainProperties;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.admin.user.UserIndexation;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.silverstatistics.access.model.StatisticRuntimeException;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.index.search.PlainSearchResult;
import org.silverpeas.core.index.search.SearchEngineProvider;
import org.silverpeas.core.index.search.model.AxisFilter;
import org.silverpeas.core.index.search.model.MatchingIndexEntry;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.i18n.I18NHelper;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.viewer.service.ViewerProvider;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.*;

public class PdcSearchSessionController extends AbstractComponentSessionController {

  // Container and Content Peas
  private ContainerWorkspace containerWorkspace = null;
  private ContainerPeas containerPeasPDC = null;
  private ContentPeas contentPeasPDC = null;
  private SearchContext searchContext = null; // Current position
  // in PDC
  private QueryParameters queryParameters = null; // Current parameters for
  // plain search
  private List<String> componentList = null;
  private String isSecondaryShowed = "NO";
  private boolean showOnlyPertinentAxisAndValues = true;
  private List<GlobalSilverResult> globalSR = new ArrayList<>();
  private List<GlobalSilverResult> filteredSR = new ArrayList<>();
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
  private ComponentAuthorization componentAuthorization = null;
  // field value of XML form used to sort results
  private String xmlFormSortValue = null;
  // keyword used to retrieve the implementation to realize sorting or filtering
  private String sortImplementor = null;
  private int currentResultsDisplay = SHOWRESULTS_ALL;
  public static final int SHOWRESULTS_ALL = 0;
  public static final int SHOWRESULTS_OnlyPDC = 1;
  // spelling word
  private List<String> spellingwords = Collections.<String>emptyList();
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

  private static final String locationSeparator = ">";

  public PdcSearchSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle,
      String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle,
        "org.silverpeas.pdcPeas.settings.pdcPeasSettings");

    isRefreshEnabled = getSettings().getBoolean("EnableRefresh", true);

    try {
      isThesaurusEnableByUser = getActiveThesaurusByUser();
    } catch (Exception e) {
      isThesaurusEnableByUser = false;
    }

    this.searchContext = new SearchContext(this.getUserId());

    // Initialize external search
    isEnableExternalSearch = getSettings().getBoolean("external.search.enable", false);
    getExternalSPConfig();

    try {
      platformUsesPDC = !getPdcManager().getAxis().isEmpty();
    } catch (PdcException ignored) {
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
      externalServers = new ArrayList<>();
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

  public int getNbItemsPerPage() {
    if (nbItemsPerPage == -1) {
      nbItemsPerPage = new Integer(getSettings().getString("NbItemsParPage", "20"));
    }
    return nbItemsPerPage;
  }

  public int getIndexOfFirstItemToDisplay() {
    return indexOfFirstItemToDisplay;
  }

  public void setIndexOfFirstItemToDisplay(String index) {
    this.indexOfFirstItemToDisplay = new Integer(index);
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
    List<String> choiceNbResToDisplay = new ArrayList<>();
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
      org.silverpeas.core.index.search.model.ParseException {

    MatchingIndexEntry[] plainSearchResults = null;
    QueryDescription query;
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

        if (componentList.size() == 1) {
          query.setRequestedFolder(getQueryParameters().getFolder());
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



        String originalQuery = query.getQuery();
        query.setQuery(getSynonymsQueryString(originalQuery));

        PlainSearchResult searchResult = SearchEngineProvider.getSearchEngine().search(query);
        plainSearchResults = searchResult.getEntries().toArray(new MatchingIndexEntry[searchResult.
            getEntries().size()]);
        // spelling words
        if (getSettings().getBoolean("enableWordSpelling", false)) {
          spellingwords = searchResult.getSpellingWords();
        }

      }
    } catch (ParseException ignore) {
    }


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
    Collection<File> listDir = new ArrayList<>();
    try {
      listDir =
          FileFolderManager.getAllSubFolder(extServerCfg.getDataPath() + File.separator
          + "index");
    } catch (UtilException e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
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
        SilverLogger.getLogger(this)
            .error("Search query error for componentId = " + componentId + ", objectId = " +
                mie.getObjectId() + ", objectType = " + mie.getObjectType(), e);
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
    } else if (UserIndexation.OBJECT_TYPE.equals(objectType) &&
        !DomainProperties.areDomainsVisibleToAll()) {
      // visibility between domains is limited, check found user domain against current user domain
      String userId = mie.getObjectId();
      UserDetail userFound = getUserDetail(userId);
      if (DomainProperties.areDomainsVisibleOnlyToDefaultOne()) {
        if ("0".equals(getUserDetail().getDomainId())) {
          // current user of default domain can see all users
          return true;
        } else {
          // current user of other domains can see only users of his domain
          return userFound.getDomainId().equals(getUserDetail().getDomainId());
        }
      } else if (DomainProperties.areDomainsNonVisibleToOthers()) {
        // user found must be in same domain of current user
        return userFound.getDomainId().equals(getUserDetail().getDomainId());
      }
    }

    return true;
  }

  private ComponentAuthorization getSecurityIntf() throws Exception {
    if (componentAuthorization == null) {
      componentAuthorization = (ComponentAuthorization) Class.forName(
          "org.silverpeas.components.kmelia.KmeliaAuthorization").newInstance();
    }

    return componentAuthorization;
  }

  public List<GlobalSilverResult> getResultsToDisplay() throws Exception {
    return getSortedResultsToDisplay(getSortValue(), getSortOrder(), getXmlFormSortValue(),
        getSortImplemtor(), getSelectedFacetEntries());
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
    Facet fileTypeFacet = new Facet("filetype", getString("pdcPeas.facet.filetype"));

    // key is the fieldName
    Map<String, Facet> fieldFacetsMap = new HashMap<>();

    if (results != null) {
      // Retrieve the black list component (we don't need to filter data on it)
      List<String> blackList = getFacetBlackList();
      Map<String, ComponentInstLight> components = new HashMap<>();

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
        processFacetDatatype(dataTypeFacet, result, components);

        processFacetFiletype(fileTypeFacet, result);

        // manage forms fields facets
        processFacetsFormField(fieldFacetsMap, result);
      }

      components.clear();
    }

    // Fill result filter with current result values
    res.setAuthorFacet(authorFacet);
    res.setComponentFacet(componentFacet);
    res.setDatatypeFacet(dataTypeFacet);
    res.setFiletypeFacet(fileTypeFacet);
    res.setFormFieldFacets(new ArrayList<>(fieldFacetsMap.values()));

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

  private void processFacetDatatype(Facet facet, GlobalSilverResult result,
      Map<String, ComponentInstLight> components) {
    String instanceId = result.getInstanceId();
    String type = result.getType();
    if (StringUtil.isDefined(type)) {
      SearchTypeConfigurationVO theSearchType = getSearchType(instanceId, type, components);
      if (theSearchType != null) {
        FacetEntryVO facetEntry = new FacetEntryVO(theSearchType.getName(), String.valueOf(
            theSearchType.getConfigId()));
        if (getSelectedFacetEntries() != null) {
          if (String.valueOf(theSearchType.getConfigId()).equals(getSelectedFacetEntries().
              getDatatype())) {
            facetEntry.setSelected(true);
          }
        }
        facet.addEntry(facetEntry);
      }
    }
  }

  private void processFacetFiletype(Facet facet, GlobalSilverResult result) {
    String filename = result.getAttachmentFilename();
    if (StringUtil.isDefined(filename)) {
      String extension = FileRepositoryManager.getFileExtension(filename).toLowerCase();
      if (StringUtil.isDefined(extension)) {
        FacetEntryVO facetEntry = new FacetEntryVO(extension, extension);
        if (getSelectedFacetEntries() != null) {
          if (extension.equals(getSelectedFacetEntries().getFiletype())) {
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
      String appLocation = location.substring(location.lastIndexOf(locationSeparator) + 1);
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
            SilverLogger.getLogger(this).error(e.getMessage(), e);
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
            SilverLogger.getLogger(this).error(e.getMessage(), e);
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
      SilverLogger.getLogger(this).error(e.getMessage(), e);
    }
    return false;
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
      String xmlFormSortValue, String sortType, ResultFilterVO filter) {

    // Tous les résultats
    List<GlobalSilverResult> results = getGlobalSR();

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
    List<GlobalSilverResult> resultsToDisplay;
    if (filter != null && !filter.isEmpty()) {
      // Check Author filter
      resultsToDisplay = filterResult(filter, sortedResults);
    } else {
      // Put the full result list in session
      setGlobalSR(sortedResults);

      // get the part of results to display
      resultsToDisplay = sortedResults.subList(getIndexOfFirstResultToDisplay(), getLastIndexToDisplay());
    }
    setExtraInfoToResultsToDisplay(resultsToDisplay);
    return resultsToDisplay;
  }

  private void setPopularityToResults() {
    List<GlobalSilverResult> results = getGlobalSR();
    StatisticService statisticService = getStatisticBm();
    ForeignPK pk = new ForeignPK("unknown");
    for (GlobalSilverResult result : results) {
      if (isPopularityCompliant(result)) {
        pk.setComponentName(result.getInstanceId());
        pk.setId(result.getId());
        int nbAccess = statisticService.getCount(pk, 1, "Publication");
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

  public StatisticService getStatisticBm() {
    try {
      return ServiceProvider.getService(StatisticService.class);
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
    List<GlobalSilverResult> sortedResults = new ArrayList<>();
    List<GlobalSilverResult> sortedResultsToDisplay;

    String authorFilter = filter.getAuthorId();
    boolean filterAuthor = StringUtil.isDefined(authorFilter);

    // Check Component filter
    String componentFilter = filter.getComponentId();
    boolean filterComponent = StringUtil.isDefined(componentFilter);

    String datatypeFilter = filter.getDatatype();
    boolean filterDatatype = StringUtil.isDefined(datatypeFilter);

    boolean filterFormFields = !filter.isSelectedFormFieldFacetsEmpty();

    List<String> blackList = getFacetBlackList();
    Map<String, ComponentInstLight> components = new HashMap<>();

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
          SearchTypeConfigurationVO theSearchType = getSearchType(gsrInstanceId, gsResult.getType(), components);
          if (theSearchType == null || !datatypeFilter.equals(String.valueOf(theSearchType.
              getConfigId()))) {
            visible = false;
          }
        }

        // check filetype facet
        if (visible && StringUtil.isDefined(filter.getFiletype())) {
          if (!gsResult.isAttachment() ||
              !StringUtil.isDefined(gsResult.getAttachmentFilename()) ||
              !FileRepositoryManager.getFileExtension(gsResult.getAttachmentFilename())
                  .toLowerCase().equals(filter.getFiletype())) {
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
              Map<String, String> theSelectedFacetEntries = filter.
                  getFormFieldSelectedFacetEntries();
              for (Map.Entry<String, String> facet : theSelectedFacetEntries.entrySet()) {
                // get stored value relative to given facet
                String resultFieldValue = gsrFormFieldsForFacets.get(facet.getKey());
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
    components.clear();

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
    List<String> blackList = new ArrayList<>();
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
    String downloadLink;
    String resultType = "";
    String underLink;
    // activate the mark as read functionality on results list
    String markAsReadJS = "";
    boolean isEnableMarkAsRead = getSettings().getBoolean("enableMarkAsRead", false);

    for (int r = 0; r < results.size(); r++) {
      GlobalSilverResult result = results.get(r);
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
        markAsReadJS = "markAsRead('" + result.getResultId() + "');";
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
              downloadLink = getAttachmentUrl(result);
            } catch (Exception e) {
              SilverLogger.getLogger(this).error(e.getMessage(), e);
            }
            underLink = getUrl(URLUtil.getApplicationURL(), indexEntry);
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
              underLink = URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, componentId);
              titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId
                  + "');document.location.href='" + underLink + "';";
            }
          }
        } else if (resultType.startsWith("Versioning")) {
          try {
            downloadLink = getVersioningUrl(result);
          } catch (Exception e) {
            SilverLogger.getLogger(this).error(e.getMessage(), e);
          }
          underLink = getUrl(URLUtil.getApplicationURL(), indexEntry);
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
          underLink = URLUtil.getApplicationURL() + URLUtil.getURL("useless", componentId)
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
          titleLink = "javascript:" + markAsReadJS + " openGlossary('" + uniqueId + "');";
        } else if (resultType.equals("Space")) {
          // retour sur l'espace
          String spaceId = indexEntry.getObjectId();
          titleLink = "javascript:" + markAsReadJS + " goToSpace('" + spaceId
              + "');document.location.href='"
              + URLUtil.getSimpleURL(URLUtil.URL_SPACE, spaceId) + "';";
        } else if (resultType.equals("Component")) {
          // retour sur le composant
          componentId = indexEntry.getObjectId();
          underLink = URLUtil.getSimpleURL(URLUtil.URL_COMPONENT,
              componentId);
          titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId
              + "');document.location.href='" + underLink + "';";
        } else if (componentId.startsWith("user@")) {
          titleLink = URLUtil.getApplicationURL() + URLUtil.getURL(resultType) + indexEntry
              .getPageAndParams();
        } else if (UserIndexation.OBJECT_TYPE.equals(resultType)) {
          UserDetail userDetail = getUserDetail(indexEntry.getPK().getObjectId());
          if (userDetail != null) {
            result.setThumbnailURL(userDetail.getSmallAvatar());
          }
          titleLink = "javascript:" + markAsReadJS + " viewUserProfile('" + indexEntry.getPK().
              getObjectId() + "');";
        } else {
          titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId
              + "');";
          if (indexEntry != null) {
            titleLink += "document.location.href='" + getUrl(URLUtil.getApplicationURL(),
                indexEntry) + "';";
          } else {
            titleLink +=
                "document.location.href='"
                + getUrl(URLUtil.getApplicationURL(), componentId, result.getURL())
                + "';";
          }
        }
      }

      result.setTitleLink(titleLink);
      if (StringUtil.isDefined(downloadLink)) {
        result.setDownloadLink(downloadLink);
      }
      result.setExportable(isCompliantResult(result));

      if (getSelectedSilverContents() != null && getSelectedSilverContents().contains(result)) {
        result.setSelected(true);
      } else {
        result.setSelected(false);
      }
    }
  }

  /**
   * Only called when isEnableExternalSearch is activated. Build an external link using Silverpeas
   * permalink
   *
   * @see URLUtil#getSimpleURL
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
        // Retrieve the URLUtil type
        int type = 0;
        String objectId = "";
        String compId = indexEntry.getComponent();
        if ("Publication".equals(resultType)) {
          // exemple http://server/silverpeas/Publication/ID_PUBLI
          type = URLUtil.URL_PUBLI;
          objectId = indexEntry.getObjectId();
        } else if ("Node".equals(resultType)) {
          // exemple http://server/silverpeas/Topic/ID_TOPIC?ComponentId=ID_COMPONENT
          type = URLUtil.URL_TOPIC;
          objectId = indexEntry.getObjectId();
        } else if ("File".equals(resultType)) {
          // exemple http://server/silverpeas/File/ID_FILE
          type = URLUtil.URL_FILE;
          objectId = indexEntry.getObjectId();
        }
        extURLSB.append(URLUtil.getSimpleURL(type, objectId, compId, false));
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
      titleLinkBuilder.append("&FileOpened=1");
    }
    titleLinkBuilder.append("';");
    return titleLinkBuilder.toString();
  }

  public int getTotalResults() {
    if (!getFilteredSR().isEmpty()) {
      return getFilteredSR().size();
    }
    return getGlobalSR().size();
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
    if (matchingIndexEntries == null || matchingIndexEntries.isEmpty()) {
      return new ArrayList<>();
    }

    // Initialize loop variables
    Map<String, String> places = null;
    List<GlobalSilverResult> results = new ArrayList<>();

    // Retrieve list of object type filter
    List<String> objectTypeFilter = getListObjectTypeFilter();

    List<String> wysiwygSuffixes = new ArrayList<>();
    for (String language : I18NHelper.getAllSupportedLanguages()) {
      wysiwygSuffixes.add(WysiwygController.WYSIWYG_CONTEXT + "_" + language + ".txt");
    }

    for (int i = 0; i < matchingIndexEntries.size(); i++) {
      MatchingIndexEntry result = matchingIndexEntries.get(i);
      boolean processThisResult = processResult(result, objectTypeFilter);

      if (processThisResult) {
        // reinitialisation
        String title = result.getTitle();
        String componentId = result.getComponent();

        GlobalSilverResult gsr = new GlobalSilverResult(result);
        gsr.setResultId(i);

        // WARNING : LINE BELOW HAS BEEN ADDED TO NOT SHOW WYSIWYG ALONE IN SEARCH
        // RESULT PAGE
        if (isWysiwyg(title, wysiwygSuffixes)
            && (componentId.startsWith("kmelia") || componentId.startsWith("kmax"))) {
          continue;
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
            place = user.getDisplayedName() + " " + locationSeparator + " " + component;
          } else if (componentId.equals("pdc")) {
            place = getString("pdcPeas.pdc");
          } else if (componentId.equals("users")) {
            place = "";
          } else {
            if (places == null) {
              places = new HashMap<>();
            }
            place = places.get(componentId);
            if (place == null) {
              place = getLocation(componentId);
              places.put(componentId, place);
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
   * Get location of result
   *
   * @param instanceId - application id
   * @return location as a String
   */
  public String getLocation(String instanceId) {
    ComponentInstLight componentInst =
        getOrganisationController().getComponentInstLight(instanceId);
    if (componentInst != null) {
      String spaceId = componentInst.getDomainFatherId();
      return getSpaceLabel(spaceId) + " " + locationSeparator + " " +
          getComponentLabel(spaceId, instanceId);
    }
    return "";
  }

  private boolean isWysiwyg(String filename, List<String> wysiwygSuffixes) {
    for (String suffix : wysiwygSuffixes) {
      if (filename.endsWith(suffix)) {
        return true;
      }
    }
    return false;
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
    String componentId = matchingIndexEntry.getComponent();
    String location;

    GlobalSilverResult gsr = new GlobalSilverResult(matchingIndexEntry);

    // preparation sur l'emplacement du document
    if (componentId.startsWith("user@")) {
      UserDetail user = getOrganisationController().getUserDetail(
          componentId.substring(5, componentId.indexOf("_")));
      String component = componentId.substring(componentId.indexOf("_") + 1);
      location = user.getDisplayedName() + " " + locationSeparator + " " + component;
    } else if (componentId.equals("pdc")) {
      location = getString("pdcPeas.pdc");
    } else if (componentId.equals("users")) {
      location = "";
    } else {
      location = getLocation(componentId);
    }

    gsr.setLocation(location);

    String userId = matchingIndexEntry.getCreationUser();
    gsr.setCreatorName(getCompleteUserName(userId));

    return gsr;
  }

  private List<GlobalSilverResult> globalSilverContents2GlobalSilverResults(
      List<GlobalSilverContent> globalSilverContents) throws Exception {
    if (globalSilverContents == null || globalSilverContents.isEmpty()) {
      return new ArrayList<>();
    }
    List<GlobalSilverResult> results = new ArrayList<>();
    for (int i = 0; i < globalSilverContents.size(); i++) {
      GlobalSilverContent gsc = globalSilverContents.get(i);
      GlobalSilverResult gsr = new GlobalSilverResult(gsc);
      String userId = gsc.getUserId();
      gsr.setCreatorName(getCompleteUserName(userId));
      gsr.setResultId(i);
      gsr.setExportable(isCompliantResult(gsr));

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

  private String getAttachmentUrl(GlobalSilverResult gsr) throws Exception {
    String componentId = gsr.getIndexEntry().getComponent();
    String id = gsr.getAttachmentId();
    String language = gsr.getAttachmentLanguage();

    SimpleDocumentPK documentPk = new SimpleDocumentPK(id, componentId);
    SimpleDocument document = AttachmentServiceProvider.getAttachmentService()
        .searchDocumentById(documentPk, language);

    // check if attachment is previewable and viewable
    File attachmentFile = new File(document.getAttachmentPath());
    boolean previewable = ViewerProvider.getPreviewService().isPreviewable(attachmentFile);
    boolean viewable = ViewerProvider.getViewService().isViewable(attachmentFile);

    gsr.setPreviewable(previewable);
    gsr.setViewable(viewable);
    gsr.setVersioned(false);
    gsr.setDownloadAllowedForReaders(document.isDownloadAllowedForReaders());
    gsr.setUserAllowedToDownloadFile(document.isDownloadAllowedForRolesFrom(getUserDetail()));

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

  private String getVersioningUrl(GlobalSilverResult gsr) throws Exception {
    String componentId = gsr.getIndexEntry().getComponent();
    String documentId = gsr.getAttachmentId();

    SimpleDocument document = AttachmentServiceProvider.getAttachmentService()
        .searchDocumentById(new SimpleDocumentPK(documentId, componentId), null);
    SimpleDocument version = document.getLastPublicVersion();

    if (version != null) {
      // check if attachment is previewable and viewable
      File file = new File(version.getAttachmentPath());
      boolean previewable = ViewerProvider.getPreviewService().isPreviewable(file);
      boolean viewable = ViewerProvider.getViewService().isViewable(file);

      gsr.setPreviewable(previewable);
      gsr.setViewable(viewable);
      gsr.setVersioned(true);
      gsr.setDownloadAllowedForReaders(document.isDownloadAllowedForReaders());
      gsr.setUserAllowedToDownloadFile(document.isDownloadAllowedForRolesFrom(getUserDetail()));

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
    this.componentList = componentList;
  }

  public List<String> getCurrentComponentIds() {
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
    return getPdcManager().getAxis();
  }

  public List<AxisHeader> getPrimaryAxis() throws PdcException {
    return getPdcManager().getAxisByType("P");
  }

  public List<SearchAxis> getAxis(String viewType) throws PdcException {
    return getAxis(viewType, new AxisFilter());
  }

  public List<SearchAxis> getAxis(String viewType, AxisFilter filter) throws PdcException {
    if (componentList == null || componentList.isEmpty()) {
      if (StringUtil.isDefined(getCurrentComponentId())) {
        return getPdcManager().getPertinentAxisByInstanceId(searchContext, viewType,
            getCurrentComponentId());
      }
      return new ArrayList<>();
    } else {
      // we get all axis (pertinent or not) from a type P or S
      List<AxisHeader> axis = getPdcManager().getAxisByType(viewType);
      // we have to transform all axis (AxisHeader) into SearchAxis to make
      // the display into jsp transparent
      return transformAxisHeadersIntoSearchAxis(axis);
    }
  }

  private List<SearchAxis> transformAxisHeadersIntoSearchAxis(List<AxisHeader> axis) {
    ArrayList<SearchAxis> transformedAxis = new ArrayList<>();
    try {
      for (int i = 0; i < axis.size(); i++) {
        AxisHeader ah = axis.get(i);
        SearchAxis sa = new SearchAxis(Integer.parseInt(ah.getPK().getId()), 0);
        // sa.setAxisName(ah.getName());
        sa.setAxis(ah);
        sa.setAxisRootId(Integer.parseInt(
            getPdcManager().getRoot(ah.getPK().getId()).getValuePK().getId()));
        sa.setNbObjects(1);
        transformedAxis.add(sa);
      }
    } catch (Exception ignored) {
    }
    return transformedAxis;
  }

  public List<Value> getDaughterValues(String axisId, String valueId)
      throws PdcException {
    return getDaughterValues(axisId, valueId, new AxisFilter());
  }

  public List<Value> getDaughterValues(String axisId, String valueId, AxisFilter filter)
      throws PdcException {
    List<Value> values;
    if (componentList == null || componentList.isEmpty()) {
      values = getPdcManager().getPertinentDaughterValuesByInstanceId(searchContext,
          axisId, valueId, getCurrentComponentId());
    } else {
      if (isShowOnlyPertinentAxisAndValues()) {
        values = getPdcManager().getPertinentDaughterValuesByInstanceIds(
            searchContext, axisId, valueId, getCopyOfInstanceIds());
      } else {
        values = setNBNumbersToOne(getPdcManager().getDaughters(axisId, valueId));
      }
    }

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

    List<Value> result;
    if (componentList == null || componentList.isEmpty()) {
      result = getPdcManager().getFirstLevelAxisValuesByInstanceId(searchContext,
          axisId, getCurrentComponentId());
    } else {
      result = getPdcManager().getFirstLevelAxisValuesByInstanceIds(searchContext,
          axisId, getCopyOfInstanceIds());
    }

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
    Axis axis = getPdcManager().getAxisDetail(axisId, filter);
    return axis;
  }

  public AxisHeader getAxisHeader(String axisId) throws PdcException {
    AxisHeader axisHeader = getPdcManager().getAxisHeader(axisId);
    return axisHeader;
  }

  public List<Value> getFullPath(String valueId, String treeId) throws PdcException {
    return getPdcManager().getFullPath(valueId, treeId);
  }

  public Value getAxisValue(String valueId, String treeId) throws PdcException {
    return getPdcManager().getAxisValue(valueId, treeId);
  }

  public Value getValue(String axisId, String valueId) throws PdcException {
    return getPdcManager().getValue(axisId, valueId);
  }

  public void setContainerPeas(ContainerPeas containerGivenPeasPDC) {
    containerPeasPDC = containerGivenPeasPDC;
  }

  public ContainerPeas getContainerPeas() {
    return containerPeasPDC;
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

    List<String> instanceIds = new ArrayList<>();
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
  private ThesaurusManager thesaurus = PdcServiceProvider.getThesaurusManager();
  private boolean activeThesaurus = false; // thesaurus actif
  private Jargon jargon = null;// jargon utilisé par l'utilisateur
  private Map<String, Collection<String>> synonyms = new HashMap<>();
  private static final int QUOTE_CHAR = new Integer('"');
  private static String[] KEYWORDS = null;
  private boolean isThesaurusEnableByUser = false;

  private boolean getActiveThesaurusByUser() {
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
    String synonymsQueryString;
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
        Collection<String> synos = thesaurus.getSynonyms(mot, getUserId());
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
    return new HashMap<>();
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
        LocalizationBundle resource =
            ResourceLocator.getLocalizationBundle("org.silverpeas.index.indexing.StopWords",
                getLanguage());
        List<String> wordList = new ArrayList<>(resource.keySet());
        KEYWORDS = wordList.toArray(new String[wordList.size()]);
      } catch (MissingResourceException e) {
        SilverLogger.getLogger(this)
            .warn("Missing stop words in org.silverpeas.search.indexing.StopWords");
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
        PlainSearchResult result = SearchEngineProvider.getSearchEngine().search(queryDescription);
        glossaryResults = result.getEntries().toArray(new MatchingIndexEntry[result.getEntries().
            size()]);
      } catch (org.silverpeas.core.index.search.model.ParseException e) {
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

    List<Value> axises = new ArrayList<>();

    for (int i = 0; i < allAxis.size(); i++) {
      AxisHeader sa = allAxis.get(i);
      String rootId = String.valueOf(sa.getRootId());
      List<Value> daughters = getPdcManager().getFilteredAxisValues(rootId, filter);
      axises.addAll(daughters);
    }
    return axises;
  }

  public List<AxisHeader> getUsedAxisHeaderByInstanceId(String instanceId)
      throws PdcException {
    List<UsedAxis> usedAxisList = getPdcManager().getUsedAxisByInstanceId(instanceId);
    List<AxisHeader> allAxis = new ArrayList<>(usedAxisList.size());
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
    List<String> usedTreeIds = new ArrayList<>(usedAxisHeaders.size());
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
    Value value = getPdcManager().getAxisValue(valueId, treeId);
    if (value != null) {
      value.setPathValues(getFullPath(valueId, treeId));
    }
    return value;
  }

  public List<Axis> getUsedAxisByAComponentInstance(String instanceId)
      throws PdcException {
    List<UsedAxis> usedAxisList = getPdcManager().getUsedAxisByInstanceId(instanceId);
    List<Axis> axisList = new ArrayList<>(usedAxisList.size());
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
  public int saveICenter(Interests ic) throws PdcException {
    try {
      int userId = Integer.parseInt(getUserId());
      ic.setOwnerID(userId);
      return InterestsManager.getInstance().createInterests(ic);
    } catch (Exception e) {
      throw new PdcPeasRuntimeException(
          "PdcSearchSessionController.saveICenter", SilverpeasException.ERROR,
          "pdcPeas.EX_SAVE_IC", e);
    }
  }

  public List<Interests> getICenters() {
    try {
      int id = Integer.parseInt(getUserId());
      return InterestsManager.getInstance().getInterestsByUserId(id);
    } catch (Exception e) {
      throw new PdcPeasRuntimeException(
          "PdcSearchSessionController.getICenters", SilverpeasException.ERROR,
          "pdcPeas.EX_GET_IC", e);
    }
  }

  public Interests loadICenter(String icId) throws PdcException {
    try {
      int id = Integer.parseInt(icId);
      Interests ic = InterestsManager.getInstance().getInterestsById(id);
      if (StringUtil.isDefined(ic.getPeasID()) &&
          !getComponentAccessController().isUserAuthorized(getUserId(), ic.getPeasID())) {
        ic.setPeasID(null);
      }
      getSearchContext().clearCriterias();
      List<? extends Criteria> criterias = ic.getPdcContext();
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
   * UserPanel methods /
   * ****************************************************************************************************************
   */
  public String initUserPanel() throws RemoteException {
    String m_context = URLUtil.getApplicationURL();
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

    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), null);
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
    componentList = new ArrayList<>();

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
    List<String> excluded = new ArrayList<>();

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
    List<String> items = new ArrayList<>();
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
    componentList = new ArrayList<>();

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
    List<ComponentInstLight> allowedList = new ArrayList<>();
    if (space != null) {
      String[] asAvailCompoForCurUser =
          getOrganisationController().getAvailCompoIdsAtRoot(space, getUserId());
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
      SilverLogger.getLogger(this).warn("Error while getting component label: {0}", e.getMessage());
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
   * Date primitives *
   */
  /**
   * ******************************************************************************************
   */
  public String getUrl(String urlBase, MatchingIndexEntry indexEntry) {
    return getUrl(urlBase, indexEntry.getComponent(), indexEntry.getPageAndParams());
  }

  public String getUrl(String urlBase, String componentId, String pageAndParams) {
    String url = urlBase + URLUtil.getURL(null, componentId) + pageAndParams;
    if (url.contains("?")) {
      url += "&From=Search";
    } else {
      url += "?From=Search";
    }
    return url;
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
  private PdcManager pdcManager = null; // To retrieve items from PDC

  // searchEngine
  private PdcManager getPdcManager() {
    if (pdcManager == null) {
      pdcManager = (PdcManager) new GlobalPdcManager();
    }
    return pdcManager;
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
      return new ArrayList<>();
    }
    List<MatchingIndexEntry> results = new ArrayList<>(matchingIndexEntries.length);
    try {
      getSecurityIntf().enableCache();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e.getMessage(), e);
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
    } catch (Exception ignored) {

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
    PublicationTemplateImpl template;
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
  private void setGlobalSR(List<GlobalSilverResult> globalSR) {
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
    return new ArrayList<>(componentList);
  }

  /**
   * Retrieve configuration from properties file
   *
   * @return a list of search type configuration value object
   */
  public List<SearchTypeConfigurationVO> getSearchTypeConfig() {
    if (dataSearchTypes == null) {
      List<SearchTypeConfigurationVO> configs = new ArrayList<>();

      int cpt = 1;
      String postConfigKey = "search.type.";
      String componentsValue = getSettings().getString(postConfigKey + cpt + ".components", "");
      while (StringUtil.isDefined(componentsValue)) {
        String typesValue = getSettings().getString(postConfigKey + cpt + ".types", "");
        String nameValue = getString(postConfigKey + cpt + ".label");

        List<String> listComponents = Arrays.asList(componentsValue.split(","));
        List<String> listTypes = new ArrayList<>();
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

  private SearchTypeConfigurationVO getSearchType(String componentId, String type, Map<String, ComponentInstLight> components) {
    ComponentInstLight component = components.get(componentId);
    if (component == null) {
      component = getOrganisationController().getComponentInstLight(componentId);
      components.put(componentId, component);
    }
    if (component != null) {
      for (SearchTypeConfigurationVO aSearchType : getSearchTypeConfig()) {
        if (aSearchType.getComponents().contains(component.getName())) {
          if (aSearchType.getTypes().isEmpty() || aSearchType.getTypes().contains(type)) {
            return aSearchType;
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
