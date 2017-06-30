$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE to customize  an email about account information       !$
$! the list of templates attributes are :     !$
$!  "userDetail"  the UserDetail object    !$
$!  "pwd"  the user password    !$
$!  "loginURL"  the login URL string representation    !$
$!  "createdUser"  is created user (IF DEFINED)    !$
Hallo  $userDetail.firstName$ $userDetail.lastName$,<br/>
Ihr Konto wurde erfolgreich $if(createdUser)$created$else$modified$endif$.<br/>
Verbindungseinstellungen sind:
<ul>
<li>Login : $userDetail.login$</li>
<li>Password : $pwd$</li>
<li>Connection URL : <a href="$loginURL$">Folgen Sie diesem Link...</a></li>
</ul>
$if(extraMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$extraMessage$</div>
$endif$