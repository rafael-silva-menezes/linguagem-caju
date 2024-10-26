# Especificação da Linguagem Caju

## 1. Introdução

Com tantas linguagens de programação em inglês na computação, neste período vamos criar um compilador para uma linguagem de programação em português. A linguagem Caju é uma linguagem imperativa, e apresenta as características descritas neste documento.

Caju, obviamente, é uma linguagem experimental, então esta especificação é passível de adaptações. Em caso de modificações na especificação, versões atualizadas serão postadas via SIGAA (no tópico de aula “Apresentação do Projeto”) e notificações serão enviadas aos alunos e alunas.

As Seções 2, 3, 4 e 5 deste documento apresentam a especificação da linguagem. A Seção 6 contém informações sobre a avaliação e entregas.

## 2. Características principais e léxico

### Regras para identificadores

- Pode-se utilizar: números, letras maiúsculas, letras minúsculas e underscore ('_').
- O primeiro caractere deve ser sempre uma letra.
- Não são permitidos espaços em branco, acentos (por motivos de portabilidade do projeto) e caracteres especiais (ex.: á, ê, @, $, +, -, ^, % etc.).
- Identificadores não podem ser iguais às palavras reservadas ou operadores da linguagem.

### Tipos primitivos

- A linguagem aceita os tipos caractere, booleano e numero.
- Caracteres são escritos com aspas simples. Exemplo: 'a', '\n'.
- Booleanos podem assumir dois valores: verdadeiro e falso.
- O tipo numero representa valores inteiros e reais. A parte decimal dos números reais deve ser separada por uma vírgula. Os valores inteiros são expressos no sistema numérico decimal.

### Vetores

- Um vetor é composto de uma ou mais variáveis com o mesmo tipo primitivo.
- O tamanho dos vetores é definido durante sua criação.
- Os índices dos vetores vão de 0 a tam-1.
- Existem vetores multidimensionais. Exemplo: vetor numero [2][3] nome.
- Vetores unidimensionais de caracteres podem ter seus valores definidos por cadeias entre aspas duplas (“ e ”).

### Blocos de Comandos

- Contêm comandos e declarações.

### Comentários

- A linguagem aceita comentários de linha, indicados pelo símbolo `#` no início da linha.
- A linguagem aceita comentários de bloco (possivelmente de múltiplas linhas) delimitados por `{` e `}`.
- O funcionamento dos comentários de bloco em Caju é similar aos da linguagem C.

### Estruturas de controle (exemplo)

O `para cada` tem funcionamento similar ao comando `for each` do Java. Como exemplo, o comando `'para cada ( tipo elemento : vet )'` faz com que, a cada iteração deste laço, `elemento` receba um dos valores armazenados em `vet`, do índice 0 até o índice `tamanho-1`. `vet` deve obrigatoriamente conter elementos do tipo `tipo`.

### Subrotinas

- Funções com parâmetros e retorno de valor. O uso do comando `retorne` é obrigatório no corpo da função quando o seu retorno for diferente de vazio. Caso seja vazio, pode-se omitir esse comando ou usar `retorne;`.

### Operadores (Parte 1)

- Operadores aritméticos: `+`, `-`, `*`, `/`
- Operadores relacionais: `>`, `<`, `>=`, `<=`, `=`
- Operadores booleanos: `'nao'`, `'e'` e `'ou'`.
- A prioridade dos operadores é igual à de C, e pode ser alterada com o uso de parênteses.
- Atribuição de valores é feita com o operador `:=`.
- Comandos são terminados com `.` (ponto-final).

### Palavras reservadas

- `caractere`, `numero`, `booleano`, `vetor`, `retorne`, `vazio`, `inicio`, `fim`, `se`, `senao`, `enquanto`, `para`, `para cada`, `verdadeiro`, `falso`.
- Todos os operadores e divisores da linguagem.

### Procedimentos primitivos (1)

- `ler`: procedimento fictício para entrada de dados a partir do teclado. Salva os valores lidos nas variáveis que foram passadas como argumentos.

## 3. Sintático

A gramática da linguagem foi escrita em uma versão de E-BNF seguindo as seguintes convenções:

