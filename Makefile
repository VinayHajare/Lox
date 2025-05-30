# Makefile for JLox - now with script file argument support

JLOX_SRC_DIR = JLox
JLOX_BUILD_DIR = build
JAVA_MAIN = JLox.lox.Lox
GENAST_CLASS = JLox.tool.GenerateAST
ASTPRINT_CLASS = JLox.lox.ASTPrinter

# Out directory of GENAST_CLASS
OUTDIR ?= JLox/lox

# Default test file
FILE ?= test/example.lox

.PHONY: all jlox run repl generate_ast print_ast clean

all: jlox

jlox:
	@if not exist "$(JLOX_BUILD_DIR)" mkdir "$(JLOX_BUILD_DIR)"
	@setlocal enabledelayedexpansion && \
	set SOURCES= && \
	for /r "$(JLOX_SRC_DIR)" %%f in (*.java) do ( \
		set SOURCES=!SOURCES! "%%f" \
	) && \
	javac -d "$(JLOX_BUILD_DIR)" !SOURCES!
	@echo JLox build complete.

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
