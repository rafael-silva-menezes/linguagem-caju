package caju.utils;

import java.io.FileNotFoundException;
import java.io.IOException;

import caju.lexer.LexerException;
import caju.parser.ParserException;

public class ErrorHandler {

    public static void handleFileError(String filePath, Exception e) {
        System.err.println("Erro ao manipular o arquivo '" + filePath + "':");
        e.printStackTrace();
    }

    public static void handleParsingError(Exception e) {
        System.err.println("Erro durante a análise sintática:");
        e.printStackTrace();
    }

    public static void handleLexicalError(Exception e) {
        System.err.println("Erro durante a análise léxica:");
        e.printStackTrace();
    }

    public static void handleUnexpectedError(Exception e) {
        System.err.println("Erro inesperado:");
        e.printStackTrace();
    }

    public static void handleErrors(String filePath, Exception e) {
        if (e instanceof FileNotFoundException || e instanceof IOException) {
            ErrorHandler.handleFileError(filePath, e);
        } else if (e instanceof ParserException) {
            ErrorHandler.handleParsingError(e);
        } else if (e instanceof LexerException) {
            ErrorHandler.handleLexicalError(e);
        } else {
            ErrorHandler.handleUnexpectedError(e);
        }
    }
}
