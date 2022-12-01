$('#public').addClass('btn-primary');

if(role !== 'user') {
    $('#moderBtn').val(publicMsg);
}
else if(publicRequest) {
    $('#moderBtn').val(cancelMsg);
    $('#moderBtn').removeClass('btn-green');
    $('#moderBtn').addClass('btn-red');
}

function sendPublic() {
    fetch("/constructor/"+hash+"/public", {
        method: "POST"
    }).then(response => {
        if (response.ok) {
            if(role !== 'user') {
                window.location = "/constructor/"+hash;
            }
            else {
                window.location.reload();
            }
        }
    });
}