# Homework 1: Implement the OurPL Lexer

## What You Must Complete

You must complete these parts of the starter:

1. `TokenType.java`
2. the `keywords` hash map in `Lexer.java`
3. `scanToken()`
4. `string()`
5. `number()`
6. `identifier()`

Edit only:

- `hw1-OurPL-starter/src/main/java/cpsc326/Lexer.java`
- `hw1-OurPL-starter/src/main/java/cpsc326/TokenType.java`

## How To Test

All testing should be done from the `hw1-OurPL-starter` directory.

Use these [Maven](https://maven.apache.org/install.html) commands:

- `mvn test` runs the tests
- `mvn clean test` deletes old build files and runs the tests again
- `mvn compile` recompiles the project
- `mvn exec:java -Dexec.args="examples/example1.opl"` runs the lexer on one example file
- `mvn clean compile exec:java -Dexec.args="examples/example1.opl"` is the safest way to rebuild and then run the lexer

If you change code and the output looks old or incorrect, run `mvn clean compile`
first and then run the file again.

The first Maven run may download dependencies. That is normal.

You should also run the lexer on the example files in:

- `hw1-OurPL-starter/examples`

Example:

```bash
mvn exec:java -Dexec.args="examples/example1.opl"
```

You can replace `example1.opl` with any other file in `examples`.

Check that the output tokens are correct.

## Grading

Your score depends on both tests and example-file output.

- each failed test is `-1` point
- each incorrect lexer output on the required example files is `-1` point

Do not rely only on the tests.

Before submitting, you should:

- run the tests
- run your lexer on the example files
- check that the printed tokens match the expected output

## Rules

- Do not create new methods.
- Use the methods already declared in the starter.
- You must implement `scanToken()`, `string()`, `number()`, and `identifier()`.
- `scanToken()` must call `string()`, `number()`, and `identifier()` when needed.

## Step 1: Fill In `TokenType.java`

Add all token types for the lexer.

Use the token names from class.

This will be checked.

You should add token types for:

- `LEFT_PAREN`, `RIGHT_PAREN`
- `LEFT_BRACE`, `RIGHT_BRACE`
- `COMMA`, `DOT`, `PLUS`, `MINUS`, `STAR`, `SLASH`, `SEMICOLON`
- `EQUAL`, `EQUAL_EQUAL`
- `BANG`, `BANG_EQUAL`
- `GREATER`, `GREATER_EQUAL`
- `LESS`, `LESS_EQUAL`
- `IDENTIFIER`, `STRING`, `NUMBER`
- `AND`, `ELSE`, `FALSE`, `FOR`, `FUN`, `IF`, `NIL`, `OR`, `PRINT`, `RETURN`, `STRUCT`, `THIS`, `TRUE`, `VAR`, `WHILE`
- `EOF`

## Step 2: Fill In The `keywords` Hash Map

In `Lexer.java`, the `keywords` map is incomplete.

You must add all reserved words.

The map should contain:

- `"and"`
- `"or"`
- `"struct"`
- `"else"`
- `"false"`
- `"true"`
- `"for"`
- `"fun"`
- `"if"`
- `"nil"`
- `"print"`
- `"return"`
- `"this"`
- `"while"`
- `"var"`

Later, `identifier()` will use this map to decide whether the scanned text is:

- a reserved word
- a regular identifier

## Step 3: Implement `scanToken()`

This is the main method of the homework.

`scanToken()` should:

1. read one character using `advance()`
2. decide what starts with that character
3. add a token, skip input, or call another method

Start with the simple single-character tokens.

When `scanToken()` sees these characters, it should add these tokens:

- `(` -> `LEFT_PAREN`
- `)` -> `RIGHT_PAREN`
- `{` -> `LEFT_BRACE`
- `}` -> `RIGHT_BRACE`
- `,` -> `COMMA`
- `.` -> `DOT`
- `+` -> `PLUS`
- `-` -> `MINUS`
- `*` -> `STAR`
- `/` -> `SLASH`
- `;` -> `SEMICOLON`

Then handle the one-character or two-character operators:

- `!` or `!=`
- `=` or `==`
- `<` or `<=`
- `>` or `>=`

Use `match()` for these cases.

The lexer should choose the two-character operator when possible.

Then handle comments and whitespace:

- if the character is `#`, skip everything until newline or end of input
- if the character is space, tab, or carriage return, ignore it
- if the character is newline, increment `line`


## Step 4: Implement `string()`

`scanToken()` should call `string()` after it reads the opening `"`.

`string()` should:

- keep consuming characters until it finds the closing `"`
- if it sees a newline before the closing `"`, increment `line`
- if it reaches end of input before the closing `"`, report an error
- after finding the closing `"`, add a string token

When you add the token:

- the lexeme should include the quotes
- the literal value should contain only the inside text

Example:

- source text: `"abc"`
- lexeme: `"abc"`
- literal: `abc`

## Step 5: Implement `number()`

`scanToken()` should call `number()` after it reads the first digit.

`number()` should:

- consume the rest of the digits
- if it sees `.` followed by a digit, continue consuming the fractional part
- stop when the number ends
- add a number token with the correct numeric literal value

Examples:

- `12.34` -> one number token
- `123.` -> number token, then dot token
- `.5` -> dot token, then number token

## Step 6: Implement `identifier()`

`scanToken()` should call `identifier()` after it reads the first letter of an identifier.

Identifier rules:

- the first character must be a letter
- later characters may be letters, digits, or `_`
- `_` cannot be the first character
- uppercase and lowercase letters are both allowed

`identifier()` should:

- continue consuming letters, digits, and `_`
- stop when the identifier ends
- get the full text
- check the `keywords` map
- if the text is a reserved word, add the reserved-word token
- otherwise, add the identifier token

Keyword matching is case-sensitive.

Examples:

- `print` is a reserved word
- `Print` is an identifier
- `abc123` is an identifier
- `abc_def` is an identifier
- `_abc` is not a valid identifier start

## Final Behavior

Your lexer should:

- scan the entire input
- continue after lexical errors
- track line numbers correctly
- skip comments and whitespace
- add one final `EOF` token at the end

## Suggested Order

Implement in this order:

1. `TokenType.java`
2. `keywords` map
3. simple cases in `scanToken()`
4. `!=`, `==`, `<=`, `>=`
5. comments and whitespace
6. `string()`
7. `number()`
8. `identifier()`
9. error handling


## Testing

Run the lexer tests often while you work.
