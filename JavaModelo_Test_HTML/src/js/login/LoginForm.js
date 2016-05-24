Module.controller('LoginForm',['$scope','$http','$timeout','path','$window',
    function($scope,$http,$timeout,path,$window){
  $scope.msg = '';
  $scope.msgClasses = 'fade right';
  var timeOut = null;
  
  
  $scope.cancelar = function(){		 
    $scope.login = '';		
    $scope.senha = '';		 
  };
  $scope.keydown = function( ev ){		 
    if( ev.which === 13 )$scope.entrar();		
  };
  
  
  
  $scope.entrar = function(){
    $http
      .post( path('r','/seguranca/login'), { login: $scope.login, senha: $scope.senha } )
      .then(function(data){
        $timeout.cancel( timeOut );
        data = data.data;
        if(data.status){
          $scope.msg = data.msg ;
          $scope.msgClasses = '';
          timeOut = $timeout(function(){
            $window.location = path();
          }, 1000);
        }else{
          $scope.msgClasses = '';
          $scope.msg = data.msg ;
          timeOut = $timeout(function(){
            $scope.msgClasses = 'fade right';
            timeOut = $timeout(function(){ $scope.msg = ''; },200);
          }, 2500 );
        }
      });
  };
  
  
  
}]);