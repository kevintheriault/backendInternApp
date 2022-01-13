package ca.theriake.shopifybackendapp.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @Column(name = "comment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(name="comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "entry_date", columnDefinition = "DATE DEFAULT (DATE('now'))")
    private String entryDate = LocalDateTime.now().toString();

    @ManyToOne
    private Item item;
}
