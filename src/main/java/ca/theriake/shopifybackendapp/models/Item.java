package ca.theriake.shopifybackendapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Column(name = "item_id")
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long itemID;

    @Column(name = "item_name", columnDefinition = "varchar(50)")
    private String itemName;

    @Column(name = "item_description", columnDefinition = "varchar(255)")
    private String itemDescription;

    @Column(name = "quantity")
    private int quantity;

    @Column(name = "enabled", columnDefinition = "bit NOT NULL default 1")
    private boolean enabled;

    @Column(name="entry_date", columnDefinition = "date default (date('now','localtime'))")
    private LocalDate entryDate = LocalDate.now();

    @Column(name = "last_updated", columnDefinition = "date default (datetime('now','localtime'))")
    private LocalDateTime lastUpdated = LocalDateTime.now();
}
