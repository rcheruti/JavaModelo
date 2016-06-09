
# Persistência (implementação em Java)




# Lembretes:

### Exemplo do formato do JSON:
A Classe que guarda essas informações é a `...utils.BuscaInfo`,
nessa classe estão as constantes de **Ação** e alguns outros atributos
para facilitar a programação de código para o sistema.

```
{
  entidade: "Carro",          // Nome da entidade que se refere esta busca
  page: 2,                    // Página que retornará na resposta (iniciando em zero)
  size: 50,                   // Quantidade de itens que devem ser considerados para cada página (no máximo)
  join: ["dono","motor"],     // Vetor com nome de atributos que devem ser retornados na 
                              //  resposta (para relação 1x1, 1xN e NxM com outras entidades)
  where: "",
  order: [""],
  id: false,                  // Informa se essa busca deve usar o ID/Chave da entidade (mesmo sendo 
                              //  chave composta) na consulta ao invés dos parâmetros informados na config WHERE
  acao: 1                     // Informa a ação que essa busca representa (verificar lista com as 
                              //  constantes de **Ação**)
}
```
