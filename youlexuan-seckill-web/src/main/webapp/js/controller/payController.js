app.controller('payController',function (payService,$scope,$location) {

    $scope.createNative=function () {
        payService.createNative().success(function (response) {
            $scope.money=(response.total_fee/100).toFixed(2);
            $scope.out_trade_no=response.out_trade_no;
            //二维码
            var qr = new QRious({
                element:document.getElementById('qrious'),
                size:250,
                level:'H',
                value:response.qrcode
            });

            queryStatus(response.out_trade_no);//查询支付状态

        })
    }

    queryStatus=function (out_trade_no) {
        payService.queryStatus(out_trade_no).success(function (response) {
            if (response.success){
                location.href="paysuccess.html#?money="+$scope.money;
            }else {
                if (response.message=="二维码超时"){
                    document.getElementById("timeout").innerHTML="二维码已失效,请刷新页面重新获取";
                    location.href="timeout.html";
                }else {
                    location.href="payfail.html";
                }

            }

        })
    }
    //获取金额
    $scope.getMoney=function(){
        return $location.search()['money'];
    }

})