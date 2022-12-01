// MediaElementJS video player with full screen icon
$('video').mediaelementplayer({
    alwaysShowControls: true,
    features: ['playpause','progress','current','duration','tracks','volume','fullscreen'],
    videoVolume: 'horizontal',
    audioWidth: 400,
    audioHeight: 30,
    startVolume: 0.8,
    toggleCaptionsButtonWhenOnlyOne: true,
    iconSprite: iconPath
});

/**
 * Update video uploading progress
 */
function updateProgress() {
    fetch("/uploadingProgress")
        .then((response) => response.json())
        .then((data) => {
            $('#progressBar').attr('value', data.progress);
            if(data.progress >= 100) {
                window.location.reload();
            }
        });
}

/**
 * Upload video by post request with fetch
 */
function uploadVideo() {
    $('#upload-video-label').text(uploading);

    fetch('/constructor/' + hash + '/' + MODULE_ID + '/' + LESSON_ID, {
        method: 'POST',
        body: $('#upload-video')[0].files[0]
    }).then(response => {
        if (!response.ok) {
            $('#error').removeAttr('hidden');
            $('#upload-video-label').text(upload);
        }
        else {
            $('#progressBar').removeAttr('hidden');
            setInterval('updateProgress()', 200);
        }
    });
}

$(document).ready(function() {
    let readURL = function(input) {
        if (input.files && input.files[0]) {
            let reader = new FileReader();

            reader.onload = function () {
                uploadVideo();
            }

            reader.readAsDataURL(input.files[0]);
        }
    }

    $("#upload-video").on('click', function() {
        readURL(this);
    });

    $("#upload-video").on('change', function() {
        readURL(this);
    });
});