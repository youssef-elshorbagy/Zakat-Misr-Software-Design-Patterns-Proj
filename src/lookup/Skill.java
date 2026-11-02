package lookup;

public class Skill extends LookupItem {

    private String description;

    public Skill(int id, String code, String name, String description) {
        super(id, code, name);
        this.description = description;
    }

    public String getSkillDetails() {
        return "Skill[" + code + "] " + name + " - " + description + " | active: " + isActive;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
