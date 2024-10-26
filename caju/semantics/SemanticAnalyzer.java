package caju.semantics;

import caju.analysis.DepthFirstAdapter;
import caju.node.*;
import java.util.*;

public class SemanticAnalyzer extends DepthFirstAdapter {
    private SymbolTable symbolTable;
    private List<String> errors;
    private String currentFunction;
    private boolean hasReturn;

    public SemanticAnalyzer() {
        this.symbolTable = new SymbolTable();
        this.errors = new ArrayList<>();
        this.currentFunction = null;
        this.hasReturn = false;
    }

    @Override
    public void inStart(Start node) {
        System.out.println("-------------------------------------------------");
        System.out.println("Iniciando análise semântica...");
    }

    @Override
    public void outStart(Start node) {
        System.out.println("-------------------------------------------------");
        if (errors.isEmpty()) {
            System.out.println("Análise semântica concluída sem erros.");
        } else {
            System.out.println("Erros semânticos encontrados:");
            for (String error : errors) {
                System.out.println("- " + error);
            }
        }
        System.out.println("-------------------------------------------------");
    }

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

    @Override
    public void inAFuncaoADeclaracao(AFuncaoADeclaracao node) {
        String nome = node.getNome().getText();
        String tipoRetorno = node.getTipoRetorno().toString().trim();

        if (symbolTable.existsInCurrentScope(nome)) {
            addError("Função '" + nome + "' já declarada.");
        } else {
            List<String> paramTypes = new ArrayList<>();
            for (PAParametro param : node.getParametros()) {
                if (param instanceof AAParametro) {
                    AAParametro aParam = (AAParametro) param;
                    paramTypes.add(aParam.getATipo().toString().trim());
                }
            }
            symbolTable.add(nome, new Symbol(nome, "funcao", tipoRetorno, paramTypes));
        }

        currentFunction = nome;
        hasReturn = false;
        symbolTable.enterScope();

        for (PAParametro param : node.getParametros()) {
            if (param instanceof AAParametro) {
                AAParametro aParam = (AAParametro) param;
                String paramNome = aParam.getNome().getText();
                String paramTipo = aParam.getATipo().toString().trim();
                symbolTable.add(paramNome, new Symbol(paramNome, paramTipo, true));
            }
        }
    }

    @Override
    public void outAFuncaoADeclaracao(AFuncaoADeclaracao node) {
        String tipoRetorno = node.getTipoRetorno().toString().trim();
        if (!tipoRetorno.equals("vazio") && !hasReturn && !currentFunction.equals("main")) {
            addError("Função '" + currentFunction + "' deve retornar um valor do tipo " + tipoRetorno + ".");
        }
        symbolTable.exitScope();
        currentFunction = null;
        hasReturn = false;
    }

    @Override
    public void inAAtribuicaoAComando(AAtribuicaoAComando node) {
        PAAtrib atrib = node.getAAtrib();
        if (atrib instanceof AAAtrib) {
            AAAtrib aAtrib = (AAAtrib) atrib;
            String varName = aAtrib.getAlvo().toString().trim();
            Symbol varSymbol = symbolTable.get(varName);

            if (varSymbol == null) {
                addError("Variável '" + varName + "' não declarada.");
                return;
            }

            String expType = getExpressionType(aAtrib.getValor());
            if (!isCompatibleType(varSymbol.type, expType)) {
                addError("Tipo incompatível na atribuição. Esperado " + varSymbol.type + ", mas recebeu " + expType + ".");
            }

            varSymbol.initialized = true;
        } else {
            addError("Estrutura de atribuição inválida.");
        }
    }

    @Override
    public void inAChamadaAComando(AChamadaAComando node) {
        checkFunctionCall(node.getAChamada());
    }

    @Override
    public void inAChamadaAExp(AChamadaAExp node) {
        checkFunctionCall(node.getAChamada());
    }