- Variáveis da gramática são escritas em letras minúsculas sem aspas;
- Tokens são escritos entre aspas simples;
- Símbolos escritos em letras maiúsculas representam o lexema de um token do tipo especificado;
- O símbolo `|` indica produções diferentes de uma mesma variável;
- O operador `[ ]` indica uma estrutura sintática opcional;
- O operador `{ }` indica uma estrutura sintática que é repetida zero ou mais vezes.

```ebnf
programa : {dec-variavel} {dec-funcao}
dec-variavel : tipo lista-nomes '.'
lista-nomes : ID { ',' ID }
tipo : tipo-base | 'vetor' tipo-base '[' exp ']' {'[' exp ']'}
tipo-base : 'numero' | 'caractere' | 'booleano'
dec-funcao : ['->'] tipo-retorno ID '(' parametros ')' bloco
tipo-retorno : tipo | 'vazio'
parametros : ε | parametro { '|' parametro }
parametro : tipo ID
bloco : 'inicio' { dec-variavel } { comando } 'fim'
atrib : var ':=' exp
lista-atrib: atrib {, atrib }
comando :
    'se' '(' exp ')' comando
  | 'se' '(' exp ')' comando 'senao' comando
  | 'enquanto' '(' exp ')' comando
  | 'para' '(' lista-atrib ';' exp ';' lista-atrib ')' comando
  | 'para cada' '(' tipo ID ':' ID ')' comando
  | atrib '.'
  | 'retorne' [ exp ] '.'
  | bloco
  | chamada '.'
var : ID | var '[' exp ']'
exp : NUMERO | CARACTERE | BOOLEANO
  | var
  | '(' exp ')'
  | chamada
  | exp '+' exp
  | exp '-' exp
  | exp '*' exp
  | exp '/' exp
  | exp '=' exp
  | exp '<=' exp
  | exp '>=' exp
  | exp '<' exp
  | exp '>' exp
  | 'nao' exp
  | exp 'e' exp
  | exp 'ou' exp
chamada : ID '(' lista-exp ')'
lista-exp : ε | exp { '|' exp }

```

## 4. Semântico

### Geral

Nos casos omissos neste documento, a semântica da linguagem segue a semântica de C.  
A execução de um programa consiste na execução de uma função marcada com `->`.

### Blocos

- Contêm comandos e declarações.
- Podem ser aninhados.
- Escopo: o tempo de vida dos identificadores declarados em um bloco é igual ao tempo de vida deste bloco.

### Espaços de memória

Não há constantes na linguagem, apenas variáveis. Variáveis podem ser inicializadas apenas em comandos posteriores à sua declaração, com uma atribuição (operador `:=`) ou através do comando `ler`. Variáveis só podem ser usadas se forem inicializadas.

### Estruturas de controle

O `para cada` tem funcionamento similar ao comando `for each` do Java. Como exemplo, o comando `'para cada ( tipo elemento : vet )'` faz com que, a cada iteração deste laço, `elemento` receba um dos valores armazenados em `vet`, do índice 0 até o índice `tamanho-1`. `vet` deve obrigatoriamente conter elementos do tipo `tipo`.

As outras estruturas são similares às suas equivalentes em C, lembrando que somente podem ser usados operadores desta linguagem (exemplo: não temos o operador unário `++` para as estruturas `para` e `para cada`).

### Operadores

A prioridade dos operadores é igual à de C, e pode ser alterada com o uso de parênteses.

### Procedimentos primitivos

- `ler`: procedimento fictício para entrada de dados a partir do teclado. Salva os valores lidos nas variáveis que foram passadas como argumentos.
- `exibir`: procedimento para exibição de um ou mais valores resultantes de expressões passadas como argumentos.

### O que checar na análise semântica

- Se entidades definidas pelo usuário (variáveis, vetores e funções) são inseridas na tabela de símbolos - com os atributos necessários - quando são declaradas;
- Se uma entidade foi declarada e está em um escopo válido no momento em que ela é utilizada (regras de escopo são iguais às de C);
- Se entidades foram definidas quando isso se fizer necessário;
- Checar a compatibilidade dos tipos de dados envolvidos nos comandos, expressões, atribuições e chamadas de função.
