package lsc;

/**
 * @author klukas
 *
 */
public class RegionSplitting {

	// position = sign( (Bx-Ax)*(Y-Ay) - (By-Ay)*(X-Ax) )
	// ==> -1 / 0 on line / +1

	public ImageCanvas drawLine(int x0, int y0, int x1, int y1, int color, double alpha, int size) {
		int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
		int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1;
		int err = dx + dy, e2; /* error value e_xy */

		while (true) { /* loop */
			fillRect(x0 - size, y0 - size, size + size, size + size, color, alpha);
			if (x0 == x1 && y0 == y1)
				break;
			e2 = 2 * err;
			if (e2 >= dy) {
				err += dy;
				x0 += sx;
			} /* e_xy+e_x > 0 */
			if (e2 <= dx) {
				err += dx;
				y0 += sy;
			} /* e_xy+e_y < 0 */
		}
		return this;
	}

}
