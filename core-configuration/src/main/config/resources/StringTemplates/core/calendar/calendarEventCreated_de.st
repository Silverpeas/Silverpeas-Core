$if(several)$
$senderName$ hat allen Veranstaltungen <b>$contributionName$</b>$if(contributionStartDate)$ ab dem <i>$contributionDate$</i> erstellt$else erstellt$endif$
$else$
$senderName$ hat die Veranstaltung <b>$contributionName$</b>$if(contributionStartDate)$$if(contributionEndDate)$ vom <i>$contributionStartDate$</i> bis zum <i>$contributionEndDate$</i> erstellt$else$ für den <i>$contributionStartDate$</i> erstellt$endif$$else$ erstellt$endif$
$endif$