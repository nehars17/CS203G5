package csd.cuemaster.profile;

import java.time.LocalDate;

import csd.cuemaster.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

// import java.io.FileOutputStream;
import java.time.LocalDate;

import csd.cuemaster.user.User;
import csd.cuemaster.user.User.UserRole;
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
    @Size(min = 2, message = "First name should be at least 5 characters long")
    private String firstname;

    @NotNull(message = "Last name should not be null")
    @Size(min = 2, message = "Last name should be at least 5 characters long")
    private String lastname;

    @NotNull(message = "Date of birth cannot be null")
    @Past(message = "Date of birth must be in the past")
    private LocalDate birthdate;

    @NotNull(message = "Location should not be null")
    private String location;

    // private FileOutputStream profilephoto;
    // private FileOutputStream profilephoto;

    private String organization;

    private int TournamentCount;

    private int TournamentWinCount;

    private int MatchCount;

    private int MatchWinCount;

    private Integer points; // use Integer so that I can assign null to points.

    private Integer rank;

    public Long getId() {
        return id;
    }

    public Integer getPoints() {
        return points;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }



    @OneToOne
    // the column "book_id" will be in the auto-generated table "review"
    // nullable = false: add not-null constraint to the database column "book_id"
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}