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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Controller
public class Routing {
//    Create itemRepo bean to give access to JpaRepository methods (and custom methods if made)
    @Autowired
    ItemRepo itemRepo;

    @Autowired
    CommentRepo commentRepo;

//    Initializing private default filtering.  This is to hide deleted items and hide comments on regular queries.
    private SimpleBeanPropertyFilter s = SimpleBeanPropertyFilter.serializeAllExcept("comments", "enabled");
    private FilterProvider defaultFilter = new SimpleFilterProvider().addFilter("filter", s);

    //    MAPPING FOR FRONTEND ELEMENTS TO MAKE API MORE USER-FRIENDLY.  Had to assume that no technologies existed on
//    Reviewers environment to allow interacting with API (ie postman).
    @GetMapping(value = "/")
    public String getHome(){
        return "/index.html";
    }

    @GetMapping(value = "/createitem")
    public String getCreateItem(Model model){
        Item item = new Item();

        model.addAttribute("item", item);

        return "/actions/create.html";
    }

    @GetMapping(value = "/crudopts")
    public String getPickItem(){
        return "/actions/crudopts.html";
    }

    @GetMapping(value = "/edititem/{id}")
    public String pickItemEdit(@PathVariable Long id, Model model){
        Optional<Item> item = itemRepo.findById(id);

        if(item.isPresent()){
            model.addAttribute("item", item.get());
        }else{
            return "/";
        }
        return "/actions/edititem.html";
    }

//    Post-redirect-get implementation for CRUD actions.
    @PostMapping(value = "/inventory/get")
    public String redirectGetItem(@RequestParam String id, @RequestParam String requestType){
        String returnUrl = "/";

        switch(requestType) {
            case "create":
                returnUrl += "inventory/create";
                break;
            case "view":
                returnUrl += "inventory/" +id;
                break;
            case "edit":
                returnUrl += "edititem/" +id;
                break;
            case "delete":
                returnUrl += "deleteitem/" + id;
                break;
            default:
                returnUrl = "";
                break;
        }
        return "redirect:" + returnUrl;
    }

