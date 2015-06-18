package tools;

import java.awt.Color;
import java.util.HashSet;
import java.util.LinkedList;

import org.AttributeHelper;
import org.graffiti.graph.AdjListGraph;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * @author klukas
 *
 */
public class Image2Graph {
	// private Graph graph;
	private Node[][] nodeMap;

	public Image2Graph(Image i, int back) {
		int[][] ia = i.getAs2A();
		int w = i.getWidth();
		int h = i.getHeight();

		Graph g = new AdjListGraph();
		Node[][] nodes = new Node[w][h];
		this.nodeMap = nodes;
		// add node for every foreground pixel
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {
				int c = ia[x][y];
				if (c != back) {
					nodes[x][y] = GraphHelper.addNodeToGraph(g, x, y, 0, 1, 1, null, new Color(c));
				}
			}

		// add edges to connect neighbour fg pixels
		for (int x = 0; x < w; x++)
			for (int y = 0; y < h; y++) {
				Node n = nodes[x][y];
				if (n == null)
					continue;
				for (int xo = -1; xo <= 1; xo++)
					for (int yo = -1; yo <= 1; yo++) {
						int xt = x + xo;
						int yt = y + yo;
						if (xt < 0 || yt < 0 || xt >= w || yt >= h)
							continue;
						Node n2 = nodes[xt][yt];
						if (n2 == n)
							continue;
						if (n2 == null)
							continue;
						g.addEdge(n, n2, false);
					}
			}
		// this.graph = g;
	}

	public int getColorOfNearestColoredNode(Node n, int uncolored) {
		LinkedList<Node> todo = new LinkedList<Node>();
		HashSet<Node> visited = new HashSet<Node>();

		todo.add(n);
		visited.add(n);

		while (!todo.isEmpty()) {
			Node nn = todo.removeFirst();
			Color c = AttributeHelper.getFillColor(nn);
			if (c.getRGB() != uncolored) {
				return c.getRGB();
			} else {
				for (Node nei : nn.getNeighbors())
					if (!visited.contains(nei)) {
						todo.add(nei);
						visited.add(nei);
					}
			}
		}

		return uncolored;
	}

	public Node[][] getNodeMap() {
		return nodeMap;
	}
}
