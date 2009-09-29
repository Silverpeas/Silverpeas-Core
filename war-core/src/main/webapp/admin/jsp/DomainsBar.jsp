<%--

    Copyright (C) 2000 - 2009 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://repository.silverpeas.com/legal/licensing"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@ page import="com.stratelia.silverpeas.pdc.control.PdcBm,
                 java.util.List,
                 java.util.ArrayList,
                 com.stratelia.silverpeas.pdc.model.SearchContext,
                 com.stratelia.silverpeas.pdc.model.SearchAxis,
                 com.stratelia.webactiv.util.viewGenerator.html.Encode,
                 com.stratelia.silverpeas.pdc.model.Value,
                 com.stratelia.webactiv.homepage.JspHelper,
                 com.stratelia.silverpeas.pdc.control.PdcBmImpl,
                 com.stratelia.webactiv.beans.admin.AdminController,
                 com.stratelia.webactiv.util.ResourceLocator,
                 com.stratelia.silverpeas.peasCore.MainSessionController,
                 com.stratelia.webactiv.beans.admin.ComponentInst,
                 com.stratelia.webactiv.beans.admin.SpaceInst"%>
<%@ include file="importFrameSet.jsp" %>
<%@ include file="usefullFunctions.jsp" %>
<%!
static PdcBm pdcBm;

public void putIn(String key, String champ, String defaut, Hashtable icons){
    if (champ==null)
        icons.put(key, defaut);
    else {
        if (champ.length()==0)
            icons.put(key, defaut);
        else
            icons.put(key, champ);
    }
}

// Constants used by urlFactory
final static int SPACE      = 0;
final static int COMPONENT  = 1;

final static int SPACE_COLLAPSE             = 2;
final static int SPACE_EXPANDED             = 3;
final static int SPACE_COMPONENT            = 4;
final static int SUBSPACE_COMPONENT         = 5;
final static int SUBSPACE_LAST_COMPONENT    = 6;

final static int AXIS = 7;
final static int AXIS_COLLAPSED = 8;
final static int AXIS_EXPANDED = 9;
final static int AXIS_LAST = 10;


public String getTabSpaces(int deep)
{
    StringBuffer spacesSpaces = new StringBuffer();
    int i;

    for (i = 0; i < deep - 1; i++)
    {
        spacesSpaces.append("&nbsp&nbsp");
    }
    return spacesSpaces.toString();
}

 /**
 * Return 23 for parameter kmelia23
 */
 private String getDriverComponentId(String sClientComponentId)
{
    String sTableClientId = "";

        // Remove the component name to get the table client id
        char[] cBuf = sClientComponentId.toCharArray();
        for(int nI = 0; nI < cBuf.length && sTableClientId.length() == 0; nI++)
            if(cBuf[nI] == '0' || cBuf[nI] == '1' || cBuf[nI] == '2' || cBuf[nI] == '3' || cBuf[nI] == '4' || cBuf[nI] == '5'||
               cBuf[nI] == '6' || cBuf[nI] == '7' || cBuf[nI] == '8' || cBuf[nI] == '9')
                sTableClientId = sClientComponentId.substring(nI);

        return sTableClientId;
}

private String firstLetterToUpperCase(String str) {
    String c = str.substring(0, 1);
    c = c.toUpperCase();
    return c + str.substring(1);
}

