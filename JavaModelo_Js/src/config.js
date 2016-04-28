
var Module = angular.module('JavaModelo',['ng','ui.router']);

var urlContext = 'persistencia/context',
    hiProvider = null,
    ctxProvider = null;
  
Module.run([function(){
  
  if( hiProvider.ativo ) return;
  
  // Pegar o context;
  var url = urlContext;
  var xhttp = new XMLHttpRequest();
  xhttp.onreadystatechange = function() {
    if (xhttp.readyState === 4 && xhttp.status === 200) {
      //console.log( 'novo context', xhttp.responseText );
      window.contextRoot = xhttp.responseText;
    }
  };
  xhttp.open("GET", url, false);
  xhttp.send();
  ctxProvider.root( window.contextRoot );
  
}]);
  
  // Configuração dos interceptadores desse módule
Module.config(['$httpProvider','contextProvider','HostInterProvider',
          //'$compileProvider','$logProvider',
        function($httpProvider,contextProvider, HostInterProvider
          //,$compileProvider,$logProvider
            ){

  //$httpProvider.useApplyAsync( true );
  //$compileProvider.debugInfoEnabled( true );
  //$logProvider.debugEnabled( true );
  
  //HostInterProvider.ativo = true;
  //HostInterProvider.url = 'http://127.0.0.1:8080';
  
  ctxProvider = contextProvider;
  hiProvider = HostInterProvider;
  
  $httpProvider.interceptors.push( 'LoginInter' );
  $httpProvider.interceptors.push( 'HostInter' );

  $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';

}]);

