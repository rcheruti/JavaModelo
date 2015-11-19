Module.service('NetbeansChromeInter',[function(){
    /**
     * Interceptador para usar as ferramentas de desenvolvimento do Chrome
     * @type NetbeansChromeInter
     */
    // Endereço do servidor onde os serviços estarão rodando
    var serverHost = 'http://127.0.0.1:8080' + context ;
    
    var ref = {
        request:function( request ){
            if( request.url.indexOf('/') === 0 ){
                request.url = serverHost + request.url ;
            }
            return request;
        }
    };
    return ref;
    
}]);