private String urlFactory(String link, String text_link, String elementLabel, String id, String imageLinked, String labelLinked, String labelLinkedNotBold, int elementType, int imageType, String level, String m_sContext)
{
    String imageOn      = m_sContext +"/util/icons/component/"+ imageLinked;
    StringBuffer result = new StringBuffer();
    String target       = "";
    String textlink_target = "";
    String boldStart    = "";
    String boldEnd  = "";
    String offset   = "";
    boolean is_axis = false;
        boolean isComponentOfSubSpace = false;

    imageLinked = "<img name=\""+elementLabel+"\" src=\""+ m_sContext +"/util/icons/component/"+ imageLinked
                + "Small.gif\" border=\"0\" onLoad=\"\" align=\"absmiddle\">";

    switch (elementType)
    {
    case SPACE :
            target  = "";
            boldStart = "<b>";
            boldEnd   = "</b>";
            break;

    case COMPONENT :
            target  = "TARGET=\"MyMain\"";
            boldStart = "<font color=666666>";
            boldEnd   = "</font>";
            break;
    case AXIS :
            textlink_target = "TARGET=\"MyMain\"";
            boldStart = "<b>";
            boldEnd   = "</b>";
            offset += level;
            break;
        case AXIS_LAST :
                textlink_target = "TARGET=\"MyMain\"";
                boldStart = "<b>";
                boldEnd   = "</b>";
                offset += level;
                break;
    }

    switch (imageType)
        {
                case AXIS_COLLAPSED :
                    result.append(offset).append("<a href=\"").append(link).append("\" ").append(target).append(" class=\"txtnote\"><img src=").append(m_sContext).append("/pdcPeas/jsp/icons/pdcPeas_maximize.gif border=0 align=\"absmiddle\"></a>");
                    imageLinked = "<img name=\""+elementLabel+"\" src=\"icons/1px.gif\" width=1 height=1 border=0 align=\"absmiddle\">";
                    imageOn = m_sContext +"/util/icons/noColorPix/16px.gif";
                    is_axis = true;
                    break;

        case AXIS_EXPANDED :
                    result.append(offset).append("<a href=\"").append(link).append("\" ").append(target).append("  class=\"txtnote\"><img src=\"").append(m_sContext).append("/pdcPeas/jsp/icons/pdcPeas_minimize.gif\" border=0 align=\"absmiddle\"></a>");
                    imageLinked = "<img name=\""+elementLabel+"\" src=\"icons/1px.gif\" width=1 height=1 border=0 align=\"absmiddle\">";
                    imageOn = m_sContext +"/util/icons/noColorPix/16px.gif";
                    is_axis = true;
                    break;
        case AXIS_LAST :
                    result.append(offset).append("<a href=\"").append(link).append("\" ").append(target).append("  class=\"txtnote\"><img src=\"").append(m_sContext).append("/pdcPeas/jsp/icons/pdcPeas_minimize.gif\" border=0 align=\"absmiddle\"></a>");
                    imageLinked = "<img name=\""+elementLabel+"\" src=\"icons/1px.gif\" width=1 height=1 border=0 align=\"absmiddle\">";
                    imageOn = m_sContext +"/util/icons/noColorPix/16px.gif";
                    is_axis = true;
                    break;

                case SPACE_COLLAPSE :
                                        result.append("<a href=\"").append(link).append("\" ").append(target).append(" class=\"txtnote\"><img src=").append(m_sContext).append("/util/icons/plusTree.gif border=0 align=\"absmiddle\"></a>");
                                        imageLinked = "<img name=\""+elementLabel+"\" src=\"icons/1px.gif\" width=1 height=1 border=0 align=\"absmiddle\">";
                                        imageOn = m_sContext +"/util/icons/noColorPix/16px.gif";
                                        break;

                case SPACE_EXPANDED :
                                        result.append("<a href=\"").append(link).append("\" ").append(target).append("  class=\"txtnote\"><img src=\"").append(m_sContext).append("/util/icons/minusTree.gif\" border=0 align=\"absmiddle\"></a>");
                                        imageLinked = "<img name=\""+elementLabel+"\" src=\"icons/1px.gif\" width=1 height=1 border=0 align=\"absmiddle\">";
                                        imageOn = m_sContext +"/util/icons/noColorPix/16px.gif";
                                        break;

                case SPACE_COMPONENT :
                                        break;

                case SUBSPACE_COMPONENT :
                                        isComponentOfSubSpace = true;
                                        result.append("<img src=\"").append(m_sContext).append("/util/icons/minusTreeT.gif\" border=0 align=\"absmiddle\">");
                                        break;

                case SUBSPACE_LAST_COMPONENT :
                                        isComponentOfSubSpace = true;
                                        result.append("<img src=\"").append(m_sContext).append("/util/icons/minusTreeL.gif\" border=0 align=\"absmiddle\">");
                                        break;
        }
// if link is a javascript method, remove the target
// necessary for called method visibility
    if (link.startsWith("javascript")) target = "";

    if ( text_link == null || "".equals(text_link) )
    {
        text_link = link;
        textlink_target = target;
    }

    if ( is_axis )
    {
        if ( id != null && !id.equals("") )
        {
            result.append("<a href=\"").append(link).append("\" ").append(target).append(" onClick=\"top.scriptFrame.setComponent('").append(id).append("','").append(elementLabel).append("','").append(imageOn).append("');return true\" class=\"txtnote\">").append(imageLinked).append("<img src=icons/ComponentsPoints.gif border=0 align=\"absmiddle\"></a>");
            result.append("<a href=\"").append(text_link).append("\" ").append(textlink_target).append(" class=\"txtnote\">").append(boldStart).append(labelLinked).append(boldEnd).append(labelLinkedNotBold).append("</a>");
            result.append("<br>");
        }
        else
        {
            result.append("<a href=\"").append(link).append("\" ").append(target).append(" onClick=\"top.scriptFrame.rollActiv('").append(elementLabel).append("','").append(imageOn).append("');return true\" class=\"txtnote\">").append(imageLinked).append("<img src=icons/ComponentsPoints.gif border=0 align=\"absmiddle\"></a>");
            result.append("<a href=\"").append(text_link).append("\" ").append(textlink_target).append(" class=\"txtnote\">").append(boldStart).append(labelLinked).append(boldEnd).append(labelLinkedNotBold).append("</a>");
            result.append("<br>");
        }
    }
    else
    {
        if ( id != null && !id.equals("") )
        {
            result.append("<a href=\"").append(link).append("\" ").append(target).append(" onClick=\"top.scriptFrame.setComponent('").append(id).append("','").append(elementLabel).append("','").append(imageOn).append("',").append(isComponentOfSubSpace).append(");return true\" class=\"txtnote\">").append(imageLinked).append("<img src=icons/ComponentsPoints.gif border=0 align=\"absmiddle\"></a>");
            result.append("<a href=\"").append(text_link).append("\" ").append(textlink_target).append(" onClick=\"top.scriptFrame.setComponent('").append(id).append("','").append(elementLabel).append("','").append(imageOn).append("',").append(isComponentOfSubSpace).append(");return true\" class=\"txtnote\">").append(boldStart).append(labelLinked).append(boldEnd).append(labelLinkedNotBold).append("</a>");
            result.append("<br>");
        }
        else
        {
            result.append("<a href=\"").append(link).append("\" ").append(target).append(" onClick=\"top.scriptFrame.rollActiv('").append(elementLabel).append("','").append(imageOn).append("');return true\" class=\"txtnote\">").append(imageLinked).append("<img src=icons/ComponentsPoints.gif border=0 align=\"absmiddle\"></a>");
            result.append("<a href=\"").append(text_link).append("\" ").append(textlink_target).append(" onClick=\"top.scriptFrame.rollActiv('").append(elementLabel).append("','").append(imageOn).append("');return true\" class=\"txtnote\">").append(boldStart).append(labelLinked).append(boldEnd).append(labelLinkedNotBold).append("</a>");
            result.append("<br>");
        }
    }
    return result.toString();
}

