package eu.equo.gamelogic;

import android.os.Parcel;
import android.os.Parcelable;

public class TargetValue implements Parcelable, Comparable<TargetValue> {
	int value;
	boolean assigned = false;
	
	public TargetValue(int value) {
		this.value = value;
	}
	
	/**Copy constructor
	 * 
	 * @param other
	 */
	public TargetValue(TargetValue other) {
		this.value = other.value;
		this.assigned = other.assigned;
	}
	
	public boolean holdValue(int value) {
		if(assigned == false && this.value == value) {
			assigned = true;
			return true;
		}
		return false;
	}
	
	public boolean isAssigned() {
		return assigned;
	}
	
	@Override
	public String toString() {
		return Integer.toString(value);
	}

	@Override
	public int compareTo(TargetValue another) {
		return this.value - another.value;
	}

	//*****Parcelling methods
	
	public TargetValue(Parcel in) {
		value = in.readInt();
		assigned = in.readByte() != 0;
	}
	
    public static final Parcelable.Creator<TargetValue> CREATOR
    = new Parcelable.Creator<TargetValue>() {
    	@Override
		public TargetValue createFromParcel(Parcel in) {
    		return new TargetValue(in);
}

	@Override
	public TargetValue[] newArray(int size) {
		return new TargetValue[size];
}
};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(value);
		dest.writeByte((byte) (assigned ? 1 : 0)); 
	}
	
}
