let placeholderModule = /*[[#{draft_courses.module.add.name]]*/ '';
let disabledSymbols = /*[[${@environment.getProperty('draft-courses.modules-disabled-symbols')}]]*/ "|:,";
let maxLength = /*[[${@environment.getProperty('draft-courses.max-module-and-lesson-name-length')}]]*/ 50;

jQuery.fn.exists = function() { return this.length > 0; }

function addModule() {
    let modules = $('.modules');
    if(!$('#last').exists()) {
        modules.html(modules.html() + "<div class='module' id='last'>" +
            "<input type='text' id='last-module-input' maxlength='"+maxLength+"' " +
            "placeholder='"+placeholderModule+"' class='add-module'/>  " +
            "<i onclick='saveModule()' class='material-icons centered-icon save-icon'>save</i>&nbsp;&nbsp;" +
            "<i onclick='cancelAddModel()' class='material-icons centered-icon delete-icon'>delete</i>" +
            "</div>");
        $('#last-module-input').on('keydown', function(e) {
            if(disabledSymbols.includes(e.key)) {
                e.preventDefault();
            }
        });
        $('#last-module').hide();
    }
}

function cancelAddModel() {
    $('#last').remove();
    $('#last-module').show();
}

function updateModules(module, lesson, del) {
    if(module !== '') module = "&module=" + module;
    if(lesson !== '') lesson = "&lesson=" + lesson;
    if(hash !== '') {
        fetch("/constructor/" + hash + "/modules?del=" + del + module + lesson)
            .then((response) => response.json())
            .then((data) => {
                $('.modules').html(data.modules);
                updateCollapsible();
            });
        $('#last-module').show();
    }
}

function saveModule() {
    let moduleName = $('#last-module-input').val();
    if(moduleName === '') return;
    updateModules(moduleName, '', 'false');
}

function delModule(moduleName) {
    updateModules(moduleName, '', 'true');
}

function updateCollapsible() {
    var coll = $('.collapsible');

    for (var i = 0; i < coll.length; i++) {
        coll[i].click(function () {
            this.classList.toggle("active");
            var content = this.nextElementSibling;
            if (content.style.display === "block") {
                content.style.display = "none";
            } else {
                content.style.display = "block";
            }
        });
    }
}