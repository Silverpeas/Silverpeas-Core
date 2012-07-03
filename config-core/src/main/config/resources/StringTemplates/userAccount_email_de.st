$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE to customize  an email about account information       !$
$! the list of templates attributes are :     !$
$!  "userDetail"  the UserDetail object    !$
$!  "pwd"  the user password    !$
$!  "loginURL"  the login URL string representation    !$
$!  "createdUser"  is created user (IF DEFINED)    !$
<div style="font-family:Arial;font-size:11px">
Hallo  $userDetail.firstName$ $userDetail.lastName$,<br/>
Ihr Konto wurde erfolgreich $if(createdUser)$created$else$modified$endif$.<br/>
Verbindungseinstellungen sind:
Login : $userDetail.login$
Password : $pwd$
Connection URL : <a href="$loginURL$">Folgen Sie diesem Link...</a>
</div>