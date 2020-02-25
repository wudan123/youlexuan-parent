app.service('cartService',function ($http) {

    this.findCartList=function () {
        return $http.get('../cart/findCartList.do');
    }

    this.addGoodsToCartList=function (itemId,num) {
        return $http.get('../cart/addCartList.do?itemId='+itemId+"&num="+num);
    }
    //求合计
    this.sum=function(cartList){
        var totalValue={totalNum:0, totalMoney:0.00 };//合计实体
        for(var i=0;i<cartList.length;i++){
            var cart=cartList[i];
            for(var j=0;j<cart.orderItemList.length;j++){
                var orderItem=cart.orderItemList[j];//购物车明细
                totalValue.totalNum+=orderItem.num;
                totalValue.totalMoney+= orderItem.totalFee;
            }
        }
        return totalValue;
    }

    // 根据用户id获取地址列表
    this.findAddressList=function () {
        return $http.get('../address/findAddressListByUserId.do');
    }

    //增加
    this.add=function(entity){
        return  $http.post('../address/add.do',entity );
    }
    //修改
    this.update=function(entity){
        return  $http.post('../address/update.do',entity );
    }
    //查询实体
    this.findOne=function(id){
        return $http.get('../address/findOne.do?id='+id);
    }
    //删除
    this.dele=function(id){
        return $http.get('../address/delete.do?id='+id);
    }

    //提交订单
    this.submitOrder=function (order) {
        return $http.post('../order/add.do',order);
    }
})