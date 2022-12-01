jQuery.fn.exists = function() { return this.length > 0; }

/**
 * Click course menus buttons
 */
function clickButton(value) {
    if(hash === '') window.location = '/courses';
    if(value !== '') value = '/' + value;
    window.location = '/courses/' + hash + value + '?active=' + Array.from(activeIDS).join(',');
}

/**
 * Redirect to lesson page
 */
function openLesson(moduleId, lessonId) {
    if(hash === '') window.location = '/courses';
    if (moduleId === undefined || lessonId === undefined ||
        moduleId === '' || lessonId === '') return;
    window.location = '/courses/' + hash + '/' + moduleId + '/' + lessonId +
        '?active=' + Array.from(activeIDS).join(',');
}

/**
 * Collapse button listener (onclick)
 */
function collapsibleListener(e, moduleId) {
    e.classList.toggle("active");
    let content = e.nextElementSibling;
    if (content.style.display === "block") {
        content.style.display = "none";
        activeIDS.delete(moduleId);
    } else {
        content.style.display = "block";
        activeIDS.add(moduleId);
    }
}

/**
 * Collapse module with 'moduleId'
 */
function collapse(moduleId) {
    let coll = document.getElementsByClassName("collapsible-"+moduleId);

    for (let i = 0; i < coll.length; i++) {
        coll[i].classList.add("active");
        let content = coll[i].nextElementSibling;
        content.style.display = "block";
    }
}

/**
 * Collapse all modules from 'activeIDS'
 */
function collapseAll() {
    for (let id of activeIDS) {
        collapse(id);
    }
}

/**
 * Add 'active' class to lesson (lesson.html page)
 */
function selectActive() {
    if(typeof MODULE_ID !== 'undefined' && typeof LESSON_ID !== 'undefined') {
        let coll = document.getElementsByClassName("lesson-" + MODULE_ID + "-" + LESSON_ID);

        for (let i = 0; i < coll.length; i++) {
            coll[i].classList.add("active");
        }
    }
}

/**
 * Function to make modules collapsible
 */
function updateCollapsible() {
    let coll = document.getElementsByClassName("collapsible");

    for (let i = 0; i < coll.length; i++) {
        coll[i].addEventListener("click", function () {
            collapsibleListener(this, i);
        });
    }
}

/**
 * Function to remove collapsible listeners
 */
function removeCollapsible() {
    let coll = document.getElementsByClassName("collapsible");

    for (let i = 0; i < coll.length; i++) {
        coll[i].removeEventListener("click", function () {
            collapsibleListener(this, i);
        });
    }
}

/**
 * Update modules with lessons on the left side of the page
 */
function updateModules() {
    $.get("/courses/" + hash + "/modules", function (data) {
        $('#modules-container').html(data);
        updateCollapsible();
        collapseAll();
        selectActive();
    });
}