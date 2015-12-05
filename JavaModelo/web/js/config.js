Module.config(['$httpProvider','$compileProvider','$logProvider','HostInterProvider',
        function($httpProvider,$compileProvider,$logProvider,HostInterProvider){
    
    $httpProvider.useApplyAsync( true );
    $compileProvider.debugInfoEnabled( true );
    $logProvider.debugEnabled( true );
    
    HostInterProvider.url = 'http://127.0.0.1:8080';
    
    $httpProvider.interceptadores.push( 'LoginInter' );
    $httpProvider.interceptadores.push( 'HostInter' );
    
}]);