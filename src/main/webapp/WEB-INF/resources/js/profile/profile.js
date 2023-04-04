let anotherUser = false;
let links = $('.links');

$('#profile').addClass('btn-primary');

if(btnClass === 'btn-disabled') {
    $('#add_friend').hide();
}

$('#add').hide();
showAdd();

// Another profile
if(userName !== userEntityName) {
    anotherUser = true;
    $('#balance').remove();
    $('#settings').remove();
    $('.delete-icon').remove();
    $('#avatar-upload').attr('disabled', 'disabled');
    $('.avatar-wrapper').css('cursor', 'default');
    $('#textarea-about').attr('disabled', 'disabled');
    $('.centered-icon').remove();
}
// Our profile
else {
    $('#add_friend').remove();
}

function showAdd() {
    let count = $('.link').length;
    if(!anotherUser && count < 10) {
        $('#add').show();
    }
}

let pred = $('#textarea-about').val();

function saveAbout() {
    let cur = $('#textarea-about').val();
    if(pred !== cur) {
        let data = {about: cur};
        fetch("/profile/aboutUpdate", {
            method: "POST",
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(data)
        }).then(response => {
            if (!response.ok) {
                window.location.reload();
            }
        });
        pred = cur;
    }
}

$(window).bind('beforeunload', function(){
    saveAbout();
});

$(window).click(function() {
    saveAbout();
});

jQuery.fn.exists = function(){ return this.length > 0; }

function saveLink() {
    let name = $('#last-name').val();
    let href = $('#last-href').val();
    name = name.replaceAll('$', '');
    name = name.replaceAll('|', '');
    href = href.replaceAll('$', '');
    href = href.replaceAll('|', '');
    if(name !== '' && href !== '' && !$('#'+name).exists()) {
        $('#last').remove();
        links.html(links.html() + '<div class="link" id="'+name+'">' +
            '<a target="_blank" href="'+href+'" th:href="'+href+'" th:text="'+name+'" rel="noopener noreferrer" class="link-href" href="#">'+name+'</a>' +
            '<i onclick="deleteLink(\''+name+'\')" class="material-icons centered-icon">delete</i>' +
            '</div>');

        showAdd();
        linksUpdate();
    }
}

function addLink() {
    if(!$('#last').exists()) {
        links.html(links.html() + "<div class='link' id='last'>" +
            "<input type='text' id='last-name' maxlength='10' placeholder='GitHub' class='add-name'/>" +
            "<input type='text' id='last-href' maxlength='300' placeholder='https://github.com' class='add-href'/>" +
            "<i onclick='saveLink(\"last\")' class='material-icons centered-icon'>save</i>" +
            "<i onclick='deleteLink(\"last\")' class='material-icons centered-icon'>delete</i>" +
            "</div>")
        $('#add').hide();
    }
}

function deleteLink(linkId) {
    $('#'+linkId).remove();
    if(linkId === 'last') {
        showAdd();
    }
    else {
        linksUpdate();
    }
}

function linksUpdate() {
    var res = '';
    $('.link').each(function (index, el) {
        let aTag = $(el).find('a');
        if(res.length > 0) res += '|';
        res += aTag.text() + '$' + aTag.attr('href');
    });
    let data = {links: res};
    fetch("/profile/linksUpdate", {
        method: "POST",
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    }).then(response => {
        if(!response.ok) {
            window.location.reload();
        }
    });
}

function clickButton(value) {
    if(userEntityName === '') window.location = '/profile/';
    window.location = '/profile/' + userEntityName + '/' + value;
}

function follow() {
    if(userName === null) {
        window.location = '/auth/login';
        return;
    }
    fetch("/profile/follow?username="+userEntityName, {
        method: 'POST'
    }).then(response => {
        if(response.ok) {
            window.location.reload();
        }
    });
}

function uploadAvatar(baseImg) {
    if(anotherUser) return;
    let data = {baseImg: baseImg};
    fetch("/profile/uploadAvatar", {
        method: "POST",
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    }).then(response => {
        if(response.ok) {
            window.location.reload();
            $('#error').attr('hidden', 'hidden');
        }
        else {
            $('#error').removeAttr('hidden');
        }
    });
}

$(document).ready(function() {
    let readURL = function(input) {
        if (input.files && input.files[0]) {
            let reader = new FileReader();

            reader.onload = function () {
                uploadAvatar(reader.result);
            }

            reader.readAsDataURL(input.files[0]);
        }
    }

    $(".file-upload").on('change', function(){
        readURL(this);
    });

    $(".upload-button").on('click', function() {
        $(".file-upload").click();
    });
});