function confirmRemove(action, params, strQuestion) {
  jQuery.popup.confirm(strQuestion, function() {
    let $div = jQuery('<div>', {id: 'Form-Removal'})
    let $form = jQuery('<form>', {name: action, action: action, method: 'POST'}).appendTo($div);
    for (let input in params) {
      let $input = jQuery('<input>', {name: input, value: params[input], type: 'hidden'});
      $form.append($input);
    }
    jQuery('body').append($div);
    setTokens('#Form-Removal');
    $form.submit();
    $div.remove();
  });
}
