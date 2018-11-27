package online.tratu.dict;

/**
 * plain pojo, define the position of a word
 * @author beethoven99@126.com
 */
public class WordPosition {
	private int startPos;
	private int length;
	
	public WordPosition(int s,int l){
		this.startPos=s;
		this.length=l;
	}
	
	public int getStartPos() {
		return startPos;
	}
	public void setStartPos(int startPos) {
		this.startPos = startPos;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	
}
