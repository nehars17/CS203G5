package csd.cuemaster.tournament;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import csd.cuemaster.match.Match;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
// @NoArgsConstructor
@EqualsAndHashCode

public class Tournament {

    private @Id @GeneratedValue (strategy = GenerationType.IDENTITY) Long id;

    @NotNull(message = "Tournament name should not be null")
    private String tournamentname;

    @NotNull(message = "The location should not be null")
    private String location; 

    @NotNull(message = "The date should not be null")
    // @Size(min = 10, max = 10, message = "The date should be 8 characters long in YYYY-MM-DD format" )
    private LocalDate startDate; 

    @NotNull(message = "The date should not be null")
    // @Size(min = 10, max = 10, message = "The date should be 8 characters long in YYYY-MM-DD format" )
    private LocalDate endDate;

    @NotNull(message = "The time should not be null")
    // @Size(min = 8, max = 8, message = "The time should be 4 characters long in HH:MM:SS format")
    private LocalTime time; 

    @NotNull(message = "The status should not be null")
    @Enumerated(EnumType.STRING) // Persist the enum as a string in the database for better readability.
    private Status status; 
    
    @NotNull(message = "The description should not be null")
    private String description; 

    private Long winnerId; // stores the winner's player ID


    @ElementCollection
    private List<Long> players = new ArrayList<>();; // Storing player IDs participating in the tournament
    // Initializing players list to avoid null issues 
    
    // Enum for status with defined values
    // Status enum definition for better control and type safety
    public enum Status {
        UPCOMING,
        ONGOING,
        COMPLETED,
        CANCELLED,
        ROUND_OF_32,
        ROUND_OF_16,
        QUARTER_FINALS,
        SEMI_FINAL,
        FINAL
    }

    @JsonManagedReference
    @OneToMany(mappedBy = "tournament", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<Match> match;

    // Default constructor
    public Tournament() {
        // Initialize fields if needed
        this.players = new ArrayList<>(); // Avoids null pointer exceptions when adding players
    }

    // Additional overloaded constructors if needed
    public Tournament(String tournamentname, String location, LocalDate startDate, LocalDate endDate, 
    LocalTime time, Status status, String description, Long winnerId, List<Long> players) {
        this.tournamentname = tournamentname;
        this.location = location;
        this.startDate = startDate;
        this.endDate = endDate;
        this.time = time;
        this.status = status;
        this.description = description;
        this.winnerId = winnerId;
        this.players = players != null ? new ArrayList<>(players) : new ArrayList<>(); // Ensure players list is not null
    }    
}