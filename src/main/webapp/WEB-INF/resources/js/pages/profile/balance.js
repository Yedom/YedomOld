$('#balance').addClass('btn-primary');
// Another profile
if(userName !== userEntityName) {
    $('#balance').remove();
    $('#settings').remove();
}

function clickButton(value) {
    if(userEntityName === '') window.location = '../../../../..';
    window.location = '/profile/' + userEntityName + '/' + value;
}