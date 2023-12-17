package graphexplorer;

/**
 * A test bed for a weighted digraph abstract data type implementation
 * and implementations of elementary classical graph algorithms that use the
 * ADT
 * @see GraphAPI.java, Graph.java, City.java
 * @author Duncan, Johnny Williams III
 * <pre>
 * usage: GraphExplorer <graphFileName>
 * <graphFileName> - a text file containing the description of the graph
 * in DIMACS file format
 * Date: 99-99-99
 * course: csc 3102
 * programming project 3
 * Instructor: Dr. Duncan
 * </pre>
 */

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.Comparator;
import java.util.PriorityQueue; 
import java.util.concurrent.atomic.AtomicBoolean;

public class GraphExplorer
{
   public static final Double INFINITY = Double.POSITIVE_INFINITY;
   public static final Integer NIL = -1;

   public static void main(String []args) throws GraphException
   {
      if (args.length != 1)
      {
         System.out.println("Usage: GraphExplorer <filename>");
         System.exit(1);
      }
      City c1, c2;
      Scanner console;
      int menuReturnValue, i,j;
      //Define a lambda function f:City -> PrintStream that displays
      //the key followed by the label (name of the city) in two-column format
      //and advances the cursor to the next line. This function will be used
      //as an argument to bfsTraverse and dfsTraverse and to display the 
      //topological listing of the vertices.
      //Function<City,PrintStream> f = ...;	 
      City[] startcity = new City[1];
      Function<City, PrintStream> othercity = city -> {
    	  if(startcity[0] == null) {
    		  startcity[0] = city;
    	  }
    	  System.out.printf("%d\t%s%n", city.getKey(), city.getLabel());
    	  return System.out;
      };
      Graph<City> g = readGraph(args[0]);      
      long s = g.size();
      menuReturnValue = -1;
      while (menuReturnValue != 0)
      {
         menuReturnValue = menu();
         switch(menuReturnValue)
         {
            case 1: //{-1,0,1} Incidence Matrix of g
               System.out.println();
               int[][] iMat = genIncMatUDG(g);
               if (iMat == null)
               {
                   System.out.println("The graph in "+args[0]+" has no edges.");
               }
               else
               {
                   System.out.println("|V| x |E| {-1,0,1}-Incidence Matrix of g in "+args[0]);
                   System.out.println("--------------------------------------------------------");
                   for (i = 0; i < g.size(); i++)
                   {
                       for (j = 0; j < g.countEdges(); j++)
                       {
                           System.out.printf("%3d",iMat[i][j]);
                       }
                       System.out.println();
                   }
               }
               System.out.println();
               break;
            case 2: //post-order depth-first-search traversal of g'
               System.out.println();
               System.out.println("Breadth-First-Search Traversal of the Graph In "+args[0]);
               System.out.println("===============================================================");
               //invoke the bfsTraverse function
               // Output should be aligned in two-column format as illustrated below:
               // 1     Charlottetown
               // 4     Halifax
               // 2     Edmonton   
               g.bfsTraverse(othercity);

               System.out.println("===============================================================");
               System.out.println();
               System.out.println("Depth-First-Search Traversal of the Graph In "+args[0]);
               System.out.println("===============================================================");
               //invoke the dfsTraverse function
               // Output should be aligned in two-column format as illustrated below:
               // 1     Charlottetown
               // 4     Halifax
               // 2     Edmonton
               g.dfsTraverse(othercity);

               System.out.println("===============================================================");
               System.out.println();
               System.out.println();
               break;
            case 3: //Check Strong Connectivity of G
                 System.out.println();
                 System.out.printf("The digraph %s is ",args[0]);
                 if (isStronglyConnected(g))
                 {
                     System.out.printf("strongly connected.%n");
                 }
                 else
                 {
                     System.out.printf("not strongly connected.%n");
                 }
                 System.out.println();
                 System.out.println();
                 break;                            
            case 4://out-degree topological sort
                 System.out.println();
                 int[] top = new int[(int)g.size()];
                 if (topSortOutDeg(g,top))
                 {
                    System.out.println("Topological Sorting of The Graph In "+args[0]);
                    System.out.println("===============================================================");           
					//Write code to display the topological listing of the vertices
					//in the top array. The should be displayed in two column
					// format. For example:
					// 6   Redwood City
					// 15  Hayward
					// 14  Dublin
					// 13  San Lorenzo
					// :   :
					// :   :
                    
                    for (int q = 0; q < g.size(); q++) {
                    	City city = g.retrieveVertex(new City(top[q]));
                    	 String label = city.getLabel();
                    	if (g.isEmpty()) {throw new GraphException("Graph is Empty!");}	
                    	System.out.printf("%d\t%s%n", top[q], label);
                    }
                    System.out.println("===============================================================");
                 }
                 else
                    System.out.println("No topological ordering possible. The digraph in "+args[0]+" contains a directed cycle.");
                 System.out.printf("%n%n");
                 top = null;
                 break;
            case 5: //KruskalMST;
               System.out.printf("%nThe Minimum Spanning Tree of the graph in "+args[0]+".%n%n");
               int edgesInMST = 0;
               int[] mstK = new int[(int)g.size()];
               double totalWt = kruskalMST(g,mstK);
               String cityNameA,cityNameB;
               for (i=1; i<=g.size(); i++)
               {
                   if (mstK[i-1] == -1)
                       cityNameA = "NONE";
                   else
                   {
                       edgesInMST++;
                       cityNameA = g.retrieveVertex(new City(mstK[i-1])).getLabel().trim();
                   }
                   cityNameB = g.retrieveVertex(new City(i)).getLabel().trim();
                   System.out.printf("%d-%s parent[%d] <- %d (%s)%n",i,cityNameB,i,mstK[i-1],cityNameA);
               }
               System.out.printf("%nThe weight of the minimum spanning tree/forest is %.2f miles.%n",totalWt);    
			   System.out.printf("Spanning Tree Edge Density is %.2f%%.%n%n",100.0*edgesInMST/g.countEdges());
               break;
            default: ;
         } //end switch
      }//end while
   }//end main

