
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Login</title>
        
        <script src="${pageContext.request.contextPath}/js/libs/angular.min.js" ></script>
        <script src="${pageContext.request.contextPath}/js/libs/angular-animate.min.js" ></script>
        <script src="${pageContext.request.contextPath}/js/libs/angular-touch.min.js" ></script>
        <script src="${pageContext.request.contextPath}/js/libs/angular-locale_pt-br.js" ></script>
        <script>
            window.context = '${pageContext.request.contextPath}';
            window.Module = angular.module('Module',['ngAnimate']);
        </script>
        
        <script src="${pageContext.request.contextPath}/js/config.js" ></script>
        
        <script src="${pageContext.request.contextPath}/js/interceptadores/LoginInter.js" ></script>
        <script src="${pageContext.request.contextPath}/js/interceptadores/NetbeansChromeInter.js" ></script>
        <script src="${pageContext.request.contextPath}/js/controladores/Login.js" ></script>
        
    </head>
    <body ng-app="Module" ng-strict-di="true">
        
        <div class="login" ng-controller="Login">
            
        </div>
        
    </body>
</html>
