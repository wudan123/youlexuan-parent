 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.totalRecode;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){

		var id = $location.search()['id'];
		if (id==null){
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				editor.html($scope.entity.tbGoodsDesc.introduction);

				//entity是复合对象
				$scope.entity.tbGoodsDesc.itemImages=JSON.parse($scope.entity.tbGoodsDesc.itemImages);
				//扩展属性
				$scope.entity.tbGoodsDesc.customAttributeItems=JSON.parse($scope.entity.tbGoodsDesc.customAttributeItems);

				//回显规格选项
				$scope.entity.tbGoodsDesc.specificationItems=JSON.parse($scope.entity.tbGoodsDesc.specificationItems);

				for (var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}
			}
		);				
	}
	//根据规格名称和规格选项返回是否被勾选
	$scope.checkAttributeValue=function(specName,optionName){

		var itemCats=$scope.entity.tbGoodsDesc.specificationItems;
		var object = $scope.searchObjectByKey(itemCats,'attributeName',specName);
		if (object==null){
			return false;
		}else {
			if (object.attributeValue.indexOf(optionName)>=0){//判断object这个数组对象不为空
				return true;
			}else {
				return false;
			}

		}

	}

	
	//保存 
	$scope.save=function(){

        $scope.entity.tbGoodsDesc.introduction=editor.html();//提取文本编辑器的值

		var serviceObject;//服务层对象
        var id=$location.search()['id'];//获取id
		if(id!=null){//如果有ID

			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
				    alert(response.message);

				    $scope.entity={};
				    editor.html('');
					//重新查询 
		        //	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
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
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.totalRecode;//更新总记录数
			}			
		);
	}

	//定义状态
	$scope.status=['未审核','已审核','审核未通过','关闭'];

	//定义分类列表
	$scope.itemCatList=[];

	$scope.findItemCatList=function(){
		itemCatService.findAll().success(function (response) {
			for (var i=0;i<response.length;i++){
				$scope.itemCatList[response[i].id]=response[i].name;
			}
		})
	}




	$scope.add=function () {

		$scope.entity.tbGoodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(function (response) {

			if (response.success){
				$scope.entity={
					tbGoodsDesc:{
						itemImages:[],
						specificationItems:[]
					}
				};//清空
				editor.html('');//清空富文本框

				alert(response.message);

			}else {
				alert(response.message);
			}
		})
	}


	/**
	 * 上传图片
	 */
	$scope.uploadFile=function(){
		uploadService.uploadFile().success(function(response) {
			if(response.success){//如果上传成功，取出url
				$scope.image_entity.url=response.message;//设置文件地址
			}else{
				alert(response.message);
			}
		}).error(function() {
			alert("上传异常");
		});
	};

	$scope.entity={
		tbGoods:{},
		tbGoodsDesc:{
			itemImages:[],
			specificationItems:[]
		}
	};//定义页面实体结构

	//添加图片
	$scope.add_image_entity=function(){
		$scope.entity.tbGoodsDesc.itemImages.push($scope.image_entity);
	}
	//移除图片
	$scope.remove_image_entity=function(index){
		$scope.entity.tbGoodsDesc.itemImages.splice(index,1);
	}

	//读取一级分类
	$scope.readCategoryList=function () {
		itemCatService.findByParentId(0).success(function (response) {

			$scope.selectItemList=response;
		})
	}

	// 根据一级分类id加载二级分类
	$scope.$watch("entity.tbGoods.category1Id",function (newvalue,oldValue) {
		if (newvalue){
			itemCatService.findByParentId(newvalue).success(function (response) {
				$scope.itemList2=response;
			})
		}

	})
	// 根据二级分类id加载三级分类
	$scope.$watch("entity.tbGoods.category2Id",function (newvalue,oldValue) {
		if (newvalue){
			itemCatService.findByParentId(newvalue).success(function (response) {
				$scope.itemList3=response;
			})
		}

	})
	// 根据三级分类id加载模板id
	$scope.$watch("entity.tbGoods.category3Id",function (newvalue,oldValue) {
		if (newvalue){
			itemCatService.findOne(newvalue).success(function (response) {
				$scope.entity.tbGoods.typeTemplateId=response.typeId;
			})
		}

	})

	//根据模板id加载品牌列表
	$scope.$watch("entity.tbGoods.typeTemplateId",function (newvalue,oldValue) {
		if (newvalue){
			typeTemplateService.findOne(newvalue).success(function (response) {
				$scope.typeTemplate=response;

				//加载品牌
                $scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);
             //   $scope.typeTemplate.brandIds=angular.fromJson($scope.typeTemplate.brandIds);

				if ($location.search()['id']==null){
					//加载扩展属性
					$scope.entity.tbGoodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);
				}



			});
			typeTemplateService.findSpecList(newvalue).success(function (response) {
				$scope.specList=response;

			});

		}
	});


	//name 机身内存  value:{8g,16g....}
	$scope.updateSpecAttribute=function ($event,name,value) {
		var object=$scope.searchObjectByKey($scope.entity.tbGoodsDesc.specificationItems,"attributeName",name);

		if(object!=null){//选中复选框
			if ($event.target.checked){
				object.attributeValue.push(value);
			}else {
				//取消
				object.attributeValue.splice(object.attributeValue.indexOf(value),1);
				//选项全部取消，就移除这条记录
				if (object.attributeValue.length==0){
					$scope.entity.tbGoodsDesc.specificationItems.splice
					($scope.entity.tbGoodsDesc.specificationItems.indexOf(object),1);

				}
			}
		}else {
			$scope.entity.tbGoodsDesc.specificationItems.push({"attributeName":name,attributeValue:[value]});
		}

	}

	//创建sku列表
	$scope.createItemList=function () {
		$scope.entity.itemList=[
			{
				spec:{},
				price:0,
				num:999,
				isDefault:'0',
				status:'0'
			}
		];

		var items=$scope.entity.tbGoodsDesc.specificationItems;
		for (var i=0;i<items.length;i++){
			$scope.entity.itemList=addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue);
		}
	}


	addColumn=function (list,columnName,columnValue) {
		var newList=[];
		for (var i=0;i<list.length;i++){
			var oldRow=list[i];

			for (var j=0;j<columnValue.length;j++){
				var newRow=JSON.parse(JSON.stringify(oldRow))// 深克隆
				newRow.spec[columnName]=columnValue[j];
				newList.push(newRow);
			}

		}

		return newList;
		
	}
	//商品是否上架
	$scope.isMarketable=function (isMarketable) {
		goodsService.isMarketable1($scope.selectIds,isMarketable).success(function (response) {
			if (response.success){
				alert("上架成功");
				$scope.reloadList();

			}else {
				alert(response.message);
			}
		})
	}
	













});	