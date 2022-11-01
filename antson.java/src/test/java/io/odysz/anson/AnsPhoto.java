package io.odysz.anson;

/**
 * Equivalent of io.oz.album.Photo.
 * @author odys-z@github.com
 *
 */
public class AnsPhoto extends Anson {
	String pid;
	
	public AnsPhoto() {}

	public AnsPhoto(String photoId) {
		this.pid = photoId;
	}
}
