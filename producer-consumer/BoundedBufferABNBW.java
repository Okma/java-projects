import java.util.Random;

/**
 * Created by Carl on 9/30/2015.
 */
public class BoundedBufferABNBW<Item> implements BoundedBuffer<Item> {
    private final Item[] buffer; // the actual buffer
    private int N; // Number of slots in buffer
    private int in = 0; // slot for next insertion
    private int out = 0; // slot for next removal
    private Object lock = new Object(); // Single lock
    private int count = 0; // count of objects in the buffer

    public BoundedBufferABNBW(int numSlots) {
        N = numSlots + 1;
        buffer = (Item[]) new Object[ N ];
    }

    public String toString() {
        String toReturn = "BoundedBuffer:= [";
        for(Item i : buffer) {
            if(i != null)
                toReturn += i.toString() + ",";
        }

        // Chop off last comma
        toReturn.substring(0, toReturn.length() - 1);
        toReturn += "]";

        return toReturn;
    }

    public Item remove(Consumer c) throws InterruptedException {
        synchronized (lock) {
            while(out == in) {
                System.out.printf("%s waiting to remove an item.\n", c.toString());
                lock.wait();
            }

            // Obtain item to return
            Item item = buffer[out];
            // c.consume(item);   // if consume was applicable

            // set that item to null in buffer
            buffer[ out ] = null;

            // Increment out counter
            out = (out + 1) % N;

            // Decrement counter of items in buffer
            count--;

            // Release the lock
            lock.notifyAll();
            System.out.printf("%s removed %s. \t%d out of %d slots full.\n",
                   c.toString(),item.toString(), count, N - 1 );
            return item;
        }
    }

    public void insert(Item i, Producer p) throws InterruptedException{
        synchronized (lock) {
            while ((in + 1) % N == out) {
                System.out.printf("%s waiting to insert %s.\n", p.toString(), i.toString());
                lock.wait();
            }

            // Set the item[in] equal to the passed item
            buffer[in] = i;

            // increment in counter
            in = (in + 1) % N;

            // increment count of items in buffer
            count++;

            // release lock
            lock.notifyAll();
            System.out.printf("%s inserted %s. \t%d out of %d slots full.\n",
                    p.toString(), i.toString(), count, N - 1);
        }
    }

}
