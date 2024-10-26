import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;
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
            Result<Integer> r = Result.of(0);
            assertFalse(() -> r.equals(null));
            assertTrue(() -> r.equals(r));
            assertFalse(() -> r.equals(new Object()));
        });

        fuzzInt(x -> {
            assertEquals(Result.of(x), Result.of(x));
            assertEquals(Result.of(x).hashCode(), Result.of(x).hashCode());
            assertEquals(Result.of(x).get(), x);
        });

        fuzzInt(x -> {
            Result<Integer> r = Result.of(IllegalStateException::new);
            assertFalse(() -> r.equals(null));
            assertTrue(() -> r.equals(r));
            assertFalse(() -> r.equals(new Object()));
        });

        // Throwables don't have a nice equals method...
        fuzzString(s -> {
            IllegalArgumentException e = new IllegalArgumentException(s);
            Result<String> r1 = Result.of(() -> e);
            Result<String> r2 = Result.of(() -> e);

            assertEquals(r1, r2);
            assertEquals(r1.hashCode(), r2.hashCode());
            assertEquals(r1.toString(), r2.toString());
            assertThrows(RuntimeException.class, r1::get);
        });


    }


    @Test
    void fromFunctionTest() {
        fuzzString(s -> {
            Result<Integer> r1 = Result.fromFunction(() -> throwsError(s));
            Result<Void> r2 = Result.fromFunction(() -> voidThrowsError(s));

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
            Result<Integer> r = Result.of(i);
            assertTrue(r.isOk());
            assertFalse(r.hasError());
            assertEquals(i, r.get());
            assertThrows(RuntimeException.class, r::getError);
        });

        fuzzString(s -> {
            Result<String> r = Result.of(() -> new IllegalArgumentException(s));
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
            Result.of(s).ifOk(res -> box.value(true));
            assertTrue(box.value());

            Result.of(s).ifOkOrElse(
                    res -> {
                        assert true;
                    },
                    () -> {
                        assert false;
                    }
            );
        });

        fuzzString(s -> {
            Result<String> r = Result.of(() -> new IllegalArgumentException(s));

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
        fuzzString(s -> assertEquals(Result.of(s).map(v -> v), Result.of(s)));

        fuzzInt(x -> {
            assertEquals(
                    Result.of(x).map(v -> 3 * v + 1),
                    Result.of(x).flatMap(v -> Result.of(3 * v + 1))
            );
            assertEquals(3 * x + 1, Result.of(x).map(v -> 3 * v + 1).get());
        });

        fuzzString(s -> {
            Result<String> r = Result.of(() -> new IllegalArgumentException(s));
            assert r.map(v -> 0).hasError();
            // Flatmap should only map Ok var
            assert r.flatMap(v -> Result.of(0)).hasError();
        });

        fuzzInt(i -> assertAllEqual(
                Result.of(i),
                Result.of(i).transformError(IllegalArgumentException::new),
                Result.of(i).transformErrorOfType(IllegalArgumentException.class, IllegalStateException::new),
                Result.of(i).mapErrorOfType(Throwable.class, e -> 1),
                Result.of(i).flatMapErrorOfType(Throwable.class, e -> Result.of(1))
        ));
    }


    @Test
    void optionTest() {
        fuzzString(s -> {
            Result<String> r = Result.of(s);
            assertEquals(r.toOptional(), Optional.of(s));

            Result<String> err = Result.of(() -> new IllegalArgumentException(s));
            assertEquals(err.toOptional(), Optional.empty());
        });
    }

    @Test
    void orTest() {
        fuzzString(s -> {
            Result<String> r1 = Result.of(s);
            Result<String> r2 = Result.of(s);

            Result<String> e1 = Result.of(IllegalStateException::new);
            Result<String> e2 = Result.of(IllegalStateException::new);

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
        fuzzInt(i -> {
            assertTrue(() -> Result.of(i).match(
                    x -> true,
                    e -> false
            ));

            assertFalse(() -> Result.of(Error::new).match(
                    x -> true,
                    e -> false
            ));
        });
    }

    @Test
    void filterTest() {
        assert Result.of(0).filter(x -> x != 0, ArithmeticException::new)
                .getError() instanceof ArithmeticException;

        assert Result.of(1).filter(x -> x != 0, ArithmeticException::new).isOk();

        assertInstanceOf(CustomException.class,
                Result.<Integer>of(CustomException::new).filter(x -> x != 0, Error::new)
                        .getError()
        );
    }

    @Test
    void example() {
        Result<Integer> goodDivision = Result.fromFunction(() -> divide(2, 4));
        Result<Integer> badDivision = Result.fromFunction(() -> divide(4, 0));

        assert Result.fromFunction(() -> divide(4, 0))
                .map(n -> "Your Result is: " + n)
                .orElse("Invalid").equals("Invalid");

        assert goodDivision.isOk();
        assert badDivision.hasError();
        assert badDivision.getError().getCause() instanceof ArithmeticException;
    }

    @Test
    void mappingErrorsTests() {
        List<Result<Integer>> rs = List.of(
                Result.of(0),
                Result.of(CustomException::new),
                Result.of(IllegalArgumentException::new),
                Result.of(AssertionError::new),
                Result.of(Error::new)
        );

        IntStream.range(0, rs.size())
                .forEach(i -> {
                            int res = rs.get(i)
                                    .mapErrorOfType(CustomException.class, CustomException::uniqueMethod)
                                    .mapErrorOfType(IllegalArgumentException.class, e -> 2)
                                    .mapErrorOfType(AssertionError.class, e -> 3)
                                    .orElseGet(() -> 4);
                            assert res == i;
                        }
                );

        IntStream.range(0, rs.size())
                .forEach(i -> {
                            int res = rs.get(i)
                                    .flatMapErrorOfType(CustomException.class, e -> Result.of(e.uniqueMethod()))
                                    .flatMapErrorOfType(IllegalArgumentException.class, e -> Result.of(2))
                                    .flatMapErrorOfType(AssertionError.class, e -> Result.of(3))
                                    .orElseGet(() -> 4);
                            assert res == i;
                        }
                );

        assertInstanceOf(
                IllegalArgumentException.class,
                Result.of(CustomException::new)
                        .transformErrorOfType(CustomException.class, IllegalArgumentException::new)
                        .getError()
        );

        assertInstanceOf(
                IllegalArgumentException.class,
                Result.of(CustomException::new)
                        .transformError(IllegalArgumentException::new)
                        .getError()
        );

        assertInstanceOf(
                CustomException.class,
                Result.of(CustomException::new)
                        .transformErrorOfType(Error.class, IllegalArgumentException::new)
                        .getError()
        );
    }


    @Test
    void orElseThrowTests() {
        fuzzString(s -> {
            assertAllEqual(
                    s,
                    Result.of(s).get(),
                    Result.of(s).orElseThrow(() -> new RuntimeException()),
                    Result.of(s).orElseThrow(e -> new RuntimeException(e))
            );
            CustomException err = new CustomException();
            assertAllThrow(
                    () -> Result.of(() -> err).get(),
                    () -> Result.of(() -> err).orElseThrow(() -> new RuntimeException()),
                    () -> Result.of(() -> err).orElseThrow(e -> new RuntimeException(e))
            );
        });
    }

    @Test
    void orElseTest() {
        fuzzInt(i ->
                assertAllEqual(
                        i,
                        Result.of(i).get(),
                        Result.of(i).orElse(null),
                        Result.of(Error::new).orElse(i)
                )
        );

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

class Box<V> {
    V value;

    public Box(V value) {
        this.value = value;
    }

    public void value(V value) {
        this.value = value;
    }

    public V value() {
        return this.value;
    }
}

class CustomException extends Throwable {
    public int uniqueMethod() {
        return 1;
    }
}