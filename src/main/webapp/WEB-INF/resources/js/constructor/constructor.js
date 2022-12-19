$('select').on('change', function() {
    optionSelect(this.value);
});

function clickAdd() {
    window.location.href = '/courses/add';
}

function clickCourse(hash) {
    window.location.href = '/constructor/' + hash;
}

function optionSelect(value) {
    window.location.href = '/constructor?section=' + value;
}