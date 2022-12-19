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
    if(userEntityName === '') window.location = '../../../..';
    window.location = '/profile/' + userEntityName + '/' + value;
}