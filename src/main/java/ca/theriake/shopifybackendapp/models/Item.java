package ca.theriake.shopifybackendapp.models;

import lombok.*;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Item {

    public Item(Long itemID, String itemName, String itemDescription, int quantity, boolean enabled, String entryDate, String lastUpdated) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.quantity = quantity;
        this.enabled = enabled;
        this.entryDate = entryDate;
        this.lastUpdated = lastUpdated;
    }

    @Column(name = "item_id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long itemID;

    @Column(name = "item_name", columnDefinition = "text")
    private String itemName;

    @Column(name = "item_description", columnDefinition = "text")
    private String itemDescription;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "enabled", columnDefinition = "bit NOT NULL default 1")
    private boolean enabled;

    @Column(name="entry_date", columnDefinition = "date default (date('now','localtime'))")
    private String entryDate = LocalDate.now().toString();

    @Column(name = "last_updated", columnDefinition = "date default (datetime('now','localtime'))")
    private String lastUpdated = LocalDateTime.now().toString();

    @OneToMany(mappedBy = "item")
    @ToString.Exclude
    private Set<Comment> comments;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        Item item = (Item) o;
        return itemID != null && Objects.equals(itemID, item.itemID);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
