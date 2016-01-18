
Module.config(['$stateProvider','$routeProvider',
    function($stateProvider,$routeProvider){
  
  $routeProvider.otherwise('/');
  
  $stateProvider.state('',{
    url: '',
    views:{ conteudo:{ templateUrl:'' } }
  });
  
}]);
