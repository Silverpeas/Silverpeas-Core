-- removing duplicated data
delete from sb_formtemplate_textfield
where fieldvalueindex is null
and recordId in (
select distinct(recordid) from sb_formtemplate_textfield
where fieldvalueindex = 0 
and recordid in (select recordid from sb_formtemplate_textfield
where fieldvalueindex is null));

-- updating old data
update sb_formtemplate_textfield
set fieldvalueindex = 0
where fieldvalueindex is null;