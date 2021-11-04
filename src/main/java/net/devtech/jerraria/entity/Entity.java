package net.devtech.jerraria.entity;

import net.devtech.jerraria.world.World;

public class Entity {
	double x, y;
	World world;

	public void setPosition(World world, double x, double y) {
		this.world = world;
		this.x = x;
		this.y = y;
	}

	public int getBlockX() {
		return (int) Math.floor(this.x);
	}

	public int getBlockY() {
		return (int) Math.floor(this.y);
	}

	public double getX() {
		return this.x;
	}

	public double getY() {
		return this.y;
	}

	public World getWorld() {
		return this.world;
	}
}
