package WikiBot.PageRep;


public class Section {
	private int position;
	private String sectionTitle;
	private int depth;
	
	public Section(String sectionTitle_, int pos_, int depth_) {
		sectionTitle = sectionTitle_;
		position = pos_;
		depth = depth_;
	}
	
	public int getPosition() {
		return position;
	}
	
	public String getSectionTitle() {
		return sectionTitle;
	}
	
	public int getDepth() {
		return depth;
	}
	
	@Override
	public String toString() {
		return "(Section) Title: " + sectionTitle + " Depth: " + depth + " (Position: " + position + ")";
	}
	
	public String toString2() {
		String output = new String(new char[depth-1]).replace("\0", "{");
		output  += "(Section) Title: " + sectionTitle;
		return output;
	}
}
