let placeholderModule = /*[[#{draft_courses.module.add.name]]*/ '';

jQuery.fn.exists = function() { return this.length > 0; }

function addModule() {
    let modules = $('.modules');
    if(!$('#last').exists()) {
        modules.html(modules.html() + "<div class='module' id='last'>" +
            "<input type='text' id='last-module-input' maxlength='50' " +
            "placeholder='"+placeholderModule+"' class='add-module'/>  " +
            "<i onclick='saveModel()' class='material-icons centered-icon save-icon'>save</i>&nbsp;&nbsp;" +
            "<i onclick='cancelAddModel()' class='material-icons centered-icon delete-icon'>delete</i>" +
            "</div>");
        $('#last-module').hide();
    }
}

function cancelAddModel() {
    $('#last').remove();
    $('#last-module').show();
}

function saveModel() {
    let moduleName = $('#last-module-input').val();
    if(moduleName.length > 0) {

    }
}

var coll = $('.collapsible');

for (var i = 0; i < coll.length; i++) {
    coll[i].click(function() {
        this.classList.toggle("active");
        var content = this.nextElementSibling;
        if (content.style.display === "block") {
            content.style.display = "none";
        } else {
            content.style.display = "block";
        }
    });
}