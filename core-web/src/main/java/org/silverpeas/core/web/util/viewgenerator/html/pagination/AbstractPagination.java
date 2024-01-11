/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.web.util.viewgenerator.html.pagination;

import org.silverpeas.core.admin.PaginationPage;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import static org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory.getSettings;

public abstract class AbstractPagination implements Pagination {

  // the total number of items to paginate
  private int nbItems = -1;
  // the number of items displayed by page
  private int nbItemsPerPage = -1;
  // the first item's index displayed
  private int firstItemIndex = -1;
  private String actionSuffix = "";
  private String altPreviousPage = "";
  private String altNextPage = "";
  private String baseURL = null;
  private int nbPageThreshold = -1;
  private int nbPagesOnLeft = -1;
  private int nbPagesOnRight = -1;
  private LocalizationBundle multilang;

  protected AbstractPagination() {
  }

  static PaginationPage getPaginationPageFrom(final String pageSizeAsString,
      final String itemIndexAsString, final PaginationPage currentPagination) {
    PaginationPage pagination =
        currentPagination != null ? currentPagination : PaginationPage.DEFAULT;
    int pageNumber = pagination.getPageNumber();
    int pageSize = pagination.getPageSize();
    if (StringUtil.isInteger(pageSizeAsString)) {
      pageSize = Integer.parseInt(pageSizeAsString);
    }
    if (StringUtil.isInteger(itemIndexAsString)) {
      pageNumber = (Integer.parseInt(itemIndexAsString) / pageSize) + 1;
    }
    return new PaginationPage(pageNumber, pageSize);
  }

  @Override
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
  }

  public String getIconsPath() {
    return GraphicElementFactory.getIconsPath();
  }

  @Override
  public int getNbItems() {
    return nbItems;
  }

  public int getNbItemsPerPage() {
    return nbItemsPerPage;
  }

  @Override
  public int getFirstItemIndex() {
    return firstItemIndex;
  }

  @Override
  public int getLastItemIndex() {
    int end = getFirstItemIndex() + getNbItemsPerPage();
    if (end > getNbItems() - 1) {
      end = getNbItems();
    }
    return end;
  }

  public int getCurrentPage() {
    final int nbItemsByPage = getNbItemsPerPage();
    int currentPage = 1;
    if (nbItemsByPage > 0) {
      currentPage = (getFirstItemIndex() / nbItemsByPage) + 1;
    }
    return currentPage;
  }

  public int getNbPage() {
    final int nbItemsByPage = getNbItemsPerPage();
    final int nbPage;
    if (nbItemsByPage > 0) {
      nbPage = ((getNbItems() - 1) / nbItemsByPage) + 1;
    } else {
      nbPage = 1;
    }
    return Math.max(nbPage, 1);
  }

  @Override
  public boolean isLastPage() {
    return getFirstItemIndex() + 1 + getNbItemsPerPage() > getNbItems();
  }

  @Override
  public int getIndexForPreviousPage() {
    return getIndexForDirectPage(getCurrentPage() - 1);
  }

  @Override
  public int getIndexForDirectPage(int page) {
    int result = (page - 1) * getNbItemsPerPage();
    if (result > getNbItems()) {
      result = getIndexForLastPage();
    }
    return result;
  }

  @Override
  public int getIndexForCurrentPage() {
    return getIndexForDirectPage(getCurrentPage());
  }

  @Override
  public int getIndexForNextPage() {
    return getIndexForDirectPage(getCurrentPage() + 1);
  }

  public int getIndexForLastPage() {
    return (getNbPage() * getNbItemsPerPage()) - getNbItemsPerPage();
  }

  public String getActionSuffix() {
    return actionSuffix;
  }

  @Override
  public void setActionSuffix(String actionSuffix) {
    this.actionSuffix = actionSuffix;
  }

  public String getBaseURL() {
    return baseURL;
  }

  @Override
  public void setBaseURL(String url) {
    this.baseURL = url;
  }

  public String getAltPreviousPage() {
    return this.altPreviousPage;
  }

  @Override
  public void setAltPreviousPage(String text) {
    this.altPreviousPage = text;
  }

  public String getAltNextPage() {
    return this.altNextPage;
  }

  @Override
  public void setAltNextPage(String text) {
    this.altNextPage = text;
  }

  protected int getNumberPageThreshold() {
    if (nbPageThreshold == -1) {
      nbPageThreshold =
          Integer.parseInt(getSettings().getString("Pagination.NbPageThreshold", "6"));
      nbPagesOnRight = nbPageThreshold / 2;
    }
    return nbPageThreshold;
  }

  protected int getNumberPageOnCurrentLeft(int currentPage) {
    if (nbPagesOnLeft == -1) {
      final int threshold = getNumberPageThreshold();
      nbPagesOnLeft = threshold / 2;
      nbPagesOnLeft = (threshold % 2) == 0 ? nbPagesOnLeft - 1 : nbPagesOnLeft;
    }
    int offset = getNbPage() - currentPage;
    offset = offset < nbPagesOnRight ? nbPagesOnRight - offset : 0;
    return nbPagesOnLeft + offset;
  }

  protected int getNumberPageOnCurrentRight(int currentPage) {
    if (nbPagesOnRight == -1) {
      nbPagesOnRight = getNumberPageThreshold() / 2;
    }
    final int offset = currentPage <= nbPagesOnLeft ? Math.abs(currentPage - nbPagesOnLeft - 1) : 0;
    return nbPagesOnRight + offset;
  }

  protected boolean displayTotalNumberOfPages() {
    return getSettings().getBoolean("Pagination.DisplayTotalNumberOfPages", false);
  }

  protected List<Integer> getNbItemPerPageList() {
    List<Integer> result = new ArrayList<>();
    for (int i = 1; true; i++) {
      int nbItemPerPage = getSettings().getInteger("Pagination.NbItemPerPage." + i, 0);
      if (nbItemPerPage > 0) {
        result.add(nbItemPerPage);
      } else {
        break;
      }
    }
    return result;
  }

  protected int getIndexThreshold() {
    int value = getSettings().getInteger("Pagination.IndexThreshold");
    return defaultValueIfNotDefined(value);
  }

  protected int getNumberPerPageThreshold() {
    int value = getSettings().getInteger("Pagination.NumberPerPageThreshold");
    return defaultValueIfNotDefined(value);
  }

  protected int getJumperThreshold() {
    int value = getSettings().getInteger("Pagination.JumperThreshold");
    return defaultValueIfNotDefined(value);
  }

  protected int getPaginationAllThreshold() {
    int value = getSettings().getInteger("Pagination.PaginationAllThreshold");
    return defaultValueIfNotDefined(value);
  }

  private int defaultValueIfNotDefined(int value) {
    if (value <= 0) {
      return Integer.MAX_VALUE;
    }
    return value;
  }

  public LocalizationBundle getMultilang() {
    return multilang;
  }

  @Override
  public void setMultilang(LocalizationBundle multilang) {
    this.multilang = multilang;
  }

  protected String getString(String key) {
    return getMultilang().getString(key);
  }

  protected String getStringWithParam(String key, String... params) {
    return getMultilang().getStringWithParams(key, params);
  }

  public String getString(String key, String defaultValue) {
    String translation;
    try {
      translation = getMultilang().getString(key);
    } catch (MissingResourceException ex) {
      SilverLogger.getLogger(this).debug(ex.getMessage(), ex);
      translation = defaultValue;
    }
    return translation;
  }
}