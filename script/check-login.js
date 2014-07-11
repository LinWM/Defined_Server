/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
$(function() {
    var user = sessionStorage["login_user"];
    if (!user) {
        alertMsg("请先登录！");
        location.href = BaseUrl + "index.html";
    }
});

