/*
 * WADataPage.java
 *
 * Created on 26 mars 2001, 10:03
 */

package com.stratelia.webactiv.util.datapaginator;

/**
 * 
 * @author jpouyadou
 * @version
 */
public interface WADataPage {
  /**
   * This method returns the count of items on the page
   */
  public int getItemCount();

  public WAItem getFirstItem();

  public WAItem getLastItem();

  public WAItem getNextItem();

  public WAItem getPreviousItem();

  public WAItem getItemByName(String name);

  /**
   * this method returns the index, <strong>relative to the parent
   * document</strong> of the first item on the page. This index is inclusive,
   * that is, the item actually belongs to the page.
   * 
   * @see getEndIndex()
   */
  public int getStartItemDocumentIndex();

  /**
   * this method returns the index, <strong>relative to the parent
   * document</strong> of the last item on the page. This index is exclusive,
   * that is, the item actually belongs to the next page (this index is the
   * index of the last visible item + 1).
   * 
   * @see getStartIndex()
   */
  public int getEndItemDocumentIndex();
}
