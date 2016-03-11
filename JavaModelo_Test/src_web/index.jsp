<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JavaModelo Test</title>
    
    <style>@@cssCritico</style>
    <script>
        window.contextRoot = '${pageContext.servletContext.contextPath}';
    </script>
    
  </head>
  <body ng-app="Module" ng-strict-di="true">
    
    <div class="bodyMargin" ui-view="login">
      
    </div>
    
    <!-- ================================================ -->
    <div id="paginas" style="display:none;">
      @@htmlPaginas
    </div>
    <script>@@jsCritico</script>
    
  </body>
</html>
