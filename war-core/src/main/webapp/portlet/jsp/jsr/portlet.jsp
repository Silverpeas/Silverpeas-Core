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

<!--Load the resource bundle for the page -->
<fmt:setBundle basename="DesktopMessages" />

<div class="portlet">

  <div class="portlet-header">
  
    <h2 class="portlet-title"><c:out value="${portlet.title}" escapeXml="false"/></h2>
    
    <ul class="portlet-options">
    
      <c:if test="${portlet.minimized==true}">
        <li>
          <a href="<c:out value="${portlet.normalizedURL}"/>">
            <img src="/silverpeas/portlet/jsp/jsr/images/unminimize_button.gif" alt="<fmt:message key="unminimize"/>" title="<fmt:message key="unminimize"/>" />
          </a>
        </li>
      </c:if>
      <c:if test="${portlet.minimized==false}">
          <li>
            <a href="<c:out value="${portlet.minimizedURL}"/>">
              <img src="/silverpeas/portlet/jsp/jsr/images/minimize_button.gif" alt="<fmt:message key="minimize"/>" title="<fmt:message key="minimize"/>" />
            </a>
          </li>
      </c:if>
      <c:if test="${portlet.maximized==true}">
          <li>
            <a href="<c:out value="${portlet.normalizedURL}"/>">
              <img src="/silverpeas/portlet/jsp/jsr/images/unmaximize_button.gif" alt="<fmt:message key="unmaximize"/>" title="<fmt:message key="unmaximize"/>" />
            </a>
          </li>
      </c:if>
      <c:if test="${portlet.maximized==false}">
          <li>
            <a href="<c:out value="${portlet.maximizedURL}"/>">
              <img src="/silverpeas/portlet/jsp/jsr/images/maximize_button.gif" alt="<fmt:message key="maximize"/>" title="<fmt:message key="maximize"/>" />
            </a>
          </li>
      </c:if>
      <c:if test="${portlet.help==true}">
        <li>
          <a href="<c:out value="${portlet.helpURL}"/>">
            <img src="/silverpeas/portlet/jsp/jsr/images/help_button.gif" alt="<fmt:message key="help"/>" title="<fmt:message key="help"/>" />
          </a>
        </li>
      </c:if>
      <c:if test="${portlet.edit==true}">
        <li>
          <a href="<c:out value="${portlet.editURL}"/>">
            <img src="/silverpeas/portlet/jsp/jsr/images/edit_button.gif" alt="<fmt:message key="edit"/>" title="<fmt:message key="edit"/>" />
          </a>
        </li>
      </c:if>
      <c:if test="${portlet.view==true}">
        <li>
          <a href="<c:out value="${portlet.viewURL}"/>">
            <img src="/silverpeas/portlet/jsp/jsr/images/view_button.gif" alt="<fmt:message key="view"/>" title="<fmt:message key="view"/>" />
          </a>
        </li>
      </c:if>
      <li>
        <a href="<c:out value="${portlet.removeURL}"/>">
          <img src="/silverpeas/portlet/jsp/jsr/images/remove_button.gif" alt="<fmt:message key="remove"/>" title="<fmt:message key="remove"/>" />
        </a>
      </li>      
    </ul>
    
  </div> <!-- closes portlet-header -->
  
  <c:choose>
    <c:when test="${portlet.minimized==false}">
      <div class="portlet-content">
        <c:out value="${portlet.content}" escapeXml="false"/>
      </div> <!-- closes portlet-content -->
    </c:when>
    <c:otherwise>
      <div class="portlet-content-minimized"></div> <!-- portlet content minimized -->
    </c:otherwise>
  </c:choose>
  
</div> <!-- closes portlet -->
