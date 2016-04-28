
window.Module = angular.module('Module',['ngAnimate','ngTouch','ngRoute','ui.router','JavaModelo']);

Module.config(['contextProvider','HostInterProvider',
    function(contextProvider, HostInterProvider ){
  
  //EntidadesProvider.defaults.cacheTimeout["POST/tipo"] = 5000 ;
  //UsuarioProvider.carregarAoIniciar = false;
  
  HostInterProvider.ativo = !!'@@hostInter'; // trocado por "grunt:replace"
  HostInterProvider.url = '@@hostInter'; // trocado por "grunt:replace"
  
  contextProvider.services('/s');
  contextProvider.websocket('/websocket');
  
}]);
