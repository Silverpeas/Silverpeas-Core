<%--<%@ include file="check.jsp" %>--%>
<table width="100%"  border="0" >
    <tr>
        <td id="profilHeadPhote" height="100" width="100" style="vertical-align: top">
            <img src="<c:url value="/directory/jsp/icons/Photo_profil.jpg"/>" style="vertical-align: top" width="80" height="90" border="0" alt="viewUser" />
        </td>
        <td id="profilHeadinfo" height="100" width="40%" align="left" style="vertical-align: middle">
            <b>Nom Prénom</b><br><br>
            Tél<br><br>
            Email@email.com<br><br>
            <img src="<c:url value="/directory/jsp/icons/connected.jpg" />" width="10" height="10"
                 alt="connected"/> <fmt:message key="directory.connected"/><br>






    </td>
    <td id="profilHeadAction" height="100" width="60%" align="right" style="vertical-align: top">

        <table width="80px"  border="0" align="right" cellspacing="0" >

            <tr> <form name="statForm" action="TestNabil" method="get">
                <td align="right" width="50px" c>

                    <textarea onblur="javascript:desabledField()" id="enabledStat"  type="text"  name="statMessage"  
                              value="" cols="50" rows="3" ><c:out value="${requestScope.statu}"/>

                    </textarea>
                     </td> 
                     <td  id="" align="right" style="vertical-align: top">
                         
                         <a  href="#"  onclick="javascript:enableField()">

                             <img  src=" <c:url value="/directory/jsp/icons/edit_button.gif" />" width="10" height="10"
                             alt="connected"/>
                    </a>
                </td>
                </form>
            </tr>
           

        </table>

    </td>

</tr>
</table>