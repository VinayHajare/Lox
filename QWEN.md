# 🦊 Lox Interpreter Project - QWEN Context

## Project Overview

This is a Lox interpreter project implementing the Lox language from the book *Crafting Interpreters*. The project contains two implementations:

- **CLox** — A tree-walk bytecode interpreter written in C
- **JLox** — A tree-walk interpreter written in Java

The goal is to gain deep understanding of interpreter design by implementing Lox in two different styles and languages.

## Directory Structure

```
Lox/
├── CLox/               # Lox implemented in C (bytecode VM)
│   ├── include/        # Header files
│   └── src/            # Source files (chunk.c, compiler.c, debug.c, main.c, memory.c, scanner.c, value.c, vm.c)
├── JLox/               # Lox implemented in Java (tree-walk)
│   ├── lox/            # Main interpreter source files
│   └── tool/           # Tools (e.g., AST generator)
├── test/               # Lox test suite organized by feature
├── test-runner/        # Fast Rust test suite runner & benchmark script
├── build/              # Build output directory
├── Lox.md              # Detailed language description and design notes
├── Makefile            # Build system
└── README.md           # Project documentation
```

## Lox Language Features

Lox is a dynamically-typed, English-like programming language with:
- Dynamic typing with automatic memory management (tracing garbage collection)
- C-family syntax with lexical scoping
- Built-in data types: Booleans, Numbers (double-precision), Strings, and Nil
- Control flow: if/else, while, for loops
- Functions with first-class support and closures
- Classes with single inheritance and methods
- Standard library function: `clock()` for benchmarking

## Building and Running

### Prerequisites
- GCC (for CLox)
- Java JDK (for JLox)
- Rust/Cargo (for test runner)

### Build Commands
```bash
# Build both interpreters
make all

# Build CLox only
make clox

# Build JLox only
make jlox

# Clean build directory
make clean
```

### Running Interpreters
```bash
# Run CLox REPL
./build/clox

# Run CLox on a file
./build/clox path/to/file.lox

# Run JLox REPL
make repl

# Run JLox on a file
make run FILE=path/to/file.lox
```

### Testing
```bash
# Test CLox against test suite
./test-runner/target/release/test-runner.exe clox

# Test JLox against test suite
./test-runner/target/release/test-runner.exe jlox
```

### Benchmarking
```bash
# Run benchmark for CLox
./test-runner/target/release/benchmark.exe clox

# Run benchmark for JLox
./test-runner/target/release/benchmark.exe jlox

# Compare both interpreters
./test-runner/target/release/benchmark.exe clox jlox
```

## Implementation Details

### CLox (C Implementation)
- Bytecode virtual machine architecture
- Files include: scanner, compiler, virtual machine, memory management, debugging utilities
- Uses chunk-based bytecode representation

### JLox (Java Implementation)
- Tree-walk interpreter
- Core classes include: Scanner, Parser, Interpreter, Resolver
- AST representation with Expr.java and Stmt.java
- Environment-based variable resolution with closures support

### Test Suite
- Organized by language features (assignments, classes, functions, etc.)
- Comprehensive test coverage for all language constructs
- Used by the Rust-based test runner for validation

## Development Conventions

- The project follows the implementation approach from "Crafting Interpreters" book
- Both implementations aim to be functionally equivalent
- Test-driven development approach with extensive test coverage
- Makefile-based build system for cross-platform compatibility

## Key Files and Components

- `Lox.md`: Complete language specification and grammar
- `Makefile`: Build automation for both interpreters
- `test/`: Comprehensive test suite organized by feature
- `test-runner/`: Rust-based testing and benchmarking infrastructure
- `CLox/src/`: C implementation source files
- `JLox/lox/`: Java implementation source files