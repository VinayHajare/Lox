# Makefile for JLox and CLox
JLOX_SRC_DIR = JLox
JLOX_BUILD_DIR = build
JAVA_MAIN = JLox.lox.Lox
GENAST_CLASS = JLox.tool.GenerateAST
ASTPRINT_CLASS = JLox.lox.ASTPrinter

# Out directory of GENAST_CLASS
OUTDIR ?= JLox/lox

# Default test file
FILE ?= test/test.lox

# CLox settings
CLOX_SRC_DIR = CLox
CLOX_BUILD_DIR = $(JLOX_BUILD_DIR)
CLOX_BINARY = $(CLOX_BUILD_DIR)/clox

.PHONY: all jlox clox run repl generate_ast print_ast clean

all: jlox clox

jlox:
	@if not exist "$(JLOX_BUILD_DIR)" mkdir "$(JLOX_BUILD_DIR)"
	@setlocal enabledelayedexpansion && \
	set SOURCES= && \
	for /r "$(JLOX_SRC_DIR)" %%f in (*.java) do ( \
		set SOURCES=!SOURCES! "%%f" \
	) && \
	javac -d "$(JLOX_BUILD_DIR)" !SOURCES!
	@echo JLox build complete.

clox:
	@if not exist "$(CLOX_BUILD_DIR)" mkdir "$(CLOX_BUILD_DIR)"
	@gcc -o "$(CLOX_BINARY)" $(wildcard $(CLOX_SRC_DIR)/*.c) -I"$(CLOX_SRC_DIR)"
	@echo CLox build complete.

run:
	@java -cp "$(JLOX_BUILD_DIR)" $(JAVA_MAIN) "$(FILE)"

repl:
	@java -cp "$(JLOX_BUILD_DIR)" $(JAVA_MAIN)

generate_ast:
	@java -cp "$(JLOX_BUILD_DIR)" $(GENAST_CLASS) "$(OUTDIR)"

print_ast:
	@java -cp "$(JLOX_BUILD_DIR)" $(ASTPRINT_CLASS)

clean:
	@if exist "$(JLOX_BUILD_DIR)" rmdir /s /q "$(JLOX_BUILD_DIR)"
	@echo Build cleaned.
