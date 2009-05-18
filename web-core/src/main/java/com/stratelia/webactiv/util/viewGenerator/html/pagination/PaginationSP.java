package com.stratelia.webactiv.util.viewGenerator.html.pagination;

public class PaginationSP extends AbstractPagination
{
    public PaginationSP()
    {
        super();
    }

	public void init(int nbItems, int nbItemsPerPage, int firstItemIndex)
    {
        super.init(nbItems, nbItemsPerPage, firstItemIndex);
    }
    
    public String printCounter()
    {
        StringBuffer result = new StringBuffer();
		if (getNbItems() <= getNbItemsPerPage())
		{
			result.append(getNbItems()).append(" ");
		} else {
			int	end	= getFirstItemIndex()+getNbItemsPerPage();
			if (end > getNbItems())
				end = getNbItems();
			result.append(getFirstItemIndex()+1).append(" - ").append(end).append(" / ").append(getNbItems()).append(" ");
		}
        return result.toString();
    }

    public String printIndex()
    {
		return printIndex(null);
    }
    
    public String printIndex(String javascriptFunc)
    {
		StringBuffer result = new StringBuffer();

		if (getNbItems() > 0 && getNbItems() > getNbItemsPerPage())
		{
			result.append("<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\" align=\"center\">");
			result.append("<tr valign=\"middle\" class=\"intfdcolor\">");
			result.append("<td align=\"center\" class=\"ArrayNavigation\">");

			String altNextPage		= getAltNextPage();
			String altPreviousPage	= getAltPreviousPage();

			int nbPage		= getNbPage();
			int currentPage = getCurrentPage();
			int index		= -1;

			if (getFirstItemIndex() >= getNbItemsPerPage()) {
				index = getIndexForPreviousPage();
				result.append(getLink(javascriptFunc, index));
				result.append("<img src=\"").append(getIconsPath()).append("/arrows/arrowLeft.gif\" border=\"0\" align=\"absmiddle\" alt=\"").append(altPreviousPage).append("\"/></a> ");
			} else {
				result.append("&#160;&#160;&#160;");
			}
			for (int i=1; i<=nbPage; i++)
			{
				if (i==currentPage)
					result.append(" <span class=\"ArrayNavigationOn\">&#160;").append(i).append("&#160;</span> ");
				else {
					index = getIndexForDirectPage(i);
					result.append(getLink(javascriptFunc, index));
					result.append(i).append("</a> ");
				}
			}
			if (!isLastPage()) {
				index = getIndexForNextPage();
				result.append(getLink(javascriptFunc, index));
				result.append("<img src=\"").append(getIconsPath()).append("/arrows/arrowRight.gif\" border=\"0\" align=\"absmiddle\" alt=\"").append(altNextPage).append("\"/></a>");
			} else {
				result.append("&#160;&#160;&#160;");
			}
			result.append("</td>");
			result.append("</tr>");
			result.append("</table>");
		}
		return result.toString();
    }
    
    //formatage du lien de la source de la balise href
    private String getLink(String javascriptFunc, int index)
    {
    	StringBuffer 	link 	= new StringBuffer();
    	String 			action 	= "Pagination"+getActionSuffix();
    	if (javascriptFunc == null)
		{
			if (getBaseURL() != null)
			{
				link.append(" <a class=\"ArrayNavigation\" href=\"").append(getBaseURL()).append(index);
			}
			else
			{
				//action pagination
				link.append(" <a class=\"ArrayNavigation\" href=\"").append(action).append("?Index=").append(index);
			}
		}
		else 
			link.append(" <a class=\"ArrayNavigation\" href=\"").append("javascript:onClick=").append(javascriptFunc).append("(").append(index).append(")");
    	link.append("\">");
    	return link.toString();
    }

    public String print()
    {
        return printIndex();
    }
}