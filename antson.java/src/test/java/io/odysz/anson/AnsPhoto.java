package io.odysz.anson;

/**
 * Equivalent of io.oz.album.Photo.
 * @author odys-z@github.com
 *
 */
public class AnsPhoto extends Anson {
	String pid;
	
	String clientpath;

	String uri;
	
	public AnsPhoto() {}

	public AnsPhoto(String photoId) {
		this.pid = photoId;
		
		clientpath = "test\\res\\my.jpg";
	}
}
