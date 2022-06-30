package net.devtech.jerraria.gui.api.shape;

import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;
import org.poly2tri.Poly2Tri;
import org.poly2tri.geometry.polygon.Polygon;
import org.poly2tri.geometry.polygon.PolygonPoint;
import org.poly2tri.triangulation.TriangulationPoint;
import org.poly2tri.triangulation.delaunay.DelaunayTriangle;

public class ShapeTriangulator {
	public static void triangulate(PathIterator iterator, Triangulation triangulation) {
		float[] buf = new float[6];
		List<Polygon> polygons = new ArrayList<>();
		List<PolygonPoint> points = new ArrayList<>();
		while(!iterator.isDone()) {
			int method = iterator.currentSegment(buf);
			if(method == PathIterator.SEG_CLOSE) {
				if(!points.isEmpty()) {
					polygons.add(new Polygon(points));
					points.clear();
				}
			} else if(method == PathIterator.SEG_MOVETO) {
				if(!points.isEmpty()) {
					polygons.add(new Polygon(points));
					points.clear();
				}
				points.add(new PolygonPoint(buf[0], buf[1]));
			} else if(method == PathIterator.SEG_LINETO) {
				points.add(new PolygonPoint(buf[0], buf[1]));
			} else {
				throw new UnsupportedOperationException();
			}
			iterator.next();
		}

		if(iterator.getWindingRule() != PathIterator.WIND_NON_ZERO) {
			throw new UnsupportedOperationException("todo");
		}

		List<Polygon> outers = new ArrayList<>();
		List<Polygon> enclosing = new ArrayList<>();
		for(Polygon polygon : polygons) {
			TriangulationPoint s1 = polygon.getPoints().get(0);
			TriangulationPoint s2 = polygon.getPoints().get(1);
			TriangulationPoint s3 = polygon.getPoints().get(2);
			double originalCross = (s2.getX() - s1.getX()) * (s3.getY() - s1.getY())
			                         - (s2.getY() - s1.getY()) * (s3.getX() - s1.getX());
			double x = s3.getX(), y = s3.getY();
			int winding = 0;
			winding += Math.signum(originalCross);
			for(Polygon poly : polygons) {
				if(poly == polygon) {
					continue;
				}
				List<TriangulationPoint> polyPoints = poly.getPoints();
				TriangulationPoint p2 = polyPoints.get(0);
				int size = polyPoints.size();
				int tally = 0;
				for(int i = 0; i < size; i++) {
					TriangulationPoint p3 = polyPoints.get((i + 1) % size);
					double slope = (p3.getY() - p2.getY()) / (p3.getX() - p2.getX());
					double px = ((y - p2.getY()) / slope) + p2.getX();
					if(((px <= p3.getX() && px >= p2.getX()) || (px <= p2.getX() && px >= p3.getX())) && px >= x) {
						if((y >= p3.getY() && y < p2.getY()) || (y >= p2.getY() && y < p3.getY())) {
							double cross = (p2.getX() - x) * (p3.getY() - y)
							              - (p2.getY() - y) * (p3.getX() - x);
							tally += Math.signum(cross);
						}
					}

					p2 = p3;
				}

				if(tally != 0) {
					enclosing.add(poly);
					winding += tally;
				}
			}

			if(winding != 0) {
				outers.add(polygon);
			} else {
				for(Polygon outer : enclosing) {
					outer.addHole(polygon);
				}
				enclosing.clear();
			}
		}

		FloatList list = new FloatArrayList();
		float minX = Float.POSITIVE_INFINITY;
		float minY = Float.POSITIVE_INFINITY;
		float maxX = Float.NEGATIVE_INFINITY;
		float maxY = Float.NEGATIVE_INFINITY;
		for(Polygon polygon : outers) {
			Poly2Tri.triangulate(polygon);
			for(DelaunayTriangle triangle : polygon.getTriangles()) {
				for(TriangulationPoint point : triangle.points) {
					float x = point.getXf();
					list.add(x);
					float y = point.getYf();
					list.add(y);
					if(x < minX) {
						minX = x;
					} else if(x > maxX) {
						maxX = x;
					}

					if(y < minY) {
						minY = y;
					} else if(y > maxY) {
						maxY = y;
					}
				}
			}
		}

		triangulation.offX = -minX;
		triangulation.offY = -minY;
		triangulation.width = maxX -minX;
		triangulation.height = maxY - minY;
		triangulation.triangles = list.toFloatArray();
	}
}
