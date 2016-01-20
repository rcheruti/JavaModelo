Module.provider('HostInter',[function(){
      /**
       * Interceptador para usar as ferramentas de desenvolvimento do Chrome
       * @type NetbeansChromeInter
       */

  var provider = this;

  provider.use = false;
  provider.url = '';

  provider.$get = ['context',function(context){

    var ref = {
      request:function( request ){
        if( provider.use ){
          request.url = provider.url + context.services + request.url ;
        }
        return request;
      }
    };
    return ref;

  }];

}]);