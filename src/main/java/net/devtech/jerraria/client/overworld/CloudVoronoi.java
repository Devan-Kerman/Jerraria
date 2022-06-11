package net.devtech.jerraria.client.overworld;

import java.util.Random;

import net.devtech.jerraria.util.math.JMath;

public class CloudVoronoi {
	final int scale;
	final Random random = new Random();
	final long seed;

	public CloudVoronoi(int scale, long seed) {
		this.scale = scale;
		this.seed = seed;
	}

	public int generate(int posX, int posY, int range) {
		int centerSectX = Math.floorDiv(posX, this.scale), centerSectY = Math.floorDiv(posY, this.scale);
		int closeGen = 0;
		int closestDistance = Integer.MAX_VALUE;
		for(int xo = -1; xo <= 1; xo++) {
			for(int yo = -1; yo <= 1; yo++) {
				int sectX = xo + centerSectX, sectY = centerSectY + yo;
				int sectXPos = sectX * this.scale, sectYPos = sectY * this.scale;
				long seed = JMath.combineInts(sectX, sectY) ^ this.seed;
				Random random = this.random;
				random.setSeed(seed);
				int sx = random.nextInt(this.scale) + sectXPos, sy = random.nextInt(this.scale) + sectYPos;
				int dx = sx-posX, dy = sy-posY;
				int dist = dx*dx + dy*dy;
				if(dist <= closestDistance) {
					closestDistance = dist;
					closeGen = random.nextInt(range);
				}
			}
		}
		return closeGen;
	}

	public static void main(String[] args) {
		CloudVoronoi voronoi = new CloudVoronoi(3, 100);
		for(int x = 0; x < 32; x++) {
			for(int y = 0; y < 32; y++) {
				System.out.print(voronoi.generate(x, y, 10));
			}
			System.out.println();
		}
	}
}
