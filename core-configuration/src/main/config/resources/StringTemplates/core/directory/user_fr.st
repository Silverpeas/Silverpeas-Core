<div class="profilPhoto">
	<a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$avatar$</a>
 </div>
 <div class="info">
   <ul>
     <li class="userName"> <a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$user.lastName$ $user.firstName$</a></li>
     <li class="infoConnection">
	$if(user.connected)$
			$if(notMyself)$
				<a href="#" title="Discuter avec $user.firstName$" onclick="javascript:window.open('$context$/RcommunicationUser/jsp/OpenDiscussion?userId=$user.id$','popupDiscussion$user.id$','menubar=no, status=no, scrollbars=no, menubar=no, width=600, height=450')">
					<img src="$context$/util/icons/online.gif" alt="Discuter avec $user.firstName$" title="Discuter avec $user.firstName$"/>
				</a>
			$else$
				<img src="$context$/util/icons/online.gif" alt="connected"/>
			$endif$
			Connecté depuis $user.durationOfCurrentSession$
	$else$
	   <img src="$context$/util/icons/offline.gif" alt="deconnected"/> Déconnecté
        $endif$
     </li>
     $if(status)$
	<li class="status">
	  "$status$"
	</li>
     $endif$
     <li class="userType">
	$type$
     </li>
     <li class="userMail">
	<a href="#" onclick="OpenPopup($user.id$,'$user.lastName$ $user.firstName$')">$mail$</a>
     </li>
     <li class="extra">
	$if(extra.title)$
	  $extra.title$<br/>
	$endif$
	$if(phone)$
	      Téléphone : $phone$
	    $endif$
     </li>
   </ul>
 </div>
<div class="action">
  $if(notMyself)$
	$if(notAContact)$
	  <a href="#" rel="$user.id$,$user.lastName$ $user.firstName$" class="link invitation">Envoyer une invitation</a>
	$endif$
	<a href="#" rel="$user.id$,$user.lastName$ $user.firstName$" class="link notification">Envoyer un message</a>
  $endif$
</div>