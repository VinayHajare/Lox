#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>
#include <ctype.h>
#include "include/common.h"
#include "include/chunk.h"
#include "include/vm.h"
#include "include/debug.h"

// Version constant
static const char* VERSION = "1.0.0";

// ANSI escape codes for colors
#define ANSI_RESET   "\x1B[0m"
#define ANSI_CYAN    "\x1B[36m"
#define ANSI_YELLOW  "\x1B[33m"
#define ANSI_GREEN   "\x1B[32m"
#define ANSI_RED     "\x1B[31m"

// Helper function to check if a line needs multiple lines
static bool needsMultipleLines(const char* line) {
    int openBraces = 0;
    int openParens = 0;
    bool inString = false;
    char stringDelim = 0;
    
    size_t len = strlen(line);
    
    // Check for common patterns that suggest incomplete statements
    if (strncmp(line, "fun ", 4) == 0 && line[len-1] != '}') {
        return true;
    }
    
    if (strncmp(line, "class ", 6) == 0 && line[len-1] != '}') {
        return true;
    }
    
    if ((strncmp(line, "if ", 3) == 0 || strncmp(line, "while ", 6) == 0 ||
         strncmp(line, "for ", 4) == 0 || strncmp(line, "else", 4) == 0) &&
        line[len-1] != '}' && line[len-1] != ';') {
        return true;
    }
    
    // Check for unbalanced braces and parentheses
    for (size_t i = 0; i < len; i++) {
        char c = line[i];
        
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
        } else if (c == stringDelim && (i == 0 || line[i-1] != '\\')) {
            inString = false;
        }
    }
    
    return openBraces > 0 || openParens > 0 || inString;
}

// Helper function to trim whitespace
static char* trim(char* str) {
    char* end;
    
    // Trim leading space
    while(isspace((unsigned char)*str)) str++;
    
    if(*str == 0)
        return str;
    
    // Trim trailing space
    end = str + strlen(str) - 1;
    while(end > str && isspace((unsigned char)*end)) end--;
    
    end[1] = '\0';
    return str;
}

// Helper function to show help
static void showHelp() {
    printf(ANSI_CYAN "CLox REPL Help:" ANSI_RESET "\n");
    printf("* Single-line expressions execute immediately\n");
    printf("* Multi-line statements are auto-detected or use 'multiline' command\n");
    printf("* Available commands: exit, quit, help, clear, multiline\n");
    printf("* Examples:\n");
    printf("  " ANSI_GREEN ">>> " ANSI_RESET "2 + 3\n");
    printf("  " ANSI_GREEN ">>> " ANSI_RESET "print \"Hello, World!\";\n");
    printf("  " ANSI_GREEN ">>> " ANSI_RESET "multiline\n");
    printf("  " ANSI_GREEN ">>> " ANSI_RESET "fun fibonacci(n) {\n");
    printf("  " ANSI_GREEN "... " ANSI_RESET "  if (n <= 1) return n;\n");
    printf("  " ANSI_GREEN "... " ANSI_RESET "  return fibonacci(n-1) + fibonacci(n-2);\n");
    printf("  " ANSI_GREEN "... " ANSI_RESET "}\n");
    printf("  " ANSI_GREEN "... " ANSI_RESET "\n");
    printf("\n");
}

// Helper function to clear screen
static void clearScreen() {
    printf("\033[2J\033[H");
    fflush(stdout);
}

