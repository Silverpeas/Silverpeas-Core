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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
import java.net.URLEncoder;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.stratelia.silverpeas.pdcPeas.model.GlobalSilverResult;
import com.stratelia.silverpeas.pdcPeas.model.QueryParameters;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.selection.Selection;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.silverpeas.util.PairObject;
import com.stratelia.silverpeas.util.SilverpeasSettings;
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
  private List globalResult = new ArrayList(); // used by global search
  private List indexEntries = new ArrayList();
  private List pdcResult = new ArrayList(); // used by pdc search via the
  // pagination de la liste des résultats (PDC via DomainsBar)
  private int indexOfFirstItemToDisplay = 1;
  private int nbItemsPerPage = -1;
  private Value currentValue = null;
  // pagination de la liste des resultats (search Engine)
  private int indexOfFirstResultToDisplay = 0;
  // CBO : REMOVE
  // private int nbResultsPerPage = 10;
  // CBO : ADD
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
  // XML Search Session's objects
  private PublicationTemplateImpl xmlTemplate = null;
  private DataRecord xmlData = null;
  private int searchScope = SEARCH_FULLTEXT;
  private ComponentSecurity componentSecurity = null;

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
   ******************************************************************************************************************/
  public void setPDCResults(List globalSilverContents) {
    indexOfFirstItemToDisplay = 0;
    this.pdcResult.clear(); // on vide la liste avant de rajouter
    this.pdcResult.addAll(globalSilverContents);
  }

  public List getPDCResults() {
    return this.pdcResult;
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
  public void setResults(List globalSilverResults) {
    indexOfFirstResultToDisplay = 0;
    this.globalResult.clear(); // on vide la liste avant de rajouter
    this.globalResult.addAll(globalSilverResults);
    setLastResults(globalSilverResults);
  }

  public List getResults() {
    return this.globalResult;
  }

  // CBO : REMOVE
  /*
   * public int getNbResultsPerPage() { return this.nbResultsPerPage; }
   */
  public int getIndexOfFirstResultToDisplay() {
    return indexOfFirstResultToDisplay;
  }

  public void setIndexOfFirstResultToDisplay(String index) {
    this.indexOfFirstResultToDisplay = new Integer(index).intValue();
  }

  // CBO : Add
  public String getDisplayParamChoices() {
    if (displayParamChoices == null) {
      displayParamChoices = getSettings().getString("DisplayParamChoices", "All");
    }
    return displayParamChoices;
  }

  public List getListChoiceNbResToDisplay() {
    List choiceNbResToDisplay = new ArrayList();
    StringTokenizer st = new StringTokenizer(getSettings().getString("ChoiceNbResToDisplay"), ",");
    while (st.hasMoreTokens()) {
      String choice = st.nextToken();
      choiceNbResToDisplay.add(choice);
    }
    return choiceNbResToDisplay;
  }

  public int getNbResToDisplay() {
    if (nbResToDisplay == -1) {
      nbResToDisplay = new Integer(((String) getListChoiceNbResToDisplay().get(
          0))).intValue();
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

  // CBO : FIN ADD
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
          && (getQueryParameters().isDefined() || getQueryParameters()
          .getXmlQuery() != null)) {
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

  public List getResultsToDisplay() throws Exception {
    return getSortedResultsToDisplay(getSortValue(), getSortOrder());
  }

  // CBO : ADD
  public List getSortedResultsToDisplay(int sortValue, String sortOrder)
      throws Exception {
    List sortedResultsToDisplay = new ArrayList();

    // Tous les résultats
    List results = null;
    if (getSearchScope() == SEARCH_PDC) {
      results = globalSilverContents2GlobalSilverResults(getResults());
    } else {
      results = matchingIndexEntries2GlobalSilverResults(getIndexEntries());
    }

    if (results != null && getSelectedSilverContents() != null) {
      GlobalSilverResult result = null;
      for (int i = 0; i < results.size(); i++) {
        result = (GlobalSilverResult) results.get(i);
        if (getSelectedSilverContents().contains(result)) {
          result.setSelected(true);
        } else {
          result.setSelected(false);
        }
      }
    }

    // Tri de tous les résultats
    GlobalSilverResult[] arrayGlobalSilverResult = (GlobalSilverResult[]) results
        .toArray(new GlobalSilverResult[0]);

    // Comparateurs
    Comparator cPertAsc = new Comparator() {

      public int compare(Object o1, Object o2) {
        Float float1 = new Float(((GlobalSilverResult) o1).getRawScore());
        Float float2 = new Float(((GlobalSilverResult) o2).getRawScore());

        if (float1 != null && float2 != null) {
          return float1.compareTo(float2);
        } else {
          return 0;
        }
      }

      public boolean equals(Object o) {
        return false;
      }
    };

    Comparator cPertDesc = new Comparator() {

      public int compare(Object o1, Object o2) {
        Float float1 = new Float(((GlobalSilverResult) o1).getRawScore());
        Float float2 = new Float(((GlobalSilverResult) o2).getRawScore());

        if (float1 != null && float2 != null) {
          return float2.compareTo(float1);
        } else {
          return 0;
        }
      }

      public boolean equals(Object o) {
        return false;
      }
    };

    Comparator cTitreAsc = new Comparator() {

      public int compare(Object o1, Object o2) {
        String string1 = ((GlobalSilverResult) o1).getName(getLanguage());
        String string2 = ((GlobalSilverResult) o2).getName(getLanguage());

        if (string1 != null && string2 != null) {
          return string1.compareToIgnoreCase(string2);
        } else {
          return 0;
        }
      }

      public boolean equals(Object o) {
        return false;
      }
    };

    Comparator cTitreDesc = new Comparator() {

      public int compare(Object o1, Object o2) {
        String string1 = ((GlobalSilverResult) o1).getName(getLanguage());
        String string2 = ((GlobalSilverResult) o2).getName(getLanguage());

        if (string1 != null && string2 != null) {
          return string2.compareToIgnoreCase(string1);
        } else {
          return 0;
        }
      }

      public boolean equals(Object o) {
        return false;
      }
    };

    Comparator cAuteurAsc = new Comparator() {

      public int compare(Object o1, Object o2) {
        String string1 = ((GlobalSilverResult) o1).getCreatorName();
        String string2 = ((GlobalSilverResult) o2).getCreatorName();

        if (string1 != null && string2 != null) {
          return string1.compareToIgnoreCase(string2);
        } else {
          return 0;
        }
      }

      public boolean equals(Object o) {
        return false;
      }
    };

    Comparator cAuteurDesc = new Comparator() {

      public int compare(Object o1, Object o2) {
        String string1 = ((GlobalSilverResult) o1).getCreatorName();
        String string2 = ((GlobalSilverResult) o2).getCreatorName();

        if (string1 != null && string2 != null) {
          return string2.compareToIgnoreCase(string1);
        } else {
          return 0;
        }
      }

      public boolean equals(Object o) {
        return false;
      }
    };

    Comparator cDateAsc = new Comparator() {

      public int compare(Object o1, Object o2) {
        String string1 = ((GlobalSilverResult) o1).getDate();
        String string2 = ((GlobalSilverResult) o2).getDate();

        if (string1 != null && string2 != null) {
          return string1.compareTo(string2);
        } else {
          return 0;
        }
      }

      public boolean equals(Object o) {
        return false;
      }
    };

    Comparator cDateDesc = new Comparator() {

      public int compare(Object o1, Object o2) {
        String string1 = ((GlobalSilverResult) o1).getDate();
        String string2 = ((GlobalSilverResult) o2).getDate();

        if (string1 != null && string2 != null) {
          return string2.compareTo(string1);
        } else {
          return 0;
        }
      }

      public boolean equals(Object o) {
        return false;
      }
    };

    Comparator cEmplAsc = new Comparator() {

      public int compare(Object o1, Object o2) {
        String string1 = ((GlobalSilverResult) o1).getLocation();
        String string2 = ((GlobalSilverResult) o2).getLocation();

        if (string1 != null && string2 != null) {
          return string1.compareToIgnoreCase(string2);
        } else {
          return 0;
        }
      }

      public boolean equals(Object o) {
        return false;
      }
    };

    Comparator cEmplDesc = new Comparator() {

      public int compare(Object o1, Object o2) {
        String string1 = ((GlobalSilverResult) o1).getLocation();
        String string2 = ((GlobalSilverResult) o2).getLocation();

        if (string1 != null && string2 != null) {
          return string2.compareToIgnoreCase(string1);
        } else {
          return 0;
        }
      }

      public boolean equals(Object o) {
        return false;
      }
    };

    if (sortValue == 1
        && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {// Pertinence
      // ASC
      Arrays.sort(arrayGlobalSilverResult, cPertAsc);
    } else if (sortValue == 1
        && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {// Pertinence
      // DESC
      Arrays.sort(arrayGlobalSilverResult, cPertDesc);
    } else if (sortValue == 2
        && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {// Titre
      // ASC
      Arrays.sort(arrayGlobalSilverResult, cTitreAsc);
    } else if (sortValue == 2
        && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {// Titre
      // DESC
      Arrays.sort(arrayGlobalSilverResult, cTitreDesc);
    } else if (sortValue == 3
        && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {// Auteur
      // ASC
      Arrays.sort(arrayGlobalSilverResult, cAuteurAsc);
    } else if (sortValue == 3
        && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {// Auteur
      // DESC
      Arrays.sort(arrayGlobalSilverResult, cAuteurDesc);
    } else if (sortValue == 4
        && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {// Date
      // ASC
      Arrays.sort(arrayGlobalSilverResult, cDateAsc);
    } else if (sortValue == 4
        && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {// Date
      // DESC
      Arrays.sort(arrayGlobalSilverResult, cDateDesc);
    } else if (sortValue == 5
        && PdcSearchSessionController.SORT_ORDER_ASC.equals(sortOrder)) {// Emplacement
      // ASC
      Arrays.sort(arrayGlobalSilverResult, cEmplAsc);
    } else if (sortValue == 5
        && PdcSearchSessionController.SORT_ORDER_DESC.equals(sortOrder)) {// Emplacement
      // DESC
      Arrays.sort(arrayGlobalSilverResult, cEmplDesc);
    }

    // retransforme en List
    List sortedResults = new ArrayList();
    for (int i = 0; i < arrayGlobalSilverResult.length; i++) {
      sortedResults.add(arrayGlobalSilverResult[i]);
    }

    sortedResultsToDisplay = sortedResults.subList(
        getIndexOfFirstResultToDisplay(), getLastIndexToDisplay());

    if (getSearchScope() != SEARCH_PDC) {
      setExtraInfoToResultsToDisplay(sortedResultsToDisplay);
    }

    return sortedResultsToDisplay;
  }

  private void setExtraInfoToResultsToDisplay(List<GlobalSilverResult> results) {
    String titleLink = "";
    String downloadLink = "";
    String resultType = "";
    String underLink = "";
    String componentId = null;
    String m_sContext = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");

    // activate the mark as read functionality on results list
    String markAsReadJS = "";
    boolean isEnableMarkAsRead = getSettings().getBoolean("enableMarkAsRead", false);

    GlobalSilverResult result = null;
    MatchingIndexEntry indexEntry = null;
    for (int r = 0; r < results.size(); r++) {
      result = results.get(r);
      indexEntry = result.getIndexEntry();

      resultType = indexEntry.getObjectType();
      componentId = indexEntry.getComponent();
      downloadLink = null;
      // create the url part to activate the mark as read functionality
      if (isEnableMarkAsRead) {
        markAsReadJS = "markAsRead('" + indexEntry.getPK().toString() + "'); ";
      }
      if (resultType.equals("Versioning")) {
        // Added to be compliant with old indexing method
        resultType = "Publication";
      }

      if (resultType.startsWith("Attachment")) {
        if (!componentId.startsWith("webPages")) {
          try {
            downloadLink = getAttachmentUrl(indexEntry);
          } catch (Exception e) {
            SilverTrace
                .error(
                "pdcPeas",
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
          ComponentInstLight componentInst = getOrganizationController()
              .getComponentInstLight(componentId);
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
        downloadLink = FileServerUtils.getUrl(indexEntry.getTitle(), indexEntry
            .getObjectId(), AttachmentController.getMimeType(indexEntry
            .getTitle()));
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
        String treeId = objectId.substring(objectId.indexOf("_") + 1, objectId
            .length());
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
        // underLink = m_sContext + URLManager.getURL("useless", componentId) +
        // "Main";
        underLink = URLManager.getSimpleURL(URLManager.URL_COMPONENT,
            componentId);
        titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId
            + "');document.location.href='" + underLink + "';";
      } else if (componentId.startsWith("user@")) {
        titleLink = getUrl(m_sContext, componentId, null, indexEntry
            .getPageAndParams());
      } else {
        titleLink = "javascript:" + markAsReadJS + " jumpToComponent('" + componentId
            + "');document.location.href='" + getUrl(m_sContext, indexEntry)
            + "';";
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
    if (getSearchScope() == SEARCH_PDC) {
      return getResults().size();
    } else {
      return getIndexEntries().size();
    }
  }

  private int getLastIndexToDisplay() {
    // CBO : UPDATE
    // int end = getIndexOfFirstResultToDisplay()+getNbResultsPerPage();
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
    SilverTrace
        .info(
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

      SilverTrace
          .info(
          "pdcPeas",
          "PdcSearchSessionController.matchingIndexEntries2GlobalSilverResults()",
          "root.MSG_GEN_PARAM_VALUE", "title= " + title);

      // WARNING : LINE BELOW HAS BEEN ADDED TO NOT SHOW WYSIWYG ALONE IN SEARCH
      // RESULT PAGE
      if (title.endsWith("wysiwyg.txt")
          && (componentId.startsWith("kmelia") || componentId
          .startsWith("kmax"))) {
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
          ComponentInstLight componentInst = getOrganizationController()
              .getComponentInstLight(componentId);
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
    if (globalSilverContents == null || globalSilverContents.size() == 0) {
      return new ArrayList<GlobalSilverResult>();
    }
    List<GlobalSilverResult> results = new ArrayList<GlobalSilverResult>();
    GlobalSilverContent gsc = null;
    GlobalSilverResult gsr = null;
    String encodedURL = null;
    for (int i = 0; i < globalSilverContents.size(); i++) {
      gsc = globalSilverContents.get(i);
      gsr = new GlobalSilverResult(gsc);
      encodedURL = URLEncoder.encode(gsc.getURL());
      gsr.setTitleLink("javaScript:submitContent('" + encodedURL + "','"
          + gsc.getInstanceId() + "')");
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

  private String getAttachmentUrl(MatchingIndexEntry indexEntry)
      throws Exception {
    return getAttachmentUrl(indexEntry.getObjectType(), indexEntry
        .getComponent());
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
    AttachmentDetail attachmentDetail = AttachmentController
        .searchAttachmentByPK(attachmentPK);

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

    return urlAttachment;
  }

  private String getVersioningUrl(String documentId, String componentId)
      throws Exception {
    SilverTrace.info("pdcPeas", "PdcSearchRequestRouter.getVersioningUrl",
        "root.MSG_GEN_PARAM_VALUE", "documentId = " + documentId
        + ", componentId = " + componentId);

    DocumentVersion version = versioningUtil
        .getLastPublicVersion(new DocumentPK(
        new Integer(documentId).intValue(), "useless", componentId));

    String urlVersioning = versioningUtil.getDocumentVersionURL(componentId,
        version.getLogicalName(), documentId, version.getPk().getId());

    return urlVersioning;
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

  public List getAllAxis() throws PdcException {
    return getPdcBm().getAxis();
  }

  public List getPrimaryAxis() throws PdcException {
    return getPdcBm().getAxisByType("P");
  }

  public List getAxis(String viewType) throws PdcException {
    return getAxis(viewType, new AxisFilter());
  }

  public List getAxis(String viewType, AxisFilter filter) throws PdcException {
    if (componentList == null || componentList.size() == 0) {
      if (StringUtil.isDefined(getCurrentComponentId())) {
        return getPdcBm().getPertinentAxisByInstanceId(searchContext, viewType,
            getCurrentComponentId());
      }
      return new ArrayList();
    } else {
      if (isShowOnlyPertinentAxisAndValues()) {
        return getPdcBm().getPertinentAxisByInstanceIds(searchContext,
            viewType, componentList);
      } else {
        // we get all axis (pertinent or not) from a type P or S
        List axis = getPdcBm().getAxisByType(viewType);
        // we have to transform all axis (AxisHeader) into SearchAxis to make
        // the display into jsp transparent
        return transformAxisHeadersIntoSearchAxis(axis);
      }
    }
  }

  private List transformAxisHeadersIntoSearchAxis(List axis) {
    ArrayList transformedAxis = new ArrayList();
    AxisHeader ah;
    SearchAxis sa;
    try {
      for (int i = 0; i < axis.size(); i++) {
        ah = (AxisHeader) axis.get(i);
        sa = new SearchAxis(Integer.parseInt(ah.getPK().getId()), 0);
        // sa.setAxisName(ah.getName());
        sa.setAxis(ah);
        sa.setAxisRootId(Integer.parseInt(getPdcBm()
            .getRoot(ah.getPK().getId()).getValuePK().getId()));
        sa.setNbObjects(1);
        transformedAxis.add(sa);
      }
    } catch (Exception e) {
    }
    return transformedAxis;
  }

  public List getDaughterValues(String axisId, String valueId)
      throws PdcException {
    return getDaughterValues(axisId, valueId, new AxisFilter());
  }

  public List getDaughterValues(String axisId, String valueId, AxisFilter filter)
      throws PdcException {
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.getDaughterValues()",
        "root.MSG_GEN_ENTER_METHOD", "axisId = " + axisId + ", valueId = "
        + valueId);
    List values = null;
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

  private List setNBNumbersToOne(List values) {
    for (int i = 0; i < values.size(); i++) {
      com.stratelia.silverpeas.pdc.model.Value value = (Value) values.get(i);
      value.setNbObjects(1);
    }
    return values;
  }

  public List getFirstLevelAxisValues(String axisId) throws PdcException {
    SilverTrace.info("pdcPeas",
        "PdcSearchSessionController.getFirstLevelAxisValues()",
        "root.MSG_GEN_ENTER_METHOD", "axisId = " + axisId);
    List result = null;
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

  public void setSelectedSilverContents(List silverContents) {
    getPdc().setSelectedSilverContents(silverContents);
  }

  public List getSelectedSilverContents() {
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
  private Map<String, Collection> synonyms = new HashMap<String, Collection>();
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

  private Collection getSynonym(String mot) {
    if (synonyms.containsKey(mot)) {
      return (Collection) synonyms.get(mot);
    } else {
      try {
        Collection synos = new ThesaurusManager().getSynonyms(mot, getUserId());
        synonyms.put(mot, synos);
        return synos;
      } catch (ThesaurusException e) {
        throw new PdcPeasRuntimeException(
            "PdcSearchSessionController.getSynonym", SilverpeasException.ERROR,
            "pdcPeas.EX_GET_SYNONYMS", e);
      }
    }
  }

  public Map getSynonyms() {
    if (activeThesaurus) {
      return synonyms;
    } else {
      return new HashMap();
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
        List wordList = new ArrayList();
        ResourceLocator resource = new ResourceLocator(
            "com.stratelia.webactiv.util.indexEngine.StopWords", getLanguage());
        Enumeration stopWord = resource.getKeys();
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
  private List axis_result = null;

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

  public List getAxisValuesByFilter(String filter_by_name,
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

    List allAxis = new ArrayList();
    if (instanceId == null) {
      allAxis = getAllAxis();
    } else {
      allAxis = getUsedAxisHeaderByInstanceId(instanceId);
    }

    List axises = (List) new ArrayList();

    for (int i = 0; i < allAxis.size(); i++) {
      AxisHeader sa = (AxisHeader) allAxis.get(i);
      String rootId = String.valueOf(sa.getRootId());
      List daughters = getPdcBm().getFilteredAxisValues(rootId, filter);
      axises.addAll(daughters);
    }
    return axises;
  }

  public List getUsedAxisHeaderByInstanceId(String instanceId)
      throws PdcException {
    List usedAxisList = getPdcBm().getUsedAxisByInstanceId(instanceId);
    AxisHeader axisHeader = null;
    UsedAxis usedAxis = null;
    String axisId = null;
    List allAxis = new ArrayList();
    // get all AxisHeader corresponding to usedAxis for this instance
    for (int i = 0; i < usedAxisList.size(); i++) {
      usedAxis = (UsedAxis) usedAxisList.get(i);
      axisId = new Integer(usedAxis.getAxisId()).toString();
      axisHeader = getAxisHeader(axisId);
      allAxis.add(axisHeader);
    }
    return allAxis;
  }

  public List getUsedTreeIds(String instanceId) throws PdcException {
    List usedAxisHeaders = getUsedAxisHeaderByInstanceId(instanceId);
    ArrayList usedTreeIds = new ArrayList();
    AxisHeader axisHeader = null;
    String treeId = null;
    for (int i = 0; i < usedAxisHeaders.size(); i++) {
      axisHeader = (AxisHeader) usedAxisHeaders.get(i);
      if (axisHeader != null) {
        treeId = new Integer(axisHeader.getRootId()).toString();
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

  public List getUsedAxisByAComponentInstance(String instanceId)
      throws PdcException {
    ArrayList usedAxisList = (ArrayList) getPdcBm().getUsedAxisByInstanceId(
        instanceId);
    ArrayList axisList = new ArrayList();
    UsedAxis usedAxis = null;
    for (int i = 0; i < usedAxisList.size(); i++) {
      usedAxis = (UsedAxis) usedAxisList.get(i);
      axisList.add(getAxisDetail(new Integer(usedAxis.getAxisId()).toString()));
    }
    return axisList;
  }

  public void setAxisResult(List result) {
    axis_result = result;
  }

  public List getAxisResult() {
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
      ArrayList criterias = ic.getPdcContext();
      Iterator i = criterias.iterator();
      while (i.hasNext()) {
        searchContext.addCriteria((SearchCriteria) i.next());
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
    String m_context = GeneralPropertiesManager.getGeneralResourceLocator()
        .getString("ApplicationURL");
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
        String[] asAvailCompoForCurUser = getOrganizationController()
            .getAvailCompoIds(space, getUserId());
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

  private boolean isSearchable(String componentId) {
    if (componentId.startsWith("silverCrawler")
        || componentId.startsWith("gallery")
        || componentId.startsWith("kmelia")) {
      boolean isPrivateSearch = "yes"
          .equalsIgnoreCase(getOrganizationController()
          .getComponentParameterValue(componentId, "privateSearch"));
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
  public List getAllowedSpaces() {
    List allowed = new ArrayList();

    String[] spaceIds = getOrganizationController().getAllSpaceIds(getUserId());

    // add each shared domains
    for (int nI = 0; nI < spaceIds.length; nI++) {
      String spaceId = spaceIds[nI];
      SpaceInstLight spaceInst = getOrganizationController()
          .getSpaceInstLightById(spaceId);
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
  public List getAllowedComponents(String space) {
    List allowedList = new ArrayList();
    if (space != null) {
      String[] asAvailCompoForCurUser = getOrganizationController()
          .getAvailCompoIds(space, getUserId());
      for (int nI = 0; nI < asAvailCompoForCurUser.length; nI++) {
        ComponentInstLight componentInst = getOrganizationController()
            .getComponentInstLight(asAvailCompoForCurUser[nI]);

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
    SpaceInstLight spaceInst = getOrganizationController()
        .getSpaceInstLightById(spaceId);
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
  private Vector searchDomains = null; // All the domains available for search

  /**
   * Get the search domains available for search The search domains are contained in a Vector of
   * array of 3 String (String[0]=domain name, String[1]=domain url page, String[2]=internal Id)
   */
  public Vector getSearchDomains() {
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
    Vector domains = new Vector();

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
      ComponentInstLight componentInst = getOrganizationController()
          .getComponentInstLight(componentId);
      return urlBase
          + URLManager.getNewComponentURL(componentInst.getDomainFatherId(),
          componentId)
          + params
          + URLManager
          .getEndURL(componentInst.getDomainFatherId(), componentId);
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
        SearchEngineBmHome home = (SearchEngineBmHome) EJBUtilitaire
            .getEJBObjectRef(JNDINames.SEARCHBM_EJBHOME,
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

  public void completeGlobalResultsWithInfoExportable(List lastResults) {
    // List lastResults = getLastResults(); //contains user's last results
    Iterator itLastResults = lastResults.iterator();
    while (itLastResults.hasNext()) {
      GlobalSilverResult result = (GlobalSilverResult) itLastResults.next();
      result.setExportable(isCompliantResult(result));
    }
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
  private List filterMatchingIndexEntries(
      MatchingIndexEntry[] matchingIndexEntries) {
    if (matchingIndexEntries == null || matchingIndexEntries.length == 0) {
      return new ArrayList();
    }

    String title = "";
    String componentId = null;

    ArrayList results = new ArrayList();

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
          && (componentId.startsWith("kmelia") || componentId
          .startsWith("kmax"))) {
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

  /**
   * @return
   */
  public List getIndexEntries() {
    return indexEntries;
  }

  /**
   * @param entries
   */
  public void setIndexEntries(MatchingIndexEntry[] indexEntries) {
    this.indexEntries = filterMatchingIndexEntries(indexEntries);
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
      template = (PublicationTemplateImpl) PublicationTemplateManager
          .loadPublicationTemplate(fileName);
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

  /**
   * gets suggestions or spelling words if a search doesn't return satisfying results. A minimal
   * score trigger the suggestions search (0.5 by default)
   * @return array that contains suggestions.
   */
  public String[] getSpellingwords() {
    return spellingwords;
  }

}
