package WikiBot.Content;

public class Position {
	private int line;
	private int posInLine;
	
	public Position(int line_, int posInLine_){
		line = line_;
		posInLine = posInLine_;
	}
	
	public void setPosInLine(int i) {
		posInLine = i;
	}
	
	public void increaseLine(int i) {
		line += i;
	}

	public int getLine() {
		return line;
	}
	
	public int getPosInLine() {
		return posInLine;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj.getClass().equals(Position.class)) {
			Position pos2 = (Position)obj;
			if (pos2.getLine() == getLine() && pos2.getPosInLine() == getPosInLine()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isGreaterThen(Position pos) {
		if (line > pos.getLine()) {
			return true;
		} else if (line < pos.getLine()) {
			return false;
		} else {
			if (posInLine > pos.getPosInLine()) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	@Override
	public String toString() {
		return "line " + (line+1) + " and letter " + (posInLine+1);
	}
}
