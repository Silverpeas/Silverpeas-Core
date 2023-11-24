<div class="profilPhoto">
	<a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$avatar$</a>
 </div>
 <div class="info">
   <ul>
     <li class="userName"> <a href="$context$/Rprofil/jsp/Main?userId=$user.id$">$user.lastName$ $user.firstName$</a></li>
     <li class="infoConnection">
	$if(user.connected)$
			$if(notMyself)$
				$if(chatEnabled)$
				  <a href="#" title="Discuter avec $user.firstName$" onclick="javascript:SilverChat.gui.openChatWindow('$user.chatId$', '$user.displayedName$')">
					  <img src="$context$/util/icons/online.gif" alt="Discuter avec $user.firstName$" title="Discuter avec $user.firstName$"/>
				  </a>
				$else$
				  <img src="$context$/util/icons/online.gif" alt="connected"/>
				$endif$
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
     $if(mail)$
     <li class="userMail">
	    <a href="mailto:$mail$">$mail$</a>
     </li>
     $endif$
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
<div class="action actionShownOnMouseOver">
  $if(notMyself)$
	  $if(invitationSent)$
	    <a href="#" rel="$invitationSent$" class="link cancel-invitation">Annuler l'invitation</a>
    $elseif(invitationReceived)$
      <a href="#" rel="$invitationReceived$" class="link view-invitation">Voir l'invitation</a>
    $elseif(aContact)$
      <a href="#" rel="$user.id$,$user.firstName$ $user.lastName$,deleteRelationCallback" class="link delete-relation">Retirer de mes contacts</a>
    $else$
      <a href="#" rel="$user.id$,$user.firstName$ $user.lastName$" class="link invitation">Envoyer une invitation</a>
    $endif$
	  <a href="#" onclick="sp.messager.open(null, {recipientUsers: $user.id$, recipientEdition: false});" class="link notification">Envoyer un message</a>
  $endif$
</div>