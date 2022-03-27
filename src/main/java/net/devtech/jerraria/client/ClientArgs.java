package net.devtech.jerraria.client;

import java.io.File;
import java.util.List;

import com.beust.jcommander.Parameter;

public class ClientArgs {
	@Parameter(names = {"-rss", "-resources"}, converter = FileConverter.class)
	public List<File> resources = List.of();


}
