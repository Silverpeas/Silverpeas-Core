/** 
 *
 * @author  akhadrou
 * @version 
 */
package com.stratelia.webactiv.util.publication.info.model;
 
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import com.stratelia.webactiv.util.indexEngine.model.IndexManager;

public class InfoDetail implements Serializable {
 
  private InfoPK pk = null;
  private Collection textList = null;
  private Collection imageList = null;
  private Collection linkList = null;
  private String content = null;
  
  //added for indexation
  private int indexOperation = IndexManager.ADD;

  public InfoDetail(InfoPK pk, Collection textList, Collection imageList, Collection linkList, String content) {
	    setPK(pk);
        setInfoTextList(textList);
		setInfoImageList(imageList);
		setInfoLinkList(linkList);
		setContent(content);
  }
  
  /**
   * module info doesn't manage attachments
   * @deprecated
   */
  public InfoDetail(InfoPK pk, Collection textList, Collection attachmentList, Collection imageList, Collection linkList, String content) {
	    setPK(pk);
        setInfoTextList(textList);
        setInfoImageList(imageList);
        setInfoLinkList(linkList);
        setContent(content);
  }

  // set get on attributes        
  //id
  public InfoPK getPK() {
        return pk;
  }	
  public void setPK(InfoPK pk) {
        this.pk = pk;
  }
  
  public Collection getInfoTextList() {
    return textList;
  }
  public void setInfoTextList(Collection textList) {
    this.textList = textList;
  }
  
  public Collection getInfoImageList() {
    return imageList;
  }
  public void setInfoImageList(Collection imageList) {
    this.imageList = imageList;
  }
  
  public Collection getInfoLinkList() {
    return linkList;
  }
  public void setInfoLinkList(Collection linkList) {
    this.linkList = linkList;
  }

  public String getContent() {
    return content;
  }  
  public void setContent(String content) {
    this.content = content;
  }
  
  public static void selectToCreateAndToUpdateItems(Collection newItems, Collection oldItems,
    Collection toCreateItems, Collection toUpdateItems)
  {
    if (newItems == null) return;
    Iterator newItemsIterator = newItems.iterator();
    while (newItemsIterator.hasNext()) {
      InfoItemDetail newItemDetail = (InfoItemDetail) newItemsIterator.next();
      boolean isToCreate = true;
      if (oldItems != null) {
        Iterator oldItemsIterator = oldItems.iterator();
        while (oldItemsIterator.hasNext()) {
          InfoItemDetail oldItemDetail = (InfoItemDetail) oldItemsIterator.next();
          if (oldItemDetail.getOrder().equals(newItemDetail.getOrder()))
          {
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