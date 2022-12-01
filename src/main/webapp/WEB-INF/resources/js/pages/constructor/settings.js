$('#settings').addClass('btn-primary');

function deleteCourse() {
    $('#delBtn').val(deleting);
    fetch("/constructor/"+hash+"/delete", {
        method: "POST"
    }).then(response => {
        if (response.ok) {
            window.location = '../../../../..';
        }
    });
}