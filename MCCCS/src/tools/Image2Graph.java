package tools;

import java.awt.Color;

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
	private Graph graph;

	public Image2Graph(Image i, int back) {
		int[][] ia = i.getAs2A();
		int w = i.getWidth();
		int h = i.getHeight();

		Graph g = new AdjListGraph();
		Node[][] nodes = new Node[w][h];
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

				for (int xo = -1; xo <= 1; xo++)
					for (int yo = -1; yo <= 1; yo++) {
						int xt = x + xo;
						int yt = y + yo;
						if (xt < 0 || yt < 0 || xt >= w || yt >= h)
							continue;
						Node n2 = nodes[xt][yt];
						if (n2 == n)
							continue;
						g.addEdge(n, n2, false);
					}
			}
		this.graph = g;
	}
}
