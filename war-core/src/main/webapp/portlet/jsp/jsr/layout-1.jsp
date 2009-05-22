<%--
  CDDL HEADER START
  The contents of this file are subject to the terms
  of the Common Development and Distribution License
  (the License). You may not use this file except in
  compliance with the License.

  You can obtain a copy of the License at
  http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
  See the License for the specific language governing
  permission and limitations under the License.

  When distributing Covered Code, include this CDDL
  Header Notice in each file and include the License file
  at legal/CDDLv1.0.txt.
  If applicable, add the following below the CDDL Header,
  with the fields enclosed by brackets [] replaced by
  your own identifying information:
  "Portions Copyrighted [year] [name of copyright owner]"

  Copyright 2006 Sun Microsystems Inc. All Rights Reserved
  CDDL HEADER END
--%>

<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/fn.tld" prefix="fn"%>

<c:set value="${sessionScope['com.sun.portal.portletcontainer.driver.portletWindows']}" var="map"/>
<c:set var="thinportlets" value='${map["thin"]}' />
<c:set var="thickportlets" value='${map["thick"]}' />

<c:set var="disableMove" value="${requestScope['DisableMove']}"/>

<c:set var="isMaximized" value="false" />

<c:if test="${thickportlets != null}">
	<c:forEach items="${thickportlets}" var="portlet2Test">     
    	<c:choose>
        	<c:when test="${portlet2Test.maximized==true}">
				<c:set var="isMaximized" value="true" />
				<c:set var="portlet" value="${portlet2Test}" />
            </c:when>
            <c:otherwise>
            </c:otherwise>
        </c:choose> 
    </c:forEach>
</c:if>

<c:if test="${thinportlets != null}">
	<c:forEach items="${thinportlets}" var="portlet2Test">     
    	<c:choose>
        	<c:when test="${portlet2Test.maximized==true}">
				<c:set var="isMaximized" value="true" />
				<c:set var="portlet" value="${portlet2Test}" />
            </c:when>
            <c:otherwise>
            </c:otherwise>
        </c:choose> 
    </c:forEach>
</c:if>

<div id="portal-content-layout">

	<c:choose>
    	<c:when test="${isMaximized==true}">
         	<!-- <div style="width:100%; height:100%"> -->
			<dl id="portlet_<c:out value="${portlet.portletWindowName}"/>" class="sort" style="width:100%; height:100%">
				<%@include file="portletSP.jsp"%>
			</dl>
			<!-- </div> -->
        </c:when>
        <c:otherwise>
			  <c:choose>
				<c:when test="${disableMove==true}">
					<div id="thick">
				</c:when>
				<c:otherwise>
					<div id="thick" class="ui-sortable">
            	</c:otherwise>
			  </c:choose>
			  <c:if test="${thickportlets != null}">
					<c:forEach items="${thickportlets}" var="portlet">     
	                  	  <dl id="portlet_<c:out value="${portlet.portletWindowName}"/>" class="sort">
	                            <%@include file="portletSP.jsp"%>
                          </dl>
	              </c:forEach>
			   </c:if>
			   </div>
				
			   <c:choose>
				<c:when test="${disableMove==true}">
					<div id="thin">
				</c:when>
				<c:otherwise>
					<div id="thin" class="ui-sortable">
            	</c:otherwise>
			   </c:choose>
			  <c:if test="${thinportlets != null}">
	               <c:forEach items="${thinportlets}" var="portlet">     
                       	<dl id="portlet_<c:out value="${portlet.portletWindowName}"/>" class="sort">
			            	<%@include file="portletSP.jsp"%>
                       	</dl>
	              </c:forEach>
			  </c:if>
			</div>
        </c:otherwise>
    </c:choose> 
</div>