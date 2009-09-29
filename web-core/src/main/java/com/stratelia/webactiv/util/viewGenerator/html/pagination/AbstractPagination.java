package com.stratelia.webactiv.util.viewGenerator.html.pagination;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.viewGenerator.html.GraphicElementFactory;

public abstract class AbstractPagination implements Pagination {
  private int nbItems = -1; // the total number of items to paginate
  private int nbItemsPerPage = -1; // the number of items displayed by page
  private int firstItemIndex = -1; // the first item's index displayed
  private String actionSuffix = "";
  private String altPreviousPage = "";
  private String altNextPage = "";
  private String baseURL = null;

  public AbstractPagination() {
  }

  public void init(int nbItems, int nbItemsPerPage, int firstItemIndex) {
    this.nbItems = nbItems;
    this.nbItemsPerPage = nbItemsPerPage;
    // verification de la coherenre du parametre firstItemIndex
    if (firstItemIndex > nbItems - 1) {
      if (nbItems == 0) {
        this.firstItemIndex = 0;
      } else {
        this.firstItemIndex = firstItemIndex - nbItemsPerPage;
      }
    } else {
      this.firstItemIndex = firstItemIndex;
    }
    SilverTrace.info("viewgenerator", "AbstractPagination.constructor()",
        "root.MSG_GEN_PARAM_VALUE", "nbItems = " + nbItems
            + ", nbItemsPerPage = " + nbItemsPerPage + ", firstItemIndex = "
            + firstItemIndex);
  }

  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  public int getNbItems() {
    return nbItems;
  }

  public int getNbItemsPerPage() {
    return nbItemsPerPage;
  }

  public int getFirstItemIndex() {
    return firstItemIndex;
  }

  public int getLastItemIndex() {
    int end = getFirstItemIndex() + getNbItemsPerPage();
    if (end > getNbItems() - 1)
      end = getNbItems();
    return end;
  }

  public int getCurrentPage() {
    int currentPage = (getFirstItemIndex() + 1) / getNbItemsPerPage();
    SilverTrace.info("viewgenerator", "AbstractPagination.getCurrentPage()",
        "root.MSG_GEN_PARAM_VALUE", "currentPage = " + currentPage);

    if (currentPage == 0)
      currentPage = 1;
    else
      currentPage++;

    return currentPage;
  }

  public int getNbPage() {
    int nbPage = (getNbItems() / getNbItemsPerPage());
    if ((getNbItems() % getNbItemsPerPage()) != 0)
      nbPage++;
    SilverTrace.info("viewgenerator", "AbstractPagination.getNbPage()",
        "root.MSG_GEN_PARAM_VALUE", "nbPage = " + nbPage);
    return nbPage;
  }

  public boolean isLastPage() {
    return getFirstItemIndex() + 1 + getNbItemsPerPage() > getNbItems();
  }

  public int getIndexForPreviousPage() {
    return getIndexForDirectPage(getCurrentPage() - 1);
  }

  public int getIndexForDirectPage(int page) {
    return (page - 1) * getNbItemsPerPage();
  }

  public int getIndexForNextPage() {
    return getIndexForDirectPage(getCurrentPage() + 1);
  }

  public void setActionSuffix(String actionSuffix) {
    this.actionSuffix = actionSuffix;
  }

  public String getActionSuffix() {
    return actionSuffix;
  }

  public void setBaseURL(String url) {
    this.baseURL = url;
  }

  public String getBaseURL() {
    return baseURL;
  }

  public void setAltPreviousPage(String text) {
    this.altPreviousPage = text;
  }

  public void setAltNextPage(String text) {
    this.altNextPage = text;
  }

  public String getAltPreviousPage() {
    return this.altPreviousPage;
  }

  public String getAltNextPage() {
    return this.altNextPage;
  }

  public abstract String print();

  public abstract String printIndex();

  public abstract String printIndex(String text);

  public abstract String printCounter();
}