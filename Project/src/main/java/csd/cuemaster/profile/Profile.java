package csd.cuemaster.profile;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnore;

import csd.cuemaster.user.User;
import lombok.*;


@Entity
@Getter                     //getter methods for all the variables 
@Setter
@ToString
@AllArgsConstructor         //generates a constructor that takes all instance variables as argument
@NoArgsConstructor          //generates a no-argument constructor
@EqualsAndHashCode          //generate equals() and hashCode() methods for a class
public class Profile {
    private @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;
    

    @NotNull(message = "First name should not be null")
    @Size(min = 5, message = "First name should be at least 5 characters long")
    private String firstname;

    @NotNull(message = "Last name should not be null")
    @Size(min = 2, message = "Last name should be at least 5 characters long")
    private String lastname;

    @NotNull(message = "Date of birth cannot be null")
    @Size(min = 8, max = 8, message = "Birth Date should be 8 characters long in this format DDMMYYYY")
    private String birthdate;

    @OneToOne
    // the column "book_id" will be in the auto-generated table "review"
    // nullable = false: add not-null constraint to the database column "book_id"
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
}