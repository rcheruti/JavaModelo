Module.config(['$stateProvider',
        function($stateProvider){
    
    $stateProvider.state('nome',{
        url:'/nome',
        views:{
            conteudo:{ templateUrl: '/paginas/nome.html' }
        }
    });
    
}]);