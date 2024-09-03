import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

public class Tests {

    String randomString() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        return random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    @Test
    void constructorTest() {
        Random rand = new Random();
        for (int i=0; i<100; i++) {
            Integer x = rand.nextInt();
            assertEquals(Result.of(x), Result.of(x));
            assertEquals(Result.of(x).get(), x);
        }

        for (int i=0; i<100; i++) {
            String s = randomString();
            assertEquals(Result.of(()-> new IllegalArgumentException(s)).toString(),
                    Result.of(()-> new IllegalArgumentException(s)).toString());
            assertThrows(RuntimeException.class,
                    ()->Result.of(()-> new IllegalArgumentException(s)).get(), s);
        }
    }

    @Test
    void fromFunctionTest() {
       for (int i=0; i<100; i++) {
           String s = randomString();
           Result<Integer> r1 = Result.fromFunction(() -> throwsAssertionError(s));
           Result<Result.EMPTY> r2 = Result.fromFunction(() -> voidThrowsAssertionError(s));

           assertThrows(RuntimeException.class, ()->r1.get(), s);
           assertThrows(RuntimeException.class, ()->r2.get(), s);

           assertEquals(Result.fromFunction(()->returnsString(s)).get(), s);
       }
    }

    @Test
    void getterAndChecksTests() {
        for (int i=0; i<100; i++) {
            String s = randomString();
            assert Result.of(s).isOk();
            assert !Result.of(s).hasError();
            Result.of(s).get();
        }
        for (int i=0; i<100; i++) {
            String s = randomString();
            Result<String> r = Result.of(()-> new IllegalArgumentException(s));
            assert !r.isOk();
            assert r.hasError();
            assertThrows(RuntimeException.class, ()->r.get(), s);
        }
    }

    @Test
    void ifTests() {
        for (int i=0; i<100; i++) {
            String s = randomString();
            AtomicReference<Boolean> x = new AtomicReference<>(false);
            Result.of(s).ifOk(res -> x.set(true));
            assert x.get();
            Result.of(s).ifOkOrElse(
                    res -> {assert true;},
                    ()->{assert false;}
            );
        }
        for (int i=0; i<100; i++) {
            String s = randomString();
            Result<String> r = Result.of(()-> new IllegalArgumentException(s));
            AtomicReference<Boolean> x = new AtomicReference<>(true);
            r.ifOk(res -> x.set(false));
            assert x.get();
            r.ifOkOrElse(
                    res -> {assert false;},
                    ()->{assert true;}
            );
        }
    }

    @Test
    void mapTest() {
        Random rand = new Random();
        for (int i = 0; i < 100; i++) {
            String s = randomString();
            Integer x = rand.nextInt();

            Result<String> r = Result.of(s);
            assertEquals(r.map(v -> v), r);
            assert r.map(v -> true).get();

            assertEquals(
                    Result.of(x).map(v->3*v+1),
                    Result.of(x).flatMap(v-> Result.of(3*v+1))
            );
        }

        for (int i = 0; i < 100; i++) {
            String s = randomString();

            Result<String> r = Result.of(() -> new IllegalArgumentException(s));
            assert r.map(v -> 0).hasError();
            assert r.flatMap(v -> Result.of(0)).hasError();
        }
    }

    @Test
    void optionTest() {
        for (int i = 0; i < 100; i++) {
            String s = randomString();

            Result<String> r = Result.of(s);
            assertEquals(r.toOption(), Optional.of(s));

            Result<String> err = Result.of(() -> new IllegalArgumentException(s));
            assertEquals(err.toOption(), Optional.empty());
        }
    }

    @Test
    void example() {
        Result<Integer> goodDivision = Result.fromFunction(()->divide(2, 4));
        Result<Integer> badDivision = Result.fromFunction(()->divide(4, 0));
        assert Result.fromFunction(()->divide(4, 0))
                .map(n-> "Your Result is: " + n)
                .orElse("Invalid").equals("Invalid");
        assert goodDivision.isOk();
        assert badDivision.hasError();
        assert badDivision.getError() instanceof ArithmeticException;
    }

    @Test
    void matchTests() {
        Result<Integer> r0 = Result.of(1);
        Result<Integer> r1 = Result.of(IllegalArgumentException::new);
        Result<Integer> r2 = Result.of(Error::new);
        Result<Integer> r3 = Result.of(AssertionError::new);
        for (int i = 0; i < 4; i++) {
            int res = List.of(r0, r1, r2, r3).get(i).match(
                    x -> 0,
                    e -> 2,
                    (IllegalArgumentException e) -> 1,
                    (AssertionError e) -> 3
            );
            assert res == i;
        }
    }

    Integer throwsAssertionError(String s) {
        assert false: s;
        return -1;
    }

    void voidThrowsAssertionError(String s) {
        assert false: s;
    }
    Integer divide(int a, int b) {
        return a/b;
    }
    String returnsString(String s) {
        return s;
    }
}
