package JLox.lox;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    // Some constants for CLI
    private static final String VERSION = "1.0.0";

    // ANSI escape codes for colors
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_GREEN = "\u001B[32m";

    // Static variables to track errors and runtime errors.
    // These are used to indicate if the program should exit with an error code.
    public static boolean hadError = false;
    public static boolean hadRuntimeError = false;

    private static final Interpreter interpreter = new Interpreter();

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
        if (hadRuntimeError) {
            System.exit(70);
        }
    }

    // Handle REPL mode execution of program
    private static void runPrompt() throws IOException {
        // ASCII art logo
        String logo = "\r\n" + //
                "                                                                                   \r\n" + //
                "                                                                                   \r\n" + //
                "          JJJJJJJJJJJ LLLLLLLLLLL                                                  \r\n" + //
                "          J:::::::::J L:::::::::L                                                  \r\n" + //
                "          J:::::::::J L:::::::::L                                                  \r\n" + //
                "          JJ:::::::JJ LL:::::::LL                                                  \r\n" + //
                "            J:::::J     L:::::L                   ooooooooooo   xxxxxxx      xxxxxxx\r\n" + //
                "            J:::::J     L:::::L                 oo:::::::::::oo  x:::::x    x:::::x \r\n" + //
                "            J:::::J     L:::::L                o:::::::::::::::o  x:::::x  x:::::x  \r\n" + //
                "            J:::::j     L:::::L                o:::::ooooo:::::o   x:::::xx:::::x   \r\n" + //
                "            J:::::J     L:::::L                o::::o     o::::o    x::::::::::x    \r\n" + //
                "JJJJJJJ     J:::::J     L:::::L                o::::o     o::::o     x::::::::x     \r\n" + //
                "J:::::J     J:::::J     L:::::L                o::::o     o::::o     x::::::::x     \r\n" + //
                "J::::::J   J::::::J     L:::::L         LLLLLL o::::o     o::::o    x::::::::::x    \r\n" + //
                "J:::::::JJJ:::::::J   LL:::::::LLLLLLLLL:::::L o:::::ooooo:::::o   x:::::xx:::::x   \r\n" + //
                " JJ:::::::::::::JJ    L::::::::::::::::::::::L o:::::::::::::::o  x:::::x  x:::::x  \r\n" + //
                "   JJ:::::::::JJ      L::::::::::::::::::::::L  oo:::::::::::oo  x:::::x    x:::::x \r\n" + //
                "     JJJJJJJJJ        LLLLLLLLLLLLLLLLLLLLLLLL    ooooooooooo   xxxxxxx      xxxxxxx\r\n" + //
                "                                                                                  \r\n" + //
                "                                                                                  \r\n" + //
                "                                                                                  \r\n" + //
                "                                                                                  \r\n" + //
                "                                                                                  \r\n" + //
                "                                                                                  \r\n" + //
                "                                                                                  \r\n" + //
                "";

        System.out.println(ANSI_CYAN + logo + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "JLox Interpreter " + ANSI_GREEN + "v" + VERSION + ANSI_RESET);
        System.out.println(ANSI_YELLOW + "Type \"exit\" or \"quit\" to leave." + ANSI_RESET);
        System.out.println();

        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.print(ANSI_GREEN + ">>> " + ANSI_RESET); // green prompt
            String line = reader.readLine();

            if (line == null || line.trim().equalsIgnoreCase("exit") || line.trim().equalsIgnoreCase("quit")) {
                System.out.println(ANSI_YELLOW + "Exiting JLox REPL. Goodbye!" + ANSI_RESET);
                break;
            }

            if (line.trim().isEmpty())
                continue;

            run(line);
            hadError = false;
        }
    }

    // Gateway function of interpreter
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        // If there was a parsing error, exit early
        if (hadError) {
            return;
        }

        // Call the interpreter to evalute expression
        interpreter.interpret(statements);
    }

    // Handle error without token
    public static void error(int line, String message) {
        report(line, "", message);
    }

    // Report the error message to the user
    public static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
                "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    // Handle error with token
    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}
