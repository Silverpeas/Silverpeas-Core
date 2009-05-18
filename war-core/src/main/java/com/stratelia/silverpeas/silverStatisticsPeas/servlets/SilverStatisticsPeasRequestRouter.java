/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) ---*/

package com.stratelia.silverpeas.silverStatisticsPeas.servlets;

import java.util.Calendar;

import javax.servlet.http.HttpServletRequest;

import org.jCharts.Chart;
import org.jCharts.nonAxisChart.PieChart2D;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silverStatisticsPeas.control.SilverStatisticsPeasSessionController;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * Class declaration
 *
 *
 * @author
 */
public class SilverStatisticsPeasRequestRouter extends ComponentRequestRouter
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Method declaration
     *
     *
     * @param mainSessionCtrl
     * @param componentContext
     *
     * @return
     *
     * @see
     */
    public ComponentSessionController createComponentSessionController(MainSessionController mainSessionCtrl, ComponentContext componentContext)
    {
        return new SilverStatisticsPeasSessionController(mainSessionCtrl, componentContext);
    }

    /**
     * This method has to be implemented in the component request rooter class.
     * returns the session control bean name to be put in the request object
     * ex : for almanach, returns "almanach"
     */
    public String getSessionControlBeanName()
    {
        return "SilverStatisticsPeas";
    }

    /**
     * This method has to be implemented by the component request router
     * it has to compute a destination page
     * @param function The entering request function (ex : "Main.jsp")
     * @param componentSC The component Session Control, build and initialised.
     * @return The complete destination URL for a forward (ex : "/almanach/jsp/almanach.jsp?flag=user")
     */
    public String getDestination(String function, ComponentSessionController componentSC, HttpServletRequest request)
    {
        String destination = "";
        SilverStatisticsPeasSessionController  silverStatisticsSC = (SilverStatisticsPeasSessionController)componentSC;
        SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "User=" + silverStatisticsSC.getUserId() + " Function=" + function);

        String userProfile = silverStatisticsSC.getUserProfile();
        if (userProfile.equals("A") || userProfile.equals(SilverStatisticsPeasSessionController.SPACE_ADMIN))
        {
            request.setAttribute("UserProfile", userProfile);
        }
        else
        {
            SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "userProfile=" + userProfile);
            return null;
        }

        Calendar today = Calendar.getInstance();
        String currentMonth= ""+today.get(Calendar.MONTH);
        String currentYear= ""+today.get(Calendar.YEAR);

        try
        {
            if (function.startsWith("Main"))
            {
                // We only enter in the main case on the first access to the silverStatistics pages
                request.setAttribute("ConnectedUsersList", silverStatisticsSC.getConnectedUsersList());
                destination = "/silverStatisticsPeas/jsp/connections.jsp";
            }
            else if (function.equals("KickSession"))
            {
                silverStatisticsSC.KickSession(request.getParameter("theSessionId"));
                request.setAttribute("ConnectedUsersList", silverStatisticsSC.getConnectedUsersList());
                destination = "/silverStatisticsPeas/jsp/connections.jsp";
            }
            else if (function.equals("DisplayNotifySession"))
            {
                request.setAttribute("userDetail", silverStatisticsSC.getTargetUserDetail(request.getParameter("theUserId")));
                request.setAttribute("action", "NotifyUser");
                destination = "/silverStatisticsPeas/jsp/writeMessage.jsp";
            }
            else if (function.equals("DisplayNotifyAllSessions"))
            {
                request.setAttribute("action", "NotifyAllUsers");
                destination = "/silverStatisticsPeas/jsp/writeMessage.jsp";
            }
            else if (function.equals("ToAlert"))
            {
                silverStatisticsSC.notifySession(request.getParameter("theUserId"),request.getParameter("messageAux"));
                request.setAttribute("action", "Close");
                destination = "/silverStatisticsPeas/jsp/writeMessage.jsp";
            }
            else if (function.equals("ToAlertAllUsers"))
            {
            	silverStatisticsSC.notifyAllSessions(silverStatisticsSC.getConnectedUsersList(),request.getParameter("messageAux"));
                request.setAttribute("action", "Close");
                destination = "/silverStatisticsPeas/jsp/writeMessage.jsp";
            }
            else if (function.startsWith("ViewConnections"))
            {
                silverStatisticsSC.setMonthBegin(currentMonth);
                silverStatisticsSC.setYearBegin(currentYear);
                silverStatisticsSC.setMonthEnd(currentMonth);
                silverStatisticsSC.setYearEnd(currentYear);
                silverStatisticsSC.setActorDetail("0");
                silverStatisticsSC.setFilterType("");
                silverStatisticsSC.setFilterLib("");
                silverStatisticsSC.setFilterId("");

                // init formulaire
                request.setAttribute("MonthBegin", silverStatisticsSC.getMonth(currentMonth));
                request.setAttribute("YearBegin", silverStatisticsSC.getYearConnection(currentYear));
                request.setAttribute("MonthEnd", silverStatisticsSC.getMonth(currentMonth));
                request.setAttribute("YearEnd", silverStatisticsSC.getYearConnection(currentYear));
                request.setAttribute("ActorDetail", silverStatisticsSC.getDetail("0"));
                request.setAttribute("FilterType", "");
                request.setAttribute("FilterLib", "");
                request.setAttribute("FilterId", "");

                destination = "/silverStatisticsPeas/jsp/viewConnection.jsp";
            }
            else if (function.startsWith("ValidateViewConnection"))
            {
                // save request param
				saveConnectionParam(request, silverStatisticsSC);

                String hostMonthBegin = silverStatisticsSC.getMonthBegin();
                String hostYearBegin = silverStatisticsSC.getYearBegin();
                String hostMonthEnd = silverStatisticsSC.getMonthEnd();
                String hostYearEnd = silverStatisticsSC.getYearEnd();
                String hostDateBegin = getRequestDate(hostYearBegin, hostMonthBegin);
                String hostDateEnd = getRequestDate(hostYearEnd, hostMonthEnd);
                String hostStatDetail = silverStatisticsSC.getActorDetail();
                String filterType = silverStatisticsSC.getFilterType();
                String filterId = silverStatisticsSC.getFilterId();

                // compute result
                if (hostStatDetail.equals("0"))// All
                {
                    if (filterType.equals("")) // no filter
                    {
                        request.setAttribute("ResultData", silverStatisticsSC.getStatsConnexionAllAll(hostDateBegin, hostDateEnd));
                        
                        //graphiques
                        request.setAttribute("GraphicDistinctUser", Boolean.TRUE);
                        Chart loginChart = silverStatisticsSC.getDistinctUserConnectionsChart(hostDateBegin, hostDateEnd);
                        request.getSession( true ).setAttribute( ChartServlet.LOGINCHART, loginChart );
                        
                        Chart userChart = silverStatisticsSC.getUserConnectionsChart(hostDateBegin, hostDateEnd);
            			request.getSession( true ).setAttribute( ChartServlet.USERCHART, userChart );
            			
                    }
                    else if (filterType.equals("0")) // filter group
                    {
                        request.setAttribute("ResultData", silverStatisticsSC.getStatsConnexionAllGroup(hostDateBegin, hostDateEnd, filterId));
                        
                        //graphiques
                        Chart userChart = silverStatisticsSC.getUserConnectionsGroupChart(hostDateBegin, hostDateEnd, filterId);
            			request.getSession( true ).setAttribute( ChartServlet.USERCHART, userChart );
                    }
                    else if (filterType.equals("1")) // filter user
                    {
                        request.setAttribute("ResultData", silverStatisticsSC.getStatsConnexionAllUser(hostDateBegin, hostDateEnd, filterId));
                        
                        //graphiques
                        Chart userChart = silverStatisticsSC.getUserConnectionsUserChart(hostDateBegin, hostDateEnd, filterId);
            			request.getSession( true ).setAttribute( ChartServlet.USERCHART, userChart );
                    }
                }

                else if (hostStatDetail.equals("1"))// Groups
                {
                    if (filterType.equals("")) // no filter
                    {
                        request.setAttribute("ResultData", silverStatisticsSC.getStatsConnexionGroupAll(hostDateBegin, hostDateEnd));
                        
                        String entiteId = request.getParameter("EntiteId");
                        
                        if(entiteId != null) {
                        	//graphiques
	                        Chart userChart = silverStatisticsSC.getUserConnectionsGroupChart(hostDateBegin, hostDateEnd, entiteId);
	            			request.getSession( true ).setAttribute( ChartServlet.USERCHART, userChart );
                        } else {
                        	//graphiques
	                        Chart userChart = silverStatisticsSC.getUserConnectionsChart(hostDateBegin, hostDateEnd);
	            			request.getSession( true ).setAttribute( ChartServlet.USERCHART, userChart );
                        }
                    }
                    else if (filterType.equals("0")) // filter group
                    {
                        request.setAttribute("ResultData", silverStatisticsSC.getStatsConnexionGroupUser(hostDateBegin, hostDateEnd, filterId));
                        
                        //graphiques
                        Chart userChart = silverStatisticsSC.getUserConnectionsGroupChart(hostDateBegin, hostDateEnd, filterId);
            			request.getSession( true ).setAttribute( ChartServlet.USERCHART, userChart );
                    }
                    else if (filterType.equals("1")) // filter user
                    {
                        // no result
                    }
                }

                else if (hostStatDetail.equals("2"))// Users
                {
                    if (filterType.equals("")) // no filter
                    {
                        request.setAttribute("ResultData", silverStatisticsSC.getStatsConnexionUserAll(hostDateBegin, hostDateEnd));
                        
                        String entiteId = request.getParameter("EntiteId");
                        
                        if(entiteId != null) {
                        	//graphiques
	                        Chart userChart = silverStatisticsSC.getUserConnectionsUserChart(hostDateBegin, hostDateEnd, entiteId);
	            			request.getSession( true ).setAttribute( ChartServlet.USERCHART, userChart );
                        } else {
                        	//graphiques
	                        Chart userChart = silverStatisticsSC.getUserConnectionsChart(hostDateBegin, hostDateEnd);
	            			request.getSession( true ).setAttribute( ChartServlet.USERCHART, userChart );
                        }
                    }
                    else if (filterType.equals("0")) // filter group
                    {
                        // no result
                    }
                    else if (filterType.equals("1")) // filter user
                    {
                        request.setAttribute("ResultData", silverStatisticsSC.getStatsConnexionUserUser(hostDateBegin, hostDateEnd, filterId));
                        
                        //graphiques
                        Chart userChart = silverStatisticsSC.getUserConnectionsUserChart(hostDateBegin, hostDateEnd, filterId);
            			request.getSession( true ).setAttribute( ChartServlet.USERCHART, userChart );
                    }
                }

                restoreConnectionParam(request, silverStatisticsSC);
                destination = "/silverStatisticsPeas/jsp/viewConnection.jsp";
            }
            else if (function.startsWith("CallUserPanel"))
            {
                // save request param
                saveConnectionParam(request, silverStatisticsSC);

                // init user panel
				destination = silverStatisticsSC.initUserPanel();
            }
            else if (function.startsWith("ReturnFromUserPanel"))
            {
            	// get user panel data (update FilterType and FilterLib, FilterId)
				silverStatisticsSC.retourUserPanel();

                // restore request param
                restoreConnectionParam(request, silverStatisticsSC);

                destination = "/silverStatisticsPeas/jsp/viewConnection.jsp";
            }
	        else if (function.startsWith("ViewFrequence"))
            {
	        	silverStatisticsSC.setMonthBegin(currentMonth);
                silverStatisticsSC.setYearBegin(currentYear);
                silverStatisticsSC.setMonthEnd(currentMonth);
                silverStatisticsSC.setYearEnd(currentYear);
                silverStatisticsSC.setFrequenceDetail("0");

                // init formulaire
                request.setAttribute("MonthBegin", silverStatisticsSC.getMonth(currentMonth));
                request.setAttribute("YearBegin", silverStatisticsSC.getYearConnection(currentYear));
                request.setAttribute("MonthEnd", silverStatisticsSC.getMonth(currentMonth));
                request.setAttribute("YearEnd", silverStatisticsSC.getYearConnection(currentYear));
                request.setAttribute("FrequenceDetail", silverStatisticsSC.getFrequenceDetail("0"));

                destination = "/silverStatisticsPeas/jsp/viewFrequence.jsp";
            }
	        else if (function.startsWith("ValidateViewFrequence"))
            {
                // save request param
	        	String hostMonthBegin = request.getParameter("MonthBegin");
	        	if (hostMonthBegin != null && !hostMonthBegin.equals(""))
	        	{
	        		silverStatisticsSC.setMonthBegin(request.getParameter("MonthBegin"));
	                silverStatisticsSC.setYearBegin(request.getParameter("YearBegin"));
	            	silverStatisticsSC.setMonthEnd(request.getParameter("MonthEnd"));
	                silverStatisticsSC.setYearEnd(request.getParameter("YearEnd"));
	            	silverStatisticsSC.setFrequenceDetail(request.getParameter("FrequenceDetail"));
	            }

                hostMonthBegin = silverStatisticsSC.getMonthBegin();
                String hostYearBegin = silverStatisticsSC.getYearBegin();
                String hostMonthEnd = silverStatisticsSC.getMonthEnd();
                String hostYearEnd = silverStatisticsSC.getYearEnd();
                String hostDateBegin = getRequestDate(hostYearBegin, hostMonthBegin);
                String hostDateEnd = getRequestDate(hostYearEnd, hostMonthEnd);
                String hostStatDetail = silverStatisticsSC.getFrequenceDetail();

                //graphiques
                Chart userFqChart = silverStatisticsSC.getUserConnectionsFqChart(hostDateBegin, hostDateEnd, hostStatDetail);
                request.getSession( true ).setAttribute( ChartServlet.USERFQCHART, userFqChart );
                request.setAttribute("Graphic", Boolean.TRUE);
            
                request.setAttribute("MonthBegin", silverStatisticsSC.getMonth(silverStatisticsSC.getMonthBegin()));
                request.setAttribute("YearBegin", silverStatisticsSC.getYearConnection(silverStatisticsSC.getYearBegin()));
                request.setAttribute("MonthEnd", silverStatisticsSC.getMonth(silverStatisticsSC.getMonthEnd()));
                request.setAttribute("YearEnd", silverStatisticsSC.getYearConnection(silverStatisticsSC.getYearEnd()));
                request.setAttribute("FrequenceDetail", silverStatisticsSC.getFrequenceDetail(silverStatisticsSC.getFrequenceDetail()));
                
                destination = "/silverStatisticsPeas/jsp/viewFrequence.jsp";
            }

            // Onglet Acces
	        else if (function.startsWith("ViewAccess"))
            {
                silverStatisticsSC.setAccessMonthBegin(currentMonth);
                silverStatisticsSC.setAccessYearBegin(currentYear);
                silverStatisticsSC.setAccessFilterLibGroup("");
                silverStatisticsSC.setAccessFilterIdGroup("");
                silverStatisticsSC.setAccessFilterLibUser("");
                silverStatisticsSC.setAccessFilterIdUser("");
                silverStatisticsSC.setAccessSpaceId("");
                silverStatisticsSC.clearCurrentStats(); 

                // init formulaire access
                request.setAttribute("MonthBegin", silverStatisticsSC.getMonth(currentMonth));
                request.setAttribute("YearBegin", silverStatisticsSC.getYearAccess(currentYear));
                request.setAttribute("FilterLibGroup", silverStatisticsSC.getAccessFilterLibGroup());
                request.setAttribute("FilterIdGroup", silverStatisticsSC.getAccessFilterIdGroup());
                request.setAttribute("FilterLibUser", silverStatisticsSC.getAccessFilterLibUser());
                request.setAttribute("FilterIdUser", silverStatisticsSC.getAccessFilterIdUser());
                request.setAttribute("SpaceId", silverStatisticsSC.getAccessSpaceId());
                request.setAttribute("Path", silverStatisticsSC.getPath());

                destination = "/silverStatisticsPeas/jsp/viewAccess.jsp";
            }
	        else if (function.startsWith("ValidateViewAccess"))
            {
                // save request param
                saveAccessVolumeParam(request, silverStatisticsSC);

                String hostMonthBegin = silverStatisticsSC.getAccessMonthBegin();
                String hostYearBegin = silverStatisticsSC.getAccessYearBegin();
                String filterIdGroup = silverStatisticsSC.getAccessFilterIdGroup();
                String filterIdUser = silverStatisticsSC.getAccessFilterIdUser();
                String spaceId = silverStatisticsSC.getAccessSpaceId();

                // compute result
                PieChart2D pieChart = silverStatisticsSC.getUserVentilChart(getRequestDate(hostYearBegin, hostMonthBegin), filterIdGroup, filterIdUser, spaceId);
    			request.getSession( true ).setAttribute( ChartServlet.USERVENTILCHART, pieChart );
    			request.setAttribute("StatsData", silverStatisticsSC.getCurrentStats());
    			
                // restore request param
                restoreAccessParam(request, silverStatisticsSC);
                destination = "/silverStatisticsPeas/jsp/viewAccess.jsp";
            }
            else if (function.startsWith("AccessCallUserPanelGroup"))
            {
                // save request param
                saveAccessVolumeParam(request, silverStatisticsSC);

                // init user panel
				destination = silverStatisticsSC.initAccessUserPanelGroup();
            }
            else if (function.startsWith("AccessReturnFromUserPanelGroup"))
            {
            	// get user panel data (update FilterLib, FilterId)
				silverStatisticsSC.retourAccessUserPanelGroup();

                // restore request param
                restoreAccessParam(request, silverStatisticsSC);

                destination = "/silverStatisticsPeas/jsp/viewAccess.jsp";
            }
            else if (function.startsWith("AccessCallUserPanelUser"))
            {
                // save request param
                saveAccessVolumeParam(request, silverStatisticsSC);

                // init user panel
				destination = silverStatisticsSC.initAccessUserPanelUser();
            }
            else if (function.startsWith("AccessReturnFromUserPanelUser"))
            {
            	// get user panel data (update FilterLib, FilterId)
				silverStatisticsSC.retourAccessUserPanelUser();

                // restore request param
                restoreAccessParam(request, silverStatisticsSC);

                destination = "/silverStatisticsPeas/jsp/viewAccess.jsp";
            }
			else if (function.startsWith("ExportAccess.txt"))
            {
                // compute result
                request.setAttribute("FilterIdGroup", silverStatisticsSC.getAccessFilterIdGroup());
                request.setAttribute("FilterIdUser", silverStatisticsSC.getAccessFilterIdUser());
				request.setAttribute("StatsData", silverStatisticsSC.getCurrentStats());

                destination = "/silverStatisticsPeas/jsp/exportDataAccess.jsp";
            }
			else if (function.startsWith("ViewEvolutionAccess"))
            {
				String entite = request.getParameter("Entite");
				String entiteId = request.getParameter("Id");
				
				String filterLibGroup = silverStatisticsSC.getAccessFilterLibGroup();
				String filterIdGroup = silverStatisticsSC.getAccessFilterIdGroup();
				String filterLibUser = silverStatisticsSC.getAccessFilterLibUser();
                String filterIdUser = silverStatisticsSC.getAccessFilterIdUser();
                
				//compute result
				Chart lineChart = silverStatisticsSC.getEvolutionUserChart(entite, entiteId, filterLibGroup, filterIdGroup, filterLibUser, filterIdUser);
    			request.getSession( true ).setAttribute( ChartServlet.EVOLUTIONUSERCHART, lineChart );
    			request.setAttribute("StatsData", silverStatisticsSC.getCurrentStats());
    			
    			 // restore request param
    			request.setAttribute("Entite", entite);
 				request.setAttribute("Id", entiteId);
                restoreAccessParam(request, silverStatisticsSC);
                destination = "/silverStatisticsPeas/jsp/viewEvolutionAccess.jsp";
            }

			// Onglet Volume
			else if (function.startsWith("ViewVolumeServices"))
            {
				if(! userProfile.equals("A")) {
					return getDestination("ViewVolumePublication", componentSC, request);
				}
				
				PieChart2D pieChart = silverStatisticsSC.getVolumeServicesChart();
    			request.getSession( true ).setAttribute( ChartServlet.KMINSTANCESCHART, pieChart );
            	request.setAttribute("StatsData", silverStatisticsSC.getCurrentStats());
            	
                destination = "/silverStatisticsPeas/jsp/viewVolumeServices.jsp";
            }
			else if (function.startsWith("ViewVolumePublication"))
            {
                silverStatisticsSC.setAccessMonthBegin(currentMonth);
                silverStatisticsSC.setAccessYearBegin(currentYear);
                silverStatisticsSC.setAccessFilterLibGroup("");
                silverStatisticsSC.setAccessFilterIdGroup("");
                silverStatisticsSC.setAccessFilterLibUser("");
                silverStatisticsSC.setAccessFilterIdUser("");
                silverStatisticsSC.setAccessSpaceId("");
                silverStatisticsSC.clearCurrentStats();

                // init formulaire
                request.setAttribute("MonthBegin", silverStatisticsSC.getMonth(currentMonth));
                request.setAttribute("YearBegin", silverStatisticsSC.getYearVolume(currentYear));
                request.setAttribute("FilterLibGroup", silverStatisticsSC.getAccessFilterLibGroup());
                request.setAttribute("FilterIdGroup", silverStatisticsSC.getAccessFilterIdGroup());
                request.setAttribute("FilterLibUser", silverStatisticsSC.getAccessFilterLibUser());
                request.setAttribute("FilterIdUser", silverStatisticsSC.getAccessFilterIdUser());
                request.setAttribute("SpaceId", silverStatisticsSC.getAccessSpaceId());
                request.setAttribute("Path", silverStatisticsSC.getPath());

                destination = "/silverStatisticsPeas/jsp/viewVolume.jsp";
            }
            else if (function.startsWith("ValidateViewVolume"))
            {
                // save request param
                saveAccessVolumeParam(request, silverStatisticsSC);

                String hostMonthBegin = silverStatisticsSC.getAccessMonthBegin();
                String hostYearBegin = silverStatisticsSC.getAccessYearBegin();
                String filterIdGroup = silverStatisticsSC.getAccessFilterIdGroup();
                String filterIdUser = silverStatisticsSC.getAccessFilterIdUser();
                String spaceId = silverStatisticsSC.getAccessSpaceId();

                // compute result
                PieChart2D pieChart = silverStatisticsSC.getPubliVentilChart(getRequestDate(hostYearBegin, hostMonthBegin), filterIdGroup, filterIdUser, spaceId);
    			request.getSession( true ).setAttribute( ChartServlet.PUBLIVENTILCHART, pieChart );
    			request.setAttribute("StatsData", silverStatisticsSC.getCurrentStats());
    			
                // restore request param
                restoreVolumeParam(request, silverStatisticsSC);

                destination = "/silverStatisticsPeas/jsp/viewVolume.jsp";
            }
            else if (function.startsWith("VolumeCallUserPanelGroup"))
            {
                // save request param
                saveAccessVolumeParam(request, silverStatisticsSC);

                // init user panel
				destination = silverStatisticsSC.initVolumeUserPanelGroup();
            }
            else if (function.startsWith("VolumeReturnFromUserPanelGroup"))
            {
            	// get user panel data (update FilterLib, FilterId)
				silverStatisticsSC.retourAccessUserPanelGroup();

                // restore request param
                restoreVolumeParam(request, silverStatisticsSC);

                destination = "/silverStatisticsPeas/jsp/viewVolume.jsp";
            }
            else if (function.startsWith("VolumeCallUserPanelUser"))
            {
                // save request param
                saveAccessVolumeParam(request, silverStatisticsSC);

                // init user panel
				destination = silverStatisticsSC.initVolumeUserPanelUser();
            }
            else if (function.startsWith("VolumeReturnFromUserPanelUser"))
            {
            	// get user panel data (update FilterLib, FilterId)
				silverStatisticsSC.retourAccessUserPanelUser();

                // restore request param
                restoreVolumeParam(request, silverStatisticsSC);

                destination = "/silverStatisticsPeas/jsp/viewVolume.jsp";
            }

			//Nbre de fichiers joints sur le serveur
			else if (function.startsWith("ViewVolumeServer"))
            {
				silverStatisticsSC.setAccessSpaceId(request.getParameter("SpaceId"));
				String spaceId = silverStatisticsSC.getAccessSpaceId();
				
				//compute result
    			PieChart2D pieChart = silverStatisticsSC.getDocsVentilChart(spaceId);
    			request.getSession( true ).setAttribute( ChartServlet.DOCVENTILCHART, pieChart );
            	request.setAttribute("StatsData", silverStatisticsSC.getCurrentStats());
            	request.setAttribute("SpaceId", silverStatisticsSC.getAccessSpaceId());
                request.setAttribute("Path", silverStatisticsSC.getPath());

                destination = "/silverStatisticsPeas/jsp/viewVolumeServer.jsp";
            }
			else if (function.startsWith("ViewVolumeSizeServer"))
            {
				silverStatisticsSC.setAccessSpaceId(request.getParameter("SpaceId"));
				String spaceId = silverStatisticsSC.getAccessSpaceId();
				
				//compute result
				PieChart2D pieChart = silverStatisticsSC.getDocsSizeVentilChart(spaceId);
    			request.getSession( true ).setAttribute( ChartServlet.DOCSIZEVENTILCHART, pieChart );
            	request.setAttribute("StatsData", silverStatisticsSC.getCurrentStats());
            	request.setAttribute("SpaceId", silverStatisticsSC.getAccessSpaceId());
                request.setAttribute("Path", silverStatisticsSC.getPath());

                destination = "/silverStatisticsPeas/jsp/viewVolumeSizeServer.jsp";
            }
			else if (function.startsWith("ViewEvolutionVolumeSizeServer"))
            {
				//compute result
				Chart lineChart = silverStatisticsSC.getEvolutionDocsSizeChart();
    			request.getSession( true ).setAttribute( ChartServlet.EVOLUTIONDOCSIZECHART, lineChart );
            	request.setAttribute("StatsData", silverStatisticsSC.getCurrentStats());
            	
                destination = "/silverStatisticsPeas/jsp/viewEvolutionVolumeSizeServer.jsp";
            }

            else
            {
                destination = "/silverStatisticsPeas/jsp/"+function;
            }

        }
        catch (Exception e)
        {
            request.setAttribute("javax.servlet.jsp.jspException", e);
            destination = "/admin/jsp/errorpageMain.jsp";
        }

        SilverTrace.info("silverStatisticsPeas", "SilverStatisticsPeasRequestRouter.getDestination()", "root.MSG_GEN_PARAM_VALUE", "Destination=" + destination);
        return destination;
    }


	private void saveConnectionParam(HttpServletRequest request, SilverStatisticsPeasSessionController silverStatisticsSC)
	{
    	String hostMonthBegin = request.getParameter("MonthBegin");
    	if (hostMonthBegin != null && !hostMonthBegin.equals(""))
    	{
    		silverStatisticsSC.setMonthBegin(request.getParameter("MonthBegin"));
            silverStatisticsSC.setYearBegin(request.getParameter("YearBegin"));
        	silverStatisticsSC.setMonthEnd(request.getParameter("MonthEnd"));
            silverStatisticsSC.setYearEnd(request.getParameter("YearEnd"));
        	silverStatisticsSC.setActorDetail(request.getParameter("ActorDetail"));
        	silverStatisticsSC.setFilterType(request.getParameter("FilterType"));
        	silverStatisticsSC.setFilterLib(request.getParameter("FilterLib"));
        	silverStatisticsSC.setFilterId(request.getParameter("FilterId"));
        }
    }

	private void restoreConnectionParam(HttpServletRequest request, SilverStatisticsPeasSessionController silverStatisticsSC)
	{
    	request.setAttribute("MonthBegin", silverStatisticsSC.getMonth(silverStatisticsSC.getMonthBegin()));
        request.setAttribute("YearBegin", silverStatisticsSC.getYearConnection(silverStatisticsSC.getYearBegin()));
        request.setAttribute("MonthEnd", silverStatisticsSC.getMonth(silverStatisticsSC.getMonthEnd()));
        request.setAttribute("YearEnd", silverStatisticsSC.getYearConnection(silverStatisticsSC.getYearEnd()));
        request.setAttribute("ActorDetail", silverStatisticsSC.getDetail(silverStatisticsSC.getActorDetail()));
        request.setAttribute("FilterType", silverStatisticsSC.getFilterType());
        request.setAttribute("FilterLib", silverStatisticsSC.getFilterLib());
        request.setAttribute("FilterId", silverStatisticsSC.getFilterId());
	}

	private void saveAccessVolumeParam(HttpServletRequest request, SilverStatisticsPeasSessionController silverStatisticsSC)
	{
    	String hostMonthBegin = request.getParameter("MonthBegin");
    	if (hostMonthBegin != null && !hostMonthBegin.equals(""))
    	{
            silverStatisticsSC.setAccessMonthBegin(request.getParameter("MonthBegin"));
            silverStatisticsSC.setAccessYearBegin(request.getParameter("YearBegin"));
            silverStatisticsSC.setAccessFilterLibGroup(request.getParameter("FilterLibGroup"));
            silverStatisticsSC.setAccessFilterIdGroup(request.getParameter("FilterIdGroup"));
            silverStatisticsSC.setAccessFilterLibUser(request.getParameter("FilterLibUser"));
            silverStatisticsSC.setAccessFilterIdUser(request.getParameter("FilterIdUser"));
            silverStatisticsSC.setAccessSpaceId(request.getParameter("SpaceId"));
        }
    }

	private void restoreAccessParam(HttpServletRequest request, SilverStatisticsPeasSessionController silverStatisticsSC)
	{
    	request.setAttribute("MonthBegin", silverStatisticsSC.getMonth(silverStatisticsSC.getAccessMonthBegin()));
        request.setAttribute("YearBegin", silverStatisticsSC.getYearAccess(silverStatisticsSC.getAccessYearBegin()));
        request.setAttribute("FilterLibGroup", silverStatisticsSC.getAccessFilterLibGroup());
        request.setAttribute("FilterIdGroup", silverStatisticsSC.getAccessFilterIdGroup());
        request.setAttribute("FilterLibUser", silverStatisticsSC.getAccessFilterLibUser());
        request.setAttribute("FilterIdUser", silverStatisticsSC.getAccessFilterIdUser());
        request.setAttribute("SpaceId", silverStatisticsSC.getAccessSpaceId());
        request.setAttribute("Path", silverStatisticsSC.getPath());
	}

	private void restoreVolumeParam(HttpServletRequest request, SilverStatisticsPeasSessionController silverStatisticsSC)
	{
    	request.setAttribute("MonthBegin", silverStatisticsSC.getMonth(silverStatisticsSC.getAccessMonthBegin()));
        request.setAttribute("YearBegin", silverStatisticsSC.getYearVolume(silverStatisticsSC.getAccessYearBegin()));
        request.setAttribute("FilterLibGroup", silverStatisticsSC.getAccessFilterLibGroup());
        request.setAttribute("FilterIdGroup", silverStatisticsSC.getAccessFilterIdGroup());
        request.setAttribute("FilterLibUser", silverStatisticsSC.getAccessFilterLibUser());
        request.setAttribute("FilterIdUser", silverStatisticsSC.getAccessFilterIdUser());
        request.setAttribute("SpaceId", silverStatisticsSC.getAccessSpaceId());
        request.setAttribute("Path", silverStatisticsSC.getPath());
	}

    private String getRequestDate(String sYear, String sMonth)
    {
        String month = Integer.toString(Integer.parseInt(sMonth)+1);
        if (month.length()<2)
        {
            month="0"+month;
        }
        return sYear + "-" + month + "-" + "01";
    }
}
