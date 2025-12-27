
# 🦊 Lox Interpreter

This repository contains two implementations of the [Lox language](Lox.md) from the book *Crafting Interpreters*:

- **CLox** — A tree-walk bytecode interpreter written in C.
- **JLox** — A tree-walk interpreter written in Java.

The goal of this project is to gain a deep understanding of interpreter design by implementing Lox in two different styles and languages.

## 📁 Directory Structure

```

Lox/
├── CLox/       # Lox implemented in C (bytecode VM)
├── JLox/       # Lox implemented in Java (tree-walk)
└── Lox.md      # Detailed language description and design notes

````
*Note: Make sure you are in Lox directory before running below commands.*
## 🛠️ Build & Run

*Note: Make sure you are in Lox directory before running below commands.*  

### CLox

```bash
# Build CLox
make clox

# Run a source file
./build/clox path/to/file.lox

# Run REPL mode
./build/clox
````

### JLox

```bash
# Build JLox
make jlox

# Run REPL mode
make repl

# Run a source file
make run FILE=path/to/file
```

## 📖 More Details

For a complete overview of the Lox language, its syntax, semantics, and implementation notes, refer to [Lox.md](Lox.md).

## 📝 License

This project is licensed under the [MIT License](LICENSE).

---
