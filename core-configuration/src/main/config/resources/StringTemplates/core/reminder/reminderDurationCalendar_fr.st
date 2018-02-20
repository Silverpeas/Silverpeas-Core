RAPPEL : $contributionTitle;format="toUpperFirstChar"$ aura lieu $if(reminderContributionSeveralDays)$
$if(reminderContributionStart.hourExisting)$
du $reminderContributionStart.dayDate$ à $reminderContributionStart.hour$ au $reminderContributionEnd.dayDate$ à $reminderContributionEnd.hour$
$else$
du $reminderContributionStart.dayDate$ au $reminderContributionEnd.dayDate$
$endif$
$else$
$if(reminderContributionStart.hourExisting)$
le $reminderContributionStart.dayDate$ de $reminderContributionStart.hour$ à $reminderContributionEnd.hour$
$else$
le $reminderContributionStart.dayDate$
$endif$
$endif$
$if(reminderContributionZoneId)$
 ($reminderContributionZoneId$)
$endif$
.