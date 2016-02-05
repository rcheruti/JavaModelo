
insert into seg_usuario( DTYPE, email, nome ) values( 1, 'rafael@someone.com','Rafael' );

insert into seg_credencial( bloqueado, erros, login, renovarSenha, senha, usuario_id ) values( 0, 0, 'rafael', 0, 'ok', 1 );

insert into valor(valor) values(75.50);
insert into valor(valor) values(130.80);

insert into carro(nome,valor_id) values('Volks',1);
insert into carro(nome,valor_id) values('Nissan',2);

insert into cor(nome,rgb,hex) values ('Amarelo','rgb(255,255,0)','#ffff00');
insert into cor(nome,rgb,hex) values ('Vermelho','rgb(255,0,0)','#ff0000');
insert into cor(nome,rgb,hex) values ('Verde','rgb(0,255,0)','#00ff00');
insert into cor(nome,rgb,hex) values ('Azul','rgb(0,0,255)','#0000ff');

insert into ref_carro_cor(carro_id,cor_id) value(1,1);
insert into ref_carro_cor(carro_id,cor_id) value(1,2);
insert into ref_carro_cor(carro_id,cor_id) value(2,3);
insert into ref_carro_cor(carro_id,cor_id) value(2,4);

