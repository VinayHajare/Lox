package JLox.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {

    // Mapping of lexeme -> keywords
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("nil", TokenType.NIL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    // Immutable source program
    private final String source;

    // List of tokens
    private final List<Token> tokens = new ArrayList<>();

    // Fields to keep track of current position and location of scanner
    private int start = 0;
    private int current = 0;
    private int line = 1;


    public Scanner(String source) {
        this.source = source;
    }

    // Scan and return all the tokens
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // beginning of the next lexeme
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    // Find the token and append to list of tokens
    private void scanToken() {
        char c = advance();

        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_PAREN); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;
            case '-': addToken(TokenType.MINUS); break;
            case '+': addToken(TokenType.PLUS); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '*': addToken(TokenType.STAR); break;
            case '!':
                addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.EQUAL);
                break;
            case '=':
                addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
                break;
            case '<':
                addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
                break;
            case '>':
                addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
                break;            
            case '/': 
                if (match('/')) {
                    // Its a comment consume all character till end of the line
                    while (peek() != '\n' && !isAtEnd()) {
                        advance();                        
                    }
                } else if (match('*')) {
                    // Its a block comment, consume all characters till closing "*/"

                    int nesting = 1;

                    while (nesting > 0 && !isAtEnd()) {
                        if (peek() == '/' && peekNext() == '*') {
                            advance(); // consume '/'
                            advance(); // consume '*'
                            nesting++;
                        } else if (peek() == '*' && peekNext() == '/') {
                            advance(); // consume '*'
                            advance(); // consume '/'
                            nesting--;
                        } else {
                            if (peek() == '\n') line++;
                            advance();
                        }
                    }
                    
                    // If we reached end of the file without closing comment then report error
                    if (nesting > 0) {
                        Lox.error(line, "Unterminated multi-line comment.");
                    }
                    
                } else {
                    addToken(TokenType.SLASH);
                } 
                break;
            case ' ':
            case '\t':
            case '\r':
                // skip whitespaces
                break;
            
            case '\n':
                line++;
                break;
            case '"': string(); break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    // Parse an identifier
    private void identifier() {
        while (isAlphaNumeric(peek())) {
            advance();
        }

        // check for keywords
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);

        // If not it is identifier
        if (type == null) {
            type = TokenType.IDENTIFIER;
        }

        addToken(type);
    }

    // Parse a number (integer, decimal) literal
    private void number() {
        // consume untill next char is digit
        while (isDigit(peek())) {
            advance();
        }

        // Check for fractional part
        if (peek() == '.' && isDigit(peekNext())) {
            // Consume "."
            advance();
            
            // Consume remaining fractional part
            while (isDigit(peek())) {
                advance();
            }
        }

        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    // Parse a string literal
    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            // Support the multi-line string
            if (peek() == '\n') {
                line++;
            }
            advance();
        }

        // If we reached end of the file without closing string
        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        // Closing "
        advance();

        // Get the string value without quotes
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    // Look for the next character (doesn't consume character), 
    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        // return 1st lookahead symbol
        return source.charAt(current);
    }

    // Look for the next next character (doesn't consume character)
    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        // return 2nd lookahead symbol
        return source.charAt(current + 1);
    }

    // Check if it alphabate or underscore (A-Za-z_)
    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
               (c >= 'A' && c <= 'Z') ||
               c == '_';
    }

    // Check if the char is digit or alphabate
    private boolean isAlphaNumeric(char c) {
        return isDigit(c) || isAlpha(c);
    }

    // Check if given character is digit or not
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    // Match the next char with given char, if its expected one consume it & return true else return false
    private boolean match(char expected) {
        // If its last character, return false
        if (isAtEnd()) {
            return false;
        }

        if (source.charAt(current) != expected) {
            return false;
        }

        current++;
        return true;
    }

    // Get current character and advance to next
    private char advance() {
        return source.charAt(current++);    
    }

    // Create and append token to list
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // Create and append token (with value) to list
    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line));
    }

    // Check if all characters are consumed by scanner
    private boolean isAtEnd() {
        return current >= source.length();
    }

}
