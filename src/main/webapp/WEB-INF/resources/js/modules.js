let disabledSymbols = /*[[${@environment.getProperty('draft-courses.modules-disabled-symbols')}]]*/ "|:,";
let maxLength = /*[[${@environment.getProperty('draft-courses.max-module-and-lesson-name-length')}]]*/ 50;

jQuery.fn.exists = function() { return this.length > 0; }

/**
 * Click draft courses menus buttons
 */
function clickButton(value) {
    if(hash === '') window.location = '/constructor/';
    if(value !== '') value = '/' + value;
    window.location = '/constructor/' + hash + value + '?active=' + Array.from(activeIDS).join(',');
}

/**
 * Redirect to lesson edit page
 */
function openLesson(moduleId, lessonId) {
    if(hash === '') window.location = '/constructor/';
    if (moduleId === undefined || lessonId === undefined ||
        moduleId === '' || lessonId === '') return;
    window.location = '/constructor/' + hash + '/' + moduleId + '/' + lessonId + '?active=' + Array.from(activeIDS).join(',');
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
    $.get("/constructor/" + hash + "/modules", function (data) {
        $('#modules-container').html(data);
        updateCollapsible();
        collapseAll();
        selectActive();
    });
}

/**
 * Display input for adding new module
 */
function addModuleInput() {
    let modules = $('.modules');
    if($('#current').exists()) cancelAdd();

    modules.html(modules.html() + "<div class='module' id='current'>" +
        "<input style='padding: 2px;' type='text' id='module-input' maxlength='"+maxLength+"' " +
        "placeholder='"+placeholderModule+"' class='add-module'/>  " +
        "<i onclick='addModule()' class='material-icons centered-icon save-icon'>save</i>&nbsp;&nbsp;" +
        "<i onclick='cancelAdd()' class='material-icons centered-icon delete-icon'>delete</i>" +
        "</div>");
    $('#module-input').on('keydown', function(e) {
        if(disabledSymbols.includes(e.key)) {
            e.preventDefault();
        }
    });
    $('.plus-model').hide();
    updateCollapsible();
}

/**
 * Display input for adding new lesson to module with name 'moduleName'
 */
function addLessonInput(moduleName, moduleId) {
    let lessons = $('.lessons-' + moduleId);
    if($('#current').exists()) cancelAdd();

    lessons.html(lessons.html() + "<div class='lesson' id='current'>" +
        "<input style='padding: 2px; width: 100%;' type='text' id='lesson-input' maxlength='"+maxLength+"' " +
        "placeholder='"+placeholderLesson+"' class='add-lesson'/>  " +
        "<i onclick='addLesson(\""+moduleId+"\")' class='material-icons centered-icon save-icon'>save</i>&nbsp;&nbsp;" +
        "<i onclick='cancelAdd()' class='material-icons centered-icon delete-icon'>delete</i>" +
        "</div>");
    $('#lesson-input').on('keydown', function(e) {
        if(disabledSymbols.includes(e.key)) {
            e.preventDefault();
        }
    });
    $('.plus-lesson').hide();
}

function cancelAdd() {
    $('#current').remove();
    if ($('.plus-model').is(':hidden')) {
        $('.plus-model').show();
    }
    if ($('.plus-lesson').is(':hidden')) {
        $('.plus-lesson').show();
    }
}

/**
 * Add new module to course
 */
function addModule() {
    let moduleName = $('#module-input').val();
    if(moduleName === '') return;
    $('.save-icon').removeAttr('onclick');
    fetch("/constructor/" + hash + "/addModule?module=" + moduleName, {
        method: "POST",
        headers: {'Content-Type': 'application/json'},
    }).then(response => {
        if (response.ok) {
            updateModules();
        }
    });
}

/**
 * Add new lesson to course
 */
function addLesson(moduleId) {
    let lessonName = $('#lesson-input').val();
    if(lessonName === '') return;
    $('.save-icon').removeAttr('onclick');
    fetch("/constructor/" + hash + "/addLesson?moduleId=" + moduleId + '&lesson=' + lessonName, {
        method: "POST",
        headers: {'Content-Type': 'application/json'},
    }).then(response => {
        if (response.ok) {
            updateModules();
        }
    });
}

/**
 * Delete module with id 'moduleId'
 */
function deleteModule(moduleId) {
    fetch("/constructor/" + hash + "/deleteModule?moduleId=" + moduleId, {
        method: "POST",
        headers: {'Content-Type': 'application/json'},
    }).then(response => {
        if (response.ok) {
            if(typeof MODULE_ID !== 'undefined' && +moduleId === MODULE_ID) {
                clickButton('');
            }
            updateModules();
        }
    });
}

/**
 * Delete lesson from 'moduleId' with id 'lessonId'
 */
function deleteLesson(moduleId, lessonId) {
    fetch("/constructor/" + hash + "/deleteLesson?moduleId=" + moduleId + '&lessonId=' + lessonId, {
        method: "POST",
        headers: {'Content-Type': 'application/json'},
    }).then(response => {
        if (response.ok) {
            if(typeof MODULE_ID !== 'undefined' && typeof LESSON_ID !== 'undefined' &&
                +moduleId === MODULE_ID && +lessonId === LESSON_ID) {
                clickButton('');
            }
            updateModules();
        }
    });
}