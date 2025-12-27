package JLox.lox;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
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
        System.out.println(ANSI_YELLOW + "Commands:" + ANSI_RESET);
        System.out.println(
                "  " + ANSI_CYAN + "exit" + ANSI_RESET + " or " + ANSI_CYAN + "quit" + ANSI_RESET + " - Exit the REPL");
        System.out.println("  " + ANSI_CYAN + "help" + ANSI_RESET + " - Show this help message");
        System.out.println("  " + ANSI_CYAN + "clear" + ANSI_RESET + " - Clear the screen");
        System.out
                .println("  " + ANSI_CYAN + "multiline" + ANSI_RESET + " - Enter multiline mode (end with empty line)");
        System.out.println();
        System.out.println(ANSI_YELLOW
                + "Tip: Single lines execute immediately. Use 'multiline' for complex statements." + ANSI_RESET);
        System.out.println();

        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        boolean multilineMode = false;

        while (true) {
            System.out.print(ANSI_GREEN + ">>> " + ANSI_RESET); // Primary prompt
            String line = reader.readLine();

            if (line == null)
                break; // Handle EOF (Ctrl+D)

            line = line.trim();

            // Handle special commands
            if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("quit")) {
                System.out.println(ANSI_YELLOW + "Exiting JLox REPL. Goodbye!" + ANSI_RESET);
                return;
            }

            if (line.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            }

            if (line.equalsIgnoreCase("clear")) {
                clearScreen();
                continue;
            }

            if (line.equalsIgnoreCase("multiline")) {
                multilineMode = true;
                System.out.println(ANSI_YELLOW + "Entered multiline mode. End with an empty line." + ANSI_RESET);
                continue;
            }

            if (line.isEmpty()) {
                continue; // Skip empty lines in single-line mode
            }

            // Check if this looks like it needs multiple lines
            if (!multilineMode && needsMultipleLines(line)) {
                System.out.println(
                        ANSI_YELLOW + "This statement appears incomplete. Entering multiline mode..." + ANSI_RESET);
                multilineMode = true;
            }

            if (multilineMode) {
                // Multiline input mode
                StringBuilder source = new StringBuilder();
                source.append(line).append("\n");

                while (true) {
                    System.out.print(ANSI_GREEN + "... " + ANSI_RESET); // Continuation prompt
                    String nextLine = reader.readLine();

                    if (nextLine == null)
                        break; // Handle EOF

                    if (nextLine.trim().isEmpty()) {
                        break; // End multiline input on empty line
                    }

                    source.append(nextLine).append("\n");
                }

                if (source.length() > 0) {
                    run(source.toString());
                    hadError = false;
                }

                multilineMode = false; // Reset to single-line mode
            } else {
                // Single-line execution
                run(line);
                hadError = false;
            }
        }
    }

    // Helper method to detect if a line likely needs continuation
    private static boolean needsMultipleLines(String line) {
        // Check for common patterns that suggest incomplete statements
        line = line.trim();

        // Function declarations
        if (line.startsWith("fun ") && !line.endsWith("}")) {
            return true;
        }

        // Class declarations
        if (line.startsWith("class ") && !line.endsWith("}")) {
            return true;
        }

        // Control structures without closing braces
        if ((line.startsWith("if ") || line.startsWith("while ") ||
                line.startsWith("for ") || line.startsWith("else")) &&
                !line.endsWith("}") && !line.endsWith(";")) {
            return true;
        }

        // Unbalanced braces
        int openBraces = 0;
        int openParens = 0;
        boolean inString = false;
        char stringDelim = 0;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (!inString) {
                if (c == '"' || c == '\'') {
                    inString = true;
                    stringDelim = c;
                } else if (c == '{') {
                    openBraces++;
                } else if (c == '}') {
                    openBraces--;
                } else if (c == '(') {
                    openParens++;
                } else if (c == ')') {
                    openParens--;
                }
            } else if (c == stringDelim && (i == 0 || line.charAt(i - 1) != '\\')) {
                inString = false;
            }
        }

        return openBraces > 0 || openParens > 0 || inString;
    }

    // Helper method to show help
    private static void showHelp() {
        System.out.println(ANSI_CYAN + "JLox REPL Help:" + ANSI_RESET);
        System.out.println("* Single-line expressions execute immediately");
        System.out.println("* Multi-line statements are auto-detected or use 'multiline' command");
        System.out.println("* Available commands: exit, quit, help, clear, multiline");
        System.out.println("* Examples:");
        System.out.println("  " + ANSI_GREEN + ">>> " + ANSI_RESET + "2 + 3");
        System.out.println("  " + ANSI_GREEN + ">>> " + ANSI_RESET + "print \"Hello, World!\";");
        System.out.println("  " + ANSI_GREEN + ">>> " + ANSI_RESET + "multiline");
        System.out.println("  " + ANSI_GREEN + ">>> " + ANSI_RESET + "fun fibonacci(n) {");
        System.out.println("  " + ANSI_GREEN + "... " + ANSI_RESET + "  if (n <= 1) return n;");
        System.out.println("  " + ANSI_GREEN + "... " + ANSI_RESET + "  return fibonacci(n-1) + fibonacci(n-2);");
        System.out.println("  " + ANSI_GREEN + "... " + ANSI_RESET + "}");
        System.out.println("  " + ANSI_GREEN + "... " + ANSI_RESET);
        System.out.println();
    }

    // Helper method to clear screen
    private static void clearScreen() {
        // ANSI escape sequence to clear screen
        System.out.print("\033[2J\033[H");
        System.out.flush();
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

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        // Stop if there was a resolution error.
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
