package csd.week5.review;

import csd.week5.book.Book;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Review {
    private  @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    /**
     * TODO: Activity 1 - Java Bean Validation
     * Add constraints here to ensure the review is not null,
     * and should be at least 10 characters long. 
     */
    // your code here
    @NotNull(message="Review should not be null")
    @Size(min=10,max=200, message="Review should be at least 10 characters")
    private String review;

    @ManyToOne
    // the column "book_id" will be in the auto-generated table "review"
    // nullable = false: add not-null constraint to the database column "book_id"
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
}