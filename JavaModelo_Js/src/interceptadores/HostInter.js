Module.provider('HostInter',[function(){
      /**
       * Interceptador para usar as ferramentas de desenvolvimento do Chrome
       * @type NetbeansChromeInter
       */

  var provider = this;

  provider.use = false;
  provider.url = '';

  provider.$get = [function(){

    var ref = {
      request:function( request ){
        if( provider.use ){
          request.url = provider.url + request.url ;
        }
        return request;
      }
    };
    return ref;

  }];

}]);