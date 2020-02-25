app.controller('indexController',function (indexService,$scope) {

    $scope.getLoginName=function () {
        indexService.getLoginName().success(function (respones) {
            $scope.loginName=respones.loginName;
        })
    }
})