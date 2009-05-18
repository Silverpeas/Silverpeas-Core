function showFieldTranslation(fieldId, varName)
{
	try
	{
		document.getElementById(fieldId).value = eval(varName);
		document.getElementById("delTranslationLink").style.display='inline';
	}
	catch (e)
	{
		document.getElementById(fieldId).value = "";
		document.getElementById("delTranslationLink").style.display='none';
	}
}
