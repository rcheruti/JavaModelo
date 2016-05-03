
Module.config(['$stateProvider','$routeProvider',
    function($stateProvider,$routeProvider){
      
  $routeProvider.otherwise('/#/carro');
  
  $stateProvider.state('index',
    { url: '/index', views:{login:{ templateUrl:'index.html' }}}
  ).state('login',
    { url: '/login', views:{login:{ templateUrl:'login.html' }}}
  ).state('carro',
    { url: '/carro', views:{conteudo:{ templateUrl:'carro.html' }}}
  ).state('tipo',
    { url: '/tipo', views:{conteudo:{ templateUrl:'tipo.html' }}}
  ).state('tipoMany',
    { url: '/tipoMany', views:{conteudo:{ templateUrl:'tipoMany.html' }}}
  ).state('janela',
    { url: '/janela', views:{conteudo:{ templateUrl:'janela.html' }}}
  );
  
}]);
