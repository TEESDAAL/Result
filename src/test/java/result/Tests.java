package result;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class Tests {
    final int FUZZ_TEST_NUM = 100;
    final Random rand = new Random();

    static void assertAllEqual(Object... objects) {
        IntStream.range(0, objects.length - 1)
                .forEach(i -> assertEquals(objects[i], objects[i + 1]));
    }

    static void assertAllThrow(Supplier<?>... suppliers) {
        for (Supplier<?> supplier : suppliers) {
            try {
                supplier.get();
                assert false;
            } catch (Exception e) {
                assert true;
            }
        }
    }

    // https://www.baeldung.com/java-random-string
    String randomString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;

        return rand.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    void fuzzString(Consumer<String> test) {
        IntStream.range(0, FUZZ_TEST_NUM)
                .parallel()
                .mapToObj(i -> randomString())
                .forEach(test);
    }

    void fuzzInt(Consumer<Integer> test) {
        IntStream.range(0, FUZZ_TEST_NUM)
                .parallel()
                .mapToObj(i -> rand.nextInt())
                .forEach(test);
    }

    @Test
    void factoryTestAndEqualityCheck() {
        fuzzInt(x -> {
            Result<Integer, ?> r = Result.ok(0);
            assertFalse(() -> r.equals(null));
            assertTrue(() -> r.equals(r));
            assertFalse(() -> r.equals(new Object()));
        });

        fuzzInt(x -> {
            assertEquals(Result.ok(x), Result.ok(x));
            assertEquals(Result.ok(x).hashCode(), Result.ok(x).hashCode());
            assertEquals(Result.ok(x).get(), x);
        });

        fuzzInt(x -> {
            Result<Integer, Throwable> r = Result.err(new IllegalStateException());
            assertFalse(() -> r.equals(null));
            assertTrue(() -> r.equals(r));
            assertFalse(() -> r.equals(new Object()));
        });

        // Throwables don't have a nice equals method...
        fuzzString(s -> {
            IllegalArgumentException e = new IllegalArgumentException(s);
            Result<String, IllegalArgumentException> r1 = Result.err(e);
            Result<String, IllegalArgumentException> r2 = Result.err(e);

            assertEquals(r1, r2);
            assertEquals(r1.hashCode(), r2.hashCode());
            assertEquals(r1.toString(), r2.toString());
            assertThrows(RuntimeException.class, r1::get);
        });


    }


    @Test
    void fromFunctionTest() {
        fuzzString(s -> {
            Result<Integer, Throwable> r1 = Result.fromFunction(() -> throwsError(s));
            Result<Void, Throwable> r2 = Result.fromFunction(() -> voidThrowsError(s));

            assertTrue(Result.fromFunction(() -> {
            }).isOk());
            assertTrue(Result.fromFunction(() -> s).isOk());

            assertThrows(RuntimeException.class, r1::get, s);
            assertThrows(RuntimeException.class, r2::get, s);

            assertEquals(Result.fromFunction(() -> returnsString(s)).get(), s);
        });
    }

    private Integer throwsError(String s) {
        throw new IllegalArgumentException(s);
    }

    @Test
    void getterAndChecksTests() {
        fuzzInt(i -> {
            Result<Integer, RuntimeException> r = Result.ok(i);
            assertTrue(r.isOk());
            assertFalse(r.hasError());
            assertEquals(i, r.get());
            assertThrows(RuntimeException.class, r::getError);
        });

        fuzzString(s -> {
            Result<String, RuntimeException> r = Result.err(new IllegalArgumentException(s));
            assertFalse(r.isOk());
            assertTrue(r.hasError());
            assertThrows(RuntimeException.class, r::get, s);
            assert r.getError() instanceof IllegalArgumentException;
        });
    }

    @Test
    void ifTests() {
        fuzzString(s -> {
            Box<Boolean> box = new Box<>(false);
            Result.ok(s).ifOk(res -> box.value(true));
            assertTrue(box.value());

            Result.ok(s).ifOkOrElse(
                    res -> {
                        assert true;
                    },
                    () -> {
                        assert false;
                    }
            );
        });

        fuzzString(s -> {
            Result<String, Throwable> r = Result.err(new IllegalArgumentException(s));

            r.ifOk(res -> {
                assert false : "error variant should not be okay";
            });

            r.ifOkOrElse(
                    res -> {
                        assert false;
                    },
                    () -> {
                        assert true;
                    }
            );
        });
    }

    @Test
    void mapsTest() {
        // Identity map shouldn't change object.
        fuzzString(s -> assertEquals(Result.ok(s).map(v -> v), Result.ok(s)));

        fuzzInt(x -> {
            assertEquals(
                    Result.ok(x).map(v -> 3 * v + 1),
                    Result.ok(x).flatMap(v -> Result.ok(3 * v + 1))
            );
            assertEquals(3 * x + 1, Result.ok(x).map(v -> 3 * v + 1).get());
        });

        fuzzString(s -> {
            Result<String, IllegalArgumentException> r = Result.err(new IllegalArgumentException(s));
            assert r.map(v -> 0).hasError();
            // Flatmap should only map Ok var
            assert r.flatMap(v -> Result.ok(0)).hasError();
        });

        fuzzInt(i -> assertAllEqual(
                Result.ok(i),
                Result.ok(i).mapError(e -> new IllegalArgumentException()),
                Result.ok(i).transformMatchingError((e) -> e instanceof IllegalArgumentException, x -> 12),
                Result.ok(i).mapError(e -> 1)
        ));
    }


    @Test
    void optionTest() {
        fuzzString(s -> {
            Result<String, Error> r = Result.ok(s);
            assertEquals(r.toOptional(), Optional.of(s));

            Result<String, IllegalArgumentException> err = Result.err(new IllegalArgumentException(s));
            assertEquals(err.toOptional(), Optional.empty());
        });
    }

    @Test
    void orTest() {
        fuzzString(s -> {
            Result<String, IllegalStateException> r1 = Result.ok(s);
            Result<String, IllegalStateException> r2 = Result.ok(s);

            Result<String, IllegalStateException> e1 = Result.err(new IllegalStateException());
            Result<String, IllegalStateException> e2 = Result.err(new IllegalStateException());

            assert r1.or(() -> r2) == r1;
            assert r2.or(() -> r1) == r2;

            assert r1.or(() -> e1) == r1;
            assert e2.or(() -> r2) == r2;

            assert e2.or(() -> e1) == e1;
            assert e1.or(() -> e2) == e2;
        });
    }

    @Test
    void matchTest() {

    }

    @Test
    void filterTest() {
        assert Result.ok(0).filter(x -> x != 0, e -> new ArithmeticException())
                .getError() instanceof ArithmeticException;

        assert Result.ok(1).filter(x -> x != 0, e -> new ArithmeticException()).isOk();

        assertInstanceOf(CustomException.class,
                Result.<Integer, Throwable>err(new CustomException()).filter(x -> x != 0, i -> new Error())
                        .getError()
        );
    }

    @Test
    void example() {
        Result<Integer, Throwable> goodDivision = Result.fromFunction(() -> divide(2, 4));
        Result<Integer, Throwable> badDivision = Result.fromFunction(() -> divide(4, 0));

        assert Result.fromFunction(() -> divide(4, 0))
                .map(n -> "Your Result is: " + n)
                .orElse("Invalid").equals("Invalid");

        assert goodDivision.isOk();
        assert badDivision.hasError();
        assert badDivision.getError().getCause() instanceof ArithmeticException;
    }

    @Test
    void mappingErrorsTests() {
        List<Result<Integer, Throwable>> rs = List.of(
                Result.ok(0),
                Result.err(new CustomException()),
                Result.err(new IllegalArgumentException()),
                Result.err(new AssertionError()),
                Result.err(new Error())
        );

        IntStream.range(0, rs.size())
                .forEach(i -> {
                            int res = rs.get(i)
                                    .transformMatchingError(e -> e instanceof CustomException, e -> ((CustomException) e).uniqueMethod())
                                    .transformMatchingError(e -> e instanceof IllegalArgumentException, e -> 2)
                                    .transformMatchingError(e -> e instanceof AssertionError, e -> 3)
                                    .orElseGet(e -> 4);
                            assert res == i;
                        }
                );

        IntStream.range(0, rs.size())
                .forEach(i -> {
                            int res = rs.get(i)
                                    .flatMapMatchingError(
                                            e -> e instanceof CustomException,
                                            e -> Result.ok(((CustomException) e).uniqueMethod())
                                    ).flatMapMatchingError(e -> e instanceof IllegalArgumentException, e -> Result.ok(2))
                                    .flatMapMatchingError(e -> e instanceof AssertionError, e -> Result.ok(3))
                                    .orElseGet(e -> 4);
                            assert res == i;
                        }
                );

        assertInstanceOf(
                IllegalArgumentException.class,
                Result.err(new CustomException())
                        .mapError(e -> new IllegalArgumentException())
                        .getError()
        );

        assertInstanceOf(
                IllegalArgumentException.class,
                Result.err(new CustomException())
                        .mapError(IllegalArgumentException::new)
                        .getError()
        );

        assertInstanceOf(
                CustomException.class,
                Result.err(new CustomException())
                        .transformMatchingError(e -> false, IllegalArgumentException::new)
                        .getError()
        );
    }


    @Test
    void orElseThrowTests() {
        fuzzString(s -> {
            assertAllEqual(
                    s,
                    Result.ok(s).get(),
                    Result.ok(s).orElseThrow(() -> new RuntimeException()),
                    Result.<String, IllegalArgumentException>ok(s).orElseThrow(e -> new RuntimeException(e))
            );
            CustomException err = new CustomException();
            assertAllThrow(
                    () -> Result.err(err).get(),
                    () -> Result.err(err).orElseThrow(() -> new RuntimeException()),
                    () -> Result.err(err).orElseThrow(e -> new RuntimeException(e))
            );
        });
    }

    @Test
    void orElseTest() {
        fuzzInt(i ->
                assertAllEqual(
                        i,
                        Result.ok(i).get(),
                        Result.ok(i).orElse(null),
                        Result.err(new Error()).orElse(i)
                )
        );

    }

    @Test
    void matchTest2() {
        final String ADMIN_PASSWORD = "admin";
        Map<Result<String, Integer>, String> GETResults = Map.of(
                Result.err(404), "404: page not found",
                Result.err(400), "400: Client Error",
                Result.err(500), "500: Server Error :(",
                Result.err(600), "Unknown error code: 600",
                Result.ok("Obtained from Server:)"), "Obtained from Server:)",
                Result.ok("Password is "+ADMIN_PASSWORD), "You tried to retrieve confidential information"
        );
        for (Result<String, Integer> response : GETResults.keySet()) {
            assertEquals(GETResults.get(response), response.match(
                    s -> s,
                    e -> "Unknown error code: " + e,
                    MatchArm.error(404, i -> i+": page not found"),
                    MatchArm.error(400, i -> i+": Client Error"),
                    MatchArm.err( i -> i >= 500 && i < 600, i -> i+": Server Error :("),
                    MatchArm.ok(s -> s.contains(ADMIN_PASSWORD), s -> "You tried to retrieve confidential information")
            ));

            assertEquals(GETResults.get(response), response.match(
                    res -> res.map(s -> s).orElseGet(i -> "Unknown error code: " + i),
                    MatchArm.err(i -> i == 404, i -> i+": page not found"),
                    MatchArm.err(i -> i == 400, i -> i+": Client Error"),
                    MatchArm.err( i -> i >= 500 && i < 600, i -> i+": Server Error :("),
                    MatchArm.okay("Password is "+ADMIN_PASSWORD, s -> "You tried to retrieve confidential information")
            ));
        }
    }

    @Test
    void coverageTests() {
        Result<Integer, Object> res = Result.ok(-1);
        assertSame(res, res.mapError(e -> Double.NaN));
        assertSame(res, res.mapError(e -> Result.ok(4)));
        assertSame(res, res.flatMapError(e -> Result.ok(4)));

        fuzzString(s -> {
            assertEquals(s+"hi", Result.err(s)
                    .flatMapError(str -> Result.err(str+"hi"))
                    .getError()
            );

        });
    }

    void voidThrowsError(String s) {
        throw new IllegalArgumentException(s);
    }

    Integer divide(int a, int b) {
        return a / b;
    }

    String returnsString(String s) {
        return s;
    }
}

