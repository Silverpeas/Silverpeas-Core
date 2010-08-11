<%--<%@ include file="check.jsp" %>--%>

<style type="text/css">

#principal{
width: 100%;
border : 0; 
}

#principal #profilHeadPhote{
height: 100px;
width :100px;
vertical-align: top;
background-repeat: no-repeat; 
background-image:url("http://localhost:8000/silverpeas/directory/jsp/icons/Photo_profil.jpg"); 
float:left;
}

#principal #profilHeadPhote .photo{
width  : 90px;
height : 90px;
}

#principal #profilHeadinfo{
 height : 100;
 width :40%;
 float : left;
 vertical-align: middle;
}


#principal #profilHeadAction{
height : 100;
float: right;
vertical-align: top;
} 

#principal #profilHeadAction #status {
float:left;
width:97%;
}
#principal #profilHeadAction #zoneStatus {
float:left;
vertical-align:top;
}

#boxes #dialog .barre{
float : right;
height: 25px;

}

#boxes #dialog #formButton .button{
font-size:0;
font-weight:bold;
padding-left:170px;
text-align:left;
}
}

</style>
<div id="principal">
<!--  <div id ="photoandInfo">-->
    <div id="profilHeadPhote">
        <img src="<c:url value="${snUserFull.profilPhoto}"/>" class="photo"/>
   </div>

   <div id="profilHeadinfo">
     <b>${snUserFull.userFull.lastName} ${snUserFull.userFull.firstName}</b><br><br>
     ${snUserFull.phone}<br><br>
     ${snUserFull.userFull.eMail}<br><br>
 <c:if test="${snUserFull.connected=='true'}">
   <img  src="<c:url value="/directory/jsp/icons/connected.jpg" />" border="1" width="10" height="10"
         alt="connected"/> <fmt:message key="directory.connected"  /> ${snUserFull.duration}

 </c:if>
 <c:if test="${snUserFull.connected=='false'}">
   <img  src="<c:url value="/directory/jsp/icons/deconnected.jpg" />" border="1" width="10" height="10"
         alt="connected"/>

 </c:if>

 <br>
 </div>
 <div id="profilHeadAction">
       <div id="status">
         <div class="StatusDiv">
         </div>
        <script>          
           getLastStatus('${urlGetLastStatus}');
         </script>
       </div>
       <div id="zoneStatus">

         <a  href="#"  onclick="javascript:enableStatusZone()">

           <img  src=" <c:url value="/directory/jsp/icons/edit_button.gif" />" width="10" height="10"
                  alt="connected"/>
          </a>
        </div>
<!-- </div>-->

</div>


</div>
<div id="boxes">
  <div id="dialog" class="window">
    <div class="barre">
      <a href="#"class="close">Fermer</a>
    </div>    
    <br>
    <view:board>
      <form Name="photoForm" action="valdateChangePhoto" Method="post" ENCTYPE="multipart/form-data" accept-charset="UTF-8">
        <div>
          
            <div class="txtlibform"><fmt:message key="directory.photo" />:</div>
          <div><input type="file" name="WAIMGVAR0" size="60"></div>
         
         
        </div>
      </form>
    </view:board>
     <div id="formButton">
            <div class="button"><!--
             <br>
             <br>
         --><fmt:message key="directory.buttonValid" var="valid"/>
        <view:button label="${valid}" action="javascript:document.photoForm.submit();" disabled="false" />
         <%--<input type="submit" value="Valider" >--%>
         </div>
         </div>
 </div>
 <!-- Mask to cover the whole screen -->
  <div id="mask"></div>
</div>

