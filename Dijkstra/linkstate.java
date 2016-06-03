import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Carl on 4/7/2016.
 */
public class linkstate {

    // router class
    private static class Router {

        /** Constructor **/
        public Router(String pathString) {

            // split the path string and extract individual costs
            String[] stringCosts = pathString.split(",");

            // attempt to parse each string cost to an int
            for(String cost : stringCosts) {
                try {
                    linkCosts.add(Integer.parseInt(cost));
                } catch(NumberFormatException e) {

                    // This is an "N" for infinite distance.
                    // Convert to MAX_VALUE for type uniformity.
                    linkCosts.add(Integer.MAX_VALUE);
                }
            }

            this.Id = ++IDcount;
        }

        /** Data to string method **/
        public String toString() {
            String toReturn = "";
            toReturn += "ID: " + this.Id + "\n";

            for(int iter = 0; iter < this.linkCosts.size(); ++iter) {
                toReturn += "Cost to router " + (iter + 1) + " = " + this.linkCosts.get(iter);
                toReturn += "\n";
            }

            return toReturn;
        }

        /** internal counter for allocating router IDs **/
        private static Integer IDcount = 0;

        /** Data members **/
        public Integer Id;
        public ArrayList<Integer> linkCosts = new ArrayList<>();

    }

    static class CostPair {

        // creating new pairs with default constructor
        public CostPair() {
            this.cost = Integer.MAX_VALUE;
            this.previousRouterID = Integer.MAX_VALUE;
        }

        public CostPair(Integer cost, Integer previousRouterID) {
            this.cost = cost;
            this.previousRouterID = previousRouterID;
        }
        public Integer cost;
        public Integer previousRouterID;
    }

    private static void Dijkstra() {

        /****** Begin header printing ******/
        // length of dashed lines is proportional to number of nodes
        String dashedLines = "----------------------------------------------------------";

        // dynamically change dashed line count based on number of routers
        for(Router r : routers) {
            dashedLines += "------------------------------------------------------------------------------";
        }

        // top of table
        System.out.println(dashedLines);

        // formatting of spacing between columns
        String format = "%-80s";

        System.out.printf(format, "Step");
        System.out.printf(format, "N\'");

        // dynamically printing header based on number of routers
        for(Router r : routers) {

            // we don't print router 1 since we start there
            if(r.Id != 1) {
                System.out.printf(format, "D(" + r.Id +"),p(" + r.Id + ")");
            }
        }

        // new line
        System.out.print("\n");
        /****** End header printing ******/

        /****** Begin Algorithm ******/
        // list containing N'
        ArrayList<Integer> shortestPath = new ArrayList<>();

        // copy of authoritative router list;
        // this will be used to track which routers are available
        // I.E., totalRouters - N'
        ArrayList<Router> availableRouters = new ArrayList<>(routers);

        // hash map that updates the cheapest cost to each router
        HashMap<Integer, CostPair> costMap = new HashMap<>();

        // setup initial data in cost map
        for(Router r : routers) {

            // ignoring starting router 1
            if(r.Id != 1) {

                // initialize all costs to infinity
                costMap.put(r.Id, new CostPair());
            }
        }

        // router '1' is first router in list
        // problem states that path always start at router '1'
        Router selectedRouter = availableRouters.get(0);

        // loop until all routers have been added to N'
        while(availableRouters.size() > 0) {

            /** update lists **/
            shortestPath.add(selectedRouter.Id);
            availableRouters.remove(selectedRouter);
            /** end update lists **/

            /** Update costs **/
            for(Integer iter = 0; iter < selectedRouter.linkCosts.size(); ++iter) {

                // ignore the path to self and routers already in N'
                if(selectedRouter.linkCosts.get(iter) != 0 && !shortestPath.contains(iter + 1)) {

                    // get the cost taken to get to selected router
                    Integer costToSelectedRouter = 0;

                    // add the cost of the previous path, if it wasn't the start.
                    if(selectedRouter.Id != 1) {
                        costToSelectedRouter += costMap.get(selectedRouter.Id).cost;
                    }

                    // the total cost is the selected router's link cost + the total cost to reach the selected router
                    // Note: For logical reasons, any number + ∞ == ∞. Therefore the overflow is caught and set to ∞.
                    Integer totalCost = selectedRouter.linkCosts.get(iter) == Integer.MAX_VALUE ? Integer.MAX_VALUE : selectedRouter.linkCosts.get(iter) + costToSelectedRouter;

                    if (totalCost < costMap.get(iter + 1).cost) {
                        costMap.put(iter + 1, new CostPair(totalCost, selectedRouter.Id));
                    }
                }
            }
            /** End update costs **/

            /** Printing table row **/

            // dashed line separator for next table row
            System.out.println(dashedLines);

            // step number
            System.out.printf(format, shortestPath.size() - 1);

            // N'
            String nString = "";
            for(Integer id : shortestPath) {
                nString += "," + id;
            }
            System.out.printf(format, nString.replaceFirst(",",""));

            Integer nextCheapestCost = Integer.MAX_VALUE;

            // print the contents of each hash map entry
            for(Map.Entry<Integer, CostPair> entry : costMap.entrySet()) {

                // only output the data if this router isn't already in N'
                if(!shortestPath.contains(entry.getKey())) {

                    // Note: inline ternary operator used to transmute numeric infinite distance to "N".
                    System.out.printf(format, "" + (entry.getValue().cost == Integer.MAX_VALUE ? "N" : entry.getValue().cost) +
                            (entry.getValue().previousRouterID == Integer.MAX_VALUE ? "" : ", " + entry.getValue().previousRouterID));

                    // piggy-back off this loop to also select the next cheapest router
                    if(entry.getValue().cost < nextCheapestCost) {
                        nextCheapestCost = entry.getValue().cost;
                        selectedRouter = routers.get(entry.getKey() - 1);
                    }

                } else {
                    // output filler space for router already in path
                    System.out.printf(format, "");
                }

            }

            // next table line
            System.out.print("\n");

            /** End printing table row **/

        }

        /**** End Algorithm ****/
    }

    public static void main(String[] args) {

        // create a buffered reader for the file
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(args[0]));
        } catch (FileNotFoundException|ArrayIndexOutOfBoundsException fio) {

            // catch file not found on host machine
            System.out.println(fio.getMessage());
            return;
        }

        // a list path strings in the network
        ArrayList<String> paths = new ArrayList<>();

        // continuously read lines
        String readString;
        try {
            while ((readString = reader.readLine()) != null) {

                // remove ending "."
                readString = readString.replace(".", "");

                // if not EOF, append to list
                if(!readString.contains("EOF")) {
                    paths.add(readString);
                }
            }
        } catch (IOException io) {
            // catch line read exception
            System.out.println(io.getMessage());
            return;
        }

        // create routers
        for(String path : paths) {
            routers.add(new Router(path));
        }

        // do dijkstra's
        Dijkstra();

    }

    // list of the routers in the network
    private static ArrayList<Router> routers = new ArrayList<>();

}
