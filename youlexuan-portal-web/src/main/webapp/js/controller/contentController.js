app.controller('contentController',function ($scope,contentService) {



    $scope.contentList=[];//创建轮播图列表
    //搜索广告列表  轮播图
    $scope.findContentList=function (categoryId) {

        contentService.findByCategoryId(categoryId).success(function (response) {
            $scope.contentList[categoryId]=response;
        })

    }

    //首页搜索跳转到搜索页
    $scope.search=function () {
        location.href="http://localhost:9105/search.html#?keywords="+$scope.keywords;
    }

    //秒杀页
    $scope.toSeckill=function () {
        location.href="http://localhost:9109/seckill-index.html";
    }

})