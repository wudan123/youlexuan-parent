app.controller('seckillGoodsController',function ($scope,seckillGoodsService,$location,$interval) {

    $scope.findList=function () {
        seckillGoodsService.findList().success(function (response) {

            $scope.list=response;
        })
    }

    $scope.findOneFromRedis=function () {
        seckillGoodsService.findOneFromRedis($location.search()['id']).success(function (response) {
            $scope.entity=response;
            //秒杀倒计时  总秒数(结束时间-当前时间)
            var allSecond=Math.round((new Date($scope.entity.endTime).getTime()-new Date().getTime())/1000);
            time=$interval(function () {
                if (allSecond>0){
                    allSecond=allSecond-1;
                    $scope.timeString=convertTimeString(allSecond);

                }else {
                    $interval.cancel(time);
                    alert("秒杀时间结束!")
                }
            },1000)
        })
    }

    convertTimeString=function(allSecond){
        var days=Math.floor(allSecond/60/60/24);//天数
        var hours=Math.floor((allSecond-days*60*60*24)/60/60);//小时数
        var minutes=Math.floor((allSecond-(days*60*60*24)-(hours*60*60))/60);//分钟数
        var second=Math.floor(allSecond-(days*60*60*24)-(hours*60*60)-(minutes*60));//秒数
        var daytime="";
        if (days>0){
            daytime=days+"天";
        }
        return daytime+hours+"时"+minutes+"分"+second+"秒";

    }

    //点击图片去详情页
    $scope.toItem=function (id) {
        location.href="seckill-item.html#?id="+id;
    }

    $scope.submitOrder=function () {
        seckillGoodsService.submitOrder($scope.entity.id).success(function (response) {
            if (response.success){
                location.href="pay.html";
            }else {
                if (response.message=="用户未登陆"){
                    location.href="login.html";
                }
                alert(response.message);
            }

        })
    }



})