package csd.week5.book;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;
import csd.week5.review.Review;
import lombok.*;


@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Book {
    private @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    

    @NotNull(message = "Book's title should not be null")
    // null elements are considered valid, so we need a size constraints too
    @Size(min = 5, max = 200, message = "Book's title should be at least 5 characters long")
    private String title;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews;
    
    public Book(String title){
        this.title = title;
    }
    
}