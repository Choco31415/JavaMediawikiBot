package APIcommands.Advanced;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import WikiBot.ContentRep.PageLocation;

/**
 * @Description
 * This command uploads a file to the wiki, via batch upload.
 * This is done synchronously.
 * 
 * Recommended not used.
 * 
 * @RequiredRights
 * upload
 * 
 * @MediawikiSupport
 * 1.20+
 */
public class UploadFileChunk extends APIcommand {

	MultipartEntityBuilder entity;
	
	/**
	 * A standard file upload, chunk #1.
	 * @param pl_ Where to upload the file.
	 * @param filesize_ The size of the file.
	 * @param chunk_ A chunk of file.
	 */
	public UploadFileChunk(PageLocation pl_, int filesize_, byte[] chunk_) {
		super("Upload file", pl_, true, "edit", "csrf");
		this.setRequiresEntity(true);		
		keys.add("action");
		values.add("upload");
		keys.add("format");
		values.add("json");
		
		String uploadingTo = getTitleWithoutNameSpace();
		entity = MultipartEntityBuilder.create()
		.addTextBody("stash", "1")
		.addTextBody("format", "json")
		.addTextBody("offset", "0")
		.addTextBody("filename", uploadingTo)
		.addTextBody("filesize", "" + filesize_)
		.addBinaryBody("chunk", chunk_, ContentType.MULTIPART_FORM_DATA, uploadingTo)
		.addTextBody("ignorewarnings", "true");
		
		enforceMWVersion("1.20");
	}
	
	/**
	 * A standard file upload, chunk #2...#n-1
	 * @param pl_ Where to upload the file.
	 * @param filesize_ The size of the file.
	 * @param chunk_ A chunk of the file.
	 * @param offset_ Offset of the start of this chunk.
	 * @param filekey_ A filekey of the upload to continue.
	 */
	public UploadFileChunk(PageLocation pl_, int filesize_, byte[] chunk_, int offset_, String filekey_) {
		super("Upload file", pl_, true, "edit", "csrf");
		this.setRequiresEntity(true);
		keys.add("action");
		values.add("upload");
		keys.add("format");
		values.add("json");
		
		String uploadingTo = getTitleWithoutNameSpace();
		entity = MultipartEntityBuilder.create()
		.addTextBody("stash", "1")
		.addTextBody("format", "json")
		.addTextBody("offset", "" + offset_)
		.addTextBody("filename", uploadingTo)
		.addTextBody("filesize", "" + filesize_)
		.addTextBody("filekey", filekey_)
		.addBinaryBody("chunk", chunk_, ContentType.MULTIPART_FORM_DATA, uploadingTo)
		.addTextBody("ignorewarnings", "true");
		
		enforceMWVersion("1.20");
	}
	
	/**
	 * A standard file upload, chunk #2...#n-1
	 * @param pl_ Where to upload the file.
	 * @param filesize_ The size of the file.
	 * @param chunk_ A chunk of the file.
	 * @param offset_ Offset of the start of this chunk.
	 * @param filekey_ A filekey of the upload to continue.
	 */
	public UploadFileChunk(PageLocation pl_, String filekey_, String uploadComment_, String pageText_) {
		super("Upload file", pl_, true, "edit", "csrf");
		this.setRequiresEntity(true);
		keys.add("action");
		values.add("upload");
		keys.add("format");
		values.add("json");
		
		String uploadingTo = getTitleWithoutNameSpace();
		entity = MultipartEntityBuilder.create()
		.addTextBody("format", "json")
		.addTextBody("filename", uploadingTo)
		.addTextBody("filekey", filekey_)
		.addTextBody("comment", uploadComment_)
		.addTextBody("text", pageText_)
		.addTextBody("ignorewarnings", "true");
		
		enforceMWVersion("1.20");
	}
	
	public HttpEntity getHttpEntity(String token) {
		return entity.addTextBody("token", token).build();
	}
}
