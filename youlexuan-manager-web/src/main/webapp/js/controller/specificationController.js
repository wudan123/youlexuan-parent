 //控制层 
app.controller('specificationController' ,function($scope,$controller   ,specificationService){	
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		specificationService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		specificationService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.totalRecode;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(id){				
		specificationService.findOne(id).success(
			function(response){
				$scope.entity= response;					
			}
		);				
	}
	
	//保存 
	$scope.save=function(){
		if($scope.entity.specification.id!=null){//如果有ID
			specificationService.update( $scope.entity ).success(
				function(response){//修改
					if(response.success){
						//重新查询
						$scope.reloadList();//重新加载
					}else{
						alert(response.message);
					}
				}
			)
		}else{
			specificationService.add( $scope.entity  ).success(//增加
				function(response){
					if(response.success){
						//重新查询
						$scope.reloadList();//重新加载
					}else{
						alert(response.message);
					}
				}
			)
		}
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		specificationService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		specificationService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.totalRecode;//更新总记录数
			}			
		);
	}

	//新增规格选项

	//定义变量存放规格选项行
	$scope.entity={optionList:[]};

	//新增一行
	$scope.addRow=function () {
		$scope.entity.optionList.push({});
	}

	//删除一行  index代表当前行下标
	$scope.delRow=function (index) {

		$scope.entity.optionList.splice(index,1);
	}

    
});	