private String shortDomain(String domainName, int nMax){
  String shortName ;
  int nbChar = nMax ;

  shortName=domainName;
  if (shortName.length()>nbChar){
    shortName=domainName.substring(0,nbChar-3)+"...";
  }
  return shortName;
}

    private static final int T_OUT = -1;
    private static final int T_TREE_T_IMAGE = 0;
    private static final int T_TREE_L_IMAGE = 1;
    private static final int T_TREE_I_IMAGE = 3;
    private static final int T_TREE_SPACE_IMAGE = 2;
    private static final int T_OPENED_NODE = 5;
    private static final int T_CLOSED_NODE = 7;
    private static final int T_HIDED_NODE = 9;

    void closeTreeNodes( int[][] tree, int x, int y, int d_size )
    {
        if ( y > 1 && x < (d_size - 1) )
        {
            int i;
            boolean closed;
            while ( x < (d_size - 1) )
            {
                i = y - 1;
                closed = false;
                while ( i > 0 )
                {
                    if ( tree[x][i] == T_TREE_T_IMAGE )
                    {
                        if ( tree[x+1][i] == T_OPENED_NODE )
                        {
                            if ( ! closed )
                            {
                                tree[x][i] = T_TREE_L_IMAGE;
                                closed = true;
                            }
                            else
                            {
                                tree[x][i] = T_TREE_T_IMAGE;
                            }
                        }
                        else if ( tree[x+1][i] == T_TREE_SPACE_IMAGE )
                        {
                            tree[x][i] = T_TREE_I_IMAGE;
                        }
                        else
                        {
                            if ( !closed )
                            {
                                tree[x][i] = T_TREE_SPACE_IMAGE;
                            }
                            else
                            {
                                tree[x][i] = T_TREE_I_IMAGE;
                            }
                        }
                    }
                    else
                    {
                        break;
                    }
                    i--;
                }
                x++;
            }
        }
    }

    void setTreeNode( int[][] tree, int level, int y, int d_size )
    {
        int i = 0;
        while ( i < d_size )
        {
            if ( i < level )
            {
                tree[i][y] = T_TREE_T_IMAGE;
            }
            else if ( i == level )
            {
                tree[i][y] = T_OPENED_NODE;
            }
            else
            {
                tree[i][y] = T_OUT;
            }
            i++;
        }
        closeTreeNodes( tree, level, y, d_size );
    }

    void collapseTree( int[][] tree, int x, int y, int d_size )
    {
        boolean bottom = false;
        if ( x >= d_size )
        {
            x = d_size - 1;
        }
        if ( y >= d_size )
        {
            y = d_size - 1;
            bottom = true;
        }

        int i = x, j = y;
        boolean state = true;

        if ( !bottom )
        {
            while ( j < d_size && i >=0 )
            {
                if ( tree[i][j] == T_OPENED_NODE )
                {
                    tree[i][j++] = T_CLOSED_NODE;
                }
                else if ( tree[i][j] != T_OUT )
                {
                    tree[0][j++] = T_HIDED_NODE;
                }
                else
                {
                    i--;
                }
            }
        }

        i = x;
        j = y - 1;

        while ( j >=0 && i >= 0 )
        {
            if ( tree[i][j] == T_OPENED_NODE )
            {
                if ( state )
                {
                    state = false;
                    j--;
                }
                else
                {
                    tree[i][j--] = T_CLOSED_NODE;
                }
            }
            else if ( tree[i][j] != T_OUT )
            {
                tree[0][j--] = T_HIDED_NODE;
            }
            else
            {
                i--;
                state = true;
            }
        }
    }

    String getTreeNodeOffset( int[][] tree, int y, int d_size, String m_context )
    {
        String offset = "";
        int i = 0;
        while ( i < d_size && tree[i][y] != T_OPENED_NODE && tree[i][y] != T_CLOSED_NODE && tree[i][y] != T_OUT )
        {
            switch ( tree[i++][y])
            {
                case T_TREE_SPACE_IMAGE:
                    offset += "<img src=\"icons/1px.gif\" width=16 height=16 border=0 align=\"absmiddle\">";
                    break;
                case T_TREE_T_IMAGE:
                    offset += "<img src=\""+ m_context+ "/util/icons/minusTreeT.gif\" border=0 align=\"absmiddle\">";
                    break;
                case T_TREE_L_IMAGE:
                    offset += "<img src=\""+ m_context+ "/util/icons/minusTreeL.gif\" border=0 align=\"absmiddle\">";
                    break;
                case T_TREE_I_IMAGE:
                    offset += "<img src=\""+ m_context+ "/util/icons/minusTreeI.gif\" border=0 align=\"absmiddle\">";
                    break;
            }
        }
        return offset;
    }
%>

<%
if (pdcBm == null) {
    pdcBm = (PdcBm) new PdcBmImpl();
}

Hashtable icons = new Hashtable();

String _visible = request.getParameter("_Visible");
if(_visible==null)
    _visible="visible";

String[] domains = null;
String[] subDomains = null;
String domain = null;
String subDomain = null;
List subDomainsToCollapse = new ArrayList();

String[] m_asPrivateDomainsIds = null;

if (m_MainSessionCtrl == null)
{
  String sessionTimeout = GeneralPropertiesManager.getGeneralResourceLocator().getString("sessionTimeout");
  getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
  return;
}

organizationCtrl = new OrganizationController(m_MainSessionCtrl.getUserId());

ResourceLocator mesGen = new ResourceLocator("com.stratelia.webactiv.beans.admin.admin","");
int nMax = 20;
String spaceMaxChar = mesGen.getString("spaceMaxChar");
if (spaceMaxChar != null)
        nMax = new Integer(spaceMaxChar).intValue();

