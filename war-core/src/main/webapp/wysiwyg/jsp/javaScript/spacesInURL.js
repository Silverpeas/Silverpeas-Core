function breakSpace(pstr)
{
 	res="";
    for(i=0; i<pstr.length;i++) 
	{
		c = pstr.charAt(i);
        switch(c) {
            case ' ':res+="%20";break;
            //case '>':res+="&gt;";break;
            //case '&':res+="&amp;";break;
            default:res+=c;
        }
    }
//	document.formtest.test.value = res;
	return res;
}

function unbreakSpace(pstr) {

	str = new String(pstr);
	res = "";
    if (pstr==null) return res;
	index = str.indexOf("%20");
	while (index != -1) {
        res += str.substring(0, index);
		res += " ";
		str = str.substring(index + 3, str.length);
		index = str.indexOf("%20");
    }
	res += str;
//	document.formtest.test.value = res;
    return res;
}