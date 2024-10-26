# Introdução

Nesta etapa do projeto Caju, implementamos um Analisador Semântico e um Gerador de Código para a linguagem C. O objetivo principal foi garantir que o código escrito em Caju fosse semanticamente correto antes de ser convertido em código C, além de gerar o código C correspondente de forma adequada.

## Analisador Semântico

### Funcionalidades Implementadas

O Analisador Semântico é responsável por verificar a validade do código Caju em termos de declarações, tipos e escopos. As principais funcionalidades incluem:

1. **Verificação de Declarações**: Garante que variáveis e funções sejam declaradas antes de serem utilizadas. Se uma variável ou função já estiver declarada no escopo atual, um erro é gerado.

2. **Verificação de Tipos**: Confere se os tipos de variáveis e expressões são compatíveis, especialmente em atribuições e retornos de funções. Se houver uma incompatibilidade de tipos, um erro é gerado.

3. **Gerenciamento de Escopos**: Implementa uma tabela de símbolos que gerencia escopos de variáveis, permitindo a declaração de variáveis locais e evitando conflitos de nomes.

4. **Mensagens de Erro**: Exibe mensagens de erro detalhadas quando inconsistências semânticas são encontradas, facilitando a depuração do código.

### Exemplo de Código - Analisador Semântico

```java
@Override
    public void inAVariavelADeclaracao(AVariavelADeclaracao node) {
        String tipo = node.getATipo().toString().trim();
        for (PANome nome : node.getNomes()) {
            String nomeStr = nome.toString().trim();
            if (symbolTable.existsInCurrentScope(nomeStr)) {
                addError("Variável '" + nomeStr + "' já declarada neste escopo.");
            } else {
                symbolTable.add(nomeStr, new Symbol(nomeStr, tipo));
            }
        }
    }
```

## Gerador de Código

### Funcionalidades do Gerador de Código Implementadas

O Gerador de Código é responsável por converter a representação interna do código Caju em código C. As principais funcionalidades incluem:

1. **Inclusão de Bibliotecas**: O gerador automaticamente inclui as bibliotecas padrão necessárias, como `<stdio.h>` e `<stdbool.h>`.

2. **Declarações de Funções**: Gera a declaração de funções em C com os tipos de retorno e parâmetros adequados.

3. **Comandos de Atribuição**: Converte atribuições de variáveis para a sintaxe C.

4. **Comandos de Retorno**: Gera o comando `return` para funções.

5. **Comandos de Exibição**: Implementa a função `printf` para exibir valores na saída padrão.

### Exemplo de Código - Gerador de Código

```java
@Override
    public void inAVariavelADeclaracao(AVariavelADeclaracao node) {
        String type = getCType(node.getATipo());
        List<PANome> names = node.getNomes();
        for (PANome name : names) {
            println(type + " " + name.toString().trim() + ";");
        }
    }
```
