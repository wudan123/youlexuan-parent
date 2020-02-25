app.service('indexService',function ($http) {


    this.getLoginName=function () {
        return $http.get('../login/getLoginName.do');
    }

})