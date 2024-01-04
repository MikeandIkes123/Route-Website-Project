import java.security.InvalidAlgorithmParameterException;
import java.io.*;
import java.util.*;


/**
 * Models a weighted graph of latitude-longitude points
 * and supports various distance and routing operations.
 */
public class GraphProcessor {
    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * @param file a FileInputStream of the .graph file
     * @throws Exception if file not found or error reading
     */
    private HashMap<Point, List<Point>> map;
    private List<Point> verticeList;
    private List<Point[]> edgesList;

    // include instance variables here

    public GraphProcessor(){
        map = new HashMap<>();
        verticeList = new ArrayList<>();
        edgesList = new ArrayList<>();
    }

    /**
     * Creates and initializes a graph from a source data
     * file in the .graph format. Should be called
     * before any other methods work.
     * @param file a FileInputStream of the .graph file
     * @throws IOException if file not found or error reading
     */

    public void initialize(FileInputStream file) throws IOException {
        try (Scanner in = new Scanner(file)) {
            int numVertices = in.nextInt();
            int numEdges = in.nextInt();
            in.nextLine();

            for (int i = 0; i < numVertices; i++) {
                String nodeName = in.next();
                double lat = in.nextDouble();
                double lon = in.nextDouble();
                Point point = new Point(lat, lon);
                verticeList.add(point);
            }

            // Read edges
            for (int i = 0; i < numEdges; i++) {
                int uEdge = in.nextInt();
                int vEdge = in.nextInt();
                String optionalEdgeName = (in.next());
                Point[] edge = new Point[]{verticeList.get(uEdge), verticeList.get(vEdge)};
                edgesList.add(edge);
            }

        }

        catch (NoSuchElementException e) {
            e.printStackTrace();
        }

        for (Point[] edge : edgesList){
                map.computeIfAbsent(edge[0], k -> new ArrayList<>()).add(edge[1]);
                map.computeIfAbsent(edge[1], k -> new ArrayList<>()).add(edge[0]);
        }
    }

    /**
     * NOT USED IN FALL 2023, no need to implement
     * @return list of all vertices in graph
     */

    public List<Point> getVertices(){
        return null;
    }

    /**
     * NOT USED IN FALL 2023, no need to implement
     * @return all edges in graph
     */
    public List<Point[]> getEdges(){
        return null;
    }

    /**
     * Searches for the point in the graph that is closest in
     * straight-line distance to the parameter point p
     * @param p is a point, not necessarily in the graph
     * @return The closest point in the graph to p
     */
    public Point nearestPoint(Point p) {
        double min = Double.MAX_VALUE;
        Point closest = null;
        for (Point v : verticeList){
            double distance = p.distance(v);
            if (distance < min){
                min = distance;
                closest = v;
            }
        }
        return closest;
    }


    /**
     * Calculates the total distance along the route, summing
     * the distance between the first and the second Points, 
     * the second and the third, ..., the second to last and
     * the last. Distance returned in miles.
     * @param start Beginning point. May or may not be in the graph.
     * @param end Destination point May or may not be in the graph.
     * @return The distance to get from start to end
     */
    public double routeDistance(List<Point> route) {
        double d = 0.0;
        for (int i = 0; i < route.size() - 1; i++){
            d += route.get(i + 1).distance(route.get(i));
        }
        return d;
    }
    

    /**
     * Checks if input points are part of a connected component
     * in the graph, that is, can one get from one to the other
     * only traversing edges in the graph
     * @param p1 one point
     * @param p2 another point
     * @return true if and onlyu if p2 is reachable from p1 (and vice versa)
     */
    public boolean connected(Point p1, Point p2) {
        return dfs(p1, p2);
    }

    private boolean dfs(Point start, Point destination) {
        Set<Point> visited = new HashSet<>();
        Stack<Point> stack = new Stack<>();
    
        stack.push(start);
    
        while (!stack.isEmpty()) {
            Point current = stack.pop();
            visited.add(current);
    
            if (current.equals(destination)) {
                return true;
            }
    
            for (Point neighbor : map.getOrDefault(current, Collections.emptyList())) {
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                }
            }
        }
    
        return false;
    }
    

    /**
     * Returns the shortest path, traversing the graph, that begins at start
     * and terminates at end, including start and end as the first and last
     * points in the returned list. If there is no such route, either because
     * start is not connected to end or because start equals end, throws an
     * exception.
     * @param start Beginning point.
     * @param end Destination point.
     * @return The shortest path [start, ..., end].
     * @throws IllegalArgumentException if there is no such route, 
     * either because start is not connected to end or because start equals end.
     */

     
    public List<Point> route(Point start, Point end) throws IllegalArgumentException {
        if (!map.containsKey(start) || !map.containsKey(end)) {
            throw new IllegalArgumentException("No valid path found.");
        }

        Map<Point, Double> distanceMap = new HashMap<>();
        Map<Point, Point> predMap = new HashMap<>();
        PriorityQueue<Point> pq = new PriorityQueue<>(Comparator.comparingDouble(distanceMap::get));

        for (Point vertex : map.keySet()) {
            distanceMap.put(vertex, Double.POSITIVE_INFINITY);
            predMap.put(vertex, null);
        }

        distanceMap.put(start, 0.0);
        pq.add(start);

        if (start.equals(end)) {
            throw new IllegalArgumentException("Start and end are the same");
        }

        while (!pq.isEmpty()) {
            Point current = pq.poll();
            if (current.equals(end)) {
                break;
            }

            for (Point neighbor : map.get(current)) {
                double newDistance = distanceMap.get(current) + current.distance(neighbor);

                if (!distanceMap.containsKey(neighbor) || newDistance < distanceMap.get(neighbor)) {
                    distanceMap.put(neighbor, newDistance);
                    predMap.put(neighbor, current);
                    pq.add(neighbor);
                }
            }
        }

        List<Point> path = new ArrayList<>();
        Point current = end;

        while (current != null) {
            path.add(current);
            current = predMap.get(current);
        }
        
        Collections.reverse(path);

        if (path.isEmpty() || path.size() <= 1) {
            throw new IllegalArgumentException("No valid path found.");
        }
        return path;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        String name = "data/usa.graph";
        GraphProcessor gp = new GraphProcessor();
        gp.initialize(new FileInputStream(name));
        System.out.println("running GraphProcessor");
    }


    
}
