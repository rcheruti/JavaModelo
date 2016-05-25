Module.controller('Menu',['$scope','$http','$window','path',
    function($scope,$http,$window,path){
  
  $scope.logoutMsg = '';
  
  $scope.logout = function(){
    //$http.post( context.services+ '/seguranca/logout').then(function(data){
    $http.post( path('r', '/seguranca/logout') ).then(function(data){
      if( data.data.status ){
        //var url = $window.location.origin + context.root ;
        //$window.location = url;
        $window.location = path();
      }else{
        $scope.logoutMsg = 'Falha no logout.';
      }
    });
  };
  
}]);