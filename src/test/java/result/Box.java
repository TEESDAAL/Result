package result;

public class Box<V> {
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
