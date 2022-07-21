package net.devtech.jerraria.mixin.self;

import net.devtech.jerraria.render.internal.state.ToggleState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ToggleState.class, remap = false)
public class ToggleStateMixin {
	@Shadow @Final Runnable on, off;
	@Shadow boolean default_;

	@Overwrite
	public boolean set(boolean value) {
		boolean default_ = this.default_;
		if(value) {
			this.on.run();
		} else {
			this.off.run();
		}
		this.default_ = default_;
		return value;
	}
}
