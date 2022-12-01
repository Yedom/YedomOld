$('#main').addClass('btn-primary');

function uploadCourseAvatar(baseImg) {
    let data = {baseImg: baseImg};
    fetch("/constructor/"+hash+"/uploadAvatar", {
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
                uploadCourseAvatar(reader.result);
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