package JLox.lox;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    // Static variables to track errors and runtime errors.
    // These are used to indicate if the program should exit with an error code.
    public static boolean hadError = false;
    public static boolean hadRuntimeError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            // Start REPL mode
            runPrompt();
            hadError = false;
        }
    }

    // Parse and execute the given file
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        // Indicate an error in the exit code
        if (hadError) {
            System.exit(65);
        }
    }

    // Handle REPL mode execution of statemets
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.println(">>> ");
            String line = reader.readLine();

            if (line == null) {
                break;
            }

            run(line);
            hadError = false;
        }
    }

    // Gateway function of interpreter
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens(); 

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    // Handle error
    public static void error(int line, String message) {
        report(line, "", message);
    }

    // Report the error message to the user
    public static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
