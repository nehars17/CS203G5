package csd.cuemaster.profile;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import csd.cuemaster.user.User;
import lombok.*;

@Entity
@Getter // getter methods for all the variables
@Setter
@ToString
@AllArgsConstructor // generates a constructor that takes all instance variables as argument
@NoArgsConstructor // generates a no-argument constructor
@EqualsAndHashCode // generate equals() and hashCode() methods for a class
public class Profile {
    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    @NotNull(message = "First name should not be null")
    @Size(min = 2, message = "First name should be at least 2 characters long")
    private String firstname;

    @NotNull(message = "Last name should not be null")
    @Size(min = 2, message = "Last name should be at least 2 characters long")
    private String lastname;

    @NotNull(message = "Date of birth cannot be null and must follow the format yyyy-MM-dd")
    @Past(message = "Date of birth must be in the past and must follow the format yyyy-MM-dd")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthdate;

    @NotNull (message = "Location should not be null")
    private String birthlocation; 

    private String profilephotopath;

    private String organization;

    private Integer TournamentCount; 

    private Integer TournamentWinCount;

    private Integer MatchCount; 

    private Integer MatchWinCount; 

    private Integer points; 
    

    @OneToOne
    // the column "book_id" will be in the auto-generated table "review"
    // nullable = false: add not-null constraint to the database column "book_id"
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Profile (String firstname, String lastname, LocalDate birthdate, String birthlocation, User user){

        this.firstname = firstname;
        this.lastname = lastname; 
        this.birthdate = birthdate;
        this.birthlocation = birthlocation;
        // this.profilephotopath = profilephotopath;
        this.TournamentCount = 0; 
        this.MatchCount = 0;
        this.organization = null; 
        this.TournamentWinCount = 0; 
        this.MatchWinCount = 0; 
        this.points = 0; 
        this.user = user; 
    }

    public Profile (String firstname, String lastname,LocalDate birthdate, String birthlocation, String organization, User user){
        this.firstname = firstname;
        this.lastname = lastname; 
        this.birthdate = birthdate;
        this.birthlocation = birthlocation;
        // this.profilephotopath = profilephotopath;
        this.organization = organization;
        this.TournamentCount = null; 
        this.MatchCount = null;
        this.TournamentWinCount = null; 
        this.MatchWinCount = null; 
        this.points = null; 
        this.user = user;
    }
    
}