package com.stratelia.webactiv.util.viewGenerator.html.pagination;

public class PaginationSilverpeasV5 extends AbstractPagination {
  public PaginationSilverpeasV5() {
    super();
  }

  public void init(int nbItems, int nbItemsPerPage, int firstItemIndex) {
    super.init(nbItems, nbItemsPerPage, firstItemIndex);
  }

  public String printCounter() {
    StringBuffer result = new StringBuffer();
    if (getNbItems() <= getNbItemsPerPage()) {
      result.append(getNbItems()).append(" ");
    } else {
      int end = getFirstItemIndex() + getNbItemsPerPage();
      if (end > getNbItems())
        end = getNbItems();
      result.append(getFirstItemIndex() + 1).append(" - ").append(end).append(
          " / ").append(getNbItems()).append(" ");
    }
    return result.toString();
  }

  public String printIndex() {
    return printIndex(null);
  }

  public String printIndex(String javascriptFunc) {
    StringBuffer result = new StringBuffer();

    if (getNbItems() > 0 && getNbItems() > getNbItemsPerPage()) {
      result
          .append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" align=\"center\">");
      result.append("<tr valign=\"middle\" class=\"intfdcolor\">");
      result.append("<td align=\"center\" >");

      String action = "Pagination" + getActionSuffix();
      String altNextPage = getAltNextPage();
      String altPreviousPage = getAltPreviousPage();

      int nbPage = getNbPage();
      int currentPage = getCurrentPage();
      int index = -1;

      if (getFirstItemIndex() >= getNbItemsPerPage()) {
        index = getIndexForPreviousPage();

        // formatage du lien de la source de la balise href
        if (javascriptFunc == null)
          result.append(" <a href=\"").append(action).append("?Index=").append(
              index);
        else
          result.append(" <a href=\"").append("javascript:onClick=").append(
              javascriptFunc).append("(").append(index).append(")");

        result.append("\"><img src=\"").append(getIconsPath()).append(
            "/arrows/arrowLeft.gif\" border=\"0\" align=\"absmiddle\" alt=\"")
            .append(altPreviousPage).append("\"></a> ");
      } else {
        result.append("&#160;&#160;&#160;");
      }
      for (int i = 1; i <= nbPage; i++) {
        if (i == currentPage)
          result.append(" ").append(i).append(" ");
        else {
          index = getIndexForDirectPage(i);
          // formatage du lien de la source de la balise href
          if (javascriptFunc == null)
            result.append(" <a href=\"").append(action).append("?Index=")
                .append(index);
          else
            result.append(" <a href=\"").append("javascript:onClick=").append(
                javascriptFunc).append("(").append(index).append(")");
          result.append("\">").append(i).append("</a> ");
        }
      }
      if (!isLastPage()) {
        index = getIndexForNextPage();

        // formatage du lien de la source de la balise href
        if (javascriptFunc == null)
          result.append(" <a href=\"").append(action).append("?Index=").append(
              index);
        else
          result.append(" <a href=\"").append("javascript:onClick=").append(
              javascriptFunc).append("(").append(index).append(")");

        result.append("\"><img src=\"").append(getIconsPath()).append(
            "/arrows/arrowRight.gif\" border=\"0\" align=\"absmiddle\" alt=\"")
            .append(altNextPage).append("\"></a>");
      } else {
        result.append("&#160;&#160;&#160;");
      }
      result.append("</td>");
      result.append("</tr>");
      result.append("</table>");
    }
    return result.toString();
  }

  public String print() {
    return printIndex();
  }
}