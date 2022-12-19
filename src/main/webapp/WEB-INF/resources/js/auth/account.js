let role = $('#role');

switch (role.val()) {
    case 'user':
        role.val(user);
        break;
    case 'moderator':
        role.css('color', 'green');
        role.val(moderator);
        break;
    case 'admin':
        role.css('color', 'red');
        role.val(admin);
        break;
}