   /**
    * This method reads a text file formatted as described in the project description.
    * @param filename the name of the DIMACS formatted graph file.
    * @return an instance of a graph.
    */
   private static Graph<City> readGraph(String filename)
   {
      try
      {
         Graph<City> newGraph = new Graph();
         try (FileReader reader = new FileReader(filename)) 
         {
            char temp;
            City c1, c2, aCity;
            String tmp;
            int k, m, v1, v2, j, size=0, nEdges=0;
            Integer key, v1Key,v2Key;
            Double weight;
            Scanner in = new Scanner(reader);
            while (in.hasNext())
            {
                tmp = in.next();
                temp = tmp.charAt(0);
                if (temp == 'p')
                {
                    size = in.nextInt();
                    nEdges = in.nextInt();
                }
                else if (temp == 'c')
                {
                    in.nextLine();
                }
                else if (temp == 'n')
                {
                    key = in.nextInt();
                    tmp = in.nextLine();
                    aCity = new City(key,tmp);
                    newGraph.insertVertex(aCity); 
                }
                else if (temp == 'e')
                {
                    v1Key = in.nextInt();
                    v2Key = in.nextInt();
                    weight = in.nextDouble();
                    c1 = new City(v1Key);
                    c2 = new City(v2Key);
                    newGraph.insertEdge(c1,c2,weight); 
                }
            }
         }
         return newGraph;
      }
      catch(IOException exception)
      {
            System.out.println("Error processing file: "+exception);
      }
      return null;
   } 

   /**
    * Display the menu interface for the application.
    * @return the menu option selected.
    */  
   private static int menu()
   {
      Scanner console = new Scanner(System.in);
      //int option;
      String option;
      do
      {
         System.out.println("  BASIC WEIGHTED GRAPH APPLICATION   ");
         System.out.println("=======================================================");
         System.out.println("[1] Generate {-1,0,1} Incidence Matrix of G");
         System.out.println("[2] BFS/DFS Traversal of G");
         System.out.println("[3] Check Strong Connectedness of G");
         System.out.println("[4] Topological Ordering of V(G)");
         System.out.println("[5] Kruskal's Minimum Spanning Tree/Forest in G");
         System.out.println("[0] Quit");
         System.out.println("=====================================");
         System.out.printf("Select an option: ");         
         option = console.nextLine().trim();
         try
         {
             int choice = Integer.parseInt(option);
             if (choice < 0 || choice > 6)
             {
                System.out.println("Invalid option...Try again");
                System.out.println();
             }
             else
                return choice;
         }
         catch(NumberFormatException e)
         {
            System.out.println("Invalid option...Try again");
         }                           
      }while(true);
   }
   
