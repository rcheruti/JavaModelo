
insert into seg_usuario( DTYPE, email, nome ) values( 1, 'rafael@someone.com','Rafael' );
insert into seg_usuario( DTYPE, email, nome ) values( 1, 'rafael@someone.com','Rafael' );

insert into seg_credencial( bloqueado, erros, login, renovarSenha, senha, usuario_id ) values( 0, 0, 'rafael', 0, 'ok', 1 );
insert into seg_credencial( bloqueado, erros, login, renovarSenha, senha, usuario_id ) values( 0, 0, 'alguem', 0, 'ok', 2 );

insert into seg_permissao( nome ) values( 'url_tipo' );
insert into seg_fk_credencial_permissao( credenciais_id, permissoes_id ) values( 1, 1 );


insert into carro(nome) values('Volks');
insert into carro(nome) values('Nissan');

insert into valor(valor,carro_id) values(75.50,1);
insert into valor(valor,carro_id) values(130.80,2);

insert into cor(nome,rgb,hex) values ('Amarelo','rgb(255,255,0)','#ffff00');
insert into cor(nome,rgb,hex) values ('Vermelho','rgb(255,0,0)','#ff0000');
insert into cor(nome,rgb,hex) values ('Verde','rgb(0,255,0)','#00ff00');
insert into cor(nome,rgb,hex) values ('Azul','rgb(0,0,255)','#0000ff');

insert into ref_carro_cor(carro_id,cor_id) value(1,1);
insert into ref_carro_cor(carro_id,cor_id) value(1,2);
insert into ref_carro_cor(carro_id,cor_id) value(2,3);
insert into ref_carro_cor(carro_id,cor_id) value(2,4);

