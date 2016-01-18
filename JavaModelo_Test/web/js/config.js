
window.Module = angular.module('Module',['ngAnimate','ngTouch','ngRoute','ui.router','JavaModelo']);

Module.config(['contextProvider',
    function(contextProvider){
  
  contextProvider.context('/s');
  
}]);
