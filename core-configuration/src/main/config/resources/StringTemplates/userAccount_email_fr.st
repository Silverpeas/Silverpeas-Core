$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE to customize  an email about account information       !$
$! the list of templates attributes are :     !$
$!  "userDetail"  the UserDetail object    !$
$!  "pwd"  the user password    !$
$!  "loginURL"  the login URL string representation    !$
$!  "createdUser"  is created user (IF DEFINED)    !$
Bonjour $userDetail.firstName$ $userDetail.lastName$,<br/>
Votre compte a été $if(createdUser)$créé$else$modifié$endif$ sur la plateforme Silverpeas.<br/>
Les paramètres de connexion sont les suivants :
login : $userDetail.login$
mot de passe : $pwd$
URL de connexion : <a href="$loginURL$">Lien d'accès</a>