m_asPrivateDomainsIds = m_MainSessionCtrl.getUserAvailSpaceIds();

// check the general domain
String sGenSpace = organizationCtrl.getGeneralSpaceId();

String sPrivateDomain = null;
String sPrivateDomainName = null;

String state = request.getParameter("state");
String selected_axis_path = request.getParameter("axis_path");
String selected_axis_id = request.getParameter("axis_id");
String component_id = request.getParameter("component_id");
//System.out.println("------------------------------------------");
//System.out.println("component_id = "+component_id);
String s_image = request.getParameter("s_image");
String s_source = request.getParameter("s_source");
String new_space = request.getParameter("new_space");
if ( "1".equals(new_space) )
{
    s_image = s_source = component_id = "";
}

if (component_id == null)
        component_id = "";

if ( state == null )
{
    state = "off";
}
if ( selected_axis_path == null )
{
    selected_axis_path = "";
}
if ( selected_axis_id == null )
{
    selected_axis_id = "";
}
if ( s_image == null)
{
    s_image = "";
}
if ( s_source == null )
{
    s_source = "";
}

List primaryAxis = null;
List daughters = null;

domains = (String[]) request.getParameterValues("privateDomain");
//System.out.println("domains = "+domains[0]);
if (domains != null)
{
    domain = domains[0];
}
else
    domain = null;

subDomains = (String[]) request.getParameterValues("privateSubDomain");
if (subDomains != null) {
    subDomain = subDomains[0];
        //System.out.println("subDomains = "+subDomain);
} else
    subDomain = null;

String superId = "";
if (subDomain !=null && subDomain.length() > 0 && !"null".equals(subDomain))
{
	//System.out.println("1 - subDomain = "+subDomain);
   SpaceInst spaceInst = organizationCtrl.getSpaceInstById(subDomain);
   subDomainsToCollapse.add(subDomain);
   while ( (spaceInst != null) && (spaceInst.getDomainFatherId() != null) && (!spaceInst.getDomainFatherId().equals("0")) && (spaceInst.getDomainFatherId().length() > 0))
   {
        superId = "WA"+spaceInst.getDomainFatherId();
     	subDomainsToCollapse.add(superId);
		//System.out.println("2 - superId = "+superId);
   	    spaceInst = organizationCtrl.getSpaceInstById(superId);
   }   
}

String spaceOrSubSpace = null;

//System.out.println("domain = "+domain);
if (domain != null && !"null".equals(domain))
{
        spaceOrSubSpace = domain;
}
if (subDomain != null && subDomain.length() > 0 && !"null".equals(subDomain))
{
		spaceOrSubSpace = subDomain;
}
//System.out.println("spaceOrSubSpace = "+spaceOrSubSpace);
boolean currentSpaceInMaintenance = false;

// Current space in Maintenance ?
if (domain != null && !domain.equals(""))
{
    if (m_MainSessionCtrl.isSpaceInMaintenance(domain.substring(2)))
        currentSpaceInMaintenance = true;
}

if ( spaceOrSubSpace != null && !spaceOrSubSpace.equals("") )
{
    ArrayList cmps = new ArrayList();
    SearchContext searchContext = new SearchContext();

    if ( component_id != null && !"".equals(component_id) )
    {
        primaryAxis = pdcBm.getPertinentAxisByInstanceId(searchContext, "P", component_id);
    }
    else
    {
        String a[] = organizationCtrl.getAvailCompoIds(spaceOrSubSpace, m_MainSessionCtrl.getUserId());
        for (int i=0; i<a.length;i++ )
        {
                        cmps.add(a[i]);
        }
                if (cmps.size()>0)
                primaryAxis = pdcBm.getPertinentAxisByInstanceIds(searchContext, "P", cmps);
    }

    if ( primaryAxis != null && primaryAxis.size() > 0 )
    {
        if ( !"".equals(selected_axis_id) )
        {
            if ( component_id != null && !"".equals(component_id) )
            {
                daughters = pdcBm.getPertinentDaughterValuesByInstanceId(searchContext, selected_axis_id, selected_axis_path, component_id);
            }
            else
            {
                daughters = pdcBm.getPertinentDaughterValuesByInstanceIds(searchContext, selected_axis_id, selected_axis_path, cmps);
            }
        }
        else
        {
            SearchAxis searchAxis = (SearchAxis)primaryAxis.get(0);
            String axisId = new Integer(searchAxis.getAxisId()).toString();
            daughters = pdcBm.getPertinentDaughterValuesByInstanceIds(searchContext, axisId, "", cmps);
        }
    }
}


int elementId = 0;
int spaceId = 0;
String elementLabel = "";
String objectLinked = "";
String link = "";

putIn("collaborativeIcon", "", "icons/accueil/esp_collabo.gif", icons);
putIn("homeSpaceIcon", "", m_sContext+"/util/icons/Home.gif", icons);


/*String collaborativeIcon = gef.getIcon("collaborativeIcon");
put("collaborativeIcon", collaborativeIcon, "icons/accueil/esp_collabo.gif", icons);
String homeSpaceIcon = gef.getIcon("homeSpaceIcon");
put("homeSpaceIcon", homeSpaceIcon, m_sContext+"/util/icons/Home.gif", icons);*/
%>

<html>
<head>
<title>Navigation</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<%
out.println(gef.getLookStyleSheet());
%>
<script language="JavaScript1.2" src="<%=m_sContext%>/admin/jsp/showHideLayer.js"></script>
<script language="javascript">
    function changeSpace(context)
    {
        top.scriptFrame.changeSpace(context);
    }
</script>
</head>

<body leftmargin="0" topmargin="0" marginwidth="0" marginheight="0" bgcolor="#FFFFFF" onload="top.scriptFrame.resizeSpaceur();top.scriptFrame.initRollOver();" onResize="top.scriptFrame.resizeSpaceur()">
<form name="privateDomainsForm" action="DomainsBar.jsp" method="POST">

