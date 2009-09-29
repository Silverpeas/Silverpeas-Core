/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.selection;

import com.stratelia.silverpeas.util.PairObject;

public class Selection {
  final public static String TYPE_USERS_GROUPS = "UsersGroups";
  final public static String TYPE_SPACES_COMPONENTS = "SpacesComponents";
  final public static String TYPE_JDBC_CONNECTOR = "JdbcConnector";

  final public static String FIRST_PAGE_DEFAULT = "Default";
  final public static String FIRST_PAGE_CART = "DisplayCart";
  final public static String FIRST_PAGE_SEARCH_ELEMENT = "DisplaySearchElement";
  final public static String FIRST_PAGE_SEARCH_SET = "DisplaySearchSet";
  final public static String FIRST_PAGE_BROWSE = "DisplayBrowse";

  protected String m_goBackURL;
  protected String m_cancelURL;

  protected String htmlFormName;
  protected String htmlFormElementName;
  protected String htmlFormElementId;

  protected String m_firstPage;

  protected String[] m_selectedSets;
  protected String[] m_selectedElements;

  protected boolean m_popupMode;
  protected boolean m_multiSelect;
  protected boolean m_setSelectable;
  protected boolean m_elementSelectable;

  protected String m_hostSpaceName;
  protected PairObject m_hostComponentName;
  protected PairObject[] m_hostPath;

  protected SelectionExtraParams m_extraParams;

  public Selection() {
    resetAll();
  }

  public void resetAll() {
    m_goBackURL = "";
    m_cancelURL = "";

    m_firstPage = FIRST_PAGE_DEFAULT;

    m_selectedSets = new String[0];
    m_selectedElements = new String[0];

    m_popupMode = false;
    m_multiSelect = true;
    m_setSelectable = true;
    m_elementSelectable = true;

    m_hostSpaceName = "";
    m_hostComponentName = new PairObject("", "");
    m_hostPath = new PairObject[0];

    m_extraParams = null;
  }

  static public String getSelectionURL(String selectionType) {
    return "/RselectionPeas/jsp/Main?SelectionType=" + selectionType;
  }

  public void setHostSpaceName(String hostSpaceName) {
    if (hostSpaceName != null) {
      m_hostSpaceName = hostSpaceName;
    } else {
      m_hostSpaceName = "";
    }
  }

  public String getHostSpaceName() {
    return m_hostSpaceName;
  }

  public void setHostComponentName(PairObject hostComponentName) {
    if (hostComponentName != null) {
      m_hostComponentName = hostComponentName;
    } else {
      m_hostComponentName = new PairObject("", "");
    }
  }

  public PairObject getHostComponentName() {
    return m_hostComponentName;
  }

  public void setHostPath(PairObject[] hostPath) {
    if (hostPath != null) {
      m_hostPath = hostPath;
    } else {
      m_hostPath = new PairObject[0];
    }
  }

  public PairObject[] getHostPath() {
    return m_hostPath;
  }

  public String getCancelURL() {
    return m_cancelURL;
  }

  public void setCancelURL(String cancelURL) {
    if (cancelURL != null) {
      m_cancelURL = cancelURL;
    } else {
      m_cancelURL = "";
    }
  }

  public String getGoBackURL() {
    return m_goBackURL;
  }

  public void setGoBackURL(String goBackURL) {
    m_goBackURL = goBackURL;
  }

  public String getFirstPage() {
    return m_firstPage;
  }

  public void setFirstPage(String firstPage) {
    m_firstPage = firstPage;
  }

  public boolean isPopupMode() {
    return m_popupMode;
  }

  public void setPopupMode(boolean popupMode) {
    m_popupMode = popupMode;
  }

  public boolean isMultiSelect() {
    return m_multiSelect;
  }

  public void setMultiSelect(boolean multiSelect) {
    m_multiSelect = multiSelect;
  }

  public boolean isSetSelectable() {
    return m_setSelectable;
  }

  public void setSetSelectable(boolean setSelectable) {
    m_setSelectable = setSelectable;
  }

  public boolean isElementSelectable() {
    return m_elementSelectable;
  }

  public void setElementSelectable(boolean elementSelectable) {
    m_elementSelectable = elementSelectable;
  }

  public String[] getSelectedElements() {
    return m_selectedElements;
  }

  public void setSelectedElements(String[] selectedElements) {
    if (selectedElements != null) {
      m_selectedElements = selectedElements;
    } else {
      m_selectedElements = new String[0];
    }
  }

  public String getFirstSelectedElement() {
    String[] elmts = m_selectedElements;
    String valret = null;

    if ((elmts != null) && (elmts.length > 0) && (elmts[0] != null)
        && (elmts[0] != null) && (elmts[0].length() > 0)) {
      valret = elmts[0];
    }
    return valret;
  }

  public String[] getSelectedSets() {
    return m_selectedSets;
  }

  public void setSelectedSets(String[] selectedSets) {
    if (selectedSets != null) {
      m_selectedSets = selectedSets;
    } else {
      m_selectedSets = new String[0];
    }
  }

  public String getFirstSelectedSet() {
    String[] sets = m_selectedSets;
    String valret = null;

    if ((sets != null) && (sets.length > 0) && (sets[0] != null)
        && (sets[0] != null) && (sets[0].length() > 0)) {
      valret = sets[0];
    }
    return valret;
  }

  public SelectionExtraParams getExtraParams() {
    return m_extraParams;
  }

  public void setExtraParams(SelectionExtraParams extraParams) {
    m_extraParams = extraParams;
  }

  public String getHtmlFormElementId() {
    return htmlFormElementId;
  }

  public void setHtmlFormElementId(String formElementId) {
    htmlFormElementId = formElementId;
  }

  public String getHtmlFormElementName() {
    return htmlFormElementName;
  }

  public void setHtmlFormElementName(String formElementName) {
    htmlFormElementName = formElementName;
  }

  public String getHtmlFormName() {
    return htmlFormName;
  }

  public void setHtmlFormName(String formName) {
    htmlFormName = formName;
  }
}
