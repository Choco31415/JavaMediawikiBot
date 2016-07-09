package WikiBot.APIcommands;

import WikiBot.ContentRep.PageLocation;

/**
 * This requires $wgAllowCopyUploads = true in
 * the wiki's local settings, and an account with
 * the upload_by_url user right.
 * This is done synchronously.
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
	}
}
