app.service("loginService",function ($http) {
    this.findName=function () {
        return $http.get('../login/getName.do');
    }
})