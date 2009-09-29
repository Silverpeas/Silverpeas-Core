package com.stratelia.webactiv.personalization.control.ejb;

import java.rmi.RemoteException;
import java.util.*;

public interface PersonalizationBmBusinessSkeleton {

  public void setActor(String userId) throws RemoteException;

  public void setLanguages(Vector languages) throws RemoteException;

  public Vector getLanguages() throws RemoteException;

  public String getFavoriteLanguage() throws RemoteException;

  public String getFavoriteLook() throws RemoteException;

  public void setFavoriteLook(String look) throws RemoteException;

  public void setPersonalWorkSpace(String spaceId) throws RemoteException;

  public String getPersonalWorkSpace() throws RemoteException;

  public void setThesaurusStatus(boolean thesaurusStatus)
      throws RemoteException;

  public boolean getThesaurusStatus() throws RemoteException;

  public void setDragAndDropStatus(boolean dragAndDropStatus)
      throws RemoteException;

  public boolean getDragAndDropStatus() throws RemoteException;

  public void setOnlineEditingStatus(boolean onlineEditingStatus)
      throws RemoteException;

  public boolean getOnlineEditingStatus() throws RemoteException;

  public void setWebdavEditingStatus(boolean webdavEditingStatus)
      throws RemoteException;

  public boolean getWebdavEditingStatus() throws RemoteException;

}