A Result Type for Java!
## Description
At a high level, a result is very similar to an option, but instead of it being something or nothing, it's something or an error.

## Usage:
This type is instantiated with the static `of` method: 
```java
Result<Integer> result = Result.of(0);
Result<String> anotherResult = Result.of("This is cool:)");
```
You can also instantiate the error variant by passing a supplier of an error.
```java
Result<Integer> evilResult = Result.of(()-> new IllegalArgumentException(s));
```
And for some reason if you find a function that unwisely throws instead of returning a result. You can (partiallly) address these crimes using the `fromFunction` passing in a supplier that calls the method to convert the exception it would throw into a result.

```java
Result<Integer> goodDivision = Result.fromFunction(()->divide(2, 4));
Result<Integer> badDivision = Result.fromFunction(()->divide(4, 0));
assert goodDivision.isOk();
assert badDivision.hasError();
assert badDivision.getError().getCause() instanceof ArithmeticException;
```

You can also do fun stuff like:
```java
String evaluation = Result.fromFunction(()->divide(4, 0))
          .map(n-> "Your Result is: " + n)
          .orElse("Invalid");
```
