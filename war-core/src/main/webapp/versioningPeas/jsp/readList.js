function submitForm( action )
{
    document.readForm.tab_number.value = action;
    document.readForm.submit();
}

function save()
{
    document.readForm.validate.value = "true";
    submitForm( 2 );
}

function deleteUser(index)
{
    document.readForm.delete_index.value = index;
    submitForm( 2 );
}