    @GetMapping(value = "/deleteitem/{id}")
    public String confirmDelete(@PathVariable Long id, Model model){
        Optional<Item> item = itemRepo.findById(id);

        if(item.isPresent()){
            model.addAttribute("item", item.get());
        }else{
            return "/";
        }
        return "/actions/deleteitem.html";
    }

//    Get mapping for all items in inventory.  This returns a JSON response entity from a list.
    @GetMapping(value = "/inventory")
    public ResponseEntity<MappingJacksonValue> getAllInventory() {
        try{
            List<Item> inventory = itemRepo.findAll();

            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(inventory);
            mappingJacksonValue.setFilters(defaultFilter);

            return new ResponseEntity<>(mappingJacksonValue, HttpStatus.OK);
        }catch (Exception e){
//            return null response entity and set HTTP Status to 500 (generic Internal server error)
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

//    Get mapping for a specific inventory item by that items ID.
    @GetMapping(value = "/inventory/{id}")
    public ResponseEntity<MappingJacksonValue> getItemByID(@PathVariable("id") long id){

//        Allows you to use .isPresent method and works with ResponseEntity.
        Optional<Item> item = itemRepo.findById(id);

        if(item.isPresent()) {
//            Return entity with the item.
            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(item.get());
            mappingJacksonValue.setFilters(defaultFilter);
            return new ResponseEntity<>(mappingJacksonValue, HttpStatus.OK);
        }else{
//            Return HTTP code 404 (not found)
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(value = "/graveyard")
    public ResponseEntity<MappingJacksonValue> getAllDeleted(){
        try{
            SimpleBeanPropertyFilter s = SimpleBeanPropertyFilter.serializeAll();
            FilterProvider includeAllFilter = new SimpleFilterProvider().addFilter("filter", s);

            List<Item> deletedInventory = itemRepo.findAllDeleted();

            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(deletedInventory);
            mappingJacksonValue.setFilters(includeAllFilter);

            return new ResponseEntity<>(mappingJacksonValue, HttpStatus.OK);
        }catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/graveyard/{id}")
    public ResponseEntity<MappingJacksonValue> undeleteItem(@PathVariable Long id, @RequestBody Comment comment){
        Optional<Item> itemToUndelete = itemRepo.findByIdDeleted(id);
        SimpleBeanPropertyFilter s = SimpleBeanPropertyFilter.serializeAll();
        FilterProvider includeAllFilter = new SimpleFilterProvider().addFilter("filter", s);

        if(itemToUndelete.isPresent()){
            Item _item = itemToUndelete.get();
            _item.setEnabled(true);
            _item.setLastUpdated(LocalDateTime.now().toString());
            itemRepo.save(_item);

            comment.setType("deletion");
            commentRepo.save(new Comment(comment.getCommentId(), comment.getComment(),
                            comment.getEntryDate(), comment.getType(), _item));

            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(itemRepo.findById(id));
            mappingJacksonValue.setFilters(includeAllFilter);

            return new ResponseEntity<>(mappingJacksonValue, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

//    Create new entries with post.
    @PostMapping(value = "/inventory")
    public ResponseEntity<MappingJacksonValue> postItem(@ModelAttribute Item item){

//        Added items are 'enabled' by default because they cannot be soft-deleted by definition once added.
        try {
            Item _item = itemRepo
                    .save(new Item(item.getItemID(), item.getItemName(), item.getItemDescription(), item.getQuantity(),
                            true, item.getEntryDate(), item.getLastUpdated()));

            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(itemRepo.findById(_item.getItemID()));
            mappingJacksonValue.setFilters(defaultFilter);

            return new ResponseEntity<>(mappingJacksonValue, HttpStatus.CREATED);
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
    @PutMapping(value = "/edititem/")
    public ResponseEntity<MappingJacksonValue> updateItem(@RequestParam Long itemID, @ModelAttribute Item item){
        Optional<Item> itemToUpdate = itemRepo.findById(itemID);


        if(itemToUpdate.isPresent()){
            Item _item = itemToUpdate.get();
            _item.setItemName(item.getItemName());
            _item.setItemDescription(item.getItemDescription());
            _item.setQuantity(item.getQuantity());
            _item.setLastUpdated(LocalDateTime.now().toString());

            MappingJacksonValue mappingJacksonValue = new MappingJacksonValue(itemRepo.save(_item));
            mappingJacksonValue.setFilters(defaultFilter);

            return new ResponseEntity<>(mappingJacksonValue, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

//    This safely deletes items by just switching the item enabled attribute to false.  All queries filter on this attribute
//    so it is essentially 'deleted' but retrievable.
    @DeleteMapping(value = "/deleteitem/")
    public ResponseEntity<Object> softDelete(@RequestParam Long itemID, @RequestParam String comment){
        Optional<Item> itemToDelete = itemRepo.findById(itemID);

        System.out.println(comment);

        Comment _comment = new Comment();
        _comment.setComment(comment);
        _comment.setType("deletion");

        if(itemToDelete.isPresent()){
            Item _item = itemToDelete.get();
            _item.setEnabled(false);
            _item.setLastUpdated(LocalDateTime.now().toString());
            itemRepo.save(_item);

            _comment.setItem(_item);

            commentRepo.save(_comment);

            return new ResponseEntity<>(null, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

//    This routing is to actually delete items from the database.  Items deleted with this end-point are not recoverable.
//    Included to ensure all actual CRUD endpoints are still available.
    @DeleteMapping(value = "/delete/harddelete/{id}")
    public ResponseEntity<Object> hardDelete(@PathVariable Long id, Model model){
        Optional<Item> itemToDelete = itemRepo.findByIdDeleted(id);

        if(itemToDelete.isPresent()){
            itemRepo.deleteById(id);
            model.addAttribute(id);
            return new ResponseEntity<>(null, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}
