
window.Module = angular.module('Module',['ngAnimate','ngTouch','ngRoute','ui.router','JavaModelo']);

Module.config(['contextProvider','UsuarioProvider','EntidadesProvider',
    function(contextProvider, UsuarioProvider,EntidadesProvider){
  
  //EntidadesProvider.defaults.cacheTimeout["POST/tipo"] = 5000 ;
  //UsuarioProvider.carregarAoIniciar = false;
  
  contextProvider.root( window.contextRoot );
  contextProvider.services('/s');
  contextProvider.websocket('/websocket');
  
}]);

Module.run(['Entidades','$window','Usuario','$q',
    function(Entidades,$window,Usuario, $q){
  
  $window.Carro = Entidades.entidade('Carro');
  $window.Cor = Entidades.entidade('Cor');
  
  Entidades.entidade('Porta');
  Entidades.entidade('Usuario'); 
  Entidades.entidade('Valor');
  Entidades.entidade('RegistroUsuario');
  
  Usuario.then(function(u){
    console.log('Usuario::: ', u);
  });
  
  
}]);
