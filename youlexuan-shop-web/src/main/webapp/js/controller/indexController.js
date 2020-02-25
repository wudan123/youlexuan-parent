app.controller("indexController",function ($scope,loginService) {

    $scope.getLoginName=function () {
        loginService.getLoginName().success(function (response) {

            $scope.name=response.loginName;// 通过map中的key赋值给name
        })
    }
})