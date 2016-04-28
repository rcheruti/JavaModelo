
var Module = angular.module('JavaModelo',['ng','ui.router']);

var urlContext = 'persistencia/context',
    hiProvider = null,
    ctxProvider = null;
  
Module.run(['$templateCache','$rootElement',
    function($templateCache,$rootElement){
  
  if( !hiProvider.ativo ){
    // Pegar o context;
    var url = urlContext;
    var xhttp = new XMLHttpRequest();
    xhttp.withCredentials = true;
    xhttp.onreadystatechange = function() {
      if (xhttp.readyState === 4 && xhttp.status === 200) {
        //console.log( 'novo context', xhttp.responseText );
        window.contextRoot = xhttp.responseText;
      }
    };
    xhttp.open("GET", url, false);
    xhttp.send();
    ctxProvider.root( window.contextRoot );
  }else{
    var scripts = $rootElement[0].querySelectorAll('script[type="text/ng-template"]');
    var url = hiProvider.url.replace(/\/$/,'');
    for(var i = 0; i < scripts.length; i++){
      var key = scripts[i].id ;
      var nome = url+ '/'+ key.replace(/^\//,'') ;
      $templateCache.put( nome, scripts[i].textContent );
    }
  }
  
  
  
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
  $httpProvider.defaults.withCredentials = true;
  
  
}]);

