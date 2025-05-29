
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

## 🛠️ Build & Run

### CLox

```bash
cd CLox
make
./clox path/to/file.lox
````

### JLox

```bash
cd JLox
javac *.java
java jlox path/to/file.lox
```

## 📖 More Details

For a complete overview of the Lox language, its syntax, semantics, and implementation notes, refer to [Lox.md](Lox.md).

## 📝 License

This project is licensed under the [MIT License](LICENSE).

---
