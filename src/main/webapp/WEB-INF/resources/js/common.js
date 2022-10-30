//check if char symbol is special (shift, ctrl, ...)
function isSpecialSymbol(code) {
    return code === 8 || code === 17 || code === 37 || code === 39;
}

//remove symbols from string to last comma
function removeSymbolsToLastComma(str) {
    let lastComma = str.lastIndexOf(',') + 2;
    if (lastComma !== -1) {
        return str.substring(0, lastComma);
    }
    return str;
}

//check if symbol in array
function isSymbolInArray(symbol, array) {
    for (let i = 0; i < array.length; i++) {
        if (symbol === array[i]) {
            return true;
        }
    }
    return false;
}

//trim string and remove first and last spaces
function trimString(string) {
    let result = "";
    let isSpace = false;
    for (let i = 0; i < string.length; i++) {
        if (string[i] === ' ') {
            if (!isSpace) {
                result += string[i];
                isSpace = true;
            }
        } else {
            result += string[i];
            isSpace = false;
        }
    }
    if (result[0] === ' ') {
        result = result.substring(1);
    }
    if (result[result.length - 1] === ' ') {
        result = result.substring(0, result.length - 1);
    }
    return result;
}

//remove spaces before commas
function removeSpacesBeforeCommas(string) {
    let result = "";
    for (let i = 0; i < string.length; i++) {
        if (string[i] === ' ' && string[i + 1] === ',') {
            result += ',';
            i++;
        } else {
            result += string[i];
        }
    }
    return result;
}

//add space after comma if not exist
function addSpaceAfterComma(string) {
    let result = "";
    for (let i = 0; i < string.length; i++) {
        if (string[i] === ',') {
            if (string[i + 1] !== ' ') {
                result += ', ';
            } else {
                result += ',';
            }
        } else {
            result += string[i];
        }
    }
    return result;
}

//check if comma exist from end white its space
function isCommaExist(string) {
    for (let i = string.length - 1; i >= 0; i--) {
        if (string[i] === ',') {
            return true;
        }
        if (string[i] !== ' ') {
            return false;
        }
    }
    return false;
}

//select first N words in every separated by comma string
function selectFirstNWords(string, N) {
    let result = "";
    let words = string.split(', ');
    for (let i = 0; i < words.length; i++) {
        let word = words[i];
        let wordArray = word.split(' ');
        let M = Math.min(wordArray.length, N);
        for (let j = 0; j < M; j++) {
            if (j < M) {
                result += wordArray[j];
                if (j < M - 1) {
                    result += ' ';
                }
            }
        }
        if (i < words.length - 1) {
            result += ', ';
        }
    }
    return result;
}

//replace repeated commas in string to one
function replaceRepeatedCommas(string) {
    let result = "";
    let isComma = false;
    for (let i = 0; i < string.length; i++) {
        if (string[i] === ',') {
            if (!isComma) {
                result += string[i];
                isComma = true;
            }
        } else {
            result += string[i];
            isComma = false;
        }
    }
    return result;
}

//get sum of symbols in array
function getSumOfSymbolsInArray(array) {
    let sum = 0;
    for (let i = 0; i < array.length; i++) {
        sum += array[i].length;
    }
    return sum;
}

//processing tags string
function processTagsString(string) {
    string = trimString(string);
    string = replaceRepeatedCommas(string);
    string = removeSpacesBeforeCommas(string);
    string = addSpaceAfterComma(string);
    string = selectFirstNWords(string, 4);
    return string;
}

//get last N words from split array string
function getLastNWords(array, N) {
    let result = "";
    let M = Math.min(array.length, N);
    for (let i = array.length - M; i < array.length; i++) {
        result += array[i];
        if (i < array.length - 1) {
            result += ' ';
        }
    }
    return result;
}

//from end to string check for comma
function isCommaExistFromEnd(string) {
    for (let i = string.length - 1; i >= 0; i--) {
        if (string[i] === ',') {
            return true;
        }
        else if(string[i] !== ' ') {
            return false;
        }
    }
    return true;
}

function checkScroll(el){
    let startY = el.height() * 2; //The point where the navbar changes in px

    if($(window).scrollTop() > startY){
        $('.navbar').addClass("scrolled");
    }else{
        $('.navbar').removeClass("scrolled");
    }
}