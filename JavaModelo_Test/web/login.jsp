<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JavaModelo Test: Login</title>
    
    <link href="${pageContext.servletContext.contextPath}/css/index.css" type="text/css" rel="stylesheet"/>
    <link href="${pageContext.servletContext.contextPath}/css/login.css" type="text/css" rel="stylesheet"/>
    
    <script src="${pageContext.servletContext.contextPath}/libs/angular/angular.min.js"></script>
    <script src="${pageContext.servletContext.contextPath}/libs/angular/angular-animate.min.js"></script>
    <script src="${pageContext.servletContext.contextPath}/libs/angular/angular-route.min.js"></script>
    <script src="${pageContext.servletContext.contextPath}/libs/angular/angular-touch.min.js"></script>
    <script src="${pageContext.servletContext.contextPath}/libs/angular/angular-ui-router.min.js"></script>
    
    <script src="${pageContext.servletContext.contextPath}/libs/JavaModelo.js"></script>
    
    <script src="${pageContext.servletContext.contextPath}/config.js"></script>
    
    <script>
      Module.controller('LoginForm',['$scope',
          function($scope){
          
      }]);
    </script>
    
  </head>
  <body ng-app="Module" ng-strict-di="true">
    
    <form class="loginForm" ng-controller="LoginForm">
      <label>
        <span>Login</span>
        <input ng-model="login"/>
      </label>
      <label>
        <span>Senha</span>
        <input ng-model="senha" type="password"/>
      </label>
    </form>
    
  </body>
</html>
