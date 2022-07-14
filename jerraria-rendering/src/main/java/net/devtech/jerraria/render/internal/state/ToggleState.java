package net.devtech.jerraria.render.internal.state;

public final class ToggleState {
	public final boolean initialState;
	boolean state, default_;
	final Runnable on, off;

	public ToggleState(boolean initialState, Runnable on, Runnable off) {
		this.default_ = this.initialState = this.state = initialState;
		this.on = on;
		this.off = off;
	}

	public boolean set(boolean enable) {
		if(GLContextState.FORCE || this.state ^ enable) {
			if(enable) {
				on.run();
			} else {
				off.run();
			}
			this.state = enable;
		}
		return enable;
	}

	public void setDefault(boolean id) {
		this.default_ = id;
	}

	public void setAndDefault(boolean id) {
		this.set(this.default_ = id);
	}

	public boolean setToDefault() {
		return this.set(this.default_);
	}

	public void defaultToInitial() {
		this.default_ = this.initialState;
	}

	public boolean getDefault() {
		return this.default_;
	}
}
