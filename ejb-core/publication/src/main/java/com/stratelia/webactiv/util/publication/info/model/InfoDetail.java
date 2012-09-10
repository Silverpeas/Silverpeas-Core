/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/** 
 *
 * @author  akhadrou
 * @version 
 */
package com.stratelia.webactiv.util.publication.info.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.silverpeas.search.indexEngine.model.IndexManager;

public class InfoDetail implements Serializable {

  private static final long serialVersionUID = 1449129863941833416L;
  private InfoPK pk = null;
  private Collection<InfoTextDetail> textList = null;
  private Collection<InfoImageDetail> imageList = null;
  /**
   * @deprecated use linkList from CompletePublication
   */
  private Collection<InfoLinkDetail> linkList = null;
  private String content = null;

  // added for indexation
  private int indexOperation = IndexManager.ADD;

  public InfoDetail(InfoPK pk, Collection<InfoTextDetail> textList,
      Collection<InfoImageDetail> imageList,
      Collection<InfoLinkDetail> linkList, String content) {
    setPK(pk);
    setInfoTextList(textList);
    setInfoImageList(imageList);
    setInfoLinkList(linkList);
    setContent(content);
  }

  public InfoPK getPK() {
    return pk;
  }

  public void setPK(InfoPK pk) {
    this.pk = pk;
  }

  public Collection<InfoTextDetail> getInfoTextList() {
    return textList;
  }

  public void setInfoTextList(Collection<InfoTextDetail> textList) {
    this.textList = textList;
  }

  public Collection<InfoImageDetail> getInfoImageList() {
    return imageList;
  }

  public void setInfoImageList(Collection<InfoImageDetail> imageList) {
    this.imageList = imageList;
  }

  /**
   * @deprecated use getLinkList() method from CompletePublication class
   * @see @link {com.stratelia.webactiv.util.publication.model.CompletePublication.getLinkList()}
   * @return
   */
  public Collection<InfoLinkDetail> getInfoLinkList() {
    return linkList;
  }

  /**
   * @deprecated use setLinkList() method from CompletePublication class
   * @see {@link com.stratelia.webactiv.util.publication.model.CompletePublication.setLinkList()}
   * @param linkList
   */
  public void setInfoLinkList(Collection<InfoLinkDetail> linkList) {
    this.linkList = linkList;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public static void selectToCreateAndToUpdateItems(Collection newItems,
      Collection oldItems, Collection toCreateItems, Collection toUpdateItems) {
    if (newItems == null)
      return;
    Iterator<InfoItemDetail> newItemsIterator = newItems.iterator();
    while (newItemsIterator.hasNext()) {
      InfoItemDetail newItemDetail = (InfoItemDetail) newItemsIterator.next();
      boolean isToCreate = true;
      if (oldItems != null) {
        Iterator<InfoItemDetail> oldItemsIterator = oldItems.iterator();
        while (oldItemsIterator.hasNext()) {
          InfoItemDetail oldItemDetail = (InfoItemDetail) oldItemsIterator
              .next();
          if (oldItemDetail.getOrder().equals(newItemDetail.getOrder())) {
            newItemDetail.setId(oldItemDetail.getId());
            isToCreate = false;
          }
        }
      }
      if (isToCreate)
        toCreateItems.add(newItemDetail);
      else
        toUpdateItems.add(newItemDetail);
    }
  }

  public int getIndexOperation() {
    return indexOperation;
  }

  public void setIndexOperation(int i) {
    indexOperation = i;
  }

}