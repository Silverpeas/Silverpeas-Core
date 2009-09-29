package com.stratelia.silverpeas.containerManager;

/**
 * The interface for all kind of positions (PDC, List, Theme, Calendar, ...)
 * Every container have to extends this class
 */
public interface ContainerPositionInterface {
  /** Return true if the position is empty */
  public boolean isEmpty();
}
