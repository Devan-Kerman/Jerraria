package net.devtech.jerraria.gui.api.shape;

public final class Triangulation {
	public float[] triangles;
	public float offX, offY, width, height;

	public void forEach(VertexConsumer consumer, int color) {
		float offX = this.offX, offY = this.offY, width = this.width, height = this.height;
		float[] triangles = this.triangles;
		for(int i = 0; i < triangles.length; i += 2) {
			consumer.accept((triangles[i] + offX) / width, (triangles[i + 1] + offY) / height, color);
		}
	}
}
