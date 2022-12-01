$('#friends').addClass('btn-primary');

// Another profile
if(userName !== userEntityName) {
    $('#balance').remove();
    $('#settings').remove();
}

function clickButton(value) {
    if(userEntityName === '') window.location = '/profile/';
    window.location = '/profile/' + userEntityName + '/' + value;
}