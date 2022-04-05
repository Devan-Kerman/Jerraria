package net.devtech.jerraria.client;

import java.util.ArrayList;

class ArticleData {
	int id;
	String title, author;
	ArrayList<String> keywords;

	ArticleData(int id, String title, String author, int expectedKeywordCount) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.keywords = new ArrayList<>(expectedKeywordCount);
	}

	void addKeyword(String keyword) {
		this.keywords.add(keyword);
	}
}
