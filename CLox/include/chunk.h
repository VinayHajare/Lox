#ifndef clox_chunk_h
#define clox_chunk_h

#include "common.h"
#include "value.h"

typedef enum
{
    OP_CONSTANT,
    OP_CONSTANT_LONG,
    OP_NIL,
    OP_TRUE,
    OP_FALSE,
    OP_POP,
    OP_GET_LOCAL,
    OP_SET_LOCAL,
    OP_GET_GLOBAL,
    OP_GET_GLOBAL_LONG,
    OP_DEFINE_GLOBAL,
    OP_DEFINE_GLOBAL_LONG,
    OP_SET_GLOBAL,
    OP_SET_GLOBAL_LONG,
    OP_GET_UPVALUE,
    OP_SET_UPVALUE,
    OP_GET_PROPERTY,
    OP_SET_PROPERTY,
    OP_EQUAL,
    OP_GREATER,
    OP_LESS,
    OP_ADD,
    OP_SUBTRACT,
    OP_MULTIPLY,
    OP_DIVIDE,
    OP_NOT,
    OP_NEGATE,
    OP_PRINT,
    OP_JUMP,
    OP_JUMP_IF_FALSE,
    OP_LOOP,
    OP_CALL,
    OP_INVOKE,
    OP_CLOSURE,
    OP_CLOSURE_LONG,
    OP_CLOSE_UPVALUE,
    OP_RETURN,
    OP_CLASS,
    OP_CLASS_LONG,
    OP_GET_PROPERTY_LONG,
    OP_SET_PROPERTY_LONG,
    OP_METHOD
} OpCode;

typedef struct
{
    int offset;
    int line;
} LineStart;

typedef struct
{
    int capacity;
    int count;
    uint8_t *code;
    ValueArray constants;
    int lineCount;
    int lineCapacity;
    LineStart *lines;
} Chunk;

void initChunk(Chunk *chunk);
void writeChunk(Chunk *chunk, uint8_t byte, int line);
int addConstant(Chunk *chunk, Value value);
void writeConstant(Chunk *chunk, Value value, int line);
int getLine(Chunk *chunk, int instruction);
void freeChunk(Chunk *chunk);

#endif