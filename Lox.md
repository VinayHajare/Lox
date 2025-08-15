# Lox 🦊- The Pretty Compact Language

Lox is a dynamically-typed, English-like programming language that derives its syntax from the C-family, with automatic memory management. It's small, simple, and high-level—but still powerful. Lox adopts lexical scoping similar to Scheme, with a clean and efficient implementation.  


## Sample Program

```Java
// Your first Lox program!
print "Hello, world!";
```

#### Note:

We have put `print` as a built in operator rather than a library function. 

## Features🛠️

- **Dynamic Typing**  
   Lox is dynamically typed. Variables can store value of any type, and single variable can even store values of different type at different time.
- **Automatic Memory Management**  
   It exists to eliminate error-prone low-level drudgery of manually managing allocation and freeing of memory by using _tracing grabage collection (GC)_.

## Data Types⚛️

In Lox's world there are only few built-in data types -

1. **Booleans**  
   Lox has Dedicated _Boolean_ type. "true" and "false" are yin and yang of software. There are two Boolean values, obviously:
   ```Java
   true; // Not false
   false; // Not *not* false
   ```
2. **Numbers**  
   It has one kind of number: double-precision floating point.
   ```Java
   10; // An integer.
   95.60; // A decimal number.
   ```
3. **Strings**
   A string literal enclosed in double quotes.
   ```Java
   "I am string";
   "" // Empty string
   "1234"; // This is a string, not number
   ```
4. **Nil**  
   The one who is never invited to the party but always seems to show up. It represents "no value". We spell it `nil`. Yes, we need it!!!

## Expressions

If built-in data type are atoms then **expressions** must be molecules.

1. **Arithmetic**  
   Lox Features basic arithmetic operators from your favourite language.
   ```Java
   add + me;
   subtract - me;
   multiply * me;
   divide / me;
   ```
   One arithmetic operator is actually both infix and prefix one.
   ```Java
   -NegateMe;
   ```
   All this operator works on numbers, except for the `+` which work with string to concatenate them.
2. **Comparison & Equality**  
   They always return a Boolean result, can compare numbers (and only numbers).
   ```Java
   less < than;
   lessThan <= orEqual;
   greater > than;
   greaterThan >= orEqual;
   ```
   We can test two values of any kind for equality or unequality:
   ```Java
   1 == 2; // false
   "cat" != "dog"; // true
   ```
   Even different types:
   ```Java
   3.14 == "pi"; // false
   ```
   Values of different types are never equivalent:
   ```Java
   123 == "123"; // false
   ```
3. **Logical Operators**  
   The not operator, a pefix `!`, returns false if operand is true and vice versa:
   ```Java
   !true; // false
   !false; // true
   ```
   The other two logical operands are just control flow construct - an `and` expression determines if the both operands are true. It
   returns the left operand if it’s false, or the right operand otherwise:
   ```Java
   true and false; // false
   true and true; // true
   ```
   And an `or` expression determines if either of two values (or both) are true. It returns the left operand if it is true and the right operand otherwise:
   ```Java
   false or false; // false
   true or false; // true
   ```
4. **Precedence and Grouping**  
   All of these operators have the same precedence and associativity that you expect coming from C-family.
   You can use `( )` to group stuff:
   ```Java
   var average = (min + max) / 2;
   ```

## Statements 📄

Where an expression's main job is to produce a _value_, a statment's job is to produce an _effect_.  
First one is:

```Python
print "Hello, World!";
```

Some statements like:

```Java
"some expression";
```

An expression followed by the semicolon (;), promotes the expression to statement-hood. This is called, an **expression statement**.
If you want to group a statments where a single is expected, you can wrap them in a block:

```Python
{
    print "Statement One.";
    print "Statement Two.";
}
```

Blocks also affect the scoping.

## Variables 📝

You declare variables with `var` keyword. If not initialize, it will assume `nil` as default value:

```Java
var iAmVariable = "Here is some value";
var iAmNil;
```

Once declared, we can access variables naturally with name:

```Java
var breakfast = "Poha";
print breakfast // Poha
breakfast = "Idli";
print breakfast; // Idli
```

## Control Flow 🎛️

An `if` statment executes one of two statements based on some condition:

```Java
if (condition) {
    print "yes";
} else {
    print "no";
}
```

