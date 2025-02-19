$if(several)$
$senderName$ has updated your presence status in all the events <b>$contributionName$</b>$if(contributionStartDate)$ from <i>$contributionStartDate$</i>$endif$
$else$
$senderName$ has updated your presence status in the event  <b>$contributionName$</b>$if(contributionStartDate)$$if(contributionEndDate)$ from <i>$contributionStartDate$</i> to <i>$contributionEndDate$</i>$else$ for <i>$contributionStartDate$</i>$endif$$endif$
$endif$