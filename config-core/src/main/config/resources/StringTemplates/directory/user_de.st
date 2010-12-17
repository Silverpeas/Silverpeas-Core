<div class="profilPhoto">
	$avatar$
 </div>
 <div class="info">
   <ul>
     <li class="userName"> <a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$user.lastName$ $user.firstName$</a></li>
     <li class="infoConnection">
     	$if(user.connected)$
	   <img src="$context$/util/icons/online.gif" alt="connected"/> Verbunden seit $user.duration$
	$else$
  	   <img src="$context$/util/icons/offline.gif" alt="deconnected"/> Offline
        $endif$
     </li>
     $if(user.status)$
     	<li class="status">
     	  "$user.status$"
     	</li>
     $endif$
     <li class="userType">
     	$type$
     </li>
     <li class="userMail">
	<a href="#" onclick="OpenPopup($user.id$,'$user.lastName$ $user.firstName$')">$user.mail$</a>
     </li>
     <li class="extra">
     	$if(extra.title)$
     	  $extra.title$<br/>
     	$endif$
     	$if(extra.phone)$
	  Telefon : $extra.phone$
	$endif$
     </li>
   </ul>
 </div>
<div class="action">
  $if(notMyself)$
  	$if(notAContact)$
	  <a href="#" class="link invitation" onclick="OpenPopupInvitaion($user.id$,'$user.lastName$ $user.firstName$');">Schicken Sie eine Einladung</a>
	$endif$
	<a href="#" class="link notification" onclick="OpenPopup($user.id$,'$user.lastName$ $user.firstName$')">Senden Sie eine Nachricht</a>
  $endif$
</div>