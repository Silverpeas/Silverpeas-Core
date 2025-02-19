$if(several)$
$senderName$ hat Ihre Teilnahme an allen Veranstaltungen <b>$contributionName$</b>$if(contributionStartDate)$ ab dem <i>$contributionDate$</i> entfernt$else$ entfernt$endif$
$else$
$senderName$ hat Ihre Teilnahme an die Veranstaltung <b>$contributionName$</b>$if(contributionStartDate)$$if(contributionEndDate)$ vom <i>$contributionStartDate$</i> bis zum <i>$contributionEndDate$</i> entfernt$else$ für den <i>$contributionStartDate$</i> entfernt$endif$$else$ entfernt$endif$
$endif$