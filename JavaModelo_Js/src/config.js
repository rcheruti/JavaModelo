
var Module = angular.module('JavaModelo',['ng','ui.router']);

  // Configuração dos interceptadores desse módule
Module.config(['$httpProvider','contextProvider',
          //'$compileProvider','$logProvider','HostInterProvider',
        function($httpProvider,contextProvider
          //,$compileProvider,$logProvider,HostInterProvider
            ){

  //$httpProvider.useApplyAsync( true );
  //$compileProvider.debugInfoEnabled( true );
  //$logProvider.debugEnabled( true );

  //HostInterProvider.url = 'http://127.0.0.1:8080';

  // Pegar o context;
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (xhttp.readyState === 4 && xhttp.status === 200) {
      //console.log( 'novo context', xhttp.responseText );
      window.contextRoot = xhttp.responseText;
    }
  };
  xhttp.open("GET", "persistencia/context", false);
  xhttp.send();
  contextProvider.root( window.contextRoot );
  
  $httpProvider.interceptors.push( 'LoginInter' );
  $httpProvider.interceptors.push( 'HostInter' );

  $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

}]);

