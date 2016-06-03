/**
 * Created by Carl on 9/30/2015.
 */
public class Item {

    static int itemCount = 1;

    public Item() {
        item_id = itemCount;
        itemCount++;
    }

    public String toString() {
        return "Item #" + Integer.toString(item_id);
    }

    private int item_id;
}
