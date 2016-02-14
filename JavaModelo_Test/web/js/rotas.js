
Module.config(['$stateProvider','$routeProvider',
    function($stateProvider,$routeProvider){
      
  $routeProvider.otherwise('/carro');
  
  $stateProvider.state('carro',
    { url: '/carro', views:{conteudo:{ templateUrl:'paginas/carro.html' }}}
  ).state('tipo',
    { url: '/tipo', views:{conteudo:{ templateUrl:'paginas/tipo.html' }}}
  );
  
}]);
