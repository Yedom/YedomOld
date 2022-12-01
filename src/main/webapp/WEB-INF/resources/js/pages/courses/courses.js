let search = $('#search');
let titles = $('.titles');

titles.each(function() {
    if(this.text.length > 34) {
        this.text = this.text.substring(0, 34) + '...';
    }
});

const urlParams = new URLSearchParams(window.location.search);
const searchParam = urlParams.get('search');
search.val(searchParam);

document.addEventListener("keypress", function(e) {
    if(e.key === "Enter") {
        if (search.val().length > 0) {
            clickSearch()
        }
    }
});

fetch("/courses/popularTags")
    .then((response) => response.json())
    .then((data) => {
        //console.log(data.tags);
        $('#tagsToAdd').html(data.tags);
    });

function clickSearch() {
    let text = search.val();
    if(text === '') {
        window.location.href = '/courses';
    }
    else {
        window.location.href = "/courses?search=" + text;
    }
}
function clickAddCourse() {
    window.location.href = '/courses/add';
}

function spanClick(span) {
    $('.tag').attr('hidden', 'hidden');
    window.location.href = "/courses?search=" + span.innerText + "&tag=true";
}