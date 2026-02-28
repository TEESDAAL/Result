package result;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ExampleTests {
	@Test
	public void instantiationExample1() {
		Result<Integer, String> result = Result.ok(0);
		Result<String, Throwable> anotherResult = Result.ok("This is cool:)");
		Result<Integer, IllegalArgumentException> evilResult = err(new IllegalArgumentException("An evil error"));
		assertEquals(0, result.get());
		assertEquals("This is cool:)", anotherResult.get());
		assertEquals("An evil error", evilResult.getError().getMessage());
	}

	@Test
	public void fromFunctionExample() {
		Result<Double, Throwable> goodDivision = Result.fromFunction(()->divide(2, 4));
		Result<Double, Throwable> badDivision = Result.fromFunction(()->divide(4, 0));
		assertTrue(goodDivision.isOk());
		assertTrue(badDivision.hasError());
        assertInstanceOf(ArithmeticException.class, badDivision.getError());
	}

    @Test
    void mapExample() {
        String evaluation = Result.fromFunction(()->divide(4, 0))
                .map(n-> "Your Result is: " + n)
                .orElse("Invalid calculation");
        assertEquals("Invalid calculation", evaluation);
    }

    @Test
    void matchExample() {
        String ADMIN_PASSWORD = "password";
        Map<Result<String, Integer>, String> GETResults = Map.of(
                Result.err(404), "404: page not found",
                Result.err(400), "400: Client Error",
                Result.err(500), "500: Server Error :(",
                Result.err(600), "Unknown error code: 600",
                Result.ok("Obtained from Server:)"), "Obtained from Server:)",
                Result.ok("Password is "+ADMIN_PASSWORD), "You tried to retrieve confidential information"
        );
        for (Result<String, Integer> response : GETResults.keySet()) {
            assertEquals(GETResults.get(response), Result.match(
                    response,
                    s -> s,
                    e -> "Unknown error code: " + e,
                    MatchArm.error(404, i -> i + ": page not found"),
                    MatchArm.error(400, i -> i + ": Client Error"),
                    MatchArm.err(i -> i >= 500 && i < 600, i -> i + ": Server Error :("),
                    MatchArm.ok(s -> s.contains(ADMIN_PASSWORD), s -> "You tried to retrieve confidential information")
            ));
        }
    }

	static double divide(double a, double b) throws ArithmeticException {
		if (b == 0) {
			throw new ArithmeticException("Cannot divide by 0");
		}

		return a / b;
	}


}
