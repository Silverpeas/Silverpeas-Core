$!-------------------------------------------------------------------------------!$
$! STRING TEMPLATE to customize  an email about account information       !$
$! the list of templates attributes are :     !$
$!  "userDetail"  the UserDetail object    !$
$!  "pwd"  the user password    !$
$!  "loginURL"  the login URL string representation    !$
$!  "createdUser"  is created user (IF DEFINED)    !$
Привіт $userDetail.firstName$ $userDetail.lastName$,<br/>
Ваш обліковий запис $if(createdUser)$створено$else$змінено$endif$ на платформі Silverpeas.<br/>
Ваші налаштування підключення такі:
<ul>
<li>Ідентифікатор: $userDetail.login$</li>
<li>Пароль: $pwd$</li>
<li>Підключення: <a href="$loginURL$">посилання доступу</a></li>
</ul>
$if(extraMessage)$
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$extraMessage$</div>
$endif$
