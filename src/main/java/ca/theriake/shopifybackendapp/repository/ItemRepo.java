package ca.theriake.shopifybackendapp.repository;

import ca.theriake.shopifybackendapp.models.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ItemRepo extends JpaRepository<Item, Long> {

    @Override
    @Query("SELECT e FROM #{#entityName} e WHERE e.enabled=true")
    List<Item> findAll();

    @Override
    @Query("SELECT e FROM #{#entityName} e WHERE e.enabled=true AND e.itemID = ?1")
    Optional<Item> findById(Long id);

    @Query("SELECT e FROM #{#entityName} e WHERE e.enabled=false")
    List<Item> findDeleted();
}