    private void checkFunctionCall(PAChamada chamada) {
        if (!(chamada instanceof AAChamada)) {
            addError("Estrutura de chamada de função inválida.");
            return;
        }
        AAChamada aaChamada = (AAChamada) chamada;
        String funcName = aaChamada.getNome().getText();
        Symbol funcSymbol = symbolTable.get(funcName);

        if (funcSymbol == null) {
            addError("Função '" + funcName + "' não declarada.");
            return;
        }

        if (!funcSymbol.type.equals("funcao")) {
            addError("'" + funcName + "' não é uma função.");
            return;
        }

        List<PAExp> args = aaChamada.getArgs();
        if (args.size() != funcSymbol.paramTypes.size()) {
            addError("Número incorreto de argumentos para a função '" + funcName + "'. Esperado "
                    + funcSymbol.paramTypes.size() + ", mas recebeu " + args.size() + ".");
            return;
        }

        for (int i = 0; i < args.size(); i++) {
            String argType = getExpressionType(args.get(i));
            String paramType = funcSymbol.paramTypes.get(i);
            if (!isCompatibleType(paramType, argType)) {
                addError("Tipo incompatível no argumento " + (i + 1) + " da função '" + funcName + "'. Esperado "
                        + paramType + ", mas recebeu " + argType + ".");
            }
        }
    }

    @Override
    public void inASeAComando(ASeAComando node) {
        String condType = getExpressionType(node.getAExp());
        if (!condType.equals("booleano")) {
            addError("Condição do 'se' deve ser booleana, mas recebeu " + condType + ".");
        }
        symbolTable.enterScope();
    }

    @Override
    public void outASeAComando(ASeAComando node) {
        symbolTable.exitScope();
    }

    @Override
    public void inAEnquantoAComando(AEnquantoAComando node) {
        String condType = getExpressionType(node.getAExp());
        if (!condType.equals("booleano")) {
            addError("Condição do 'enquanto' deve ser booleana, mas recebeu " + condType + ".");
        }
        symbolTable.enterScope();
    }

    @Override
    public void outAEnquantoAComando(AEnquantoAComando node) {
        symbolTable.exitScope();
    }

    @Override
    public void inAParaAComando(AParaAComando node) {
        symbolTable.enterScope();
        // Verificar inicialização, condição e incremento
        if (node.getInc() != null) {
            for (PAAtrib atrib : node.getInc()) {
                atrib.apply(this);
            }
        }
        if (node.getCond() != null) {
            String condType = getExpressionType(node.getCond());
            if (!condType.equals("booleano")) {
                addError("Condição do 'para' deve ser booleana, mas recebeu " + condType + ".");
            }
        }
        if (node.getInc() != null) {
            for (PAAtrib atrib : node.getInc()) {
                atrib.apply(this);
            }
        }
    }

    @Override
    public void outAParaAComando(AParaAComando node) {
        symbolTable.exitScope();
    }

    @Override
    public void inAParaCadaAComando(AParaCadaAComando node) {
        symbolTable.enterScope();
        String varType = node.getATipo().toString().trim();
        String varName = node.getVar().getText();
        symbolTable.add(varName, new Symbol(varName, varType, true));

        String containerName = node.getContainer().getText();
        Symbol containerSymbol = symbolTable.get(containerName);
        if (containerSymbol == null) {
            addError("Variável '" + containerName + "' não declarada.");
        } else if (!containerSymbol.type.startsWith("vetor")) {
            addError("'" + containerName + "' não é um vetor.");
        } else {
            String elementType = containerSymbol.type.substring(6, containerSymbol.type.length() - 1);
            if (!isCompatibleType(varType, elementType)) {
                addError("Tipo incompatível no 'para cada'. Esperado " + elementType
                        + ", mas a variável de iteração é do tipo " + varType + ".");
            }
        }
    }

    @Override
    public void outAParaCadaAComando(AParaCadaAComando node) {
        symbolTable.exitScope();
    }

    @Override
    public void inARetorneAComando(ARetorneAComando node) {
        if (currentFunction == null) {
            addError("Comando 'retorne' fora de uma função.");
            return;
        }

        Symbol funcSymbol = symbolTable.get(currentFunction);
        String expectedType = funcSymbol.returnType;

        if (node.getValor() != null) {
            String returnType = getExpressionType(node.getValor());
            if (!isCompatibleType(expectedType, returnType)) {
                addError("Tipo de retorno incompatível. Esperado " + expectedType + ", mas recebeu " + returnType + ".");
            }
        } else if (!expectedType.equals("vazio")) {
            addError("Função '" + currentFunction + "' deve retornar um valor do tipo " + expectedType + ".");
        }

        hasReturn = true;
    }

