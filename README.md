A Result Type for Java!
## Description
At a high level, a result is very similar to an option, but instead of it being something or nothing, it's something or an error.

## Usage:
This type is instantiated with the static `of` method: 
```java
Result<Integer, String> result = Result.ok(0);
Result<String, Throwable> anotherResult = Result.ok("This is cool:)");
```
You can also instantiate the error variant by passing a supplier of an error.
```java
Result<Integer, IllegalArgumentException> evilResult = Result.of(new IllegalArgumentException("An evil error"));
```
And for some reason if you find a function that unwisely throws instead of returning a result. You can (partiallly) address these crimes using the `fromFunction` passing in a supplier that calls the method to capture the exception, and return a result.

```java
Result<Integer, Throwable> goodDivision = Result.fromFunction(()->divide(2, 4));
Result<Integer, Throwable> badDivision = Result.fromFunction(()->divide(4, 0));
assert goodDivision.isOk();
assert badDivision.hasError();
assert badDivision.getError() instanceof ArithmeticException;
```

You can also do fun stuff like:
```java
String evaluation = Result.fromFunction(()->divide(4, 0))
          .map(n-> "Your Result is: " + n)
          .orElse("Invalid");
```

### Breaking! Now supports match style statements!

```java
Result<Integer> r0 = Result.of(1);
Result<Integer> r1 = Result.of(IllegalArgumentException::new);
Result<Integer> r2 = Result.of(AssertionError::new);
Result<Integer> r3 = Result.of(Error::new);

for (int i = 0; i < 4; i++) {
  int res = List.of(r0, r1, r2, r3).get(i).match(
          x -> 0,
          e -> 3,  // Match arm that gets run if below aren't matched
          (IllegalArgumentException e) -> 1,
          (AssertionError e) -> 2
  );
  assert res == i;
}
```
