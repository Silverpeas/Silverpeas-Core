<div class="profilPhoto">
	<a href="javascript:showContact('$context$$url$')">$avatar$</a>
 </div>
 <div class="info">
   <ul>
     <li class="userName"> <a href="javascript:showContact('$context$$url$')">$contact.lastName$ $contact.firstName$</a></li>
     <li class="userMail">
	 <a href="mailto:$mail$">$mail$</a>
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
$if(mail)$<div class="action">
  <a href="mailto:$mail$" class="link notification">Envoyer un mail</a>
</div>$endif$