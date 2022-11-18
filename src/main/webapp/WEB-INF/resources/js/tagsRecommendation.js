tagsArray = [',', '.', '@', '#', '\n', '\r'];

let title = $('#title');
let tags = $('#tags');

$(document).ready(function () {
    tags.bind('copy paste', function (e) {
        e.preventDefault();
    });
    if(title.val().length >= 5) {
        updateTags();
    }
});

function spanClick(span) {
    if(title.val().split(', ').length < 30) {
        span.setAttribute("hidden", "hidden");
        let tag = span.innerText;
        tags.val(isCommaExistFromEnd(tags.val()) ? tags.val() + tag : tags.val() + ', ' + tag);
        updateTags();
    }
}

//remove repeated tags
function processTags(dataTags) {
    let splTags = tags.val().split(', ');
    splTags.forEach(function (splTag) {
        if(splTag.length > 0) {
            dataTags = dataTags.replaceAll(
                "<span onclick=\"spanClick(this)\">" + splTag + "</span>", "");
        }
    });
    return dataTags;
}

function updateTags() {
    let data = tags.val() + '@' + title.val();
    fetch("/courses/add/tagsUpdate?tags="+data)
        .then((response) => response.json())
        .then((data) => {
            //console.log(data.tags);
            $('#tagsToAdd').html(processTags(data.tags));
        });
}

title.on('keydown', function(e) {
    if(title.val().split(', ').length >= 30) {
        if(!isSpecialSymbol(e.keyCode)) return false;
    }
});

title.on('keyup', function () {
    if(this.value.length >= 5) {
        updateTags();
    }
    else {
        $('#tagsToAdd').html("");
    }
});

//fit to format
tags.on('keyup', function(e) {
    let sym = this.value[this.value.length - 1];
    //check if symbol is tag's separator
    if(isSymbolInArray(sym, tagsArray) && e.keyCode !== 8) {
        let predVal = this.value.substring(0, this.value.length - 1);
        let spl = predVal.split(', ');

        this.value = predVal;
        if (!isCommaExist(this.value) && getSumOfSymbolsInArray(spl[spl.length - 1]) >= 3) {
            this.value += ', ';
            this.value = processTagsString(this.value)
        }
        updateTags();
    }
});

//prevent keydown repeating
tags.on('keydown', function(e) {
    let sym = this.value[this.value.length - 1];
    if(isSymbolInArray(sym, tagsArray) && e.keyCode !== 8) {
        e.preventDefault();
    }
});