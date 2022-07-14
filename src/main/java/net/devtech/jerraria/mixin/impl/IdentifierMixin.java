package net.devtech.jerraria.mixin.impl;

import net.devtech.jerraria.util.Id;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.SoftOverride;

import net.minecraft.util.Identifier;

@Implements(@Interface(iface = Id.class, prefix = "soft$"))
@Mixin(Identifier.class)
public abstract class IdentifierMixin {
	@Shadow
	public abstract int compareTo(Object par1);

	@Shadow
	public abstract String getPath();

	@Shadow
	public abstract String getNamespace();

	@SoftOverride
	@Intrinsic
	public String soft$mod() {
		return this.getNamespace();
	}

	@SoftOverride
	@Intrinsic
	public String soft$path() {
		return this.getPath();
	}

	@SoftOverride
	@Intrinsic
	Identifier soft$to() {
		return (Identifier) (Object) this;
	}

	@SoftOverride
	@Intrinsic
	public int compareTo(@NotNull Id o) {
		return this.compareTo((Object) o);
	}
}
