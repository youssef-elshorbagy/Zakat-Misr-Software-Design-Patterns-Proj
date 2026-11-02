package lookup;

import core.DatabaseManager;

import java.util.ArrayList;
import java.util.List;

/** CRUD for lookup tables (DonationCategory, Skill). */
public class LookupService {

    private DatabaseManager db = DatabaseManager.getInstance();
    private final List<LookupItem> items = new ArrayList<>(); // in-memory table

    public void create(LookupItem item) {
        if (items.contains(item)) {
            throw new IllegalArgumentException("Lookup item with id " + item.getId() + " already exists");
        }
        items.add(item);
        db.executeQuery("INSERT INTO lookup (id, code, name) VALUES (" + item.getId() + ", '"
                + item.getCode() + "', '" + item.getName() + "')");
    }

    public List<LookupItem> readAll() {
        db.executeQuery("SELECT * FROM lookup");
        return new ArrayList<>(items);
    }

    public void update(LookupItem item) {
        int index = items.indexOf(item);
        if (index < 0) {
            throw new IllegalArgumentException("Lookup item with id " + item.getId() + " not found");
        }
        // update IN PLACE so objects that reference this item (tasks, volunteers) see the change
        LookupItem existing = items.get(index);
        existing.setCode(item.getCode());
        existing.setName(item.getName());
        if (item.isActive()) existing.activate(); else existing.deactivate();
        if (existing instanceof Skill && item instanceof Skill)
            ((Skill) existing).setDescription(((Skill) item).getDescription());
        if (existing instanceof DonationCategory && item instanceof DonationCategory)
            ((DonationCategory) existing).setTaxDeductible(((DonationCategory) item).isTaxDeductible());
        db.executeQuery("UPDATE lookup SET code='" + item.getCode() + "', name='" + item.getName()
                + "' WHERE id=" + item.getId());
    }

    public void delete(int id) {
        boolean removed = items.removeIf(i -> i.getId() == id);
        if (!removed) {
            throw new IllegalArgumentException("Lookup item with id " + id + " not found");
        }
        db.executeQuery("DELETE FROM lookup WHERE id=" + id);
    }

    public LookupItem findById(int id) {
        for (LookupItem i : items) if (i.getId() == id) return i;
        return null;
    }

    public int nextId() {
        int max = 0;
        for (LookupItem i : items) max = Math.max(max, i.getId());
        return max + 1;
    }

    /** Active skills only (used by volunteer screens). */
    public List<Skill> getActiveSkills() {
        List<Skill> r = new ArrayList<>();
        for (LookupItem i : items) if (i instanceof Skill && i.isActive()) r.add((Skill) i);
        return r;
    }

    /** Active donation categories only (used by the donation form). */
    public List<DonationCategory> getActiveCategories() {
        List<DonationCategory> r = new ArrayList<>();
        for (LookupItem i : items) if (i instanceof DonationCategory && i.isActive()) r.add((DonationCategory) i);
        return r;
    }
}
