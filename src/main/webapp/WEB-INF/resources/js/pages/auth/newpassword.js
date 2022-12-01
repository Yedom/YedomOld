document.addEventListener("keypress", function(e) {
    if(e.key === "Enter") {
        clickApply();
    }
});

function clickApply() {
    let data = {operation: "newpassword", password: $('#password').val()};
    const response = fetch("/auth/newpassword", {
        method: "POST",
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify(data)
    });
    response.then(function (response) {
        if (response.status === 200) {
            window.location.href = "/auth/login";
        } else if(response.status === 500) {
            $('#error').text(err1);
        } else if(response.status === 501) {
            $('#error').text(err2);
        } else if(response.status === 502) {
            $('#error').text(err3);
        }
    });
}