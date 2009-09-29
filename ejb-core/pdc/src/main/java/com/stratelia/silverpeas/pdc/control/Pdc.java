/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/
package com.stratelia.silverpeas.pdc.control;

import java.util.List;

import com.stratelia.silverpeas.contentManager.GlobalSilverContent;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * This object is used by the MainSessionController like userPanel,
 * personalization. It allows to transmit data from pdcPeas to component.
 * 
 * @author neysseri
 */
public class Pdc {

  private List selectedSilverContents = null;
  private String urlToReturn = null;

  public Pdc() {
  }

  public void setSelectedSilverContents(List selectedSilverContents) {
    this.selectedSilverContents = selectedSilverContents;
    GlobalSilverContent gsc = null;
    for (int i = 0; i < selectedSilverContents.size(); i++) {
      gsc = (GlobalSilverContent) selectedSilverContents.get(i);
      SilverTrace.info("Pdc", "Pdc.setSelectedSilverContents()",
          "root.MSG_GEN_PARAM_VALUE", "new silverContent selected = "
              + gsc.getName());
    }
  }

  public List getSelectedSilverContents() {
    return this.selectedSilverContents;
  }

  public void setURLToReturn(String url) {
    urlToReturn = url;
  }

  public String getURLToReturn() {
    return urlToReturn;
  }
}
