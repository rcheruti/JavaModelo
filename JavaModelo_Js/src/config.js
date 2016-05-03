
var Module = angular.module('JavaModelo',['ng','ui.router']);

var urlContext = '/context',
    pProvider = null,
    hiProvider = null;
  
Module.run(['$templateCache','$rootElement',  'path',
    function($templateCache,$rootElement,  path){
  
  console.log( 'HostInterProvider em .run', hiProvider );
  if( !hiProvider.ativo ){
    // Pegar o context;
    var url = path('p', urlContext );
    var xhttp = new XMLHttpRequest();
    //xhttp.withCredentials = true;
    xhttp.onreadystatechange = function() {
      if (xhttp.readyState === 4 && xhttp.status === 200) {
        pProvider.context = xhttp.responseText;
      }
    };
    xhttp.open("GET", url, false);
    xhttp.send();
  }else{
    var scripts = $rootElement[0].querySelectorAll('script[type="text/ng-template"]');
    var url = hiProvider.url.replace(/\/$/,'');
    for(var i = 0; i < scripts.length; i++){
      var key = scripts[i].id ;
      var nome = url+ '/'+ key.replace(/^\//,'') ;
      $templateCache.put( nome, scripts[i].textContent );
    }
  }
  
  console.log( 'Testando path', path );
  console.log( "path('servico','persistencia')", path('servico','/persistencia') );
  console.log( "path('persistencia','context')", path('persistencia','/context') );
  
  
}]);
  
  // Configuração dos interceptadores desse módule
Module.config(['$httpProvider','HostInterProvider', 'pathProvider',
          //'$compileProvider','$logProvider',
        function($httpProvider,HostInterProvider , pathProvider
          //,$compileProvider,$logProvider
            ){
            
  //$httpProvider.useApplyAsync( true );
  //$compileProvider.debugInfoEnabled( true );
  //$logProvider.debugEnabled( true );
  
  pProvider = pathProvider;
  hiProvider = HostInterProvider;
  
  $httpProvider.interceptors.push( 'LoginInter' );
  $httpProvider.interceptors.push( 'HostInter' );

  $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
  $httpProvider.defaults.withCredentials = true;
  
  
}]);

