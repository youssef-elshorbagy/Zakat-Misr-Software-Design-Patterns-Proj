package lookup;

public abstract class LookupItem {

    protected int id;
    protected String code;
    protected String name;
    protected boolean isActive;

    protected LookupItem(int id, String code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.isActive = true;
    }

    public void activate() { this.isActive = true; }

    public void deactivate() { this.isActive = false; }

    public int getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isActive() { return isActive; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LookupItem)) return false;
        return id == ((LookupItem) o).id; // ids are unique across all lookup tables
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }
}