   /**
    * Generates a {-1,0,1} vertices x edges incidence matrice for a
    * digraph; the columns representing the edges of the graph are arranged 
    * in lexicographical order, source=1 -> destination=-1 (source,destination).
    * @param g a digraph
    * @return a |V| x |E| 2-D array representing the {-1,0,1} incidence matrix
    * of g such that m[i-1,e-1] = 1 and m[j-1,e-1] = -1 if there is an outgoing
    * edge from vertex i to vertex j representing the eth edge (i->j) in g when the
    * edges are arranged in lexicographical order (source,destination); all otherwise
    * entries in a column is 0 when there isn't an incident edge. The null reference is
    * returned when g contains no edges.
    */
   private static int[][] genIncMatUDG(Graph<City> g)  throws GraphException
   {
	  //Implement this method
	   int edges = (int)g.countEdges();
	   int vertices = (int)g.size();
	   
	   int[][] incidencematrix = new int[vertices][];
	   if(edges == 0) {return null;}
	   for (int i = 0; i < vertices; i++) {
		   incidencematrix[i] = new int[edges];
		   for(int j = 0; j < edges; j++) {
			   incidencematrix[i][j] = 0;
		   }
	   }
			   City[] cities = new City[vertices];
			   for(int k = 1; k <= vertices; k++) {
				   cities[k - 1] = g.retrieveVertex(new City(k));
			   }
			   int count = 0;
			   for(int f = 0; f < vertices; f++) {
				   for(int j = 0; j < vertices; j++) {
					   
				   if(g.isEdge(cities[f], cities[j])) {
					   incidencematrix[f][count] = 1;
					   incidencematrix[j][count] = -1;
					   count++;
				   }
				   
				   }
		   
			   }
 
	   return incidencematrix;
   }
   
   /**
    * Determines whether the specified digraph is strongly connected
    * @param g a digraph
    * @return true if the digraph is strongly connected; otherwise, false
    */
   private static boolean isStronglyConnected(Graph<City> g)  throws GraphException
   {
      //Implement this method
	   City[] cities = new City[(int)g.size()];
	   for(int i = 1; i <= g.size(); i++) {
		   cities[i - 1] = g.retrieveVertex(new City(i));
	   }
	   HashMap<City, Double>[] edges = new HashMap[cities.length];
	   for(int i = 0; i < cities.length; i++) {
		   edges[i] = new HashMap<>();
		   for(int k = 0; k < cities.length; k++) {
			   if(g.isEdge(cities[i],cities[k])) {
				   edges[i].put(cities[k], g.retrieveEdge(cities[i], cities[k]));
			   }
		   }
	   }
	   for(int i = 0; i < cities.length; i++) {
		   Stack<City> stack = new Stack<>();
		   Set<City> set = new HashSet<>();
		   stack.push(cities[i]);
		   set.add(cities[i]);
		   while(!stack.isEmpty()) {
			   City tmp = stack.pop();
			   int index = tmp.getKey() - 1;
			   Set<City> keys = edges[index].keySet();
			   for(City link : keys) {
				   if(!set.contains(link)) {
					   stack.push(link);
					   set.add(link);
				   }
			   }
			 }
		   if(set.size() != g.size()) {
			   return false;
		   }
	   }
	   return true;
	   
	   

	  
  }

   /**
    * Merges two trees in a forest where (i,j) is a bridge between
    * trees.
    * @param i a vertex in one tree (source)
    * @param j a vertex in another tree (destination)
    * @param parent the parent-array representation of a forest
    * where the parent of a root is -1
    */
   public static void trUnion(int i, int j, int[] parent)
   {
       //Implement this function]
	   int x = find(parent, i);
	   int y = find(parent, j);
	   
	   if (parent[y] < parent[x]) {
		   parent[y] = x;
	   }
	   else if (parent[x] < parent[y]) {
		   parent[x] = y;
	   }
	   else {
		   parent[y] = x;
		   parent[x]++;
	   }

   }

