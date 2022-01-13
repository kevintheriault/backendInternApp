package ca.theriake.shopifybackendapp.controller;

import ca.theriake.shopifybackendapp.models.Comment;
import ca.theriake.shopifybackendapp.models.Item;
import ca.theriake.shopifybackendapp.repository.CommentRepo;
import ca.theriake.shopifybackendapp.repository.ItemRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Controller
public class Routing {
//    Create itemRepo bean to give access to JpaRepository methods (and custom methods if made)
    @Autowired
    ItemRepo itemRepo;

    @Autowired
    CommentRepo commentRepo;

    @GetMapping(value ="/")
    public String getHome(){
        return "/Index.html";
    }

//    Get mapping for all items in inventory.  This returns a JSON response entity from a list.
    @GetMapping(value = "/inventory")
    public ResponseEntity<List<Item>> getAllInventory() {
        try{
//            Create list for all inventory using the JpaRepository method findAll.  The mapping to the table is done
//            using the item.java model.
            List<Item> inventory = new ArrayList<Item>(itemRepo.findAll());
            return new ResponseEntity<>(inventory, HttpStatus.OK);
        }catch (Exception e){
//            return null response entity and set HTTP Status to 500 (generic Internal server error)
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    Get mapping for a specific inventory item by that items ID.
    @GetMapping(value = "/inventory/{id}")
    public ResponseEntity<Item> getItemByID(@PathVariable("id") long id){
//        Allows you to use .isPresent method and works with ResponseEntity.
        Optional<Item> item = itemRepo.findById(id);

        if(item.isPresent()) {
//            Return entity with the item.
            return new ResponseEntity<>(item.get(), HttpStatus.OK);
        }else{
//            Return HTTP code 404 (not found)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/graveyard")
    public ResponseEntity<List<Item>> getAllDeleted(){
        try{
            List<Item> deletedInventory = new ArrayList<Item>(itemRepo.findAllDeleted());
            return new ResponseEntity<>(deletedInventory, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    Create new entries with post.
    @PostMapping(value = "/inventory")
    public ResponseEntity<Item> postItem(@RequestBody Item item){

//        Added items are 'enabled' by default because they cannot be soft-deleted by definition once added.
        try {
            Item _item = itemRepo
                    .save(new Item(item.getItemID(), item.getItemName(), item.getItemDescription(), item.getQuantity(),
                            true, item.getEntryDate(), item.getLastUpdated()));

            return new ResponseEntity<>(_item, HttpStatus.CREATED);
        } catch (Exception e) {

            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //    Create new entries with post.
    @PostMapping(value = "/inventory/bulk")
    public ResponseEntity<Item> bulkPost(@RequestBody List<Item> bulkItems){
        for(Item item : bulkItems) {
            try {
                Item _item = itemRepo
                        .save(new Item(item.getItemID(), item.getItemName(), item.getItemDescription(), item.getQuantity(),
                                item.isEnabled(), item.getEntryDate(), item.getLastUpdated()));
            } catch (Exception e) {

                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

//    update existing entities
    @PutMapping(value = "/inventory/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable long id, @RequestBody Item item){
        Optional<Item> itemToUpdate = itemRepo.findById(id);

        if(itemToUpdate.isPresent()){
            Item _item = itemToUpdate.get();
            _item.setItemName(item.getItemName());
            _item.setItemDescription(item.getItemDescription());
            _item.setQuantity(item.getQuantity());
            _item.setLastUpdated(LocalDateTime.now().toString());

            return new ResponseEntity<>(itemRepo.save(_item), HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

//    This safely deletes items.
    @DeleteMapping(value = "/inventory/{id}")
    public String softDelete(@PathVariable long id, @RequestBody Comment comment){
        Optional<Item> itemToDelete = itemRepo.findById(id);

        if(itemToDelete.isPresent()){
            Item _item = itemToDelete.get();
            _item.setEnabled(false);
            itemRepo.save(_item);

            commentRepo.save(new Comment(comment.getCommentId(), comment.getComment(), comment.getEntryDate(), _item));

            return "/delete/DeleteSuccess.html";
        }else{
            return "/delete/DeleteNoSuccess.html";
        }
    }

//    This routing is to actually delete items from the database.
    @DeleteMapping(value = "/delete/harddelete/{id}")
    public String hardDelete(@PathVariable long id){
        Optional<Item> itemToDelete = itemRepo.findById(id);

        if(itemToDelete.isPresent()){
            itemRepo.deleteById(id);
            return "/delete/DeleteSuccess.html";
        }else{
            return "/delete/DeleteNoSuccess.html";
        }
    }

}
