$if(several)$
$senderName$ hat Ihren Präsenz-Status für allen Veranstaltungen <b>$contributionName$</b>$if(contributionStartDate)$ ab dem <i>$contributionDate$</i> geändert$else$ geändert$endif$
$else$
$senderName$ hat Ihren Präsenz-Status an die Veranstaltung <b>$contributionName$</b>$if(contributionStartDate)$$if(contributionEndDate)$ vom <i>$contributionStartDate$</i> bis zum <i>$contributionEndDate$</i> geändert$else$ für den <i>$contributionStartDate$</i> geändert$endif$$else$ geändert$endif$
$endif$