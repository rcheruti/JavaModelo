Module.provider('LoginInter',['state',function(state){

  var provider = this;
  
  provider.handler = null;
  provider.state = 'login';
  provider.url = '/login.jsp';
  provider.ativo = true;
  provider.ERRORCODE_LOGIN = 401 ;

  provider.$get = ['context','$window',function(context,$window){
    /**
     * Esse interceptador redireciona o usuário para a página de login
     * caso o servidor informe o código de erro de usuário não logado.
     * @type LoginInter
     */

    var ref = {
      response:function( response ){
        if( provider.ativo && response.data && response.data.code === provider.ERRORCODE_LOGIN ){
          if( provider.handler ){
            provider.handler( response );
            //-----------------------------------------------------------------
          }else{
            if( provider.state ){
              state.go( provider.state );
            }else{
              var origin = $window.location.origin ;
              $window.location = origin + context.services + provider.url ;
            }
          }
        }
        return response ;
      }
    };
    return ref;

  }];

}]);