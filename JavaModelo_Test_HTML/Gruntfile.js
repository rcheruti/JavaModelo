
module.exports = function (grunt) {
  
  // caminhos do processo de montagem:
  var p = {
    dist:'dist/',
    src:'src/',
    srcCssC:'src/css/critico/',
    srcCssN:'src/css/normal/',
    srcJsC:'src/js/critico/',
    srcJsN:'src/js/normal/',
    htmlPaginas:'src/paginas/',
    htmlIndex:'src/index.html',
    temp:'temp/',
    test:'test/',
    javaModeloJs:'../JavaModelo_Js/dist/JavaModelo.js',
    serverDir: 'C:/wamp/www-JavaModelo'
  };
  
  
  // nomes finais
  var nf = {
    jsNormal:           p.temp+"jsNormal.js",
    jsCritico:          p.temp+"jsCritico.js",
    jsCriticoLogin:     p.temp+"jsCriticoLogin.js",
    jsNormalLogin:      p.temp+"jsNormalLogin.js",
    cssNormal:          p.temp+"cssNormal.css",
    cssCritico:         p.temp+"cssCritico.css",
    htmlPaginas:        p.temp+"htmlPaginas.html",
    deployMin:          false,
    hostInter:          'http://127.0.0.1:9090/JavaModelo_Test/'
  };
  
  grunt.registerTask('default', ['clean','concat','less',
    'copy:dist','replace','copy:server']);
  grunt.registerTask('montarCompleto',['montar:completo','clean','concat','less',
    'cssmin','uglify','htmlmin',
    'copy:dist','replace','copy:server']);
  
  grunt.registerMultiTask('montar','',function(){
    if( this.data.nf ){
      for(var g in this.data.nf){
        var val = this.data.nf[g];
        nf[g] = val;
      }
    }
  });
  
  //===================  Processo de montagem  ============================
  
  var lf = grunt.util.linefeed;
  
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-htmlmin');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-replace');
  
  if( !p.serverDir ) p.serverDir = 'FAKEDIR_abcdefghijk';
  // Project configuration.
  grunt.initConfig({
    montar:{
      normal:{
        
      },
      completo:{
        nf:{
          cssCritico:       p.temp+"cssCritico.min.css",
          cssNormal:        p.temp+"cssNormal.min.css",
          jsNormal:         p.temp+"jsNormal.min.js",
          jsCritico:        p.temp+"jsCritico.min.js",
          jsCriticoLogin:   p.temp+"jsCriticoLogin.min.js",
          jsNormalLogin:    p.temp+"jsNormalLogin.min.js",
          htmlPaginas:      p.temp+"htmlPaginas.min.html",
          hostInter:        '',
          deployMin:        true
        }
      }
    },
    // ---------------  limpeza
    clean:{
      options:{
        force: true
      },
      temp:{
        src: [ p.temp ]
      },
      dist:{
        src: [ p.dist ]
      },
      server:{
        src: [ p.serverDir? p.serverDir : 'FAKEDIR_abcdefghijk' ]
      }
    },
    // ----------------  juntando os arquivos
    concat:{
      cssCritico:{
        src:[ 
          p.srcCssC+ 'index.less', 
          p.srcCssC+ 'styles.less', 
          p.srcCssC+ 'stylesDefaults.less', 
          p.srcCssC+ '**/*.less' 
        ],
        dest: p.temp+'cssCritico.less'
      },
      cssNormal:{
        src:[ 
          p.srcCssN+ 'index.less', 
          p.srcCssN+ 'styles.less', 
          p.srcCssN+ 'stylesDefaults.less', 
          p.srcCssN+ '**/*.less' 
        ],
        dest: p.temp+'cssNormal.less'
      },
      jsCritico:{
        src:[ 
          p.srcJsC+ 'libs/angular/angular.min.js',
          p.srcJsC+ 'libs/angular/*.js',
          p.srcJsC+ 'libs/*.js',
          p.srcJsC+ 'config.js',
          p.srcJsC+ '**/*.js'
        ],
        dest: p.temp+'jsCritico.js'
      },
      jsCriticoLogin:{
        src:[ 
          p.srcJsC+ 'libs/*.js'
        ],
        dest: p.temp+'jsCriticoLogin.js'
      },
      jsNormalLogin:{
        src:[ 
          p.srcJsN+ 'libs/angular/angular.min.js',
          p.srcJsN+ 'libs/angular/*.js',
          p.srcJsN+ 'libs/*.js',
          p.javaModeloJs ,
          p.srcJsN+ 'config.js',
          p.src+ 'js/login/**'
        ],
        dest: p.temp+'jsNormalLogin.js'
      },
      jsNormal:{
        src:[ 
          p.srcJsN+ 'libs/angular/angular.min.js',
          p.srcJsN+ 'libs/angular/*.js',
          p.srcJsN+ 'libs/*.js',
          p.javaModeloJs ,
          p.srcJsN+ 'config.js',
          p.srcJsN+ '**/*.js' 
        ],
        dest: p.temp+'jsNormal.js'
      },
      html:{
        options:{
          process: function(src, filepath){
            var id = filepath.replace(/.*\/paginas\//i,'');
            return '<script type="text/ng-template" id="'+ id +'">'+ src +'</script>'; 
          }
        },
        files:[{
          src:[ p.htmlPaginas+ '**/*.html' ],
          dest: p.temp+'htmlPaginas.html'
        }]
      }
    },
    
    //-------------  interpretando LESS
    less:{
      cssCritico:{
        files:[{
          src:[ p.temp+'cssCritico.less' ],
          dest: p.temp+'cssCritico.css'  
        }]
      },
      cssNormal:{
        files:[{
          src:[ p.temp+'cssNormal.less' ],
          dest: p.temp+'cssNormal.css'
        }]
      }
    },
    
    //-------------  minimizando os arquivos
    cssmin:{
      cssCritico:{
        files:[{
          src: p.temp+'cssCritico.css' ,
          dest: p.temp+'cssCritico.min.css'
        }]
      },
      cssNormal:{
        files:[{
          src: p.temp+'cssNormal.css' ,
          dest: p.temp+'cssNormal.min.css'
        }]
      }
    },
    uglify:{
      jsCritico:{
        files:[{
          src: p.temp+'jsCritico.js' ,
          dest: p.temp+'jsCritico.min.js'
        }]
      },
      jsCriticoLogin:{
        files:[{
          src: p.temp+'jsCriticoLogin.js' ,
          dest: p.temp+'jsCriticoLogin.min.js'
        }]
      },
      jsNormalLogin:{
        files:[{
          src: p.temp+'jsNormalLogin.js' ,
          dest: p.temp+'jsNormalLogin.min.js'
        }]
      },
      jsNormal:{
        files:[{
          src: p.temp+'jsNormal.js' ,
          dest: p.temp+'jsNormal.min.js'
        }]
      }
    },
    htmlmin:{
      html:{
        options:{
          removeComments: true,
          collapseWhitespace: true,
          processScripts: ['text/ng-template']
          //,maxLineLength: 140
        },
        files:[{
          src: p.temp+'htmlPaginas.html' ,
          dest: p.temp+'htmlPaginas.min.html'
        }]
      }
    },
    
    //-------------  copiando os arquivos finais
    copy:{
      dist:{
        files:[{
          expand: true,
          cwd: p.temp ,
          src:[
            'jsNormalLogin.js',
            'jsNormalLogin.min.js',
            'cssNormal.css',
            'jsNormal.js',
            'cssNormal.min.css',
            'jsNormal.min.js'
          ],
          dest: p.dist,
          filter: function(path){
            return !nf.deployMin || (nf.deployMin && /\.min\./.test(path) );
          }
        }]
      },
      server:{
        files:[{
          expand: true,
          cwd: p.dist ,
          src:[ '**/*' ],
          dest: p.serverDir
        }]
      }
    },
    
    //-------------  replace 
    replace:{
      dist:{
        options:{
          detail: false,
          patterns:[{
            match: 'htmlPaginas',
            replacement: function(){
              return grunt.file.read(nf.htmlPaginas);
            }
          },{
            match: 'cssCritico',
            replacement: function(){
              return grunt.file.read(nf.cssCritico);
            }
          },{
            match: 'cssNormal',
            replacement: function(){
              var arr = nf.cssNormal.split('/');
              return arr[ arr.length - 1 ];
            }
          },{
            match: 'jsCritico',
            replacement: function(){
              return grunt.file.read(nf.jsCritico);
            }
          },{
            match: 'jsNormal',
            replacement: function(){
              var arr = nf.jsNormal.split('/');
              return arr[ arr.length - 1 ];
            }
          },{
            match: 'jsCriticoLogin',
            replacement: function(){
              return grunt.file.read(nf.jsCriticoLogin);
            }
          },{
            match: 'jsNormalLogin',
            replacement: function(){
              var arr = nf.jsNormalLogin.split('/');
              return arr[ arr.length - 1 ];
            }
          },{
            match: 'hostInter',
            replacement: function(){
              return nf.hostInter;
            }
          }]
        },
        files:[{
          expand:true,
          flatten:true,
          src: [p.src+ 'index.html', p.src+ 'login.html'],
          dest: p.dist 
        },{
          expand:true,
          flatten:true,
          src: [ p.dist+ '**/*.*' ],
          dest: p.dist 
        }]
      }
    }
    
    
  });
  
  
};