A `while` loop executes the body repeatedly as long as the condition expression evalutes to true:

```Java
var a = 1;
while (a < 10) {
    print a;
    a = a + 1;
}
```

And at last we have `for` loop:

```Java
for (var a = 1; a < 10; a = a + 1) {
    print a;
}
```

## Functions

In Lox we define functions using `fun` keyword:

```python
fun printSum(a, b) {
    print a + b;
}
```

A function call expression looks the same as C-family:

```Java
printSum(2, 3);
```

Parenthesis are mandatory, if you leave them off, it doesn't call function, it just refers to it.
The body of the function is always a block, inside it we can use `return` to return a value:

```Java
fun returnSum(a, b) {
    return a + b;
}
```

If execution reaches end of the block without hiting a `return`, it implicitly return `nil`.

## Closures

Functions are _first class_ in Lox, which means they are real values that you can reference to, store in variables, pass arounf, etc. Like:

```Java
fun addPair(a, b) {
    return a + b;
}

fun identiy(a) {
    return a;
}

print identity(addPair)(1, 2); // prints 3
```

Functions are statements, so you can write local functions inside a function:

```Java
fun outerFunction() {
    fun lcoalFunction() {
        print "I am local";
    }

    localFunction();
}
```

If you combine local functions, first-class functions and block scope, we got into this amazing situation:

```Java
fun returnFunction() {
    var outside = "outside";

    fun inner() {
        print outside;
    }

    return inner;
}

var fn  = returnFunction();
fn();
```

For that to work, inner() has to “hold on” to references to any surrounding
variables that it uses so that they stay around even after the outer function has
returned. We call functions that do this **closures**.

## Classes

You declare a class and methods like so:

```Java
class Breakfast {
    cook() {
        print "Egg's are frying!";
    }

    serve(who) {
        print "Enjoy your breakfast, " + who + ".";
    }
}
```

The body of a class contains its method, they are like function declaration but without the `fun` keyword. When a class declaration is executed, Lox creates a class object and stores that in a variable named after the class. Just like functions, classes are also first-class in Lox.

```Java
// Store it in a variable
var someVariable = Breakfast;

// Pass it to the function
someFunction(someVariable);
```

In Lox, class itself is a factory function which returns the instance of itself:

```Java
var breakfast = Breakfast();
print breakfast // Breakfast instance.
```

### Instantiation & Initialization

Lox, like other dynamically-typed languages, lets you freely add properties onto objects:

```Java
breakfast.meat = "Sausage";
breakfast.bread = "sourdough";
```

Assigning to a field creates it if it doesn't exist.

If you want to access fields or methods on the current object from within a method, you use `this`:

```Java
class Breakfast {
    serve(who) {
        print "Enjoy your " + this.meat + " and " + this.bread +", " + who + ".";
    }

    // ...
}
```

To ensure that object is in valid state when its creatd, you can define a initializer. If your class has a method named `init()`, it is called automatically when the object is constructed. Any parameters passed to class are forwarded to the initializer:

```Java
class Breakfast() {
    init(meat, bread) {
        this.meat = meat;
        this.bread = bread;
    }
    // ...
}

var baconAndToast = Breakfast("bacon", "toast");
baconAndToast.serve("Dear reader");
// Enjoy your bacon and toast, Dear reader.
```

### Inheritance

Lox support single inheritance. When you declare a class, you can specify a class that it inherits from using less-than (<) operator:

```Java
class Brunch < Breakfast {
    drink() {
       print "How about a Tak?!";
    }
}

var benedict = Brunch("Ham", "English muffin");
benedict.serve("Reader");
```

Even `init()` method get inherited. But we need original one to be called so that supeclass can maintain its state.  
As in Java, you use **super** for that:

```Java
class Brunch < Breakfast {
    init(meat, bread, drink){
        super.init(meat, bread);
        this.drink = drink;
    }

    drink() {
       print "How about a " + this.drink + "?!";
    }
}
```
---
## Standard Library 📚

Lox keeps its standard library minimal by design. A few built-in functions and properties are available:

* `clock()` – Returns the number of seconds since the program started, useful for benchmarking.

```Java
print clock(); // 0.12345
```

Other functions (e.g., string manipulation, math operations) can be defined by the user or extended through the interpreter.

