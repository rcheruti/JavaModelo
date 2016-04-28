
window.Module = angular.module('Module',['ngAnimate','ngTouch','ngRoute','ui.router','JavaModelo']);

Module.config(['contextProvider','HostInterProvider',
    function(contextProvider, HostInterProvider ){
  
  //EntidadesProvider.defaults.cacheTimeout["POST/tipo"] = 5000 ;
  //UsuarioProvider.carregarAoIniciar = false;
  
  HostInterProvider.ativo = true;
  HostInterProvider.url = 'http://127.0.0.1:9090/JavaModelo_Test/';
  
  contextProvider.services('/s');
  contextProvider.websocket('/websocket');
  
}]);
