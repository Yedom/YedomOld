document.addEventListener("keypress", function(e) {
    if(e.key === "Enter") {
        clickRestore();
    }
});

function clickRestore() {
    let data = {operation: "restore", email: $('#email').val()};
    const response = fetch("/auth/restore", {
        method: "POST",
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });
    $('#error').text("");
    response.then(function (response) {
        if(response.status === 200) {
            $('#label').removeAttr("hidden");
            $('#restore').attr('hidden', 'hidden');
        }
        else if(response.status === 500) {
            $('#error').text(incorrectEmail);
        }
    });
}