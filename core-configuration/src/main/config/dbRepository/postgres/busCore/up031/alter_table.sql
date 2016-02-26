-- REMOVE DUPLICATE TRANSLATIONS IN THE i18n TABLES AND BASE TABLES FOR SPACES AND COMPONENTS
delete from st_componentinstancei18n
where id in
  (select i.id from st_componentinstancei18n i, st_componentinstance c
   where i.componentid = c.id and i.lang = c.lang);

delete from st_spacei18n
where id in
  (select i.id from st_spacei18n i, st_space s
   where i.spaceid = s.id and i.lang = s.lang);