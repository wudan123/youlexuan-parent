
app.controller('baseController' ,function($scope){
    //重新加载列表 数据
    $scope.reloadList=function(){
        //切换页码
        $scope.search( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }
    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();//重新加载
        }
    };
    $scope.selectIds=[];//选中的ID集合
    //更新复选
    $scope.updateSelection = function($event, id) {
        if($event.target.checked){//如果是被选中,则增加到数组
            $scope.selectIds.push( id);
        }else{
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除
        }
    }

    //处理列表json数据  提取要显示的属性  拼接显示 用逗号隔开
    $scope.jsonToString=function (jsonString,key) {
        var json=JSON.parse(jsonString);
        var value="";
        for (var i=0;i<json.length;i++){

            if (i>0){
                value+=",";
            }

            value+=json[i][key];

        }
        return value;

    }
//[{"attributeValue":["移动4G","移动3G"],"attributeName":"网络"},{"attributeValue":["16G","32G"],"attributeName":"机身内存"}]
    //从集合中按key查询对象  key:attributeName  keyValue:"网络"
    $scope.searchObjectByKey=function (list,key,keyValue) {
        for (var i=0;i<list.length;i++){
            if (list[i][key]==keyValue){
                return list[i];
            }
        }

        return null;

    }

});