<table width="10" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td><img src="icons/1px.gif" width="1" height="5"></td>
    </tr>
</table>

<%//*************** DomainsBar choice ***************%>
<table width="100%" cellspacing="0" cellpadding="0" border="0">
<tr>
    <td width="100%" class="intfdcolor13"><img src="icons/1px.gif" width="1" height="1"></td>
    <td rowspan="3" colspan="2" class="intfdcolor51"><img src="icons/angleHautDomainsBar3.gif"></td>
</tr>
<tr>
    <td width="100%" class="intfdcolor4"><img src="icons/1px.gif" width="1" height="1"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%"><img src="icons/1px.gif" width="2" align="middle">&nbsp;</td>
</tr>
</table>

<%//*************** Collaborative Space ***************%>
<table width="100%" cellspacing="0" cellpadding="0" border="0">
<tr>
    <td width="100%" class="intfdcolor13"><img src="icons/1px.gif" width="1" height="1"></td>
    <td rowspan="3" colspan="2" class="intfdcolor"><img src="icons/angleHautDomainsBar.gif" width="8" height="8"></td>
</tr>
<tr>
    <td width="100%" class="intfdcolor4"><img src="icons/1px.gif" width="1" height="1"></td>
</tr>
<tr class="intfdcolor">
    <td width="100%"><img src="icons/1px.gif" width="1" height="6"></td>
</tr>
<tr class="intfdcolor">
    <td width="100%"><img src="icons/1px.gif" width="1" height="1"></td>
    <td><img src="icons/1px.gif" width="7" height="1"></td>
    <td class="intfdcolor"><img src="icons/1px.gif" width="1" height="1"></td>
