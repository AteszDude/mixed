package eu.equo.gamelogic;

public enum Operator{
	PLUS, MINUS, MULT, DIV;
	
	public int execute(int a, int b) {
	switch(this) {
	case PLUS: return a + b;
	case MINUS: return a - b;
	case MULT: return a * b;
	case DIV: return b == 0 ? 0 : a / b;
	default: return 0;
	}
	}
	
	public int getColor() {return 0;}//TODO
	
	public static Operator getFromSign(char sign) {
		return getFromSign(Character.toString(sign));
	}
	
	public static Operator getFromSign(String sign) {
		switch(sign) {
		case "+": return PLUS;
		case "-": return MINUS;
		case "*": return MULT;
		case "/": return DIV;
		case "\u00F7": return DIV;
		default: return null;
		}
	}
	
	@Override
	public String toString() {
	switch(this) {
	case PLUS: return "+";
	case MINUS: return "-";
	case MULT: return "*";
	case DIV: return "\u00F7";
	default: return null;
	}
	}
}
