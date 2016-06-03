import java.util.Random;
/**
 * Created by Carl on 9/30/2015.
 */
public class Consumer implements Runnable {

    // Internalized counting for id assignment
    private static int consumerCount = 1;

    // Reference to the buffer used
    private final BoundedBufferABNBW bufferRef;

    // Number of currently consumed items
    private int consumedItems = 0;

    // Unique Id of this consumer
    private final int consumerId;

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

    Consumer(BoundedBufferABNBW bufferInput) {
        bufferRef = bufferInput;
        consumerId = consumerCount;
        consumerCount++;
    }

    public void consume(Item item) {

        // consume logic
    }

    @Override
    public void run() {
        while(!Thread.currentThread().isInterrupted()) {
            try {
                if(Thread.currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }

                // Remove a random quantity of items
                int itemsToRemove = rand.nextInt(upperItemBound - lowerItemBound) + lowerItemBound;
                System.out.printf("%s ready to consume %d items.\n", this.toString(), itemsToRemove);
                for(int i = 0; i < itemsToRemove; i++) {
                    bufferRef.remove(this);
                    consumedItems++;
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
        return this.toString() + " finished consuming " + this.consumedItems + " items and waited " + this.totalTimeWaited +" ms.\n";
    }

    // To string method
    public String toString() {
        return "Consumer" + Integer.toString(consumerId);
    }

}
