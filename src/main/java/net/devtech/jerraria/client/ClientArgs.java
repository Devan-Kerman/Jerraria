package net.devtech.jerraria.client;

import java.io.File;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;

public class ClientArgs {
	@Parameter(names = {"-rss", "-resources"}, converter = FileConverter.class)
	public List<File> resources = List.of();

	@Parameter(names = {"-rssDirs", "-resourcesDirectories"}, converter = FileConverter.class)
	public List<File> resourcesDirectories = List.of();

}
