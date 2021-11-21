import java.util.Objects;

public class Pair<L, R> {
    private L left;
    private R right;
    public Pair(L left, R right){
        this.left = left;
        this.right = right;
    }
    public L getLeft(){
        return left;
    }
    public R getRight(){
        return right;
    }
    public void setLeft(L left){
        this.left = left;
    }
    public void setRight(R right){
        this.right = right;
    }

    @Override
    public String toString() {
        return "(" + left +
                "," + right +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(getLeft(), pair.getLeft()) && Objects.equals(getRight(), pair.getRight());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLeft(), getRight());
    }
}
