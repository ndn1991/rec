function writeTree(root, data, level) {
    var ul = $("<ul>");
    if (level == 0){
        ul.attr("role", "menu");
        ul.attr("id", "menu");
    }
    else {
        ul.attr("id", "_" + data.id);
    }
    data.children.map(function(c) {
        var li = $("<li>").attr("id", c.id);
        var a = $("<a>").attr("href", "/cat?cat=" + c.id).append(c.text);
        li.append(a);
        if (typeof(c.children) != "undefined" && c.children != null) {
            li.append(writeTree(li, c, 1));
        }
        ul.append(li);
    });
    root.append(ul);
    return ul;
}

function tree(root, data, cats) {
    writeTree(root, data, 0);
    var $menu = $("#menu");
    $menu.menuAim({
        activate: activateSubmenu,
        deactivate: deactivateSubmenu
    });

    function activateSubmenu(row) {
        var $submenu = $("#_" + row.id),
            height = $menu.outerHeight(),
            width = $menu.outerWidth();
        $submenu.css({
            display: "block",
            top: -1,
            left: width + 5,
            height: height - 4
        });
        $(row).addClass("maintainHover");
    }
    function deactivateSubmenu(row) {
        var $submenu = $("#_" + row.id);
        $submenu.css("display", "none");
        $(row).removeClass("maintainHover");
    }
    $("#menu li").click(function(e) {
        e.stopPropagation();
    });
    $("#menu li:has(ul)").find("ul").hide();

    root.hide();
    root.mouseleave(function(){
       root.hide();
    });

    cats.mouseenter(function(){
        root.show();
    })
}

function subTree(root, data) {
    var ul = $("<ul>");
    data.children.map(function(c) {
        var li = $("<li>");
        var a = $("<a>").attr("href", "/cat?cat=" + c.id).append(c.text);
        li.append(a);
        if (typeof(c.children) != "undefined" && c.children != null) {
            li.append(subTree(li, c));
        }
        ul.append(li);
    });
    root.append(ul);
    return ul;
}

function productList(root, data) {
    data.map(function(c){
        var item = $("<div>").addClass("item");
        var image = $("<div>").addClass("image").append($("<img>").attr("src", "http://img.adayroi.com/" + c.image));
        var name = $("<div>").addClass("name").text(c.name);
        item.append(image).append(name);
        var a = $("<a>").attr("href", "/p?p=" + c.id).append(item);
        root.append(a);
    });
}

function detail (root, p) {
    var item_image = $("<div>").addClass("item_image").append($("<img>").attr("src", "http://img.adayroi.com/" + p.image));
    root.append(item_image);
    var info = $("<div>").addClass("info");
    var name = $("<div>").addClass("name").append(p.name);
    var price = $("<div>").addClass("price").append(p.price + " VNĐ");
    var fullCat = $("<div>").addClass("full_cat");
    var cats = p.fullCate.map(function(c){
        return "<a href='cat?cat=" + c.id + "'>" + c.text + "</a>";
    }).join("<span> &raquo; </span>");
    fullCat.append($(cats));
    info.append(name).append(price).append(fullCat);
    root.append(info);
}

function rec(root, data) {
    data.map(function(p){
        var item = $("<div>").addClass("item");
        var item_image = $("<div>").addClass("item_image").append($("<img>").attr("src", "http://img.adayroi.com/" + p.image));
        item.append(item_image);
        var info = $("<div>").addClass("info");
        var name = $("<div>").addClass("name").append($("<a>").attr("href", "/p?p=" + p.id).text(p.name));
        var price = $("<div>").addClass("price").append(p.price + " VNĐ");
        var fullCat = $("<div>").addClass("full_cat");
        var cats = p.fullCate.map(function(c){
            return "<a href='cat?cat=" + c.id + "'>" + c.text + "</a>";
        }).join("<span> &raquo; </span>");
        fullCat.append($(cats));
        info.append(name).append(price).append(fullCat);
        item.append(info);
        root.append(item);
    });
}