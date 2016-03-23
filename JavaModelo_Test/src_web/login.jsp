<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JavaModelo Test: Login</title>
    
    <style>@@cssCritico</style>
    <script>
        window.contextRoot = '${pageContext.servletContext.contextPath}';
    </script>
    
    <script>@@jsCriticoLogin</script>
    <script>
window.Module = angular.module('Module',['ngAnimate','ngTouch','ngRoute','ui.router','JavaModelo']);
Module.config(['contextProvider',
    function(contextProvider){
  
  //EntidadesProvider.defaults.cacheTimeout["POST/tipo"] = 5000 ;
  //UsuarioProvider.carregarAoIniciar = false;
  
  contextProvider.root( window.contextRoot );
  contextProvider.services('/s');
  contextProvider.websocket('/websocket');
  
}]);

Module.controller('LoginForm',['$scope','$http','$timeout','$window','context','$state',
    function($scope,$http,$timeout,$window,context,state){
  $scope.msg = '';
  $scope.msgClasses = 'fade right';
  var timeOut = null;
  $scope.entrar = function(){
    $http
      .post( context.services+ '/seguranca/login', { login: $scope.login, senha: $scope.senha } )
      .then(function(data){
        $timeout.cancel( timeOut );
        data = data.data;
        if(data.status){
          $scope.msg = data.msg ;
          $scope.msgClasses = '';
          timeOut = $timeout(function(){ 
            var url = $window.location.origin + context.root ;
            $window.location = url;
            //state.go('index');
          }, 1000);
        }else{
          $scope.msgClasses = '';
          $scope.msg = data.msg ;
          timeOut = $timeout(function(){
            $scope.msgClasses = 'fade right';
            timeOut = $timeout(function(){ $scope.msg = ''; },200);
          }, 2500 );
        }
      });
  };
}]);
    </script>
  </head>
  <body ng-app="Module" ng-strict-di="true">
    
    
    <div class="loginForm" ng-controller="LoginForm">
      <label>
        <span>Login</span>
        <input ng-model="login"/>
      </label>

      <br/><br/>

      <label>
        <span>Senha</span>
        <input ng-model="senha" type="password"/>
      </label>

      <br/><br/>

      <button type="button" ng-click="entrar()">Log in</button>
      <br/><br/>

      <div class="msg trans" ng-class="msgClasses">{{ msg }}</div>
    </div>
    
  </body>
</html>
