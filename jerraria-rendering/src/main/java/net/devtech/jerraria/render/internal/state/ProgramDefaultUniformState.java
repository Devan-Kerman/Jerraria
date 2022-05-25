package net.devtech.jerraria.render.internal.state;

public class ProgramDefaultUniformState {
	Object currentUniform;

	public boolean updateUniform(Object newUniform, boolean force) {
		if(force || newUniform != this.currentUniform) {
			this.currentUniform = newUniform;
			return true;
		} else {
			return false;
		}
	}
}
