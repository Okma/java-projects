/**
 * Created by Carl on 9/30/2015.
 */
public interface BoundedBuffer<E> {
    String toString();
    E remove(Consumer c) throws InterruptedException;
    void insert(E item, Producer p) throws InterruptedException;
}
