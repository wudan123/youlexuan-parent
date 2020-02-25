app.controller("indexController",function ($scope,loginService) {

    $scope.showName=function () {
        loginService.findName().success(function (response) {
            $scope.name=response.loginName;
        })
    }

})