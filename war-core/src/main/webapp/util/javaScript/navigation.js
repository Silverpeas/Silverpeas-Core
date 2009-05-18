
// Enter in a new component
function gotoComponent(baseURL) {
    window.top.bottomFrame.MyMain.location.replace(baseURL + "Main.jsp");
}

// goto a particular part in the component
function gotoComponentPart(baseURL, Id, Type) {
    window.top.bottomFrame.MyMain.location.replace(baseURL + "searchResult.jsp?Type=" + Type + "&Id=" + Id);
}

