package io.odysz.anson;

public class Photo extends Anson {
	String pid;
	
	public Photo() {}

	public Photo(String photoId) {
		this.pid = photoId;
	}
}
