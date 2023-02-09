$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE to customize  an email about account information       !$
$! the list of templates attributes are :     !$
$!  "userDetail"  the UserDetail object    !$
$!  "pwd"  the user password    !$
$!  "loginURL"  the login URL string representation    !$
$!  "createdUser"  is created user (IF DEFINED)    !$
Hola $userDetail.firstName$ $userDetail.lastName$,<br/>
Tu cuenta ha sido $if(createdUser)$creada$else$modificada$endif$ en la plataforma Silverpeas.<br/>
Su configuración de conexión es la siguiente:
<ul>
<li>Identificador: $userDetail.login$</li>
<li>Contraseña: $pwd$</li>
<li>Conexión: <a href="$loginURL$">Enlace de acceso</a></li>
</ul>
$if(extraMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$extraMessage$</div>
$endif$
