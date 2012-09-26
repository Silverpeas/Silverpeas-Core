    function confirmRemove( strURL, strQuestion )
    {
        if ( confirm( strQuestion ) )
            location.href = strURL;
    }
