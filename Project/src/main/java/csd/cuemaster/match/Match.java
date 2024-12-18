package csd.cuemaster.match;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import csd.cuemaster.tournament.Tournament;
import csd.cuemaster.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
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
public class Match {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @JsonBackReference
    @OneToOne 
    @JoinColumn(name = "tournament_id", nullable=false)
    private Tournament tournament;

    //stores each of the players id
    @ManyToOne 
    @JoinColumn(name="user_id_1", nullable = false)
    private User user1;

    @ManyToOne 
    @JoinColumn(name="user_id_2", nullable = false)
    private User user2;

    //stores the id of the winner of the match
    @ManyToOne 
    @JoinColumn(name = "winner_id")
    @JsonIgnore
    private User winner;

    private LocalDate matchDate;
    private LocalTime matchTime;

    private int user1Score;
    private int user2Score;
    
    @Enumerated(EnumType.STRING)
    private Tournament.Status status;
    
    public Match(Tournament tournament, User user1, User user2, LocalDate matchDate, LocalTime matchTime, int user1Score, int user2Score, Tournament.Status status) {
        this.tournament = tournament;
        this.user1 = user1;
        this.user2 = user2;
        this.matchDate = matchDate;
        this.matchTime = matchTime;
        this.user1Score = user1Score;
        this.user2Score = user2Score;

        this.status = status != null ? status : (tournament != null ? tournament.getStatus() : null);
    }
}