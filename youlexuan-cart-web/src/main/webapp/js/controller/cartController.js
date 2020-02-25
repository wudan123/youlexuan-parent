app.controller('cartController',function (cartService,$scope) {


    //查询购物车列表
    $scope.findCartList=function () {
        cartService.findCartList().success(function (response) {
            $scope.cartList=response;
            $scope.totalValue=cartService.sum($scope.cartList);//求合计数
        })
    }

    $scope.addGoodsToCartList=function (itemId,num) {
        cartService.addGoodsToCartList(itemId,num).success(function (response) {
            if (response.success){
                $scope.findCartList();//刷新
            }else {
                alert(response.message);
            }

        })
    }

    $scope.findAddressList=function () {
        cartService.findAddressList().success(function (response) {
            $scope.addressList=response;
            //默认地址
            for (var i=0;i<$scope.addressList.length;i++){
                if ($scope.addressList[i].isDefault==1){
                    $scope.address=$scope.addressList[i];
                    break;
                }
            }
        })
    }

    //选择地址
    $scope.selectAddress=function (address) {
        $scope.address=address;
    }

    // 判断是否是当前选择的地址
    $scope.isSelectAddress=function (address) {
        if (address==$scope.address){
            return true;
        }else {
            return false;
        }
    }
    //查询实体
    $scope.findOne=function(id){
        cartService.findOne(id).success(
            function(response){
                $scope.entity= response;
            }
        );
    }

    //保存
    $scope.save=function(){
        var serviceObject;//服务层对象
        if($scope.entity.id!=null){//如果有ID
            serviceObject=cartService.update( $scope.entity ); //修改
        }else{
            serviceObject=cartService.add( $scope.entity  );//增加
        }
        serviceObject.success(
            function(response){
                if(response.success){
                    //重新查询
                    $scope.findAddressList();//重新加载
                }else{
                    alert(response.message);
                }
            }
        );
    }
    //删除
    $scope.dele=function (id) {
        cartService.dele(id).success(function (response) {
            if (response.success){
                $scope.findAddressList();
            }else {
                alert(response.message);
            }
        })

    }


   $scope.order={paymentType:1};

    // 选择支付方式
    $scope.selectPayType=function (pay) {
        $scope.order.paymentType=pay;
    }

    // 提交订单
    $scope.submitOrder=function () {
        $scope.order.receiverAreaName=$scope.address.address;//详细地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机号
        $scope.order.receiver=$scope.address.contact;//收货人
        cartService.submitOrder($scope.order).success(function (response) {
            if (response.success){
                if ($scope.order.paymentType==1){

                    location.href="pay.html";
                }else {
                    location.href="paysuccess.html";
                }

            }else {
                alert(response.message);

            }

        })
    }




})