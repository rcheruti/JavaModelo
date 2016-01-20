Module.provider('ResourceService', [
  function () {

    var provider = this;

    provider.defaults = {
      size: 20,
      page: 0,
      url: null //'/s/persistence/'
    };

    this.$get = ['$http', '$q', 'context', 'entidades',
      function ($http, $q, context, entidades) {

        if (provider.defaults.url === null) {
          provider.defaults.url = context.services + '/persistencia';
        }

        function constantsConstructor() {
          this.equal = '=';
          this.notEqual = '!=';
          this.lowerThanOrEqualTo = '<=';
          this.greaterThanOrEqualTo = '>=';
          this.lowerThan = '<';
          this.greaterThan = '>';
          this.notLike = 'notlike';
          this.like = 'like';

          this.size = provider.defaults.size;
          this.page = provider.defaults.page;
          this.url = provider.defaults.url;
        }

        function EntityConstructor(nome, config) {
          config = config||{};
          this._entidadeNome = nome;
          
          this.size = config.size||this.size;
          this.page = config.page||this.page;
          this.url = config.url|| (this.url+'/'+this._entidadeNome);
        }
        EntityConstructor.prototype = new constantsConstructor();
        var proto = EntityConstructor.prototype;
        proto.query = function (config) {
          config = angular.extend( {}, this, config ); 
          var query = new QueryConstructor( config );

          //query.size(config.size);
          //query.page(config.page);
          if (config.url)
            query._url = config.url;
          else
            query._url += this._entidadeNome;

          var entidades = config.entidades;
          if (entidades instanceof Array) {
            for (var g in entidades) {
              query.param(g, this.equal, entidades[g]);
            }
          } else {
            for (var g in config) {
              //if (g === 'size' || g === 'page' || g === 'url')
              if( g in this ) continue;
              query.param(g, this.equal, config[g]);
            }
          }
          return query;
        };

        function QueryConstructor(config) {
          if( !config ) config = {};
          this._size = config.size || 0;
          this._page = config.page || 0;
          this._order = [];
          this._join = [];
          this._url = config.url || '';
          this._param = [];
          //this.config = config;
        }
        var proto = QueryConstructor.prototype; 
        proto.order = function (vals) {
          if (vals instanceof Array) {
            for (var g in vals) {
              var val = vals[g];
              if (typeof val === 'string')
                this._order.push(arguments[g]);
            }
          } else if (typeof vals === 'string') {
            this._order.push(vals);
          }
          return this;
        };
        proto.size = function (val) {
          if (typeof val === 'number')
            this._size = val;
          return this;
        };
        proto.page = function (val) {
          if (typeof val === 'number')
            this._page = val;
          return this;
        };
        proto.join = function (vals) {
          if (vals instanceof Array) {
            for (var g in vals) {
              var val = vals[g];
              if (typeof val === 'string')
                this._join.push(arguments[g]);
            }
          } else if (typeof vals === 'string') {
            this._join.push(vals);
          }
          return this;
        };
        proto.param = function (paramOrNome, comp, val, logicOp, quoteVal) {
          if (!paramOrNome)
            return null;
          if (!logicOp)
            logicOp = '&'; // Padão para E se for falso
          else if (logicOp !== '&' && logicOp !== '|')
            logicOp = '|'; // Padão para OU se for verdadeiro
          if (quoteVal === undefined) { // se não for definido verificaremos o valor para tentar sempre usar as aspas
            if (val.indexOf('"') !== 0 && val.indexOf("'") !== 0)
              val = '"' + val;
            if (val.lastIndexOf('"') !== val.length - 1 && val.lastIndexOf("'") !== val.length - 1)
              val = val + '"';
          } else if (quoteVal) {
            val = '"' + val + '"';
          }
          this._param.push(paramOrNome + ' ' + comp + ' ' + val + logicOp);
          return this;
        };
        proto.get = function (params) {

          // Montar Query String:
          //this.param(params);
          var queryStr = '';
          for (var g in this._param) {
            queryStr += this._param[g];
          }
          queryStr = queryStr.replace(/[&\|\s]+$/, '');
          if (queryStr) queryStr = '?' + queryStr;

          // Montar Matrix Params:
          var matrix = [
            'size=' + this._size,
            'page=' + this._page
          ];
          if (this._join.length > 0)
            matrix.push('join=' + this._join.join(','));
          if (this._order.length > 0)
            matrix.push('order=' + this._order.join(','));
          matrix = matrix.join(';');
          if (matrix)
            matrix = ';' + matrix;

          // Buscar:
          return $http.get(this._url + matrix + queryStr);
        };

        var ref = {
          entidade: function (nome, config) {
            entidades[nome] = new EntityConstructor(nome, config);
            return entidades[nome];
          }
        };
        return ref;
      }];

  }]);