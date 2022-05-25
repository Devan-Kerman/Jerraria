package net.devtech.jerraria.render.internal;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.Validate;

public final class ShaderPreprocessor {
	final SourceProvider includePreprocessor;
	final List<String> sourceInjections = new ArrayList<>();
	final Map<String, Object> includeParameters = new HashMap<>();

	ShaderPreprocessor(SourceProvider preprocessor) {
		this.includePreprocessor = preprocessor;
	}

	public ShaderPreprocessor(ShaderPreprocessor old) {
		this.includePreprocessor = old.includePreprocessor;
	}

	public void insert(String string, List<String> lines) {
		if(this.includeParameters.remove("version") instanceof String s) {
			lines.add("#version " + s + " core\n");
		}

		boolean reset = false;
		if(!this.sourceInjections.isEmpty()) {
			lines.add("#line 67000\n");
			int ln = 67000;
			for(String injection : this.sourceInjections) {
				this.parseInclude(lines, ln++, injection);
			}
			this.sourceInjections.clear();
			reset = true;
		}

		if(this.includeParameters.remove("defines") instanceof Iterable<?> i) {
			lines.add("#line 88000\n");
			for(Object o : i) {
				lines.add("#define " + o + "\n");
			}
			reset = true;
		}

		if(reset) {
			lines.add("#line 1\n");
		}

		int lineNumber = 0;
		for(String ln : (Iterable<String>) string.lines()::iterator) {
			this.parseInclude(lines, lineNumber, ln);
			lineNumber++;
		}
	}

	private void parseInclude(List<String> lines, int lineNumber, String ln) {
		int include = ln.indexOf("#include");
		if(include != -1) {
			int idx = include + "#include".length() + 1;
			if(idx < ln.length()) {
				int empty = ln.indexOf(' ', idx);
				String idStr;
				if(empty == -1) {
					idStr = ln.substring(idx);
				} else {
					idStr = ln.substring(idx, empty);
				}
				Id parse = Id.parse(idStr);
				Pair<ShaderPreprocessor, String> processor = this.includePreprocessor.getSource(this, parse);
				if(processor == null) {
					throw Validate.rethrow(new FileNotFoundException("Could not find lib file with id " + parse + " in #include on line " + lineNumber + " \"" + ln + "\""));
				}
				lines.add("#line 1\n");
				processor.key().insert(processor.value(), lines);
				lines.add("#line " + lineNumber + "\n");
			} else {
				throw new UnsupportedOperationException("Empty #include on line " + lineNumber);
			}
		} else {
			lines.add(ln + "\n");
		}
	}

	public List<String> getSourceInjections() {
		return this.sourceInjections;
	}

	public Map<String, Object> getIncludeParameters() {
		return this.includeParameters;
	}
}
