
window.Module = angular.module('Module',['ngAnimate','ngTouch','ngRoute','ui.router','JavaModelo']);

Module.config(['pathProvider','HostInterProvider',
    function(pathProvider, HostInterProvider ){
  
  //UsuarioProvider.carregarAoIniciar = false;
  
  HostInterProvider.url = '@@hostInter'; // trocado por "grunt:replace"
  HostInterProvider.ativo = HostInterProvider.url? true : false;
  
  pathProvider.servico = '/s';
  pathProvider.websocket = '/websocket';
  
}]);
