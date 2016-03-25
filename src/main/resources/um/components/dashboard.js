$("#sidebar-collapse").click(function() {
    $("#sidebar").toggle();
    if($("#container").hasClass("col-md-10")) {
        $("#container")
        .removeClass("col-md-10")
        .addClass("col-md-12");
    } else {
        $("#container")
        .removeClass("col-md-12")
        .addClass("col-md-10");
    }
});