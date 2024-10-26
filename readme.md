# Guia para Executar o Projeto Caju com SableCC

Este documento fornece instruções passo a passo para executar o projeto Caju, utilizando o SableCC para gerar arquivos Java a partir do arquivo `caju.sable`, compilar os arquivos gerados e rodar a aplicação principal.

## Pré-requisitos

- Java JDK instalado e configurado no seu sistema.
- SableCC instalado e acessível em seu caminho de sistema.

## Passo 1: Rodar o SableCC

Primeiro, você precisará executar o SableCC para gerar os arquivos Java a partir do arquivo `caju.sable`. Abra o terminal e execute o seguinte comando:

```bash
java -jar /Users/rmenezes/Documents/ufs/Compiladores/SableCC/lib/sablecc.jar caju.sable
```

## Passo 2: Compilar os Arquivos Java

Após gerar os arquivos Java, compile todos os arquivos no pacote caju. Execute o seguinte comando no terminal:

```bash
javac caju/*.java caju/**/*.java
```

## Passo 3: Executar o Programa

Por fim, você pode rodar a classe principal do seu projeto. Lembre-se de verificar se o caminho para o arquivo de teste (teste.cj) está correto. Você pode passar o arquivo de teste como um parâmetro. Execute o comando:

```bash

java -cp . caju.Main test/teste.caju
```

## Observações

Certifique-se de que o arquivo caju.sable e o arquivo de teste teste.caju estão localizados no diretório correto.
O parâmetro teste.caju pode ser modificado para o caminho correto do seu arquivo de teste, caso necessário.
