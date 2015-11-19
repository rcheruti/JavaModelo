Module.config(['$httpProvider','$compileProvider','$logProvider',
        function($httpProvider,$compileProvider,$logProvider){
    
    $httpProvider.useApplyAsync( true );
    $compileProvider.debugInfoEnabled( true );
    $logProvider.debugEnabled( true );
    
    $httpProvider.interceptadores.push( 'LoginInter' );
    $httpProvider.interceptadores.push( 'NetbeansChromeInter' );
    
}]);