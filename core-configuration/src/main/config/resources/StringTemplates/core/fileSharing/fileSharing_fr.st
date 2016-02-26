<b>$senderUser.firstName$ $senderUser.lastName$</b> a partagé le fichier <i>$ticket.resource.name$</i>.
$if(ticket.endDate)$Attention le fichier est disponible jusqu'au $ticket.endDate; format="dd/MM/yyyy"$. $if(limitedAccess)$Le nombre de téléchargement est limité à $ticket.nbAccessMax$.$endif$
$endif$


$if(additionalMessage)$
Message :
<div style="background-color:#FFF9D7; border:1px solid #E2C822; padding:5px; width:390px;">$additionalMessage$</div>
$endif$