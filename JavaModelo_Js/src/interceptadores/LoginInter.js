Module.provider('LoginInter',[function(){

  var provider = this;

  provider.url = '/login.jsp';
  provider.ERRORCODE_LOGIN = 401 ;

  provider.$get = ['context','$window',function(context,$window){
    /**
     * Esse interceptador redireciona o usuário para a página de login
     * caso o servidor informe o código de erro de usuário não logado.
     * @type LoginInter
     */

    var ref = {
      response:function( response ){
        if( response.data && response.data.code === provider.ERRORCODE_LOGIN ){
          var origin = $window.location.origin ;
          $window.location = origin + context + provider.url ;
        }
        return response ;
      }
    };
    return ref;

  }];

}]);