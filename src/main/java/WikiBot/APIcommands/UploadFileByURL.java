package WikiBot.APIcommands;

import WikiBot.ContentRep.PageLocation;

/**
 * @Description
 * This command uploads a file to the wiki, given a url to the file.
 * This is done synchronously.
 * 
 * Recommended used.
 * 
 * @RequiredRights
 * upload
 * upload_by_url
 * 
 * @ReqiuredOther
 * In LocalSettings.php, the following is required:
 * $wgAllowCopyUploads = true;
 * 
 * @MediawikiSupport
 * 1.16+
 */
public class UploadFileByURL extends APIcommand {

	public UploadFileByURL(PageLocation pl_, String URL_, String pageText_, String uploadComment_) {
		super("Upload file", pl_, true, "edit", "csrf");
		keys.add("action");
		values.add("upload");
		keys.add("filename");
		values.add(getTitleWithoutNameSpace());
		keys.add("url");
		values.add(URL_);
		keys.add("text");
		values.add(pageText_);
		keys.add("comment");
		values.add(uploadComment_);
		
		enforceMWVersion("1.16");
	}
}
