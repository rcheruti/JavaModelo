
Module.config(['$stateProvider','$routeProvider',
    function($stateProvider,$routeProvider){
      
  $routeProvider.otherwise('/carro');
  
  $stateProvider.state('carro',
    { url: '/carro', views:{conteudo:{ templateUrl:'paginas/carro.html' }}}
  );
  
}]);
