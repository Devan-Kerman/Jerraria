package rendering;

import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.api.element.AutoStrat;

public class TriangulatorTest {
	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			//GeometryFactory fact = new GeometryFactory();
			//RenderThread.addRenderStage(() -> {
			//	ConformingDelaunayTriangulationBuilder builder = new ConformingDelaunayTriangulationBuilder();
			//	Shape shape = new RoundRectangle2D.Float(0, 0, 100, 100, 10, 10);
			//	Geometry geometry = ShapeReader.read(shape.getPathIterator(null, .1f), fact);
			//	builder.setSites(geometry);
			//	Geometry triangles = builder.getTriangles(fact);
			//	SolidColorShader shader = SolidColorShader.INSTANCE;
			//	shader.strategy(AutoStrat.sequence(DrawMethod.TRIANGLE));
			//	for(int n = 0; n < triangles.getNumGeometries(); n++) {
			//		Geometry triangle = triangles.getGeometryN(n);
			//		Coordinate[] coordinates = triangle.getCoordinates();
			//		for(int i = 0; i < 3; i++) {
			//			Coordinate coordinate = coordinates[i];
			//			shader.vert().vec3f((float) coordinate.x/100, (float) coordinate.y/100, 1).argb(0xFFFFFFFF);
			//		}
			//	}
			//	shader.draw();
			//},10);

			return null;
		});
	}
}