---

## Tokens in Lox ⚙️  
| Lexeme  | Token           |
|---------|-----------------|
|    (    |  LEFT_PAREN     |
|    )    |  RIGHT_PAREN    |
|    {    |  LEFT_BRACE     |
|    }    |  RIGHT_BRACE    |
|    ,    |  COMMA          |
|    .    |  DOT            |
|    -    |  MINUS          |
|    +    |  PLUS           |
|    /    |  SLASH          |
|    *    |  STAR           | 
|    !    |  BANG           |
|    !=   |  BANG_EQUAL     |
|    =    |  EQUAL          |
|    ==   |  EQUAL_EQUAL    |
|    <    |  GREATER        |
|    <=   |  GREATER_EQUAL  |
|    >    |  LESS           |
|    >=   |  LESS_EQUAL     |
|(A-Za-z_)(A-Za-z)* |   IDENTIFIER|
|   ""    |  STRING         |
|  (0-9)  |  NUMBER         |
|  and    |  AND            |
|  class  |  CLASS          |
|  else   |  ELSE           |
|  false  |  FALSE          |
|  for    |  FOR            |
|  fun    |  FUN            |
|  if     |  IF             |
|  nil    |  NIL            |
|  or     |  OR             |
|  print  |  PRINT          |
|  return |  RETURN         |
|  super  |  SUPER          |
|  this   |  THIS           |
|  true   |  TRUE           |
|  var    |  VAR            |
|  while  |  WHILE          |
   

## Operator Precedence⤵️  
Precedence rules are same as **C/Java**, going from *lowest* to *highest*:
 |Name      |Operators  |Associates|
 |----------|-----------|----------|
 |Equality  | == !=     | Left     |
 |Comparison| > >= < <= | Left     |
 |Term      | - +       | Left     |
 |Factor    | / *       | Left     |
 |Unary     | ! =       | Right    |
 |Primary   | Literals, parenthesized expression | - |  
 

## Grammar of Lox 🆎  
**program** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;declaration* EOF ;  
**declaration**&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;varDecl | statement ;  
**varDecl**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;"var" IDENTIFIER ( "=" expression )? ";" ;  
**statement**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;exprStmt | forStmt | ifStmt | whileStmt | printStmt | block;  
**block**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;"{" declaration* "}" ;  
**exprStmt**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;expression ";" ;  
**forStmt**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;"for" "(" ( varDecl | exprStmt | ";" )    expression? ";" expression? ")" statement ;
**ifStmt**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;"if" "(" expression ")" statement ( "else" statement )? ;  
**whileStmt**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;"while" "(" expression ")" statement ;  
**printStmt**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;"print" expression ";" ;  
**expression** &nbsp;&nbsp;&nbsp;&nbsp;→&nbsp; assignment ;  
**assignment**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;IDENTIFIER "=" assignment | logic_or ;  
**logic_or**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;logic_and ( "or" logic_and )* ;  
**logic_and**&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp;equality ( "and" equality )* ;  
**equality** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp; comparison ( ( "!=" | "==" ) comparison )* ;  
**comparison** &nbsp;&nbsp;→&nbsp; term ( ( ">" | ">=" | "<" | "<=" ) term )* ;  
**term** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp; factor ( ( "-" | "+" ) factor )* ;  
**factor** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp; unary ( ( "/" | "*" ) unary )* ;  
**unary** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp; ( "!" | "-" ) unary | primary ;  
**primary** &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;→&nbsp; NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER ;  


We are going to use **Recursive Descent** parser, which is top-down type of parser. They are simple, fast, robust, and can support sophisticated error handling. In a top-down parser, you reach the lowest-precedence expressions first because they may in turn contain subexpressions of higher precedence. It starts from the top or outermost grammar rule (here expression) and works its way down into the nested subexpressions before finally reaching the leaves of the syntax tree.

## Limitations & Design Choices ⚖️

* **Not a pure object-oriented language** – While Lox supports classes and instances, primitive types are not objects.
* **Single inheritance** – Multiple inheritance is intentionally left out to keep the language simple.
* **No modules or imports (yet)** – Lox is kept compact, but can be extended in the interpreter if needed.
* **Scripting-focused** – Lox is designed for small programs, rapid prototyping, and educational use.

---