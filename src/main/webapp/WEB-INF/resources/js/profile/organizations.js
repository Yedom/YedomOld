$('#organizations').addClass('btn-primary');
// Another profile
if(userName !== userEntityName) {
    $('#balance').remove();
    $('#settings').remove();
    $('#orgCreate').remove();
}

function clickOrg(name) {
    window.location = '/organization/' + name;
}

function clickButton(value) {
    if(userEntityName === '') window.location = '/profile/';
    window.location = '/profile/' + userEntityName + '/' + value;
}
