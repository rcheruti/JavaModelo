
window.Module = angular.module('Module',['ngAnimate','ngTouch','ngRoute','ui.router','JavaModelo']);

Module.config(['contextProvider',
    function(contextProvider){

  contextProvider.root( window.contextRoot );
  contextProvider.services('/s');
  contextProvider.websocket('/websocket');
  
}]);

Module.run(['Entidades','$window',
    function(Entidades,$window){
  
  $window.Carro = Entidades.entidade('Carro');
  $window.Cor = Entidades.entidade('Cor');
  
  Entidades.entidade('Porta');
  Entidades.entidade('Usuario');
  Entidades.entidade('Valor');
  
}]);
