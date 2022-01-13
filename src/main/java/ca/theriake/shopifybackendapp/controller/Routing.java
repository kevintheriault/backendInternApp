package ca.theriake.shopifybackendapp.controller;

import ca.theriake.shopifybackendapp.models.Comment;
import ca.theriake.shopifybackendapp.models.Item;
import ca.theriake.shopifybackendapp.repository.CommentRepo;
import ca.theriake.shopifybackendapp.repository.ItemRepo;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@RestController
public class Routing {
//    Create itemRepo bean to give access to JpaRepository methods (and custom methods if made)
    @Autowired
    ItemRepo itemRepo;

    @Autowired
    CommentRepo commentRepo;

//    Get mapping for all items in inventory.  This returns a JSON response entity from a list.
    @GetMapping(value = "/inventory")
    public ResponseEntity<MappingJacksonValue> getAllInventory() {
        try{
            SimpleBeanPropertyFilter s = SimpleBeanPropertyFilter.serializeAllExcept("comments");

            FilterProvider filter = new SimpleFilterProvider().addFilter("filter", s);

            List<Item> inventory = itemRepo.findAll();

            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(inventory);
            mappingJacksonValue.setFilters(filter);

            return new ResponseEntity<>(mappingJacksonValue, HttpStatus.OK);
        }catch (Exception e){
//            return null response entity and set HTTP Status to 500 (generic Internal server error)
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    Get mapping for a specific inventory item by that items ID.
    @GetMapping(value = "/inventory/{id}")
    public ResponseEntity<MappingJacksonValue> getItemByID(@PathVariable("id") long id){
        SimpleBeanPropertyFilter s = SimpleBeanPropertyFilter.serializeAllExcept("comments");

        FilterProvider filter = new SimpleFilterProvider().addFilter("filter", s);

//        Allows you to use .isPresent method and works with ResponseEntity.
        Optional<Item> item = itemRepo.findById(id);

        MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(item.get());
        mappingJacksonValue.setFilters(filter);

        if(item.isPresent()) {
//            Return entity with the item.
            return new ResponseEntity<>(mappingJacksonValue, HttpStatus.OK);
        }else{
//            Return HTTP code 404 (not found)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/graveyard")
    public ResponseEntity<MappingJacksonValue> getAllDeleted(){
        try{
            SimpleBeanPropertyFilter s = SimpleBeanPropertyFilter.serializeAllExcept();
            FilterProvider filter = new SimpleFilterProvider().addFilter("filter", s);

            List<Item> deletedInventory = itemRepo.findAllDeleted();

            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(deletedInventory);
            mappingJacksonValue.setFilters(filter);

            return new ResponseEntity<>(mappingJacksonValue, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/graveyard/{id}")
    public ResponseEntity<MappingJacksonValue> undeleteItem(@PathVariable Long id, @RequestBody Comment comment){
        Optional<Item> itemToUndelete = itemRepo.findByIdDeleted(id);
        SimpleBeanPropertyFilter s = SimpleBeanPropertyFilter.serializeAll();

        FilterProvider filter = new SimpleFilterProvider().addFilter("filter", s);

        if(itemToUndelete.isPresent()){
            Item _item = itemToUndelete.get();
            _item.setEnabled(true);
            _item.setLastUpdated(LocalDateTime.now().toString());
            itemRepo.save(_item);

            comment.setType("deletion");
            commentRepo.save(new Comment(comment.getCommentId(), comment.getComment(),
                            comment.getEntryDate(), comment.getType(), _item));

            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(itemRepo.findById(id));
            mappingJacksonValue.setFilters(filter);

            return new ResponseEntity<>(mappingJacksonValue, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
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
    public ResponseEntity<Object> bulkPost(@RequestBody List<Item> bulkItems){
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
    public ResponseEntity<MappingJacksonValue> updateItem(@PathVariable long id, @RequestBody Item item){
        Optional<Item> itemToUpdate = itemRepo.findById(id);
        SimpleBeanPropertyFilter s = SimpleBeanPropertyFilter.serializeAllExcept("comments");
        FilterProvider filter = new SimpleFilterProvider().addFilter("filter", s);

        if(itemToUpdate.isPresent()){
            Item _item = itemToUpdate.get();
            _item.setItemName(item.getItemName());
            _item.setItemDescription(item.getItemDescription());
            _item.setQuantity(item.getQuantity());
            _item.setLastUpdated(LocalDateTime.now().toString());

            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(itemRepo.save(_item));
            mappingJacksonValue.setFilters(filter);

            return new ResponseEntity<>(mappingJacksonValue, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

//    This safely deletes items.
    @DeleteMapping(value = "/inventory/{id}")
    public ResponseEntity<Object> softDelete(@PathVariable long id, @RequestBody Comment comment){
        Optional<Item> itemToDelete = itemRepo.findById(id);

        if(itemToDelete.isPresent()){
            Item _item = itemToDelete.get();
            _item.setEnabled(false);
            itemRepo.save(_item);

            comment.setType("deletion");
            commentRepo.save(new Comment(comment.getCommentId(), comment.getComment(),
                            comment.getEntryDate(), comment.getType(), _item));

            return new ResponseEntity<>(null, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

//    This routing is to actually delete items from the database.
    @DeleteMapping(value = "/delete/harddelete/{id}")
    public ResponseEntity<Object> hardDelete(@PathVariable long id, Model model){
        Optional<Item> itemToDelete = itemRepo.findById(id);

        if(itemToDelete.isPresent()){
            itemRepo.deleteById(id);
            model.addAttribute(id);
            return new ResponseEntity<>(null, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

}
