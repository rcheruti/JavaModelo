Module.controller('LoginForm',['$scope','$http','$timeout','$window','path','state',
    function($scope,$http,$timeout,$window,path,state){
  $scope.msg = '';
  $scope.msgClasses = 'fade right';
  var timeOut = null;
  $scope.entrar = function(){
    $http
      //.post( context.services+ '/seguranca/login', { login: $scope.login, senha: $scope.senha } )
      .post( path('s','/seguranca/login'), { login: $scope.login, senha: $scope.senha } )
      .then(function(data){
        $timeout.cancel( timeOut );
        data = data.data;
        if(data.status){
          $scope.msg = data.msg ;
          $scope.msgClasses = '';
          timeOut = $timeout(function(){ 
            //var url = $window.location.origin + context.root ;
            //$window.location = url;
            state.go('index');
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