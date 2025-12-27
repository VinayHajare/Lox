#include "include/common.h"
#include "include/chunk.h"
#include "include/debug.h"

int main(int argc, char const *argv[])
{
    Chunk chunk;
    initChunk(&chunk);

    int constant = addConstant(&chunk, 1.2);
    writeChunk(&chunk, OP_CONSTANT, 123);
    writeChunk(&chunk, constant, 123);
    writeConstant(&chunk, 242424, 124);
    writeChunk(&chunk, OP_RETURN, 125);

    disassembleChunk(&chunk, "test chunk");

    freeChunk(&chunk);
    return 0;
}
