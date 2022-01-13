package ca.theriake.shopifybackendapp.repository;

import ca.theriake.shopifybackendapp.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepo extends JpaRepository<Comment, Long> {

}
