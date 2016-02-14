<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JavaModelo Test</title>
    
    <link href="${pageContext.servletContext.contextPath}/css/index.css" type="text/css" rel="stylesheet"/>
    <link href="${pageContext.servletContext.contextPath}/css/styles.css" type="text/css" rel="stylesheet"/>
    <link href="${pageContext.servletContext.contextPath}/css/informacoes.css" type="text/css" rel="stylesheet"/>
    <link href="${pageContext.servletContext.contextPath}/css/carros.css" type="text/css" rel="stylesheet"/>
    
    <script src="${pageContext.servletContext.contextPath}/js/libs/angular/angular.min.js"></script>
    <script src="${pageContext.servletContext.contextPath}/js/libs/angular/angular-animate.min.js"></script>
    <script src="${pageContext.servletContext.contextPath}/js/libs/angular/angular-route.min.js"></script>
    <script src="${pageContext.servletContext.contextPath}/js/libs/angular/angular-touch.min.js"></script>
    <script src="${pageContext.servletContext.contextPath}/js/libs/angular/angular-ui-router.min.js"></script>
    
    <script src="${pageContext.servletContext.contextPath}/js/libs/JavaModelo.js"></script>
    
    <script src="${pageContext.servletContext.contextPath}/js/config.js"></script>
    <script src="${pageContext.servletContext.contextPath}/js/rotas.js"></script>
    <script src="${pageContext.servletContext.contextPath}/js/informacoes.js"></script>
    
    <script src="${pageContext.servletContext.contextPath}/js/controladores/Menu.js"></script>
    <script src="${pageContext.servletContext.contextPath}/js/controladores/Carro.js"></script>
    <script src="${pageContext.servletContext.contextPath}/js/controladores/Tipo.js"></script>
    
    <script>
        window.contextRoot = '${pageContext.servletContext.contextPath}';
    </script>
    
  </head>
  <body ng-app="Module" ng-strict-di="true">
    
    <div class="bodyMargin">
      
      <div class="menu" ng-controller="Menu">
        <button ng-click="logout()">Logout</button>
        <span>{{ logoutMsg }}</span>
        <a ui-sref="carro">Carros</a>
        <a ui-sref="tipo">Tipos</a>
      </div>

      <h1>Hello World!</h1>
      
      <div ui-view="conteudo"></div>
      
    </div>
    
  </body>
</html>
