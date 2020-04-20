package shared.data;

import java.util.ArrayList;

public enum EdgeLogic {

    WRAP_AROUND(0, "Wrap Around"), DIE(1, "Die Off-Screen");

    int value = -1;
    String desc = "null";

    private EdgeLogic(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public int toValue() { return value; }

    public String toString() { return desc; }

    public static ArrayList<EdgeLogic> getConnectionTypes() {
        ArrayList<EdgeLogic> list = new ArrayList();
        list.add(EdgeLogic.WRAP_AROUND);
        list.add(EdgeLogic.DIE);
        return list;
    }

}
