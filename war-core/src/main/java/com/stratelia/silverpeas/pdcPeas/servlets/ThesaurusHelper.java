package com.stratelia.silverpeas.pdcPeas.servlets;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.thesaurus.model.Jargon;
import com.stratelia.silverpeas.pdc.model.PdcException;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController;
import com.stratelia.webactiv.util.exception.SilverpeasException;

public class ThesaurusHelper {

  public static void initializeJargon(PdcSearchSessionController pdcSC)
      throws PdcException {
    pdcSC.initializeJargon();
  }

  public static void setJargonInfoInRequest(PdcSearchSessionController pdcSC,
      HttpServletRequest request) throws PdcException {
    try {
      setJargonInfoInRequest(pdcSC, request, pdcSC.getActiveThesaurus());
    } catch (RemoteException e) {
      throw new PdcException("ThesaurusHelper.setJargonInfoInRequest()",
          SilverpeasException.ERROR,
          "pdcPeas.EX_CANT_SET_JARGON_INFO_IN_REQUEST", "", e);
    }
  }

  public static void setJargonInfoInRequest(PdcSearchSessionController pdcSC,
      HttpServletRequest request, boolean isThesaurusActive) {
    Jargon jargon = pdcSC.getJargon();
    request.setAttribute("Jargon", jargon);
    request.setAttribute("ActiveThesaurus", new Boolean(isThesaurusActive));
  }

}