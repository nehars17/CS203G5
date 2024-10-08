package csd.cuemaster.client;


import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import csd.cuemaster.user.User;

// import csd.cuemaster.profile.Book;

@Component
public class RestTemplateClient {
    
    private final RestTemplate template;

    /**
     * Add authentication information for the RestTemplate
     * 
     */
    public RestTemplateClient(RestTemplateBuilder restTemplateBuilder) {
        this.template = restTemplateBuilder
                .basicAuthentication("admin", "goodpassword")
                .build();
    }
    /**
     * Get a book with given id
     * 
     * @param URI
     * @param id
     * @return
     */
    // public Book getBook(final String URI, final Long id) {
    //     final Book book = template.getForObject(URI + "/" + id, Book.class);
    //     return book;
    // }

    /**
     * Add a new book
     * 
     * @param URI
     * @param newBook
     * @return
     */
    public User addOrganiserUser(final String URI, final User newUser) {
        final User returned = template.postForObject(URI, newUser, User.class);
        
        return returned;
    }

    /**
     * Get a book, but return a HTTP response entity.
     * @param URI
     * @param id
     * @return
     */
    // public ResponseEntity<Book> getBookEntity(final String URI, final Long id){
    //     return template.getForEntity(URI + "/{id}", Book.class, Long.toString(id));
    // }
    
}