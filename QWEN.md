# Lox Interpreter Project Documentation

## Project Overview

The Lox interpreter project contains two complete implementations of the Lox programming language from the book "Crafting Interpreters" by Robert Nystrom:

- **CLox**: A tree-walk bytecode interpreter written in C with a virtual machine
- **JLox**: A tree-walk interpreter written in Java

The goal of this project is to provide hands-on experience with interpreter design by implementing the same language in two different styles and languages, demonstrating various approaches to parsing, evaluation, and execution.

## Architecture

### CLox (C Implementation)
- **Language**: C
- **Execution Model**: Bytecode virtual machine with tree-walk interpretation
- **Structure**: Modular C code organized into source files and header files
  - `src/`: Contains implementation files (.c)
  - `include/`: Contains header files (.h)
- **Key Components**:
  - Scanner: Lexical analysis
  - Compiler: Parsing and bytecode generation
  - Virtual Machine: Bytecode execution
  - Memory management: Garbage collection
  - Objects: String, function, and class representations

### JLox (Java Implementation)
- **Language**: Java
- **Execution Model**: Direct tree-walk interpretation
- **Structure**: Object-oriented Java code in packages
  - `JLox/lox/`: Core interpreter classes
  - `JLox/tool/`: Code generation utilities
- **Key Components**:
  - Scanner: Tokenization
  - Parser: Syntax analysis and AST construction
  - Interpreter: Direct execution of AST nodes
  - Resolver: Variable scoping and resolution
  - Environment: Variable storage and lookup

## Building and Running

### Prerequisites
- **CLox**: GCC compiler, Make utility
- **JLox**: Java JDK 8 or higher
- **Test Runner**: Rust (for the test runner and benchmark tools)

### Build Commands
From the project root directory (`D:\Vinay Hajare\Lanuguage Hacking\Lox`):

```bash
# Build both interpreters
make all

# Build only CLox
make clox

# Build only JLox
make jlox

# Clean build artifacts
make clean
```

### Running Interpreters

#### CLox
```bash
# Run REPL mode
./build/clox

# Run a Lox source file
./build/clox path/to/file.lox
```

#### JLox
```bash
# Run REPL mode
make repl

# Run a Lox source file
make run FILE=path/to/file

# Or directly with Java
java -cp build JLox.lox.Lox path/to/file.lox
```

## Testing Infrastructure

### Test Suite
Located in the `test/` directory, organized into subdirectories by feature:
- `assignment/`, `bool/`, `class/`, `closure/`, `constructor/`, `expressions/`, `field/`, `for/`, `function/`, `if/`, `inheritance/`, `method/`, `nil/`, `number/`, `operator/`, `print/`, `return/`, `scanning/`, `string/`, `super/`, `this/`, `variable/`, `while/`

### Test Runner
A Rust-based test runner located in `test-runner/` that can execute tests against both interpreters:
```bash
# Test CLox
./test-runner/target/release/test-runner.exe clox

# Test JLox
./test-runner/target/release/test-runner.exe jlox
```

### Benchmarking
Performance comparison tools are also available:
```bash
# List available benchmarks
./test-runner/target/release/benchmark.exe --list

# Run benchmark for CLox
./test-runner/target/release/benchmark.exe clox

# Run benchmark for JLox
./test-runner/target/release/benchmark.exe jlox

# Compare both implementations
./test-runner/target/release/benchmark.exe clox jlox
```

## Lox Language Features

### Core Features
- **Dynamic Typing**: Variables can store values of any type
- **Automatic Memory Management**: Tracing garbage collection
- **Lexical Scoping**: Similar to Scheme
- **First-Class Functions**: Functions as values with closures
- **Object-Oriented Programming**: Classes with inheritance

### Data Types
- **Booleans**: `true`, `false`
- **Numbers**: Double-precision floating point
- **Strings**: Enclosed in double quotes
- **Nil**: Represents "no value"
- **Objects**: Instances of classes
- **Functions**: Callable entities with closure support

### Syntax Elements
- **Variables**: Declared with `var` keyword
- **Control Flow**: `if/else`, `while`, `for` loops
- **Functions**: Defined with `fun` keyword
- **Classes**: Defined with `class` keyword, supporting methods and inheritance
- **Special Keywords**: `and`, `or`, `class`, `else`, `false`, `for`, `fun`, `if`, `nil`, `print`, `return`, `super`, `this`, `true`, `var`, `while`

## Development Conventions

### Code Organization
- **CLox**: Follows C modular programming with header/source separation
- **JLox**: Uses Java packages and object-oriented design patterns
- Both implementations follow the same semantic structure despite different languages

### Error Handling
- **Compile-time Errors**: Syntax and resolution errors
- **Runtime Errors**: Execution errors with stack trace information
- Both implementations use consistent error reporting mechanisms

### AST Structure
Both interpreters use Abstract Syntax Trees (ASTs) to represent parsed code:
- **Expr**: Expression nodes (literals, binary/unary operations, variables, etc.)
- **Stmt**: Statement nodes (expressions, if/while loops, prints, etc.)

### Testing Approach
- Comprehensive test coverage organized by language feature
- Integration tests to verify both implementations behave identically
- Performance benchmarks to compare execution speed

## Project Structure

```
Lox/
├── CLox/               # Lox implemented in C (bytecode VM)
│   ├── include/        # Header files
│   └── src/            # Source files
├── JLox/               # Lox implemented in Java (tree-walk)
│   ├── lox/            # Core interpreter implementation
│   └── tool/           # Code generation utilities
├── test/               # Lox test suite organized by feature
├── test-runner/        # Fast Rust test suite runner & benchmark script
├── build/              # Compiled binaries and artifacts
├── Makefile            # Build automation
├── Lox.md              # Detailed language specification
├── README.md           # Project overview
└── LICENSE             # MIT license
```

## Key Files and Their Purpose

- `Lox.md`: Complete language specification including syntax, semantics, and grammar
- `Makefile`: Cross-platform build system (supports Windows with cmd.exe)
- `JLox/lox/Lox.java`: Main entry point for Java interpreter with REPL implementation
- `CLox/src/main.c`: Main entry point for C interpreter with REPL implementation
- `test-runner/Cargo.toml`: Rust test runner dependencies and configuration

## Development Workflow

1. **Implementation**: Modify either CLox or JLox implementations to add features
2. **Testing**: Run test suite against modified implementation
3. **Verification**: Ensure both implementations maintain compatibility
4. **Benchmarking**: Compare performance characteristics if relevant

This project serves as an excellent educational resource for understanding interpreter construction techniques, language design decisions, and implementation trade-offs between different approaches (tree-walk vs. bytecode VM).