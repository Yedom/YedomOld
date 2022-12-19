let anotherUser = false;

// Another organization
if(userId !== organizationEntityAdminId) {
    anotherUser = true;
    $('#avatar-upload').attr('disabled', 'disabled');
    $('.avatar-wrapper').css('cursor', 'default');
}

function uploadOrganizationAvatar(baseImg) {
    if(anotherUser) return;
    let data = {baseImg: baseImg};
    fetch("/organization/uploadAvatar?orgname="+organizationEntityName, {
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
                uploadOrganizationAvatar(reader.result);
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