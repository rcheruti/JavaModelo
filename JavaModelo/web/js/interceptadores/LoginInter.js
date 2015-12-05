Module.provider('LoginInter',[function(){
    
    var provider = this;
    
    provider.url = '/login.jsp';
    
    provider.$get = ['context',function(context){
        /**
         * Esse interceptador redireciona o usuário para a página de login
         * caso o servidor informe o código de erro de usuário não logado.
         * @type LoginInter
         */

        var ref = {
            response:function( reponse ){
                if( reponse.data && reponse.data.code === 401 ){
                    var origin = location.origin ;
                    location = origin + context + provider.url ;
                }
                return response ;
            }
        };
        return ref;

    }];
    
}]);