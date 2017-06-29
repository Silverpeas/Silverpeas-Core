$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE to customize  an email about account information       !$
$! the list of templates attributes are :     !$
$!  "userDetail"  the UserDetail object    !$
$!  "pwd"  the user password    !$
$!  "loginURL"  the login URL string representation    !$
$!  "createdUser"  is created user (IF DEFINED)    !$
Bonjour $userDetail.firstName$ $userDetail.lastName$,<br/>
Votre compte a été $if(createdUser)$créé$else$modifié$endif$ sur la plate-forme Silverpeas.<br/>
Vos paramètres de connexion sont les suivants :
<ul>
<li>Identifiant : $userDetail.login$</li>
<li>Mot de passe : $pwd$</li>
<li>Connexion : <a href="$loginURL$">Lien d'accès</a></li>
</ul>
$if(extraMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$extraMessage$</div>
$endif$