    private String getExpressionType(PAExp exp) {
        if (exp instanceof ANumeroAExp) {
            return "numero";
        } else if (exp instanceof ACaractereAExp) {
            return "caractere";
        } else if (exp instanceof ABooleanoAExp) {
            return "booleano";
        } else if (exp instanceof AVarAExp) {
            return getVarType((AVarAExp) exp);
        } else if (exp instanceof AAditivaAExp || exp instanceof AMultiplicativaAExp) {
            return "numero";
        } else if (exp instanceof ARelacionalAExp || exp instanceof AIgualdadeAExp || exp instanceof AEAExp
                || exp instanceof AOuAExp) {
            return "booleano";
        } else if (exp instanceof AChamadaAExp) {
            return getFunctionReturnType((AChamadaAExp) exp);
        } else if (exp instanceof ANaoAExp) {
            String subExpType = getExpressionType(((ANaoAExp) exp).getAExp());
            if (!subExpType.equals("booleano")) {
                addError("Operador 'nao' espera uma expressão booleana, mas recebeu " + subExpType + ".");
            }
            return "booleano";
        } else if (exp instanceof ANegativoAExp) {
            String subExpType = getExpressionType(((ANegativoAExp) exp).getAExp());
            if (!subExpType.equals("numero")) {
                addError("Operador '-' unário espera uma expressão numérica, mas recebeu " + subExpType + ".");
            }
            return "numero";
        }
        return "desconhecido";
    }

    private String getVarType(AVarAExp exp) {
        String varName = exp.getAVar().toString().trim();
        Symbol varSymbol = symbolTable.get(varName);
        if (varSymbol == null) {
            addError("Variável '" + varName + "' não declarada.");
            return "desconhecido";
        }
        if (!varSymbol.initialized) {
            addError("Variável '" + varName + "' não inicializada.");
        }
        return varSymbol.type;
    }

    private String getFunctionReturnType(AChamadaAExp exp) {
        PAChamada chamada = exp.getAChamada();
        if (!(chamada instanceof AAChamada)) {
            addError("Estrutura de chamada de função inválida.");
            return "desconhecido";
        }
        AAChamada aaChamada = (AAChamada) chamada;
        String funcName = aaChamada.getNome().getText();
        Symbol funcSymbol = symbolTable.get(funcName);
        if (funcSymbol == null || !funcSymbol.type.equals("funcao")) {
            addError("Função '" + funcName + "' não declarada.");
            return "desconhecido";
        }
        return funcSymbol.returnType;
    }

    private boolean isCompatibleType(String expected, String actual) {
        if (expected.equals(actual)) {
            return true;
        }
        // Add more type compatibility rules if needed
        return false;
    }

    private void addError(String message) {
        errors.add(message);
    }
}

class SymbolTable {
    private LinkedList<HashMap<String, Symbol>> scopes;

    public SymbolTable() {
        scopes = new LinkedList<>();
        enterScope();
    }

    public void enterScope() {
        scopes.addFirst(new HashMap<>());
    }

    public void exitScope() {
        scopes.removeFirst();
    }

    public void add(String name, Symbol symbol) {
        scopes.getFirst().put(name, symbol);
    }

    public boolean exists(String name) {
        for (HashMap<String, Symbol> scope : scopes) {
            if (scope.containsKey(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean existsInCurrentScope(String name) {
        return scopes.getFirst().containsKey(name);
    }

    public Symbol get(String name) {
        for (HashMap<String, Symbol> scope : scopes) {
            Symbol symbol = scope.get(name);
            if (symbol != null) {
                return symbol;
            }
        }
        return null;
    }
}

class Symbol {
    String name;
    String type;
    String returnType;
    List<String> paramTypes;
    boolean initialized;

    public Symbol(String name, String type) {
        this(name, type, null);
    }

    public Symbol(String name, String type, String returnType) {
        this.name = name;
        this.type = type;
        this.returnType = returnType;
        this.initialized = false;
        this.paramTypes = new ArrayList<>();
    }

    public Symbol(String name, String type, boolean initialized) {
        this(name, type);
        this.initialized = initialized;
    }

    public Symbol(String name, String type, String returnType, List<String> paramTypes) {
        this(name, type, returnType);
        this.paramTypes = paramTypes;
    }
}
