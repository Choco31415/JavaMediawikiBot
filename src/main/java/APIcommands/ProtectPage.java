package APIcommands;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import Content.PageLocation;

/**
 * @Description
 * This command protects a page from being edited or moved.
 * 
 * Recommended used directly.
 * 
 * @RequiredRights
 * protect
 * 
 * @MediawikiSupport
 * 1.12+
 */
public class ProtectPage extends APIcommand {
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	
	public ProtectPage(PageLocation loc, String editGroup, Date editExpiry, String reason) {
		super("Move", loc, true, "protect", "csrf");
		keys.add("action");
		values.add("protect");
		keys.add("title");
		values.add(loc.getTitle());
		keys.add("protections");
		values.add("edit=" + editGroup);
		keys.add("expiry");
		values.add(dateFormat.format(editExpiry));
		keys.add("reason");
		values.add(reason);
		
		enforceMWVersion("1.12");
	}
	
	public ProtectPage(PageLocation loc, String editGroup, String moveGroup, Date editExpiry, Date moveExpiry, String reason, boolean cascade) {
		super("Move", loc, true, "protect", "csrf");
		keys.add("action");
		values.add("protect");
		keys.add("title");
		values.add(loc.getTitle());
		keys.add("protections");
		values.add("edit=" + editGroup + "|move=" + moveGroup);
		keys.add("expiry");
		values.add(dateFormat.format(editExpiry) + "|" + dateFormat.format(moveExpiry));
		if (cascade) {
			keys.add("cascade");
			values.add(""); // buffer
		}
		keys.add("reason");
		values.add(reason);
		
		enforceMWVersion("1.12");
	}
}
