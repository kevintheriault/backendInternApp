package ca.theriake.shopifybackendapp.repository;

import ca.theriake.shopifybackendapp.models.Comment;
import ca.theriake.shopifybackendapp.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepo extends JpaRepository<Comment, Long> {

}
