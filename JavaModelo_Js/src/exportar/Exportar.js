(function(){
  
  var $http = null, $q = null ;
  
  
  
//============================================================================
  Module.provider('Entidades',[function(){
    
    var that = this;
    
    this.$get = ['$http','$q',function(inj$http,inj$q){
      
      $http = inj$http;
      $q = inj$q;
    }];
    
  }]);
  
})();