   /**
    * Finds the root vertex of the tree in which the specified is
    * @param parent the parent implementation of disjointed subtrees of a graph
    * @param v a vertex
    * @return the root of the subtree containing v or v if v is a root.
    */  
   private static int find(int[] parent, int v)
   {
       //implement this method
	   while(parent[v] != -1) {
	   v = parent[v];
	   }
	   return v;
   }
   /**
    * This method generates a minimum spanning tree using Kruskal's 
    * algorithm. If no such MST exists, then it generates a minimum spanning forest.
    * @param g a weighted directed graph
    * @param parent the parent implementation of the minimum spanning tree/forest
    * @return the weight of such a tree or forest.
    * @throws GraphException when this graph is empty
    * <pre>
    * {@code
    * If a minimum spanning tree cannot be generated,
    * the parent implementation of a minimum spanning tree or forest is
    * determined. This implementation is based on the union-find stragey.
    * }
    * </pre>
    */    
   private static double  kruskalMST(Graph<City> g, int [] parent) throws GraphException
   {
       /**
        * An auxiliary data structure to store the information
        * about an edge for the MST algorithm
        * 
        */
       class EdgeType
       {
           public double weight;
           public int source;
           public int destination;
           public boolean chosen;
       }       
       /* An EdgeType comparator */
       Comparator<EdgeType> cmp = (t1, t2) -> 
       {
           if (t1.weight < t2.weight)
               return -1;
           if (t1.weight > t2.weight)
               return 1;
           if (t1.source < t2.source)
               return -1;
           if (t1.source > t2.source)
               return 1;
           if (t1.destination < t2.destination)
               return -1;
           if (t1.destination > t2.destination)
               return 1;
           return 0;        
       };
       
      
       PriorityQueue<EdgeType> pedges = new PriorityQueue<EdgeType>(cmp);
       int vertices = (int)g.size();
       City[] cities = new City[vertices];
       for (int i = 0; i < vertices; i++) {
    	   parent[i] = -1;
       }
       double TWeight = 0;
       int index = 0;
       while(!pedges.isEmpty() && index < vertices - 1) {
    	   EdgeType edge = pedges.poll();
    	   int Findex = edge.source - 1;
    	   int Tindex = edge.destination - 1;
    	   
    	   if(Findex < 0 || Findex >= vertices || Tindex < 0 || Tindex >= vertices) {
    		   System.out.println("Incorrect Indexes : findex = " + Findex + ", tindex = " + Tindex);
    	   }
    	   if(find(parent, Findex) != find(parent, Tindex)) {
    		   trUnion(find(parent, Findex), find(parent, Tindex), parent);
    		   TWeight += edge.weight;
    		   edge.chosen = true; 
    		   index++;
    	   }
    	   
    	   for(int i = 0; i < vertices; i++) {
    		   if (parent[i] != -1) {
    			   parent[i] = -1;
    		   }
    	   }
       }
   
      return TWeight;
   }         

   
    	
   /**
    * Generates a topological labeling of the specified digraph, in reverse order,
    * using the decrease-and-conquer algorithm that successively selects and r 
    * emoves a vertex of out-degree 0 until all the vertices are selected. 
    * The graph is explored in lexicographical order when adding a new vertex to the  
    * topological ordering and the graph is not modified. Updates of the degrees 
    * and vertices that are selected are tracked using auxiliary data structures. 
    * @param g a digraph
    * @param linearOrder the topological ordering of the vertices 
    * @return true if a topological ordering of the vertices of the specified digraph 
    * exists; otherwise, false. 
    */   
   private static boolean topSortOutDeg(Graph<City> g, int linearOrder[]) throws GraphException
   {
      //Implement this method, out-degree-based topological sort
	  if(g.isEmpty()) {
		  throw new GraphException("Graph is Empty!");
		  }
	
	   int vertices = (int)g.size();
	   int index = vertices - 1;
	   int v = -1;
	   int[] oDegree = new int[vertices];
	   for(int i = 1; i <= vertices; i++) {
		   City city = g.retrieveVertex(new City(i));
		   oDegree[i - 1] = (int)g.outDegree(city);
	   }
		linearOrder = new int[vertices];
	   
	   
		   for(int k = 1; k <= vertices; k++) {
			   if(oDegree[k - 1] == 0) {
				 v = k;
				 linearOrder[index--] = v;
				for(int j = 1; j <= vertices; j++) {
					if(g.isEdge(g.retrieveVertex(new City(j)), g.retrieveVertex(new City(v)))) {
						oDegree[j - 1]--;
					}
			   }
				oDegree[k - 1] = -1;
				k = 0;
		      }
		   }
	   
		   if(index >= 0) {
			   linearOrder = null;
			   return false;
		   }
		   return true;
   }		   
}