</tr>
<tr class="intfdcolor">
    <td width="100%">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td><img src="<%=icons.get("collaborativeIcon")%>"></td>
            <td width="100%"><span class="txtpetitblanc"><%=message.getString("SpaceCollaboration")%></span></td>
          </tr>
        </table>
        </td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%"><img src="icons/1px.gif" width="1" height="1"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor13">
    <td width="100%"><img src="icons/1px.gif"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor4"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor4">
    <td width="100%"><img src="icons/1px.gif"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%"><img src="icons/1px.gif" width="1" height="3"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor51" valign="top"><input name="privateSubDomain" type="hidden" value="<%=subDomain%>">
    <td width="100%" nowrap valign="top">
        <img src="icons/1px.gif" height="20" width="0" align="absmiddle">
        <span class="selectNS"><select name="privateDomain" size=1 onChange="changeSpace('<%=m_sContext%>/admin/jsp/Main.jsp')">
          <%
                // Other space selected
                sPrivateDomain = domain;
                                String labelSpace = "";
                for(int nI = 0; nI < m_asPrivateDomainsIds.length; nI++)
                {
								//System.out.println("3 - m_asPrivateDomainsIds["+nI+"] = "+m_asPrivateDomainsIds[nI]);
                                   SpaceInst spaceInst = organizationCtrl.getSpaceInstById(m_asPrivateDomainsIds[nI]);
                                   if (spaceInst.getDomainFatherId().equals("0"))
                                   {
                                          sPrivateDomainName = spaceInst.getName();

                                          //Spaces in Maintenance with (M)
                                          if (!m_asPrivateDomainsIds[nI].equals("") && m_asPrivateDomainsIds[nI] != null)
                                          {
                                                if (m_MainSessionCtrl.isSpaceInMaintenance(m_asPrivateDomainsIds[nI].substring(2)))
                                                {
                                                        sPrivateDomainName += " (M)";
                                                }
                                                // Current space in maintenance ?
                                                if (m_MainSessionCtrl.isSpaceInMaintenance(sPrivateDomain.substring(2)))
                                                        currentSpaceInMaintenance = true;
                                          }

                                          if (m_asPrivateDomainsIds[nI].equals(sPrivateDomain))
                                          {
                                                  out.println("<option selected value=" + sPrivateDomain + ">" + shortDomain(sPrivateDomainName,nMax) + "</option>");
                                          }
                                          else
                                                  out.println("<option value=" + m_asPrivateDomainsIds[nI] + ">" + shortDomain(sPrivateDomainName,nMax) + "</option>");
                                   }
                }
        %>
        </select></span><a href="#" onclick="changeSpace('<%=m_sContext%>/admin/jsp/Main.jsp')"><img src="icons/1px.gif" width="2" height="1" border="0"><img src="<%=icons.get("homeSpaceIcon")%>" border="0" align="absmiddle" alt="<%=message.getString("BackToMainPage")%>" title="<%=message.getString("BackToMainPage")%>"></a>
        </td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor4">
    <td width="100%"><img src="icons/1px.gif"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor13">
    <td width="100%"><img src="icons/1px.gif"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor4"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%"><img src="icons/1px.gif" width="1" height="3"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%">
        <table border="0" cellspacing="0" cellpadding="0" width="100%">
          <tr>
            <td>&nbsp;</td>
            <td width="100%"><span class="txtnote">
                        <table cellpadding=0 cellspacing=0 border=0 width="100%">
                        <tr><td>
              <%
                if ((sPrivateDomain != null) && (sPrivateDomain.length()>0))
                {
                  // Get all sub spaces
                  String[] asSubSpaceIds = organizationCtrl.getAllSubSpaceIds(sPrivateDomain);
                  String   sSubSpaceId;
                  ArrayList alSubSpaces = new ArrayList();

                  for (int nI = 0; nI < asSubSpaceIds.length; nI++)
                  {
                        alSubSpaces.add(asSubSpaceIds[nI]);
                  }
                  // Keep only those available to current user
                  while (alSubSpaces.size() > 0)
                  {
                      boolean bFound = false;
                      sSubSpaceId = (String)alSubSpaces.remove(0);

      	              if (subDomainsToCollapse.contains(sSubSpaceId))
		              {
                          asSubSpaceIds = organizationCtrl.getAllSubSpaceIds(sSubSpaceId);
                          for (int nI = 0; nI < asSubSpaceIds.length; nI++)
                          {
                               alSubSpaces.add(nI,asSubSpaceIds[nI]);
                          }
                      }

                      for (int nJ = 0; nJ < m_asPrivateDomainsIds.length && !bFound; nJ++)
                      {
                          if (sSubSpaceId.equals(m_asPrivateDomainsIds[nJ]))
                          {
                              bFound = true;

                              if (subDomain!=null && subDomain.equals(sSubSpaceId))
                                                          {
									//System.out.println("4 - sSubSpaceId = "+sSubSpaceId);
                                  SpaceInst spaceInst = organizationCtrl.getSpaceInstById(sSubSpaceId);
                                  String label = spaceInst.getName();
                                  String spaceLabel = "space"+spaceId;

                                  objectLinked = "";

				                  if ( (spaceInst!= null) && (spaceInst.getDomainFatherId() != null)  && (!spaceInst.getDomainFatherId().equals("0")) && (spaceInst.getDomainFatherId().length() > 0))
                                  	 link = "javascript:top.scriptFrame.changeSubSpace('WA"+ spaceInst.getDomainFatherId() +"','"+m_sContext+"/admin/jsp/Main.jsp');";
                                  else
                                     link = "javascript:top.scriptFrame.changeSubSpace('','"+m_sContext+"/admin/jsp/Main.jsp');";
                                  out.println(getTabSpaces(spaceInst.getLevel()) + urlFactory(link, "", spaceLabel, "", objectLinked, label, "", SPACE, SPACE_EXPANDED, "", m_sContext));
                                  String[] asAvailCompoForCurUser = organizationCtrl.getAvailDriverCompoIds(spaceInst.getId(), m_MainSessionCtrl.getUserId());

                                                                  // Get all the component instances for the space
                                                                  ArrayList alCompoInst = spaceInst.getAllComponentsInst();

                                  String[] asCompoNames = new String[alCompoInst.size()];
                                  Vector vAllowedComponents = new Vector();

                                  // Build Vector of indexes of allowed components
                                  for(int nK = 0; nK <asCompoNames.length; nK++)
                                  {
                                      // Check if the component is accessible to the user
                                      boolean bAllowed = false;
                                      for(int nL=0; asAvailCompoForCurUser != null && nL < asAvailCompoForCurUser.length; nL++)
                                          if( getDriverComponentId( ( (ComponentInst)alCompoInst.get(nK) ).getId()).equals(asAvailCompoForCurUser[nL]) )
                                              bAllowed = true;

                                      if(bAllowed)
                                            vAllowedComponents.add(new Integer(nK));
                                  }

                                  // Print the allowed components
                                  String id;

//                                  ArrayList sortedList  = JspHelper.sortComponentList(vAllowedComponents, alCompoInst);
                                  Vector sortedList  = vAllowedComponents;

                                  for (int nAC=0; nAC<sortedList.size(); nAC++)
                                  {
                                      int nK = ((Integer) sortedList.get(nAC)).intValue();
                                      label = ((ComponentInst)alCompoInst.get(nK)).getLabel();
                                      if ((label == null) || (label.length() == 0))
                                          label = ((ComponentInst)alCompoInst.get(nK)).getName();
                                      id = ((ComponentInst)alCompoInst.get(nK)).getId();
                                      elementId++;
                                      elementLabel = "element"+elementId;
                                      objectLinked = ((ComponentInst)alCompoInst.get(nK)).getName();
                                      link = m_sContext + URLManager.getURL(((ComponentInst)alCompoInst.get(nK)).getName(), sSubSpaceId, ((ComponentInst)alCompoInst.get(nK)).getId()) + "Main";
                                      // Afffiche les sous espaces

                                      if (nAC == (sortedList.size()-1)) {
                                          out.println(getTabSpaces(spaceInst.getLevel()) + urlFactory(link, "", elementLabel, id, objectLinked, label, "", COMPONENT, SUBSPACE_LAST_COMPONENT, "", m_sContext));
                                      } else {
                                          out.println(getTabSpaces(spaceInst.getLevel()) + urlFactory(link, "", elementLabel, id, objectLinked, label, "", COMPONENT, SUBSPACE_COMPONENT, "", m_sContext));
                                      }
                                  }
                              }
                              else
                              {
                                  spaceId++;
								  //System.out.println("5 - sSubSpaceId = "+sSubSpaceId);
                                  SpaceInst spaceInst = organizationCtrl.getSpaceInstById(sSubSpaceId);
                                  String label = spaceInst.getName();
                                  String spaceLabel = "space"+spaceId;
                                  objectLinked = "";
                                  link = "javascript:top.scriptFrame.changeSubSpace('"+ sSubSpaceId +"','"+m_sContext+"/admin/jsp/Main.jsp');";
                                  out.println(getTabSpaces(spaceInst.getLevel()) + urlFactory(link, "", spaceLabel, "", objectLinked, label, "", SPACE, SPACE_COLLAPSE, "", m_sContext));
                                }
                          }
                      }
                  }

					//System.out.println("6 - sPrivateDomain = "+sPrivateDomain);
                  SpaceInst spaceInst = organizationCtrl.getSpaceInstById(sPrivateDomain);
                  String[] asAvailCompoForCurUser = organizationCtrl.getAvailDriverCompoIds(spaceInst.getId(), m_MainSessionCtrl.getUserId());

                  // Get all the component instances for the space
                  ArrayList alCompoInst = spaceInst.getAllComponentsInst();

                  String[] asCompoNames = new String[alCompoInst.size()];
                  String id;
                  Vector allowedComponents = new Vector();

                  for(int nI = 0; nI <asCompoNames.length; nI++)
                  {
                      // Check if the component is accassible to the user
                      boolean bAllowed = false;

                      for(int nJ=0; asAvailCompoForCurUser != null && nJ < asAvailCompoForCurUser.length; nJ++)
                          if( getDriverComponentId( ( (ComponentInst)alCompoInst.get(nI) ).getId()).equals(asAvailCompoForCurUser[nJ]) )
                              bAllowed = true;

                      if(bAllowed)
                      {
                          allowedComponents.add(new Integer(nI));
                      }
                  }

//                  ArrayList sortedComponents = JspHelper.sortComponentList(allowedComponents, alCompoInst);
                  Vector sortedComponents = allowedComponents;
                  for(int nI = 0; nI < sortedComponents.size(); nI++) {
                      int nK = ((Integer) sortedComponents.get(nI)).intValue();

                      String label = ((ComponentInst)alCompoInst.get(nK)).getLabel();
                      if ((label == null) || (label.length() == 0))
                          label = ((ComponentInst)alCompoInst.get(nK)).getName();
                      id = ((ComponentInst)alCompoInst.get(nK)).getId();
                      elementId++;
                      elementLabel = "element"+elementId;
                      objectLinked = ((ComponentInst)alCompoInst.get(nK)).getName();
                      link = m_sContext + URLManager.getURL(((ComponentInst)alCompoInst.get(nK)).getName(), sPrivateDomain, ((ComponentInst)alCompoInst.get(nK)).getId()) + "Main";
                      out.println(urlFactory(link, "", elementLabel, id, objectLinked, label, "", COMPONENT, SPACE_COMPONENT, "", m_sContext));
                  }

              }
              %>
                                </td></tr>
                                </table>
              </span></td>
          </tr>
        </table>
        </td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<!--</table>-->
  <input type="hidden" name="_Visible" value="<%=_visible%>" >
  <input type="hidden" name="_Visible" value="<%=_visible%>" >
  <input type="hidden" name="state" value="<%=state%>" >
  <input type="hidden" name="axis_path" value="<%=selected_axis_path%>">
  <input type="hidden" name="axis_id" value="<%=selected_axis_id%>">
  <input type="hidden" name="component_id" value="<%=component_id%>">
  <input type="hidden" name="s_image" value="<%=s_image%>">
  <input type="hidden" name="s_source" value="<%=s_source%>">
  <input type="hidden" name="new_space" value="">
