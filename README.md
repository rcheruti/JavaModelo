
# Persistência (impl. de Servidor em Java)




# Interface do servidor:

## Buscas:

#### Contexto do sistema:
A URL `/persistencia/context` retorna o nome do contexto do sistema.

Ex.: **"/Aplicacao1"** para a URL **"https://www.empresa.com.br/Aplicacao1/persistencia/context"**

#### URL de buscas:
A URL `/persistencia` é o endereço para onde enviamos as mensagens com o JSON de busca.

O servidor pode receber um único objeto JSON com as informações de uma busca, ou pode
ser enviado um vetor JavaScript com vários objetos de busca dentro.

As busca serão executadas na ordem que forem declaradas no vetor de buscas.

Ex.: `{ /* infos da busca */ }` ou
```
[ 
  { /* infos da busca 1 */ } ,
  { /* infos da busca 2 */ } ,
  { /* infos da busca 3 */ } ,
  ...
]
```

#### JSON de busca:
A Classe que guarda essas informações é a `...utils.BuscaInfo`,
nessa classe estão as constantes de **Ação** e alguns outros atributos
para facilitar a programação de código para o sistema.

```
{
  entidade: "Carro",              // Nome da entidade que se refere esta busca
  page: 2,                        // Página que retornará na resposta (iniciando em zero)
  size: 50,                       // Quantidade de itens que devem ser considerados para cada página (no máximo)
  join: ["dono","motor"],         // Vetor com nome de atributos que devem ser retornados na 
                                  //   resposta (para relação 1x1, 1xN e NxM com outras entidades)
  where: "rodas >= 4 & dono.nome LIKE 'Fabiana%' & motor.potencia >= 4500 ",
                                  // Informa os parâmetros de filtragem dos dados que serão enviados
                                  //   na resposta
  order: ["nome ASC","cor DESC"], // Informa a ordenação que deve ter a resposta
  id: false,                      // Informa se essa busca deve usar o ID/Chave da entidade (mesmo sendo 
                                  //   config WHERE chave composta) na consulta ao invés dos parâmetros 
                                  //   informados na 
  acao: 1,                        // Informa a ação que essa busca representa (verificar lista com as 
                                  //   constantes de Ação)
  data: ...                       // Informações extra que poderão ser usadas durante os processos
                                  //   do servidor
}
```

## Segurança:
Na URL `/{JAX context}/seguranca` estão os serviços referentes a segurança e usuários do sistema.

##### POST /{JAX context}/seguranca/login
Disponível para fazer login no sistema.

Essa URL pode receber o `login` e `senha` no formato `url-encoded`, ou como um objeto JSON
quando o cabeçalho **HTTP** `Content-Type: application/json` estiver presente.

##### POST /{JAX context}/seguranca/logout
Essa URL na recebe parâmetros, e ao chamar essa URL o usuário será deslogado do sistema.

**! Atenção:** *para identificar um usuário logado no sistema existem 2 `cookies` que são usados.
Um desses é o `cookie` da sessão Web de um servidor/servlet Java.
O outro é um mecanismo de login usado por este sistema (este não é obrigatório na maior parte das URLs,
mas também não é necessário se preocupar com a presença dele; é opcional)*

##### GET /{JAX context}/seguranca/usuario
Essa URL retorna as informações do usuário que estiver logado no momento, e que chamou essa URL.

As informações incluem as credencias, grupos e permissões do usuário, e outros atributos específicos
de cada aplicação, como nome, e-mail, data de nascimento e outros.

**! Atenção:** *a senha do usuário não pode ser enviada na resposta! A impl. padrão (atual) já 
verifica isso.*

## Relatórios:
*Ainda em desenvovimento*

O sistema terá formas de exportar a informação em formatos comuns externos ao sistema.

##### /exportar
Essa URL recebe um objeto de busca (ou vetor, mas usará apenas a primeira busca da lista na 
impl. de hoje) no parâmetro HTTP `json`, que será usada para buscar as informações 
que serão retornadas no relatório.

O valor de `data` no objeto de busca deve ser um JSON com as seguintes informações:
```
{
  ... ,
  data: {
    nome: "Relatório de Carros",                      // Informa o nome do arquivo que será retornado
    titulos: ["Nome", "Dono", "Motor"],               // Informa os nomes das colunas no relatório
    atributos: ["nome", "dono.nome", "motor.marca"]   // Informa os dados que serão buscados das
                                                      //   entidades/classes envolvidas
  }
}
```

O índice dos vetores em `titulos` e `atributos` será usado para identificar qual o título para qual
coluna de atributo no resultado final.

Ex.:

| Nome   | Dono    | Motor  |
| ------ | ------- | ------ |
| Carro1 | Joaquim | Marca1 |
| Carro2 | Mariana | Marca2 |


