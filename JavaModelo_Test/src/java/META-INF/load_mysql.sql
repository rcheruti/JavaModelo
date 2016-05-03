
insert into seg_usuario( DTYPE, email, nome ) values( 1, 'rafael@someone.com','Rafael' );
insert into seg_usuario( DTYPE, email, nome ) values( 1, 'alguem@someone.com','Alguém' );
insert into seg_usuario( DTYPE, email, nome ) values( 1, 'rosana@someone.com','Rosana' );
insert into seg_usuario( DTYPE, email, nome ) values( 1, 'joao.bloqueado@someone.com','João Bloqueado' );

insert into seg_credencial( bloqueado, erros, login, renovarSenha, usuario_id, senha ) values( 0, 0, 'rafael', 0, 1, 'ok' );
insert into seg_credencial( bloqueado, erros, login, renovarSenha, usuario_id, senha ) values( 0, 0, 'alguem', 0, 2, 'ok' );
insert into seg_credencial( bloqueado, erros, login, renovarSenha, usuario_id, senha ) values( 0, 0, 'rosana', 0, 3, 'ok' );
insert into seg_credencial( bloqueado, erros, login, renovarSenha, usuario_id, senha ) values( 1, 0, 'joao',   0, 4, 'ok' );

insert into seg_grupo(chave,nome) values('admin','Admin');
insert into seg_grupo(chave,nome) values('usuario1','Usuários 1');
insert into seg_fk_credencial_grupo(credenciais_id, grupos_id) values(1,1), (2,2), (3,2), (4,2);

insert into seg_permissao( nome ) values( 'url_tipo' );
insert into seg_permissao( nome ) values( 'registro_usuario' );
insert into seg_permissao( nome ) values( 'ver_registroUsuario' );
insert into seg_fk_credencial_permissao( credenciais_id, permissoes_id ) values( 1, 1 );

insert into seg_fk_grupo_permissao(grupos_id, permissoes_id) values(1,2);
insert into seg_fk_grupo_permissao(grupos_id, permissoes_id) values(1,3);
insert into seg_fk_grupo_permissao(grupos_id, permissoes_id) values(3,2);







insert into Carro(nome, atualizacao, usuario_id) values('Volks', '2016-02-15', 1);
insert into Carro(nome, atualizacao, usuario_id) values('Nissan', '2016-02-27', 2);

insert into Porta(cor,carro_id) values('Vermelho Seco', 1);
insert into Porta(cor,carro_id) values('Laranja amarelado', 1);
insert into Porta(cor,carro_id) values('Rosa purpurina', 2);

insert into Janela(eletrica, porta_id) values(1, 1);
insert into Janela(eletrica, porta_id) values(0, 2);
insert into Janela(eletrica, porta_id) values(1, 3);

insert into Valor(valor,carro_id) values(75.50,1);
insert into Valor(valor,carro_id) values(130.80,2);

insert into Cor(nome,rgb,hex) values ('Amarelo','rgb(255,255,0)','#ffff00');
insert into Cor(nome,rgb,hex) values ('Vermelho','rgb(255,0,0)','#ff0000');
insert into Cor(nome,rgb,hex) values ('Verde','rgb(0,255,0)','#00ff00');
insert into Cor(nome,rgb,hex) values ('Azul','rgb(0,0,255)','#0000ff');

insert into ref_carro_cor(carro_id,cor_id) value(1,1);
insert into ref_carro_cor(carro_id,cor_id) value(1,2);
insert into ref_carro_cor(carro_id,cor_id) value(2,3);
insert into ref_carro_cor(carro_id,cor_id) value(2,4);

insert into MensagemCorreio(usuario_id,atualizacao,mensagem) values(1,null,'rafael mensagem 1');
insert into MensagemCorreio(usuario_id,atualizacao,mensagem) values(1,null,'rafael mensagem 2');
insert into MensagemCorreio(usuario_id,atualizacao,mensagem) values(2,null,'alguem mensagem 1');
insert into MensagemCorreio(usuario_id,atualizacao,mensagem) values(2,null,'alguem mensagem 2');
insert into MensagemCorreio(usuario_id,atualizacao,mensagem) values(2,null,'alguem mensagem 3');
insert into MensagemCorreio(usuario_id,atualizacao,mensagem) values(3,null,'rosana mensagem 1');
insert into MensagemCorreio(usuario_id,atualizacao,mensagem) values(3,null,'rosana mensagem 2');
insert into MensagemCorreio(usuario_id,atualizacao,mensagem) values(3,null,'rosana mensagem 3');
insert into MensagemCorreio(usuario_id,atualizacao,mensagem) values(3,null,'rosana mensagem 4');


