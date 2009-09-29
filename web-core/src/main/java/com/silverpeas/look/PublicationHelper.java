/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.silverpeas.look;

import com.stratelia.silverpeas.peasCore.MainSessionController;
import java.util.List;

/**
 * 
 * @author ehugonnet
 */
public interface PublicationHelper {

  public List getPublications(String spaceId, int nbPublis);

  public void setMainSessionController(MainSessionController mainSC);
}
