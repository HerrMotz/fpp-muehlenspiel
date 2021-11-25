package backend;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Objects;

public class Triplet<O> {
    private final O one;
    private final O two;
    private final O three;

    public Triplet(O one, O two, O three){
        this.one = one;
        this.two = two;
        this.three = three;
    }

    public ArrayList<O> toArray() {
        return new ArrayList<>() {{
            add(one);
            add(two);
            add(three);
        }};
    }

    @Override
    public String toString() {
        return "Triplet{" +
                "one=" + one +
                ", two=" + two +
                ", three=" + three +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triplet<?> triplet = (Triplet<?>) o;
        return Objects.equals(getOne(), triplet.getOne()) && Objects.equals(getTwo(), triplet.getTwo()) && Objects.equals(getThree(), triplet.getThree());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOne(), getTwo(), getThree());
    }

    public O getOne() {
        return one;
    }

    public O getTwo() {
        return two;
    }

    public O getThree() {
        return three;
    }
}
