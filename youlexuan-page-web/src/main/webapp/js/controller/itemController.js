//商品详情页
app.controller('itemController',function ($scope,$http) {

    //商品数量的加减
    $scope.addNum=function (x) {
        $scope.num=$scope.num+x;//每次点击加1或加-1
        if ($scope.num<1){
            $scope.num=1;
        }
    }

    //记录用户选择的规格
    $scope.specifications={};

    //用户选择规格
    $scope.selectSpecification=function (name,value) {
        $scope.specifications[name]=value;
        searchSku();//查找到用户选中的sku列表
    }


    //判断某规格选项是否被选中
    $scope.isSelect=function (name,value) {

        if ($scope.specifications[name]==value){
            return true;
        }else{
            return false;
        }

    }

    //默认加载sku
    $scope.loadSku=function () {
        $scope.sku=skuList[0];
        $scope.specifications=JSON.parse(JSON.stringify($scope.sku.spec));//stringify将js对象转化成json字符串
    }

    //匹配两个对象
    matchObject=function (map1,map2) {
        for (var k in map1){
            if (map1[k]!=map2[k]){
                return false;
            }
        }
        for (var k in map2){
            if (map2[k]!=map1[k]){
                return false;
            }
        }

        return true;
    }

    //查找当前用户选择的sku
    searchSku=function () {
        for (var i=0;i<skuList.length;i++){
            if (matchObject(skuList[i].spec,$scope.specifications)){
                $scope.sku=skuList[i];
                return ;
            }
        }
        $scope.sku={id:0,title:'',price:0}//如果没有匹配到
    }


    //添加商品到购物车
    $scope.addCart=function () {
        $http.get('http://localhost:9108/cart/addCartList.do?itemId='+$scope.sku.id+"&num="+$scope.num,{'withCredentials':true}).success(function (response) {
            if (response.success){
                location.href="http://localhost:9108/cart.html";//跳转到购物车页面
            }else {
                alert(response.message);
            }
        })
    }







})