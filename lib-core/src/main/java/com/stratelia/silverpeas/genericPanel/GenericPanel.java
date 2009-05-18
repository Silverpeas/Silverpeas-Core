/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.genericPanel;

import com.stratelia.silverpeas.util.PairObject;

public class GenericPanel
{
    final static public String OPERATION_VALIDATE = "GENERICPANELVALIDATE";

    protected String         m_goBackURL;
    protected String         m_cancelURL;
    protected String         m_zoomToItemURL;

    protected String[]       m_selectedElements;
    protected PanelProvider  m_panelProvider;
    protected boolean        m_popupMode;
    protected boolean        m_multiSelect;
    protected boolean        m_selectable;
    protected boolean        m_zoomToItemInPopup;

    protected PanelOperation[]  m_panelOperations;
    protected String            m_selectedOperation;

    protected String         m_hostSpaceName;
    protected PairObject     m_hostComponentName;
    protected PairObject[]   m_hostPath;

    public GenericPanel()
    {
        resetAll();
    }

    public void resetAll()
    {
        m_goBackURL = "";
        m_cancelURL = "";
        m_zoomToItemURL = null;
        m_selectedElements = new String[0];
        m_panelOperations = new PanelOperation[0];
        m_panelProvider = null;
        m_popupMode = false;
        m_multiSelect = false;
        m_selectable = true;
        m_zoomToItemInPopup = false;
        m_selectedOperation = "";
        m_hostSpaceName = "";
        m_hostComponentName = new PairObject("","");
        m_hostPath = new PairObject[0];
    }

    static public String getGenericPanelURL(String panelKey)
    {
        return "/RgenericPanelPeas/jsp/Main?PanelKey=" + panelKey;
    }

    public void setHostSpaceName(String hostSpaceName) { m_hostSpaceName = hostSpaceName; }
    public String getHostSpaceName() { return m_hostSpaceName; }
    public void setHostComponentName(PairObject hostComponentName) { m_hostComponentName = hostComponentName; }
    public PairObject getHostComponentName() { return m_hostComponentName; }
    public void setHostPath(PairObject[] hostPath) { m_hostPath = hostPath; }
    public PairObject[] getHostPath() { return m_hostPath; }

    public String getCancelURL() { return m_cancelURL; }
    public void setCancelURL(String cancelURL) { m_cancelURL = cancelURL; }

    public String getGoBackURL() { return m_goBackURL; }
    public void setGoBackURL(String goBackURL) { m_goBackURL = goBackURL; }

    // WARNING : ZoomToItem must not contains any extra parameter !!!! It will be called with elementId parameter
    public String getZoomToItemURL() { return m_zoomToItemURL; }
    public void setZoomToItemURL(String zoomToItemURL) { m_zoomToItemURL = zoomToItemURL; }

    public PanelProvider getPanelProvider() { return m_panelProvider; }
    public void setPanelProvider(PanelProvider panelProvider) { m_panelProvider = panelProvider; }

    public boolean isPopupMode() { return m_popupMode; }
    public void    setPopupMode(boolean popupMode) { m_popupMode = popupMode; }

    public boolean isMultiSelect() { return m_multiSelect; }
    public void setMultiSelect(boolean multiSelect) { m_multiSelect = multiSelect; }

    public boolean isSelectable() { return m_selectable; }
    public void setSelectable(boolean selectable) { m_selectable = selectable; }

    public boolean isZoomToItemInPopup() { return m_zoomToItemInPopup; }
    public void setZoomToItemInPopup(boolean zoomToItemInPopup) { m_zoomToItemInPopup = zoomToItemInPopup; }

    public PanelOperation[] getPanelOperations() { return m_panelOperations; }
    public void setPanelOperations(PanelOperation[] panelOperations) { m_panelOperations = panelOperations; }

    public String getSelectedOperation() { return m_selectedOperation; }
    public void setSelectedOperation(String selectedOperation) { m_selectedOperation = selectedOperation; }

    public String[] getSelectedElements() { return m_selectedElements; }
    public void setSelectedElements(String[] selectedElements) { m_selectedElements = selectedElements; }
}
