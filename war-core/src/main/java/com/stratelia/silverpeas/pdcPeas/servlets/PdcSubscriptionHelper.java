package com.stratelia.silverpeas.pdcPeas.servlets;

import java.rmi.RemoteException;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import com.silverpeas.pdcSubscription.model.PDCSubscription;
import com.silverpeas.pdcSubscription.util.PdcSubscriptionUtil;
import com.stratelia.silverpeas.classifyEngine.Criteria;
import com.stratelia.silverpeas.pdc.model.SearchContext;
import com.stratelia.silverpeas.pdc.model.SearchCriteria;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchSessionController;

public class PdcSubscriptionHelper {

	public static void init(PdcSearchSessionController pdcSC, HttpServletRequest request) throws Exception {
		String pdcSubscr = request.getParameter("isPDCSubscription");
		if (pdcSubscr != null && pdcSubscr.equalsIgnoreCase("true")) {
			request.setAttribute("isPDCSubscription", "true");
			pdcSC.setShowOnlyPertinentAxisAndValues(false);
		}
		String newPdcSubscr = request.getParameter("isNewPDCSubscription");
		if (newPdcSubscr != null && newPdcSubscr.equalsIgnoreCase("true")) {
			request.setAttribute("isNewPDCSubscription", "true");
		}
	}

	public static void loadSubscription(PdcSearchSessionController pdcSC, HttpServletRequest request) throws RemoteException {
		request.setAttribute("isPDCSubscription", "true");
		doPdcSubscription(pdcSC, request);
	}

	public static void addSubscription(PdcSearchSessionController pdcSC, HttpServletRequest request) throws RemoteException {
		doPdcSubscriptionAdd(request, pdcSC);
	}
	
	public static void updateSubscription(PdcSearchSessionController pdcSC, HttpServletRequest request) throws RemoteException {
		doPdcSubscriptionUpdate(pdcSC, request);
	}

	private static void doPdcSubscription(PdcSearchSessionController pdcSC, HttpServletRequest request) throws RemoteException {
        pdcSC.getSearchContext().clearCriterias();
        pdcSC.setPDCSubscription(null);

        boolean isNewSubcription = true;
        String requestId = request.getParameter("pdcSId");
        PDCSubscription pdcSubscription = null;
        if (requestId != null) {
            int pdcScId = -1;
            try {
                pdcScId = Integer.parseInt(requestId);
                isNewSubcription = false;
            }
            catch (NumberFormatException e) {
            }
            pdcSubscription = (new PdcSubscriptionUtil()).getPDCSubsriptionById(pdcScId);
            pdcSC.getSearchContext().clearCriterias();

            if (pdcSubscription != null && pdcSubscription.getPdcContext() != null) {
                for (int i = 0; i < pdcSubscription.getPdcContext().size(); i++) {
                    Criteria c = (Criteria) pdcSubscription.getPdcContext().get(i);
                    pdcSC.getSearchContext().addCriteria(makeSearchCriteria(c));
                }
            }
            pdcSC.setPDCSubscription(pdcSubscription);
        }

        request.setAttribute("PDCSubscription", pdcSubscription);
        if (isNewSubcription) {
            request.setAttribute("isNewPDCSubscription", "true");
        }
    }

	private static void doPdcSubscriptionAdd(HttpServletRequest request, PdcSearchSessionController pdcSC) throws RemoteException {
        String name = request.getParameter("scName");
        if (name == null) {
            name = "";
        }
        
        //retrieve a SearchContext according to request parameters
		SearchContext context = getSearchContextFromRequest(request);
        
        PDCSubscription subscription = new PDCSubscription(-1, name, context.getCriterias(), Integer.parseInt(pdcSC.getUserId()));
        (new PdcSubscriptionUtil()).createPDCSubsription(subscription);
        request.setAttribute("requestSaved", "yes");
    }

	private static void doPdcSubscriptionUpdate(PdcSearchSessionController pdcSC, HttpServletRequest request) throws RemoteException {
        PDCSubscription subscription = pdcSC.getPDCSubscription();
        if (subscription != null) {
            String name = request.getParameter("scName");
            if (name == null) {
                name = "";
            }
            subscription.setName(name);
            
			//retrieve a SearchContext according to request parameters
			SearchContext context = getSearchContextFromRequest(request);
            
            subscription.setPdcContext(context.getCriterias());
            (new PdcSubscriptionUtil()).updatePDCSubsription(subscription);
        }
        request.setAttribute("requestSaved", "yes");
    }

	private static SearchCriteria makeSearchCriteria(Criteria c) {
        return new SearchCriteria(c.getAxisId(), c.getValue());
    }
    
	//build a SearchContext according to request parameters
    public static SearchContext getSearchContextFromRequest(HttpServletRequest request)
    {
		String 					axisValueCouples 	= request.getParameter("AxisValueCouples");
		String 					axisValueCouple		= null;
		StringTokenizer 		tokenizer 			= new StringTokenizer(axisValueCouples, ",");
		SearchContext 			context 			= new SearchContext();
		String 					axisId 				= null;
		String 					valuePath 			= null;
		int 					i 					= -1;
		while (tokenizer.hasMoreTokens())
		{
			axisValueCouple = (String) tokenizer.nextToken();
			i = axisValueCouple.indexOf("-");
			if (i != -1)
			{
				axisId = axisValueCouple.substring(0, i);
				valuePath = axisValueCouple.substring(i+1);
				context.addCriteria(new SearchCriteria(Integer.parseInt(axisId), valuePath));
			}
		}
		return context;
    }

}