<!--
<tr>
    <td width="100%" class="intfdcolor13"><img src="icons/1px.gif" width="1" height="1"></td>
    <td rowspan="3" colspan="2" class="intfdcolor"><img src="icons/angleHautDomainsBar.gif" width="8" height="8"></td>
</tr>
<tr>
    <td width="100%" class="intfdcolor4"><img src="icons/1px.gif" width="1" height="1"></td>
</tr>
<tr class="intfdcolor">
    <td width="100%"><img src="icons/1px.gif" width="1" height="6"></td>
</tr>
<tr class="intfdcolor">
    <td width="100%"><img src="icons/1px.gif" width="1" height="1"></td>
    <td><img src="icons/1px.gif" width="7" height="1"></td>
    <td class="intfdcolor"><img src="icons/1px.gif" width="1" height="1"></td>
</tr>
-->
<tr class="intfdcolor51">
    <td width="100%"><img src="icons/1px.gif" width="1" height="3"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor">
    <td width="100%">
        <table width="100%" border="0" cellspacing="0" cellpadding="0">
          <tr>
            <td>&nbsp;</td>
            <td width="100%"><span class="txtpetitblanc"><%=JspHelper.formatAxesCaption(component_id, spaceOrSubSpace, message, m_MainSessionCtrl)%></span></td>
          </tr>
        </table>
        </td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%"><img src="icons/1px.gif" width="1" height="1"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor13">
    <td width="100%"><img src="icons/1px.gif"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor4"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor4">
    <td width="100%"><img src="icons/1px.gif"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<tr class="intfdcolor51">
    <td width="100%"><img src="icons/1px.gif" width="1" height="3"></td>
    <td><img src="icons/1px.gif"></td>
    <td class="intfdcolor"><img src="icons/1px.gif"></td>
