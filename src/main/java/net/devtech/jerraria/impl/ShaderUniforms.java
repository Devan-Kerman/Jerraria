package net.devtech.jerraria.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class ShaderUniforms implements IMixinConfigPlugin {
	public static final MappingResolver MAPPINGS = FabricLoader.getInstance().getMappingResolver();
	public static final String IDENTIFIER = MAPPINGS.mapClassName("intermediary", "net.minecraft.class_2960").replace('.', '/');
	public static final String MARK_DIRTY = MAPPINGS.mapMethodName("intermediary", "net.minecraft.class_3679", "method_1279", "()V");

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		return true;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
		if("net.devtech.jerraria.mixin.dummy.DummyShaderMixin".equals(mixinClassName)) {
			Label label = new Label();
			MethodNode newInit = null;
			MethodNode init = null;
			for(MethodNode method : targetClass.methods) {
				if("<init>".equals(method.name)) {
					newInit = new MethodNode(Opcodes.ASM9, method.access, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0])) {
						@Override
						public void visitTypeInsn(int opcode, String type) {
							if(opcode == Opcodes.NEW && IDENTIFIER.equals(type)) {
								this.visitVarInsn(Opcodes.ALOAD, 1);
								this.visitJumpInsn(Opcodes.IFNULL, label);
							}
							super.visitTypeInsn(opcode, type);
						}

						@Override
						public void visitMethodInsn(
							int opcodeAndSource,
							String owner,
							String name,
							String descriptor,
							boolean isInterface) {
							super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
							if(MARK_DIRTY.equals(name)) {
								this.visitLabel(label);
							}
						}
					};
					method.accept(newInit);
					init = method;
				}
			}
			targetClass.methods.remove(init);
			targetClass.methods.add(0, newInit);
		}
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	// @formatter:off
	@Override public void onLoad(String mixinPackage) {}
	@Override public String getRefMapperConfig() {return null;}
	@Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
	@Override public List<String> getMixins() {return null;}
}
