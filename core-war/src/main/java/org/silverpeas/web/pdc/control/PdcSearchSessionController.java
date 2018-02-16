/*
 * Copyright (C) 2000 - 2018 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.pdc.control;

import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.component.model.SilverpeasComponentInstance;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.silverpeas.core.admin.user.UserIndexation;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FieldDisplayer;
import org.silverpeas.core.contribution.content.form.FieldTemplate;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.PagesContext;
import org.silverpeas.core.contribution.content.form.TypeManager;
import org.silverpeas.core.contribution.content.form.field.DateField;
import org.silverpeas.core.contribution.content.form.field.TextFieldImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateException;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateImpl;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.exception.UtilException;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.index.search.model.SearchEngineException;
import org.silverpeas.core.index.search.model.SearchResult;
import org.silverpeas.core.pdc.PdcServiceProvider;
import org.silverpeas.core.pdc.classification.Criteria;
import org.silverpeas.core.pdc.interests.model.Interests;
import org.silverpeas.core.pdc.interests.service.InterestsManager;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.SearchContext;
import org.silverpeas.core.pdc.pdc.model.SearchCriteria;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.core.pdc.pdc.service.GlobalPdcManager;
import org.silverpeas.core.pdc.pdc.service.PdcManager;
import org.silverpeas.core.pdc.thesaurus.model.Jargon;
import org.silverpeas.core.pdc.thesaurus.model.ThesaurusException;
import org.silverpeas.core.pdc.thesaurus.service.ThesaurusManager;
import org.silverpeas.core.search.SearchService;
import org.silverpeas.core.silverstatistics.access.model.StatisticRuntimeException;
import org.silverpeas.core.silverstatistics.access.service.StatisticService;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.MimeTypes;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.WebEncodeHelper;
import org.silverpeas.core.util.file.FileFolderManager;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.viewer.service.ViewerProvider;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.selection.Selection;
import org.silverpeas.web.pdc.QueryParameters;
import org.silverpeas.web.pdc.vo.ExternalSPConfigVO;
import org.silverpeas.web.pdc.vo.Facet;
import org.silverpeas.web.pdc.vo.FacetEntryVO;
import org.silverpeas.web.pdc.vo.FacetOnDates;
import org.silverpeas.web.pdc.vo.ResultFilterVO;
import org.silverpeas.web.pdc.vo.ResultGroupFilter;
import org.silverpeas.web.pdc.vo.SearchTypeConfigurationVO;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.io.StringWriter;
import java.rmi.RemoteException;
import java.time.LocalDate;
import java.util.*;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

public class PdcSearchSessionController extends AbstractComponentSessionController {

  public static final String SORT_ORDER_ASC = "ASC";
  public static final String SORT_ORDER_DESC = "DESC";
  // only field query
  public static final int SEARCH_SIMPLE = 0;
  // Simple + filter
  public static final int SEARCH_ADVANCED = 1;
  // Advanced + pdc
  public static final int SEARCH_EXPERT = 2;
  // XML
  public static final int SEARCH_XML = 3;
  public static final int SEARCH_FULLTEXT = 0;
  public static final int SEARCH_PDC = 1;
  public static final int SEARCH_MIXED = 2;
  public static final int SHOWRESULTS_ALL = 0;
  public static final int SHOWRESULTS_ONLY_PDC = 1;
  // Component search type
  public static final String ALL_DATA_TYPE = "0";
  private static final int DEFAULT_NBRESULTS_PERPAGE = 10;
  private static final String LOCATION_SEPARATOR = ">";
  private static final int QUOTE_CHAR = (int) '"';
  private static final String USER_PREFIX = "user@";
  private static final String PUBLICATION_RESOURCE = "Publication";
  private static final String VERSIONING_RESOURCE = "Versioning";
  private static final String NODE_RESOURCE = "Node";
  private static final String FILE_RESOURCE = "File";
  private static final String KMELIA_COMPONENT = "kmelia";
  private static final String KMAX_COMPONENT = "kmax";
  private static final String TOOLBOX_COMPONENT = "toolbox";
  private static final String GALLERY_COMPONENT = "gallery";
  private static final String SILVER_CRAWLER_COMPONENT = "silverCrawler";
  private static final String DIRECTORY_SERVICE = "users";
  private static final String PDC_SERVICE = "pdc";
  private static final String SPACES_INDEX = "Spaces";
  private static final String COMPONENTS_INDEX = "Components";

  private static String[] KEYWORDS = null;
  // Container and Content Peas
  private SearchContext searchContext = null;
  // Current parameters for plain search in PDC
  private QueryParameters queryParameters = null;
  private List<String> componentList = null;
  private String isSecondaryShowed = "NO";
  private boolean showOnlyPertinentAxisAndValues = true;
  private List<GlobalSilverResult> globalSR = new ArrayList<>();
  private List<GlobalSilverResult> filteredSR = new ArrayList<>();
  private int indexOfFirstItemToDisplay = 1;
  private int nbItemsPerPage = -1;
  private Value currentValue = null;
  // Pagination of result list (search Engine)
  private int indexOfFirstResultToDisplay = 0;
  // All,  Res or Req
  private String displayParamChoices = null;
  private int nbResToDisplay = DEFAULT_NBRESULTS_PERPAGE;
  // 1, 2, 3, 4 or 5
  private int sortValue = -1;
  // ASC || DESC
  private String sortOrder = null;
  private boolean isRefreshEnabled = false;
  private int searchType = SEARCH_EXPERT;
  private String searchPage = null;
  private String searchPageId = null;
  private String resultPage = null;
  private String resultPageId = null;
  // XML Search Session's objects
  private PublicationTemplateImpl xmlTemplate = null;
  private DataRecord xmlData = null;
  private int searchScope = SEARCH_FULLTEXT;
  // Field value of XML form used to sort results
  private String xmlFormSortValue = null;
  // Keyword used to retrieve the implementation to realize sorting or filtering
  private String sortImplementor = null;
  private int currentResultsDisplay = SHOWRESULTS_ALL;
  // Spelling word
  private List<String> spellingWords = Collections.emptyList();
  // Activate external search
  private boolean isEnableExternalSearch = false;
  private List<ExternalSPConfigVO> externalServers = null;
  private String dataType = ALL_DATA_TYPE;
  private List<SearchTypeConfigurationVO> dataSearchTypes = null;
  // Forms fields facets from current results
  private Map<String, Facet> fieldFacets = null;
  // Facets entry selected by the user
  private ResultFilterVO selectedFacetEntries = null;
  private boolean platformUsesPDC = false;
  private boolean includeUsers = false;
  private boolean includePDC = false;
  private List<GlobalSilverResult> selectedSilverContents = null;
  private ThesaurusManager thesaurus = PdcServiceProvider.getThesaurusManager();
  // Vocabulary used by the user
  private Jargon jargon = null;
  private Map<String, Collection<String>> synonyms = new HashMap<>();

  private boolean isThesaurusEnableByUser = false;

  // To retrieve items from PDC
  private PdcManager pdcManager = null;

  public PdcSearchSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext, String multilangBundle,
      String iconBundle) {
    super(mainSessionCtrl, componentContext, multilangBundle, iconBundle,
        "org.silverpeas.pdcPeas.settings.pdcPeasSettings");

    isRefreshEnabled = getSettings().getBoolean("EnableRefresh", true);

    try {
      isThesaurusEnableByUser = getActiveThesaurusByUser();
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
      isThesaurusEnableByUser = false;
    }

    this.searchContext = new SearchContext(this.getUserId());

    // Initialize external search
    isEnableExternalSearch = getSettings().getBoolean("external.search.enable", false);
    getExternalSPConfig();

    try {
      platformUsesPDC = !getPdcManager().getAxis().isEmpty();
    } catch (PdcException e) {
      SilverLogger.getLogger(this).error(e);
    }

    includeUsers = getSettings().getBoolean("search.users.included", false);
    includePDC = getSettings().getBoolean("search.pdc.included", false);
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
        try {
          srvName = getSettings().getString(prefixKey + cptSrv + nameKey);
        } catch (MissingResourceException e) {
          SilverLogger.getLogger(this).debug("", e);
          break;
        }
      }
    }
  }

  public SearchContext getSearchContext() {
    return this.searchContext;
  }

  /**
   * **************************************************************************************************************
   * PDC search methods (via DomainsBar)
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

  public int getNbResToDisplay() {
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
      if (fileName.contains(authorizedComp) && isDataTypeSearch(fileName)) {
        query.addExternalComponents(extServerCfg.getName(), fileName, extServerCfg.getDataPath(),
            extServerCfg.getUrl());
      }
    }
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
    Facet lastUpdateFacet = new FacetOnDates("lastUpdate", getString("pdcPeas.facet.lastUpdate"));

    // key is the fieldName
    Map<String, Facet> fieldFacetsMap = new HashMap<>();

    if (results != null) {
      // Retrieve the black list component (we don't need to filter data on it)
      List<String> blackList = getFacetBlackList();
      Map<String, ComponentInstLight> components = new HashMap<>();
      Map<String, String> userNames = new HashMap<>();

      // Loop on each result
      for (GlobalSilverResult result : results) {
        if (isEnableExternalSearch && result.isExternalResult()) {
          continue;
        }

        // manage "author" facet
        processFacetAuthor(authorFacet, result, userNames);

        processFacetLastUpdate(lastUpdateFacet, result);

        // manage "datatype" facet
        processFacetDatatype(dataTypeFacet, result, components);

        // manage "component" facet
        processFacetComponent(componentFacet, result, blackList, components);

        processFacetFiletype(fileTypeFacet, result);

        // manage forms fields facets
        processFacetsFormField(fieldFacetsMap, result);
      }

      components.clear();
    }

    // Fill result filter with current result values
    res.setAuthorFacet(authorFacet);
    res.setLastUpdateFacet(lastUpdateFacet);
    res.setComponentFacet(componentFacet);
    res.setDatatypeFacet(dataTypeFacet);
    res.setFiletypeFacet(fileTypeFacet);
    res.setFormFieldFacets(new ArrayList<>(fieldFacetsMap.values()));

    // sort facets entries descending
    res.sortFacetsEntries();

    this.fieldFacets = fieldFacetsMap;

    return res;
  }

  private void processFacetLastUpdate(Facet facet, GlobalSilverResult result) {
    LocalDate lastUpdate = result.getLastUpdateDate();
    if (lastUpdate != null) {
      FacetEntryVO facetEntry = ((FacetOnDates) facet).addEntry(lastUpdate);
      if (getSelectedFacetEntries() != null) {
        String year = String.valueOf(lastUpdate.getYear());
        if (year.equals(getSelectedFacetEntries().getLastUpdate())) {
          facetEntry.setSelected(true);
        }
      }
    }
  }

  private void processFacetAuthor(Facet facet, GlobalSilverResult result,
      Map<String, String> userNames) {
    String authorId = result.getCreatorId();
    String authorName = userNames.computeIfAbsent(authorId, k -> result.getCreatorName());
    if (StringUtil.isDefined(authorId) && StringUtil.isDefined(authorName)) {
      FacetEntryVO facetEntry = new FacetEntryVO(authorName, authorId);
      if (getSelectedFacetEntries() != null &&
          authorId.equals(getSelectedFacetEntries().getAuthorId())) {
        facetEntry.setSelected(true);
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
        if (getSelectedFacetEntries() != null &&
            String.valueOf(theSearchType.getConfigId()).equals(getSelectedFacetEntries().
                getDatatype())) {
          facetEntry.setSelected(true);
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
        if (getSelectedFacetEntries() != null &&
            extension.equals(getSelectedFacetEntries().getFiletype())) {
          facetEntry.setSelected(true);
        }
        facet.addEntry(facetEntry);
      }
    }
  }

  private void processFacetComponent(Facet facet, GlobalSilverResult result, List<String> blackList,
      Map<String, ComponentInstLight> components) {
    String instanceId = result.getInstanceId();
    String type = result.getType();
    if (!blackList.contains(type)) {
      FacetEntryVO facetEntry = facet.getEntryById(instanceId);
      if (facetEntry == null) {
        Mutable<String> appLabel = Mutable.empty();
        ComponentInstLight component = components.computeIfAbsent(instanceId,
            k -> getOrganisationController().getComponentInstLight(instanceId));
        if (component != null) {
          appLabel.set(component.getLabel(getLanguage()));
        }
        String appLocation = appLabel.orElse("");
        if (StringUtil.isNotDefined(appLocation) && DIRECTORY_SERVICE.equals(instanceId)) {
          appLocation = getString("pdcPeas.facet.service.directory");
        }
        if (StringUtil.isDefined(appLocation)) {
          facetEntry = new FacetEntryVO(appLocation, instanceId);

          if (getSelectedFacetEntries() != null &&
              instanceId.equals(getSelectedFacetEntries().getComponentId())) {
            facetEntry.setSelected(true);
          }
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
            FieldTemplate fieldTemplate = getFieldTemplate(formName, fieldName);
            if (fieldTemplate.getTypeName().equals(DateField.TYPE)) {
              facet = new FacetOnDates(facetId, fieldTemplate.getLabel(getLanguage()));
            } else {
              facet = new Facet(facetId, fieldTemplate.getLabel(getLanguage()));
            }
          } catch (Exception e) {
            SilverLogger.getLogger(this).error(e.getMessage(), e);
          }
          fieldFacetsMap.put(facetId, facet);
        } else {
          // facet already initialized
          facet = fieldFacetsMap.get(facetId);
        }
        if (facet != null) {
          FacetEntryVO entry = null;
          String fieldValueKey = fieldsForFacets.get(facetId);
          if (facet instanceof FacetOnDates) {
            entry = ((FacetOnDates) facet).addEntry(fieldValueKey);
          } else {
            String fieldValueLabel = fieldValueKey;
            try {
              fieldValueLabel = getFieldValue(formName, fieldName, fieldValueKey);
            } catch (Exception e) {
              SilverLogger.getLogger(this).error(e.getMessage(), e);
            }
            entry = new FacetEntryVO(fieldValueLabel, fieldValueKey);
            facet.addEntry(entry);
          }
          // check if this entry have been selected
          if (getSelectedFacetEntries() != null) {
            String selectedEntry =
                getSelectedFacetEntries().getFormFieldSelectedFacetEntry(facet.getId());
            boolean selected = false;
            if (facet instanceof FacetOnDates) {
              selected = StringUtil.startsWith(fieldValueKey, selectedEntry);
            } else {
              selected = selectedEntry != null && selectedEntry.equals(fieldValueKey);
            }
            entry.setSelected(selected);
          }
        }
      }
    }
  }

  public Map<String, Facet> getFieldFacets() {
    return fieldFacets;
  }

  private FieldTemplate getFieldTemplate(String formName, String fieldName)
      throws PublicationTemplateException, FormException {
    PublicationTemplate form =
        PublicationTemplateManager.getInstance().loadPublicationTemplate(formName);
    return form.getRecordTemplate().getFieldTemplate(fieldName);
  }

  private String getFieldValue(String formName, String fieldName, String fieldValue)
      throws PublicationTemplateException, FormException {
    FieldTemplate fieldTemplate = getFieldTemplate(formName, fieldName);
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
        int nbAccess = statisticService.getCount(pk, 1, PUBLICATION_RESOURCE);
        result.setHits(nbAccess);
      }
    }
  }

  private boolean isPopularityCompliant(GlobalSilverResult gsr) {
    final String instanceId = gsr != null ? defaultStringIfNotDefined(gsr.getInstanceId()) : "";
    final String resourceType = gsr != null ? gsr.getType() : "";
    final boolean isHandledComponent = instanceId.
        startsWith(KMELIA_COMPONENT) || instanceId.startsWith(KMAX_COMPONENT) || instanceId.
        startsWith(TOOLBOX_COMPONENT);
    return isHandledComponent && PUBLICATION_RESOURCE.equals(resourceType);
  }

  private StatisticService getStatisticBm() {
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

    String lastUpdateFilter = filter.getLastUpdate();
    boolean filterLastUpdate = StringUtil.isDefined(lastUpdateFilter);

    boolean filterFormFields = !filter.isSelectedFormFieldFacetsEmpty();

    List<String> blackList = getFacetBlackList();
    Map<String, ComponentInstLight> components = new HashMap<>();

    for (GlobalSilverResult gsResult : listGSR) {
      if (!blackList.contains(gsResult.getType())) {
        String gsrUserId = gsResult.getCreatorId();
        String gsrInstanceId = gsResult.getInstanceId();
        boolean visible = true;

        // check author facet
        if (filterAuthor && !gsrUserId.equals(authorFilter)) {
          visible = false;
        }

        if (visible && filterLastUpdate) {
          String year = String.valueOf(gsResult.getLastUpdateDate().getYear());
          visible = year.equals(lastUpdateFilter);
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
                Facet uiFacet = getFieldFacets().get(facet.getKey());
                if (uiFacet instanceof FacetOnDates) {
                  visible =
                      resultFieldValue != null && resultFieldValue.startsWith(facet.getValue());
                } else {
                  // visible if stored value is equals to selected facet entry
                  visible = facet.getValue().equalsIgnoreCase(resultFieldValue);
                }
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
      resultType = result.getType();
      if (!StringUtil.isDefined(resultType)) {
        resultType = "";
      }
      String componentId = result.getInstanceId();
      downloadLink = null;
      // create the url part to activate the mark as read functionality
      if (isEnableMarkAsRead) {
        markAsReadJS = "markAsRead('" + result.getResultId() + "');";
      }
      if (VERSIONING_RESOURCE.equals(resultType)) {
        // Added to be compliant with old indexing method
        resultType = PUBLICATION_RESOURCE;
      }

      // Declare if it's an internal server search
      boolean isInternalSearch = true;

      // Add only when External Search is enabled
      // Check if external search exists
      if (isEnableExternalSearch && StringUtil.isDefined(result.getServerName())) {
        // build external search url location
        String serverName = result.getServerName();
        if (StringUtil.isDefined(serverName) && result.isExternalResult()) {
          isInternalSearch = false;
          titleLink = buildExternalServerURL(resultType, markAsReadJS, result, serverName);
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
            underLink = getResultURL(result);
            int iStart = underLink.indexOf("Attachment");
            int iEnd = underLink.indexOf('&', iStart);
            underLink = underLink.substring(0, iStart) + PUBLICATION_RESOURCE +
                underLink.substring(iEnd, underLink.length());
            StringBuilder titleLinkBuilder = new StringBuilder(256);
            titleLinkBuilder.append("javascript:").append(markAsReadJS).append(" window.open('").
                append(WebEncodeHelper.javaStringToJsString(downloadLink)).append("');jumpToComponent('").append(componentId).
                append("');document.location.href='").append(WebEncodeHelper.javaStringToJsString(underLink)).append("&FileOpened=1';");
            titleLink = titleLinkBuilder.toString();
          } else {
            ComponentInstLight componentInst = getOrganisationController().getComponentInstLight(componentId);
            if (componentInst != null) {
              String title = componentInst.getLabel(getLanguage());
              result.setName(title);
              underLink = URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, componentId);
              titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId +
                  "');document.location.href='" + underLink + "';";
            }
          }
        } else if (resultType.startsWith(VERSIONING_RESOURCE)) {
          try {
            downloadLink = getVersioningUrl(result);
          } catch (Exception e) {
            SilverLogger.getLogger(this).error(e.getMessage(), e);
          }
          underLink = getResultURL(result);
          int iStart = underLink.indexOf(VERSIONING_RESOURCE);
          int iEnd = underLink.indexOf('&', iStart);
          underLink = underLink.substring(0, iStart) + PUBLICATION_RESOURCE +
              underLink.substring(iEnd, underLink.length());
          titleLink = buildTitleLink(markAsReadJS, downloadLink, componentId, underLink, true);
        } else if ("LinkedFile".equals(resultType)) {
          // open the linked file inside a popup window
          downloadLink = FileServerUtils.getUrl(result.getName(), result.getId(), componentId);
          // window opener is reloaded on the main page of the component
          underLink = URLUtil.getApplicationURL() + URLUtil.getURL("useless", componentId) + "Main";
          titleLink = buildTitleLink(markAsReadJS, downloadLink, componentId, underLink, false);
        } else if ("TreeNode".equals(resultType)) {
          // the PDC uses this type of object.
          titleLink = "javascript:" + markAsReadJS;
        } else if ("Space".equals(resultType)) {
          // retour sur l'espace
          String spaceId = result.getId();
          titleLink = "javascript:" + markAsReadJS + " goToSpace('" + spaceId +
              "');document.location.href='" + URLUtil.getSimpleURL(URLUtil.URL_SPACE, spaceId) +
              "';";
        } else if ("Component".equals(resultType)) {
          // retour sur le composant
          underLink = URLUtil.getSimpleURL(URLUtil.URL_COMPONENT, result.getId());
          titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId +
              "');document.location.href='" + underLink + "';";
        } else if (UserIndexation.OBJECT_TYPE.equals(resultType)) {
          User user = User.getById(result.getId());
          if (user != null) {
            result.setThumbnailURL(user.getSmallAvatar());
          }
          titleLink = "javascript:" + markAsReadJS + " viewUserProfile('" + result.getId() + "');";
        } else if ("todo".equals(resultType)) {
          titleLink =
              "javascript:" + markAsReadJS + " document.location.href='" + getResultURL(result) +
                  "';";
        } else {
          titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId
              + "');";
          titleLink += "document.location.href='" + getResultURL(result) + "';";
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

      // Check if it's an external search before searching components information
      String place;
      if (result.isExternalResult()) {
        place = getString("pdcPeas.external.search.label") + " ";
        place += getExternalServerLabel(result.getServerName());
      } else {
        // preparation sur l'emplacement du document
        if (componentId.startsWith(USER_PREFIX)) {
          UserDetail user = getOrganisationController().getUserDetail(
              componentId.substring(5, componentId.indexOf("_")));
          String component = componentId.substring(componentId.indexOf("_") + 1);
          place = user.getDisplayedName() + " " + LOCATION_SEPARATOR + " " + component;
        } else if (PDC_SERVICE.equals(componentId)) {
          place = getString("pdcPeas.pdc");
        } else if (DIRECTORY_SERVICE.equals(componentId)) {
          place = "";
        } else {
          place = getLocation(componentId);
        }
      }
      result.setLocation(place);
    }
  }

  /**
   * Only called when isEnableExternalSearch is activated. Build an external link using Silverpeas
   * permalink
   *
   * @see URLUtil#getSimpleURL
   * @param resultType the result type
   * @param markAsReadJS javascript string to mark this result as read
   * @param result the current result
   * @param serverName the server name string
   * @return a string which represents an external server URL
   */
  private String buildExternalServerURL(String resultType, String markAsReadJS,
      GlobalSilverResult result, String serverName) {
    StringBuilder extURLSB = new StringBuilder();
    for (ExternalSPConfigVO extSrv : externalServers) {
      if (serverName.equalsIgnoreCase(extSrv.getName())) {
        extURLSB.append("javascript:").append(markAsReadJS).append(" ");
        extURLSB.append("window.open('").append(extSrv.getUrl());
        // Retrieve the URLUtil type
        int type = 0;
        String objectId = "";
        String compId = result.getInstanceId();
        if (PUBLICATION_RESOURCE.equals(resultType)) {
          // exemple http://server/silverpeas/Publication/ID_PUBLI
          type = URLUtil.URL_PUBLI;
          objectId = result.getId();
        } else if (NODE_RESOURCE.equals(resultType)) {
          // exemple http://server/silverpeas/Topic/ID_TOPIC?ComponentId=ID_COMPONENT
          type = URLUtil.URL_TOPIC;
          objectId = result.getId();
        } else if (FILE_RESOURCE.equals(resultType)) {
          // exemple http://server/silverpeas/File/ID_FILE
          type = URLUtil.URL_FILE;
          objectId = result.getId();
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
    titleLinkBuilder.append(WebEncodeHelper.javaStringToJsString(downloadLink));
    titleLinkBuilder.append("');jumpToComponent('").append(componentId);
    titleLinkBuilder.append("');document.location.href='");
    titleLinkBuilder.append(WebEncodeHelper.javaStringToJsString(underLink));
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
      return getSpaceLabel(spaceId) + " " + LOCATION_SEPARATOR + " " +
          getComponentLabel(spaceId, instanceId);
    }
    return "";
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
  private boolean processResult(SearchResult result, List<String> objectTypeFilter) {
    // Default loop variable
    boolean processThisResult = true;

    // Check if we filter this object type or not before doing any data processing
    if (objectTypeFilter != null && !objectTypeFilter.isEmpty()) {
      // If object type filter is defined, change processThisResult default value.
      processThisResult = false;
      for (String objType : objectTypeFilter) {
        if (result.getType().equalsIgnoreCase(objType)) {
          processThisResult = true;
        }
      }
    }
    return processThisResult;
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

  private String getAttachmentUrl(GlobalSilverResult gsr) throws Exception {
    String componentId = gsr.getInstanceId();
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
    String componentId = gsr.getInstanceId();
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

  public void removeAllCriterias() {
    this.searchContext.clearCriterias();
  }

  public List<GlobalSilverResult> getSelectedSilverContents() {
    return selectedSilverContents;
  }

  public void setSelectedSilverContents(List<GlobalSilverResult> silverContents) {
    selectedSilverContents = silverContents;
  }

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

  public Interests loadICenter(String icId) {
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

    return Selection.getSelectionURL();
  }

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
    if (componentId.startsWith(SILVER_CRAWLER_COMPONENT)
        || componentId.startsWith(GALLERY_COMPONENT)
        || componentId.startsWith(KMELIA_COMPONENT)) {
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
      if (!spaceId.startsWith(USER_PREFIX) && !"transverse".equals(spaceId)) {
        componentInst = getOrganisationController().getComponentInstLight(
            componentId);
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .warn("Error while getting component label: {0}", e.getMessage(), e);
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

  // searchEngine
  private PdcManager getPdcManager() {
    if (pdcManager == null) {
      pdcManager = new GlobalPdcManager();
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
    String instanceId = defaultStringIfNotDefined(result.getInstanceId());
    String type = result.getType();
    final boolean isHandledComponent =
        instanceId.startsWith(KMELIA_COMPONENT) || instanceId.startsWith(TOOLBOX_COMPONENT);
    return isHandledComponent && !NODE_RESOURCE.equals(type);
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

  public PublicationTemplateImpl setXmlTemplate(String fileName) {
    // init xml template data
    clearXmlTemplateAndData();
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

  private void clearXmlTemplateAndData() {
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

  private void resetSearchPage() {
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

  private void resetSearchPageId() {
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

  private void resetResultPage() {
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

  private void resetResultPageId() {
    resultPageId = null;
  }

  /**
   * gets suggestions or spelling words if a search doesn't return satisfying results. A minimal
   * score trigger the suggestions search (0.5 by default)
   *
   * @return array that contains suggestions.
   */
  public List<String> getSpellingWords() {
    return spellingWords;
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

  public List<GlobalSilverResult> getFilteredSR() {
    if (filteredSR.isEmpty()) {
      // filtered list has not been processed yet
      return globalSR;
    }
    return filteredSR;
  }

  public void setFilteredSR(List<GlobalSilverResult> filteredSR) {
    this.filteredSR = filteredSR;
  }

  public void clearFilteredSR() {
    filteredSR.clear();
  }

  public int getCurrentResultsDisplay() {
    return currentResultsDisplay;
  }

  public void setCurrentResultsDisplay(int currentResultsDisplay) {
    this.currentResultsDisplay = currentResultsDisplay;
  }

  public void setCurrentResultsDisplay(String param) {
    int currentResultDisplay = 0;
    if (StringUtil.isInteger(param)) {
      currentResultDisplay = Integer.parseInt(param);
    }
    setCurrentResultsDisplay(currentResultDisplay);
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

  private SearchTypeConfigurationVO getSearchType(String componentId, String type,
      Map<String, ComponentInstLight> components) {
    if (isNotAContributionFromAComponent(componentId)) {
      return null;
    }
    ComponentInstLight component = components.computeIfAbsent(componentId,
        k -> getOrganisationController().getComponentInstLight(componentId));
    if (component != null) {
      for (SearchTypeConfigurationVO aSearchType : getSearchTypeConfig()) {
        if (aSearchType.getComponents().contains(component.getName())) {
          final boolean isSearchedType =
              aSearchType.getTypes().isEmpty() || aSearchType.getTypes().contains(type);
          if (isSearchedType) {
            return aSearchType;
          }
        }
      }
    }
    return null;
  }

  private boolean isNotAContributionFromAComponent(String componentId) {
    boolean isUserRelative = DIRECTORY_SERVICE.equals(componentId) ||
        defaultStringIfNotDefined(componentId).startsWith(USER_PREFIX);
    return isUserRelative || PDC_SERVICE.equals(componentId) ||
        SPACES_INDEX.equals(componentId) || COMPONENTS_INDEX.equals(componentId);
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
  private boolean isDataTypeSearch(String curComp) {
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

  public ResultFilterVO getSelectedFacetEntries() {
    return selectedFacetEntries;
  }

  public void setSelectedFacetEntries(ResultFilterVO selectedFacetEntries) {
    this.selectedFacetEntries = selectedFacetEntries;
  }

  public boolean isPlatformUsesPDC() {
    return platformUsesPDC;
  }

  public void clearSearchParameters(boolean clearPages) {
    if (queryParameters != null) {
      queryParameters.clear();
      queryParameters.setXmlTitle(null);
    }
    removeAllCriterias();
    setSortOrder(PdcSearchSessionController.SORT_ORDER_DESC);
    setSortValue(1);
    clearXmlTemplateAndData();
    setDataType(PdcSearchSessionController.ALL_DATA_TYPE);
    setSelectedFacetEntries(null);
    if (clearPages) {
      resetResultPage();
      resetResultPageId();
      resetSearchPage();
      resetSearchPageId();
    }
  }

  public void search(final boolean isOnlyInPdcSearch) throws SearchEngineException {
    search(null, isOnlyInPdcSearch);
  }

  public void search(final String taxonomyPosition, final boolean isOnlyInPdcSearch)
      throws SearchEngineException {
    setSelectedSilverContents(new ArrayList<>());
    selectedFacetEntries = null;

    QueryDescription query = getQueryDescription(isOnlyInPdcSearch);
    query.setTaxonomyPosition(taxonomyPosition);

    if (query.isTaxonomyUsed()) {
      if (!query.isEmpty()) {
        setSearchScope(PdcSearchSessionController.SEARCH_MIXED);
      } else {
        setSearchScope(PdcSearchSessionController.SEARCH_PDC);
        // case of PDC results : pertinence sort is not applicable
        // sort by updateDate desc
        setSortValue(5);
        setSortOrder(SORT_ORDER_DESC);
      }
    } else if (!query.isEmpty()) {
      setSearchScope(PdcSearchSessionController.SEARCH_FULLTEXT);
    }

    SearchService searchService = SearchService.get();
    List<SearchResult> results = searchService.search(query);

    List<GlobalSilverResult> results2Display = searchResultsToGlobalSilverResults(results);
    setGlobalSR(results2Display);
  }

  private QueryDescription getQueryDescription(final boolean isOnlyInPdcSearch) {
    QueryDescription query = getQueryParameters().getQueryDescription(getUserId(), "*");

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
      // it's a global search. Search on personal components, taxonomy, spaces and components
      // description
      query.addComponent(USER_PREFIX + getUserId() + "_todo");
      if (includePDC) {
        query.addComponent(PDC_SERVICE);
      }
      query.addComponent(SPACES_INDEX);
      query.addComponent(COMPONENTS_INDEX);
      if (includeUsers) {
        query.addComponent(DIRECTORY_SERVICE);
      }
    } else if (getQueryParameters().getSpaceId() != null) {
      // used for search by space without keywords
      query.setSearchBySpace(!isOnlyInPdcSearch);
    } else if (isDataTypeDefined()) {
      // used for search by component type without keywords
      query.setSearchByComponentType(true);
    }

    // Add external components into QueryDescription
    addExternalComponents(query);

    String originalQuery = query.getQuery();
    query.setQuery(getSynonymsQueryString(originalQuery));

    return query;
  }

  private List<GlobalSilverResult> searchResultsToGlobalSilverResults(List<SearchResult> results) {
    if (results == null || results.isEmpty()) {
      return new ArrayList<>();
    }

    // Initialize loop variables
    List<GlobalSilverResult> resultsToDisplay = new ArrayList<>();

    // Retrieve list of object type filter
    List<String> objectTypeFilter = getListObjectTypeFilter();

    for (int i = 0; i < results.size(); i++) {
      SearchResult result = results.get(i);
      boolean processThisResult = processResult(result, objectTypeFilter);

      if (processThisResult) {
        GlobalSilverResult gsr = new GlobalSilverResult(result);
        gsr.setResultId(i);

        resultsToDisplay.add(gsr);
      }
    }

    return resultsToDisplay;
  }

  private String getResultURL(GlobalSilverResult result) {
    String url = "";
    if ("todo".equals(result.getType())) {
      url = URLUtil.getApplicationURL() + URLUtil.getURL(result.getType(), null, null);
    } else {
      String instanceId = result.getInstanceId();
      url = URLUtil.getApplicationURL() + URLUtil.getComponentInstanceURL(instanceId);
    }
    url += "searchResult?Type=" + result.getType() + "&Id=" + result.getId();
    url += "&From=Search";
    return url;
  }
}