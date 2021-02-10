package eu.equo.gamelogic;

import android.os.Parcel;
import android.os.Parcelable;

public class NumericalCell implements Parcelable{
	protected int value;
	protected Operator operator;
	private CellType type = CellType.NUMERICAL;
	
	/**Creates an empty cell!
	 * 
	 */
	public NumericalCell() {
		value = 0;
		operator = Operator.PLUS;
		type = CellType.EMPTY;
	}
	
	/**Copy constructor
	 * 
	 * @param other
	 */
	public NumericalCell(NumericalCell other) {
		this.value = other.value;
		this.operator = other.operator;
		this.type = other.type;
	}
	
	public NumericalCell(int value, Operator operator) {
		this.value = value;
		this.operator = operator;
	}

	public void merge(NumericalCell other) {
		if(operator == Operator.MINUS) {
			value *= (-1);
			operator = Operator.PLUS;
		}
		value = other.operator.execute(value, other.value);
		
		if(value < 0 && operator == Operator.MINUS) {operator = Operator.PLUS; value *= -1;}
		else if(value < 0 && operator == Operator.PLUS) {operator = Operator.MINUS; value *= -1;}
	}
	
	public CellType getType() {
		return type;
	}
	
	public void setEmpty() {type = CellType.EMPTY;}

	public void evaluate() {
		type = CellType.EVALUATED;
		if(operator == Operator.MINUS) {
			operator = Operator.PLUS;
			value *= -1;
		}
			
		}
	
	public boolean isEmpty() {
		return type == CellType.EMPTY ? true : false;
	}
	
	@Override
	public String toString() {
		if(type == CellType.EVALUATED) return "" + value;
		return operator + " " + value;
	}

	//*****Parcelling methods
	
	public NumericalCell(Parcel in) {
		this.value = in.readInt();
		this.type = CellType.valueOf(in.readString());
		this.operator = Operator.valueOf(in.readString());
	}
	
    public static final Parcelable.Creator<NumericalCell> CREATOR
    = new Parcelable.Creator<NumericalCell>() {
    	@Override
		public NumericalCell createFromParcel(Parcel in) {
    		return new NumericalCell(in);
}

	@Override
	public NumericalCell[] newArray(int size) {
		return new NumericalCell[size];
}
};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(value);
		dest.writeString(type.name());
		dest.writeString(operator.name());
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
