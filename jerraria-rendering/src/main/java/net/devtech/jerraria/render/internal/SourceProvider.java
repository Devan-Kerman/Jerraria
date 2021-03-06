package net.devtech.jerraria.render.internal;

import java.io.IOException;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.util.Id;

public interface SourceProvider {
	Pair<ShaderPreprocessor, String> getSource(ShaderPreprocessor current, Id path);
}
