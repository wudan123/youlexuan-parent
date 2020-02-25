app.controller('searchController',function ($scope,$location,searchService) {

    //搜索对象
    $scope.searchMap={
        'keywords':'',
        'category':'',
        'brand':'',
        'spec':{},
        'price':'',
        'pageNo':1,
        'pageSize':10,
        'sort':'',       //排序ASC 或DESC
        'sortField':''   //排序字段
    }

    //接收首页搜索条件
    $scope.loadKeywords=function(){
        $scope.searchMap.keywords = $location.search()['keywords'];//获取关键字
        $scope.searchItemList();

    }


    //排序规则
    $scope.sortSearch=function(sort,sortField){
        $scope.searchMap.sort=sort;
        $scope.searchMap.sortField=sortField;
        $scope.searchItemList();//调用搜索

    }

    //判断搜索条件是不是品牌
    $scope.keywordsIsBrand=function(){
        for (var i=0;i<$scope.resultMap.brandList.length;i++){
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                return true;
            }
        }
        return false;
    }



    //当前页是否是第一页
    $scope.isStartPage=function(){
        if ($scope.searchMap.pageNo==1){
            return true;
        }else {
            return false;
        }
    }

    //当前页是否是最后一页
    $scope.isEndPage=function(){
        if ($scope.searchMap.pageNo==resultMap.totalPage){
            return true;
        }else {
            return false;
        }
    }

    //判断是否是当前页
    $scope.isPageNo=function(p){

        if ($scope.searchMap.pageNo==p){
            return true;
        }else {
            return false;
        }
    }

    //根据页码查询
    $scope.searchByPage=function(pageNo){
        if (pageNo<1 || pageNo>$scope.resultMap.totalPage){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.searchItemList();
    }

    // 构建分页标签
    buildPageLable=function(){
        $scope.pageLabel=[];//定义分页属性
        var maxPage=$scope.resultMap.totalPage;//最大页码
        var startPage=1;//开始页码
        var lastPage=maxPage;//截止页
        $scope.startDot=true;//前面有点
        $scope.lastDot=true;//后面有点
        if ($scope.resultMap.totalPage>5){//如果总页数大于5，只显示部分页码
            if ($scope.searchMap.pageNo<=3){
                lastPage=5;//截止页为5，前5页
                $scope.startDot=false;//前面没点
            }else if ($scope.searchMap.pageNo>=$scope.resultMap.totalPage-2){//当前页大于等于最大页码-2
                startPage=$scope.resultMap.totalPage-4;//显示后5页
                $scope.lastDot=false;//后面没点
            }else {//以当前页为中心
                startPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
            }

        }else {//总页数小于等于5
            $scope.startDot=false;//前面没点
            $scope.lastDot=false;//后面没点
        }
        //循环生成页码标签
        for (var i=startPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }

    }

    //添加搜索条件
    $scope.addSearchItem=function(key,value){
        $scope.searchMap.pageNo=1;//每次添加条件初始化当前页

        if (key=='category' || key=='brand'|| key=='price'){
            $scope.searchMap[key]=value;
        }else {
            $scope.searchMap.spec[key]=value;
        }
        $scope.searchItemList();//自动提交查询
    }

    //撤销搜索条件
    $scope.removeSearchItem=function(key){
        $scope.searchMap.pageNo=1;//每次撤销条件初始化当前页

        if (key=='category' || key=='brand' || key=='price'){
            $scope.searchMap[key]='';
        }else {
            delete $scope.searchMap.spec[key];//移除属性
        }
        $scope.searchItemList();//自动提交查询
    }


    // 搜索
    $scope.searchItemList=function () {
        //前端传入的pageNo转化为int类型
        $scope.searchMap.pageNo=parseInt($scope.searchMap.pageNo);

        searchService.searchItem($scope.searchMap).success(function (response) {
            $scope.resultMap=response;
            buildPageLable();//分页查询
        })
    }
})