</tr>
<%
if (!currentSpaceInMaintenance)
{
%>
<tr class="intfdcolor51">
     <td width="100%">
        <table border="0" cellspacing="0" cellpadding="0" width="100%">
           <tr>
             <td>&nbsp;</td>
             <td width="100%"><span class="txtnote">
                        <table cellpadding=0 cellspacing=0 border=0 width="100%">
                        <tr><td>
                <%
                    // il peut y avoir aucun axe primaire dans un 1er temps
                    if ( (primaryAxis != null) && (primaryAxis.size()>0) )
                    {
                        int prev_level = 0;
                        int last_level = 0;
                        String text_link = "";

                        for (int i=0; i<primaryAxis.size(); i++)
                        {
                            SearchAxis searchAxis = (SearchAxis)primaryAxis.get(i);
                            String axisId = new Integer(searchAxis.getAxisId()).toString();
                            String axisRootId = new Integer(searchAxis.getAxisRootId()).toString();
                            String axisName = Encode.javaStringToHtmlString(searchAxis.getAxisName());

                            int nbPositions = searchAxis.getNbObjects();
                            if ( nbPositions == 0 && component_id != null )
                            {
                                continue;
                            }
                            objectLinked = "";
                            link = "javascript:top.scriptFrame.axisClick('"+axisId+"','');";
                            text_link = m_sContext + "/RpdcSearch/jsp/showaxishfromhomepage?query=&AxisId="+axisId+"&ValueId=/"+axisRootId+"/&SearchContext=isNotEmpty&component_id="+component_id+"&space_id="+spaceOrSubSpace;
                            if ( axisId.equals(selected_axis_id) )
                            {
                                if ( daughters != null )
                                {
                                    if ( "".equals(selected_axis_path) )
                                    {
                                        if ( "off".equals(state) )
                                        {
                                            out.println(urlFactory(link, text_link, axisId, "", objectLinked, axisName, "&nbsp;("+nbPositions+")", AXIS, AXIS_COLLAPSED, "", m_sContext));
                                            continue;
                                        }
                                    }
                                    out.println(urlFactory(link, text_link, axisId, "", objectLinked, axisName, "&nbsp;("+nbPositions+")", AXIS, AXIS_EXPANDED, "", m_sContext));
//**********************************Begin daughters

                                    int d_size = daughters.size();

                                    int[][] tree_gui_represintation = new int[d_size][d_size];
                                    int selected_x = -1;
                                    int selected_y = 0;


                                    for ( int j=0; j<d_size; j++ )
                                    {
                                        Value value = (Value) daughters.get(j);
                                        int valueLevel = value.getLevelNumber();
                                        String path = value.getFullPath();
                                        setTreeNode( tree_gui_represintation, valueLevel,  j, d_size );
                                        if ( selected_axis_path.equals(path) )
                                        {
                                            selected_x = valueLevel;
                                            selected_y = j;
                                        }
                                    }

                                    closeTreeNodes( tree_gui_represintation, 0, d_size, d_size );
                                    if ( selected_x >=0 )
                                    {
                                        if ( state.equals("on") )
                                        {
                                            selected_x++;
                                            selected_y++;
                                        }
                                        collapseTree( tree_gui_represintation, selected_x, selected_y, d_size );
                                    }

                                    for (int j = 1; j<d_size; j++)
                                    {
                                        Value value = (Value) daughters.get(j);
                                        String valueName = Encode.javaStringToHtmlString(value.getName());
                                        String valueId = value.getPK().getId();
                                        int valueLevel = value.getLevelNumber();
                                        int valueNbObjects = value.getNbObjects();
                                        if ( valueNbObjects == 0 && component_id != null )
                                        {
                                            continue;
                                        }
                                        String valueFullPath = value.getFullPath();
                                        String valueMotherId = value.getMotherId();
                                        String valuePath = value.getPath();
                                        String valueTreeId = value.getTreeId();
                                        link = "javascript:top.scriptFrame.axisClick('"+axisId+"','"+valueFullPath+"');";
                                        text_link = m_sContext + "/RpdcSearch/jsp/showaxishfromhomepage?query=&AxisId="+axisId+"&ValueId="+valueFullPath+"&SearchContext=isNotEmpty&component_id="+component_id+"&space_id="+spaceOrSubSpace;
                                        if ( tree_gui_represintation[0][j] == T_HIDED_NODE )
                                        {
                                            continue;
                                        }
                                        String offset = getTreeNodeOffset( tree_gui_represintation, j, d_size, m_sContext );
                                        int node_type;
                                        if ( tree_gui_represintation[valueLevel][j] == T_OPENED_NODE )
                                        {
                                            node_type = AXIS_EXPANDED;
                                        }
                                        else
                                        {
                                            node_type = AXIS_COLLAPSED;
                                        }
                                        if ( selected_axis_path.equals("") )
                                        {
                                            if ( valueLevel == 1)
                                            {
                                                out.println(urlFactory(link, text_link, valueFullPath, "", objectLinked, valueName, " ("+valueNbObjects+")", AXIS_LAST, AXIS_COLLAPSED, offset, m_sContext));
                                            }
                                            continue;
                                        }
                                        else
                                        {
                                            out.println(urlFactory(link, text_link, valueFullPath, "", objectLinked, valueName, " ("+valueNbObjects+")", AXIS_LAST, node_type, offset, m_sContext));
                                        }
                                    }
                                }
                            }
                            else
                            {
                                out.println(urlFactory(link, text_link, axisId, "", objectLinked, axisName, "&nbsp;("+nbPositions+")", AXIS, AXIS_COLLAPSED, "", m_sContext));
                            }
                        }// fin du for
                    }
                    /*else
                    {
                        out.println(message.getString("AxisEmpty"));
                    }*/%>
                                        </td></tr>
                                                        </table>
               </span></td>
           </tr>
         </table>
        </td>
     <td><img src="icons/1px.gif"></td>
     <td class="intfdcolor"><img src="icons/1px.gif"></td>
 </tr>
<%
}
%>

<tr>
    <td width="100%" class="intfdcolor51"><img src="icons/1px.gif" width="1" height="6"></td>
    <td rowspan="3" colspan="2" class="intfdcolor51"><img src="icons/angleBasDomainsBar.gif" width="8" height="8"></td>
</tr>
<tr>
    <td width="100%" class="intfdcolor4"><img src="icons/1px.gif" width="1" height="1"></td>
</tr>
<tr class="intfdcolor13">
    <td width="100%"><img src="icons/1px.gif" width="1" height="1"></td>
</tr>
      </table>
</form>
<%if ( s_image != null && !s_image.equals("") )
{
out.println("<script language=\"JavaScript1.2\">");
out.println("top.scriptFrame.rollActiv('"+s_image+"','"+s_source+"')");
out.println("</script>");
}%>

</body>
</html>
