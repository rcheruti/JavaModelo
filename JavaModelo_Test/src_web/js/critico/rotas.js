
Module.config(['$stateProvider','$routeProvider',
    function($stateProvider,$routeProvider){
      
  $routeProvider.otherwise('/carro');
  
  $stateProvider.state('index',
    { url: '/index', views:{login:{ templateUrl:'paginas/index.html' }}}
  ).state('login',
    { url: '/login', views:{login:{ templateUrl:'paginas/login.html' }}}
  ).state('carro',
    { url: '/carro', views:{conteudo:{ templateUrl:'paginas/carro.html' }}}
  ).state('tipo',
    { url: '/tipo', views:{conteudo:{ templateUrl:'paginas/tipo.html' }}}
  ).state('tipoMany',
    { url: '/tipoMany', views:{conteudo:{ templateUrl:'paginas/tipoMany.html' }}}
  );
  
}]);
