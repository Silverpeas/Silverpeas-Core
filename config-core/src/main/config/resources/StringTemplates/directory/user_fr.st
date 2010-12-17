<div class="profilPhoto">
	$avatar$
 </div>
 <div class="info">
   <ul>
     <li class="userName"> <a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$user.lastName$ $user.firstName$</a></li>
     <li class="infoConnection">
     	$if(user.connected)$
	   <img src="$context$/util/icons/online.gif" alt="connected"/> Connecté depuis $user.duration$
	$else$
  	   <img src="$context$/util/icons/offline.gif" alt="deconnected"/> Déconnecté
        $endif$
     </li>
     $if(user.status)$
     	<li>
     	  "$user.status$"
     	</li>
     $endif$
     <li class="userType">
     	$type$
     </li>
     <li class="userMail">
	<a href="#" onclick="OpenPopup($user.id$,'$user.lastName$ $user.firstName$')">$user.mail$</a>
     </li>
     <li>
     	$if(extra.title)$
     	  $extra.title$<br/>
     	$endif$
     	$if(extra.phone)$
	  Téléphone : $extra.phone$
	$endif$
     </li>
   </ul>
 </div>
<div class="action">
  $if(notMyself)$
  	$if(notAContact)$
	  <a href="#" class="link invitation" onclick="OpenPopupInvitaion($user.id$,'$user.lastName$ $user.firstName$');">Envoyer une invitation</a>
	$endif$
	<a href="#" class="link notification" onclick="OpenPopup($user.id$,'$user.lastName$ $user.firstName$')">Envoyer un message</a>
  $endif$
</div>