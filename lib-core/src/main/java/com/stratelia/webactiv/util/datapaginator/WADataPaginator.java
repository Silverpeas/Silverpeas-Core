/*
 * WADataPaginator.java
 *
 * Created on 26 mars 2001, 09:51
 */
 
package com.stratelia.webactiv.util.datapaginator;

/** 
 * This interface is used when pagination of large result sets is needed, mostly
 * as part of the rendering process.
 * @author  jpouyadou
 * @version 
 */
public interface WADataPaginator 
{
	/** this method returns the next page of data, or null is there is no
	 * such page
	 * @see #getPreviousPage
	 */
	public WADataPage getNextPage();
	/** this method returns the previous page of data, or null is there is no
	 * such page
	 * @see #getNextPage
	 */
	public WADataPage getPreviousPage();
	/** this method returns the first page of data, or null is there is no
	 * such page. The next call to getNextPage will thus return the second page.
	 * @see #getNextPage
	 * @see #getPreviousPage
	 */
	public WADataPage getFirstPage();
	/** this method returns the current page of data, or null is there is no
	 * such page. 
	 * @see #getNextPage
	 * @see #getPreviousPage
	 */
	public WADataPage getCurrentPage();
	/** this method returns the last page of data, or null is there is no
	 * such page. T
	 * @see #getNextPage
	 * @see #getpreviousPage
	 */
	public WADataPage getLastPage();
	/** this method returns the count of pages in this set
	 * @see #getNextPage
	 * @see #getPreviousPage
	 */
	public int getPageCount();
	/**
	 * this method returns the total count of items
	 */
	public int getItemCount();
	/**
	 * This method retuns the current, 0-base page number, or -1 if there is
	 * no current page
	 */
	public int getCurrentPageNumber();
	/**
	 * This method sets the page size, that is, the count of items that fit on
	 * one page. Usually called from the rendering process.
	 */
	public void setPageSize(int size);
	/**
	 * This method sets the header. The header is used mostly to know how to
	 * sort items
	 */
	public void setHeader(WADataPaginatorHeader h);
	/*
	 * this method returns the header set via the {@link #setHeader} method
	 */
	public WADataPaginatorHeader getHeader();
}
