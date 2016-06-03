/**
 * Created by Carl on 10/1/2015.
 */
public class ProducerConsumerDemo {

    public static void main(String args[]) {
        BoundedBufferABNBW<Item> boundedBuffer = new BoundedBufferABNBW<>(3); // Create a new buffer size 3

        final int numProducers = 2;
        final int numConsumers = 2;

        // Initialize producer threads
        Thread[] producerThreads = new Thread[ numProducers ];
        for(int i = 0; i < numProducers; i++) {
            producerThreads[i] = new Thread(new Producer(boundedBuffer));
        }

        // Initialize consumer threads
        Thread[] consumerThreads = new Thread[ numConsumers ];
        for(int i = 0; i < numProducers; i++) {
            consumerThreads[i] = new Thread(new Consumer(boundedBuffer));
        }

        // Start producer threads
        for(Thread th : producerThreads)
            th.start();

        // Start consumer threads
        for (Thread th : consumerThreads)
            th.start();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.out.println("Sleep was interrupted.");
        }

        // Interrupt producer threads
        for(Thread th : producerThreads)
            th.interrupt();

        // Interrupt consumer threads
        for (Thread th : consumerThreads)
            th.interrupt();

        // Wait for producer threads to join
        for(Thread th: producerThreads) {
            try {
                th.join();
            } catch (InterruptedException e) {
                System.out.println("Producer join interrupted.");
            }
        }

        // Wait for consumer threads to join
        for(Thread th: consumerThreads) {
            try {
                th.join();
            } catch (InterruptedException e) {
                System.out.println("Consumer join interrupted.");
            }
        }

        // Print buffer contents
        System.out.printf("\n%s", boundedBuffer.toString());
    }
}
