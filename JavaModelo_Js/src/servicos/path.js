(function(){
  
  Module.provider('path',[function(){
    var that = this;
    
    that.protocol = '';
    that.host = '';
    that.port = '';
    that.context = '';
    
    that.hasHost = false;
    
    that.root = '/';
    that.servico = '/';
    that.websocket = '/';
    that.persistencia = '/persistencia';
    
    
    this.$get = ['$window',function($window){
      // tratamentos do HOST:
      var wl = $window.location;
      var host = that.host;
      var hostParsed = null;
      if( that.host ){
        hostParsed = /(\w+:\/+)?([^\/:]+)(:\d+)?(\/.*)?/i.exec( host );
        that.hasHost = true;
      }else{
        hostParsed = [
          wl+'',
          wl.protocol+'//',
          wl.host.replace(/:.*/,''),
          wl.port,
          wl.pathname
        ];
      }
      
      if(!that.protocol) that.protocol = hostParsed[1] || 'http://';
      if(that.hasHost || !that.host) that.host = hostParsed[2];
      
      if(!that.port) that.port = parseInt(hostParsed[3].replace(':','')) || 80;
      else that.port = parseInt(that.port);
      
      if(!that.context) that.context = hostParsed[4];
      
      // tratamentos dos nomes padrão:
      that.r = that.root;
      that.s = that.servico;
      that.ws = that.websocket;
      that.p = that.persistencia;
      
      //console.log( 'path:this,pathProvider:this', this, that );
      
      
      var pathFunc = function( pathName, path, fullPath ){
        if( !pathName ) pathName = 'r';
        if( !path ) path = '';
        var str = (that.context+that[pathName] + path).replace(/\/+/g,'/') ;
        if( that.hasHost || fullPath ){
          str = that.protocol + (that.host+':'+that.port+str).replace(/\/+/g,'/');
        }
        return str;
      };
      
      pathFunc.get = function( pathName ){
        return that[pathName];
      };
      pathFunc.hasHost = function(  ){
        return that.hasHost;
      };
      
      return pathFunc;
    }];
    
  }]);
  
})();