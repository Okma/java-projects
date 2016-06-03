import java.util.Random;

/**
 * Created by Carl on 9/30/2015.
 */
public class Producer implements Runnable {

    // Internalized counting for id assignment
    private static int ProducerCount = 1;

    // Reference to the buffer used
    private final BoundedBufferABNBW bufferRef;

    // Number of currently consumed items
    private int producedItems = 0;

    // Unique Id of this Producer
    private final int ProducerId;

    // Total quantity of time spent waiting (in ms)
    public int totalTimeWaited = 0;

    // Random object
    private static Random rand = new Random();

    // lower bound for thread waiting (in ms)
    private static final int lowerWaitBound = 10;

    // upper bound for thread waiting (in ms)
    private static final int upperWaitBound = 100;

    // lower bound for item manipulation count
    private static final int lowerItemBound = 1;

    // upper bound for item manipulation count
    private static final int upperItemBound = 5;

    Producer(BoundedBufferABNBW bufferInput) {
        bufferRef = bufferInput;
        ProducerId = ProducerCount;
        ProducerCount++;
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                if(Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }
                // Insert a random quantity of items
                int itemsToInsert = rand.nextInt(upperItemBound - lowerItemBound) + lowerItemBound;
                System.out.printf("%s ready to insert %d items.\n", this.toString(), itemsToInsert);
                for(int i = 0; i < itemsToInsert; i++) {
                    bufferRef.insert(new Item(), this);
                    producedItems++;
                }
                int timeToWait = rand.nextInt(upperWaitBound - lowerWaitBound) + lowerWaitBound;
                System.out.printf("%s napping for %d ms.\n", this.toString(), timeToWait);
                totalTimeWaited += timeToWait;
                Thread.sleep(timeToWait);
            } catch (InterruptedException e) {
                System.out.printf(this.toString() + " TERMINATED.\n");
                System.out.printf("%s", this.getStatus());
                break;
            }
        }
    }

    // Final status string
    public String getStatus() {
        return this.toString() + " finished producing " + this.producedItems + " items and waited " + this.totalTimeWaited +" ms.\n";
    }

    // To string method
    public String toString() {
        return "Producer" + Integer.toString(ProducerId);
    }

}
