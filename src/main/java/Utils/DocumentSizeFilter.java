package Utils;

import javax.swing.JTextArea;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * Specifically tailored for the Core/BotPanel.
 */
public class DocumentSizeFilter extends DocumentFilter {
	int maxLines;
	JTextArea area;
	
	public DocumentSizeFilter(int maxLines_, JTextArea JTextArea_) {
		maxLines = maxLines_;
		area = JTextArea_;
	}
	
	public void insertString(DocumentFilter.FilterBypass fb, int offset, String string,
			AttributeSet attr) throws BadLocationException {
		
		super.insertString(fb, offset, string, attr);
		
		if (area.getLineCount() > maxLines) {
			//Truncate the input
			remove(fb, 0, area.getLineEndOffset(area.getLineCount() - maxLines - 1));
		}
	}

	public void remove(DocumentFilter.FilterBypass fb, int offset, int length)
			throws BadLocationException {
		    
	    super.remove(fb, offset, length);
	}

	public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text,
			AttributeSet attrs) throws BadLocationException {

		super.replace(fb, offset, length, text, attrs);
	}
}
