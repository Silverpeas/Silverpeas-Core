package org.silverpeas.core.web.util;

/**
 * Created by Nicolas on 08/02/2017.
 */
public class ListIndex {

  private int nbItems;
  private int currentIndex = 0;

  public ListIndex(final int nbItems) {
    this.nbItems = nbItems;
  }

  public int getNbItems() {
    return nbItems;
  }

  public void setNbItems(final int nbItems) {
    this.nbItems = nbItems;
  }

  public int getCurrentIndex() {
    return currentIndex;
  }

  public void setCurrentIndex(final int currentIndex) {
    this.currentIndex = currentIndex;
  }

  public int getPreviousIndex() {
    if (currentIndex > 0) {
      currentIndex--;
    }
    return currentIndex;
  }

  public int getNextIndex() {
    if (currentIndex < nbItems-1) {
      currentIndex++;
    }
    return currentIndex;
  }
}
