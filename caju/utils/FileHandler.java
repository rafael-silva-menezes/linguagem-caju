package caju.utils;

import caju.lexer.*;
import caju.node.*;
import caju.parser.*;
import caju.semantics.SemanticAnalyzer;
import caju.codegen.*;

import java.io.*;

public class FileHandler {
    
    public static boolean isValidFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile() && file.canRead();
    }

    public static FileReader getFileReader(String filePath) throws IOException {
        return new FileReader(new File(filePath));
    }

    public static void parseFile(String filePath) throws IOException, ParserException, LexerException {
        if (!isValidFile(filePath)) {
            throw new FileNotFoundException("File not found or cannot be read: " + filePath);
        }

        try (FileReader fileReader = getFileReader(filePath)) {
            Lexer lexer = new Lexer(new PushbackReader(fileReader, 1024));
            Parser parser = new Parser(lexer);
            Start tree = parser.parse();

            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            tree.apply(semanticAnalyzer);

            CCodeGenerator codeGenerator = new CCodeGenerator();
            tree.apply(codeGenerator);

            String outputPath = filePath.replaceAll("\\.(caju|cj)$", ".c");
            System.out.println(codeGenerator.getGeneratedCode());
            try (FileWriter writer = new FileWriter(outputPath)) {
                writer.write(codeGenerator.getGeneratedCode());
            }
        } catch (IOException | ParserException | LexerException e) {
            e.printStackTrace();
            throw e; 
        }
    }
}
