
window.Module = angular.module('Module',['ngAnimate','ngTouch','ngRoute','ui.router','JavaModelo']);

Module.config(['contextProvider',
    function(contextProvider){

  contextProvider.root( window.contextRoot );
  contextProvider.services('/s');
  contextProvider.websocket('/websocket');
  
}]);

Module.run(['ResourceService','$window',
    function(ResourceService,$window){
  
  $window.Carro = ResourceService.entidade('Carro');
  $window.Cor = ResourceService.entidade('Cor');
  
}]);
