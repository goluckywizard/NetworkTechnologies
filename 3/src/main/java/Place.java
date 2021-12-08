public class Place {
    String xid;
    String name;
    String description;

    public Place() {
    }

    public Place(String xid, String name) {
        this.xid = xid;
        this.name = name;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "xid: " + xid +
                ", имя: " + name +
                ", описание: " + description;
    }
}