static void repl()
{
    // ASCII art logo
    const char* logo = 
        "\n"
        "                                                                                   \n"
        "                                                                                   \n"
        "           CCCCCCCCCCCCC LLLLLLLLLLL                                               \n"
        "        CCC::::::::::::C L:::::::::L                                               \n"
        "      CC:::::::::::::::C L:::::::::L                                               \n"
        "     C:::::CCCCCCCC::::C LL:::::::LL                                               \n"
        "    C:::::C       CCCCCC   L:::::L                   ooooooooooo   xxxxxxx      xxxxxxx\n"
        "   C:::::C                 L:::::L                 oo:::::::::::oo  x:::::x    x:::::x \n"
        "   C:::::C                 L:::::L                o:::::::::::::::o  x:::::x  x:::::x  \n"
        "   C:::::C                 L:::::L                o:::::ooooo:::::o   x:::::xx:::::x   \n"
        "   C:::::C                 L:::::L                o::::o     o::::o    x::::::::::x    \n"
        "   C:::::C                 L:::::L                o::::o     o::::o     x::::::::x     \n"
        "   C:::::C                 L:::::L                o::::o     o::::o     x::::::::x     \n"
        "    C:::::C       CCCCCC   L:::::L         LLLLLL o::::o     o::::o    x::::::::::x    \n"
        "     C:::::CCCCCCCC::::C LL:::::::LLLLLLLLL:::::L o:::::ooooo:::::o   x:::::xx:::::x   \n"
        "      CC:::::::::::::::C L::::::::::::::::::::::L o:::::::::::::::o  x:::::x  x:::::x  \n"
        "        CCC::::::::::::C L::::::::::::::::::::::L  oo:::::::::::oo  x:::::x    x:::::x \n"
        "           CCCCCCCCCCCCC LLLLLLLLLLLLLLLLLLLLLLLL    ooooooooooo   xxxxxxx      xxxxxxx\n"
        "                                                                                  \n"
        "                                                                                  \n"
        "\n";
    
    printf(ANSI_CYAN "%s" ANSI_RESET, logo);
    printf(ANSI_YELLOW "CLox Interpreter " ANSI_GREEN "v%s" ANSI_RESET "\n", VERSION);
    printf(ANSI_YELLOW "Commands:" ANSI_RESET "\n");
    printf("  " ANSI_CYAN "exit" ANSI_RESET " or " ANSI_CYAN "quit" ANSI_RESET " - Exit the REPL\n");
    printf("  " ANSI_CYAN "help" ANSI_RESET " - Show this help message\n");
    printf("  " ANSI_CYAN "clear" ANSI_RESET " - Clear the screen\n");
    printf("  " ANSI_CYAN "multiline" ANSI_RESET " - Enter multiline mode (end with empty line)\n");
    printf("\n");
    printf(ANSI_YELLOW "Tip: Single lines execute immediately. Use 'multiline' for complex statements." ANSI_RESET "\n");
    printf("\n");
    
    char line[1024];
    bool multilineMode = false;
    
    for (;;)
    {
        printf(ANSI_GREEN ">>> " ANSI_RESET);
        
        if (!fgets(line, sizeof(line), stdin))
        {
            printf("\n");
            break;
        }
        
        // Remove newline
        size_t length = strlen(line);
        if (length > 0 && line[length-1] == '\n') {
            line[length-1] = '\0';
        }
        
        char* trimmed = trim(line);
        
        // Handle special commands
        if (strcasecmp(trimmed, "exit") == 0 || strcasecmp(trimmed, "quit") == 0) {
            printf(ANSI_YELLOW "Exiting CLox REPL. Goodbye!" ANSI_RESET "\n");
            break;
        }
        
        if (strcasecmp(trimmed, "help") == 0) {
            showHelp();
            continue;
        }
        
        if (strcasecmp(trimmed, "clear") == 0) {
            clearScreen();
            continue;
        }
        
        if (strcasecmp(trimmed, "multiline") == 0) {
            multilineMode = true;
            printf(ANSI_YELLOW "Entered multiline mode. End with an empty line." ANSI_RESET "\n");
            continue;
        }
        
        if (strlen(trimmed) == 0) {
            continue; // Skip empty lines in single-line mode
        }
        
        // Check if this looks like it needs multiple lines
        if (!multilineMode && needsMultipleLines(trimmed)) {
            printf(ANSI_YELLOW "This statement appears incomplete. Entering multiline mode..." ANSI_RESET "\n");
            multilineMode = true;
        }
        
        if (multilineMode) {
            // Multiline input mode
            char source[4096] = "";
            strcat(source, trimmed);
            strcat(source, "\n");
            
            while (true) {
                printf(ANSI_GREEN "... " ANSI_RESET);
                
                if (!fgets(line, sizeof(line), stdin)) {
                    break;
                }
                
                // Remove newline
                length = strlen(line);
                if (length > 0 && line[length-1] == '\n') {
                    line[length-1] = '\0';
                }
                
                char* nextTrimmed = trim(line);
                
                if (strlen(nextTrimmed) == 0) {
                    break; // End multiline input on empty line
                }
                
                strcat(source, nextTrimmed);
                strcat(source, "\n");
            }
            
            if (strlen(source) > 0) {
                interpret(source);
            }
            
            multilineMode = false; // Reset to single-line mode
        } else {
            // Single-line execution
            interpret(trimmed);
        }
    }
}

static char *readFile(const char *path)
{
    FILE *file = fopen(path, "rb");
    if (file == NULL)
    {
        fprintf(stderr, "Could not open file \"%s\".\n", path);
        exit(74);
    }

    fseek(file, 0L, SEEK_END);
    size_t fileSize = ftell(file);
    rewind(file);

    char *buffer = (char *)malloc(fileSize + 1);
    if (buffer == NULL)
    {
        fprintf(stderr, "Not enough memory to read \"%s\".\n", path);
        exit(74);
    }

    size_t bytesRead = fread(buffer, sizeof(char), fileSize, file);
    if (bytesRead < fileSize)
    {
        fprintf(stderr, "Could not read file \"%s\".\n", path);
        exit(74);
    }

    buffer[bytesRead] = '\0';

    fclose(file);
    return buffer;
}

static void runFile(const char *path)
{
    char *source = readFile(path);
    InterpretResult result = interpret(source);
    free(source);

    if (result == INTERPRET_COMPILE_ERROR)
        exit(65);
    if (result == INTERPRET_RUNTIME_ERROR)
        exit(70);
}

int main(int argc, char const *argv[])
{
    initVM();

    if (argc == 1)
    {
        repl();
    }
    else if (argc == 2)
    {
        runFile(argv[1]);
    }
    else
    {
        fprintf(stderr, "Usage: clox [path]\n");
        exit(64);
    }

    freeVM();
    return 0;
}