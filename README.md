A Result Type for Java!
## Description
At a high level, a result is very similar to an option, but instead of it being something or nothing, it's something or an error.

## Usage:
This type is instantiated with the static `of` method: 
```java
Result<Integer, String> result = Result.ok(0);
Result<String, Throwable> anotherResult = Result.ok("This is cool:)");
assertEquals(0, result.get());
assertEquals("This is cool:)", anotherResult.get());
```
You can also instantiate the error variant by passing a supplier of an error.
```java
Result<Integer, IllegalArgumentException> evilResult = Result.err(new IllegalArgumentException("An evil error"));
assertEquals("An evil error", evilResult.getError().getMessage());
```
And for some reason if you find a function that unwisely throws instead of returning a result. You can (partiallly) address these crimes using the `fromFunction` passing in a supplier that calls the method to capture the exception, and return a result.

```java
Result<Double, Throwable> goodDivision = Result.fromFunction(()->divide(2, 4));
Result<Double, Throwable> badDivision = Result.fromFunction(()->divide(4, 0));
assert goodDivision.isOk();
assert badDivision.hasError();
assert badDivision.getError() instanceof ArithmeticException;
```

You can also do fun stuff like:
```java
String evaluation = Result.fromFunction(()->divide(4, 0))
        .map(n-> "Your Result is: " + n)
        .orElse("Invalid calculation");
assertEquals("Invalid calculation", evaluation);
```

### Breaking! Now supports match style statements!
Results also support match style statements using the below syntax.
```java
String ADMIN_PASSWORD = "password";
Map<Result<String, Integer>, String> GETResults = Map.of(
        Result.err(404), "404: page not found",
        Result.err(400), "400: Client Error",
        Result.err(500), "500: Server Error :(",
        Result.err(600), "Unknown error code: 600",
        Result.ok("Obtained from Server:)"), "Obtained from Server:)",
        Result.ok("Password is "+ADMIN_PASSWORD), "You tried to retrieve confidential information"
);
for (Result<String, Integer> response : GETResults.keySet()){
    assertEquals(GETResults.get(response), Result.match(
        response,
        s -> s,
        e -> "Unknown error code: "+e,
        MatchArm.error(404, i -> i+": page not found"),
        MatchArm.error(400, i -> i+": Client Error"),
        MatchArm.err(i -> i>=500 && i<600, i -> i +": Server Error :("),
        MatchArm.ok(s -> s.contains(ADMIN_PASSWORD), s -> "You tried to retrieve confidential information")));
    ));
}
```
