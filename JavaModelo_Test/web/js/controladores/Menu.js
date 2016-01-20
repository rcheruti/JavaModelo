Module.controller('Menu',['$scope','$http','$window','context',
    function($scope,$http,$window,context){
  
  $scope.logoutMsg = '';
  
  $scope.logout = function(){
    $http.post( context.services+ '/seguranca/logout').then(function(data){
      if( data.data.status ){
        var url = $window.location.origin + context.root ;
        $window.location = url;
      }else{
        $scope.logoutMsg = 'Falha no logout.';
      }
    });
  };
  
}]);