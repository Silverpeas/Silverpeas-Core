package com.stratelia.webactiv.util.viewGenerator.html.pagination;

import com.stratelia.webactiv.util.viewGenerator.html.SimpleGraphicElement;

/**
 * Pagination is an interface to be implemented by a graphic element to print a pages index or a elements counter.
 * @author neysseri
 */
public interface Pagination extends SimpleGraphicElement
{
    public void init(int nbItems, int nbItemsPerPage, int firstItemIndex);

	public void setAltPreviousPage(String text);

	public void setAltNextPage(String text);

	public void setActionSuffix(String actionSuffix);
	
	public String print();

    /**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String printIndex();

    public String printIndex(String text);
    
	public int getFirstItemIndex();

	public int getLastItemIndex();

	/**
     * Method declaration
     *
     *
     * @return
     *
     * @see
     */
    public String printCounter();
}