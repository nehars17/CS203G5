package csd.cuemaster.tournament;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode

public class Tournament {

    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;

    @NotNull(message = "Tournament name should not be null")
    private String tournamentname;

    @NotNull(message = "The Date should not be null")
    @Size(min = 8, max = 8, message = "The date should be 8 characters long in DDMMYYYY format" )
    private String tournamentdate; //can consider Datetime object 

    @NotNull(message = "The Time should not be null")
    @Size(min = 4, max = 4, message = "The time should be 4 characters long in HHMM format")
    private String tournamenttime; 

    @NotNull(message = "The location should not be null")
    private String location; 

    @NotNull(message = "The status should not be null")
    private String status; 

    
}