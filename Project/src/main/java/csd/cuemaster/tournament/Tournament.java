package csd.cuemaster.tournament;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

public class Tournament {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @NotNull(message = "Tournament name should not be null")
    private String tournamentName;

    @NotNull(message = "The Date should not be null")
    @Size(min = 8, max = 8, message = "The date should be 8 characters long in DDMMYYYY format" )
    private LocalDate tournamentDate; //can consider Datetime object 

    @NotNull(message = "The Time should not be null")
    @Size(min = 4, max = 4, message = "The time should be 4 characters long in HHMM format")
    private LocalTime tournamentTime; 

    @NotNull(message = "The location should not be null")
    private String location; 

    @NotNull(message = "The status should not be null")
    private String status; 

    public Tournament(String tournamentName, LocalDate tournamentDate, LocalTime tournamentTime, String location, String status) {
        this.tournamentName = tournamentName;
        this.tournamentDate = tournamentDate;
        this.tournamentTime = tournamentTime;
        this.location = location;
        this.status = status;
    }
}