
var Module = angular.module('JavaModelo',['ng']);

  // Configuração dos interceptadores desse módule
Module.config(['$httpProvider',
          //'$compileProvider','$logProvider','HostInterProvider',
        function($httpProvider
          //,$compileProvider,$logProvider,HostInterProvider
            ){
    
    //$httpProvider.useApplyAsync( true );
    //$compileProvider.debugInfoEnabled( true );
    //$logProvider.debugEnabled( true );
    
    //HostInterProvider.url = 'http://127.0.0.1:8080';
    
    $httpProvider.interceptors.push( 'LoginInter' );
    $httpProvider.interceptors.push( 'HostInter' );
    
    $httpProvider.defaults.headers.common['X-Requested-With'] = 'XMLHttpRequest';
    
}]);

