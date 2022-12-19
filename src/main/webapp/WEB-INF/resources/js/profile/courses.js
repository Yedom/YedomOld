$('#courses').addClass('btn-primary');
// Another profile
if(userName !== userEntityName) {
    $('#balance').remove();
    $('#settings').remove();
}

function clickButton(value) {
    if(userEntityName === '') window.location = '../../../..';
    window.location = '/profile/' + userEntityName + '/' + value;
}

function clickCourse(hash) {
    window.location.href = '/courses/' + hash;
}