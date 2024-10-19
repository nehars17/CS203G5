package csd.cuemaster.user;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import csd.cuemaster.deserializer.*;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import csd.cuemaster.profile.Profile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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

/* Implementations of UserDetails to provide user information to Spring Security, 
e.g., what authorities (roles) are granted to the user and whether the account is enabled or not
*/
public class User implements UserDetails{
    private static final long serialVersionUID = 1L;

    private @Id @GeneratedValue(strategy = GenerationType.IDENTITY) Long id;
    
    @NotNull(message = "Email should not be null")
    @Size(min = 5, max = 20, message = "Email address should be between 5 and 20 characters")
    private String username;
    
    @NotNull(message = "Password should not be null")
    @Size(min = 8, message = "Password should be at least 8 characters")
    private String password;

    @NotNull(message = "Authorities should not be null")
    @JsonDeserialize(using = AuthorityDeserializer.class)
    // We define three roles/authorities: ROLE_PLAYER or ROLE_ADMIN or ROLE_ORGANISER
    private List<String> authorities;

    private boolean enabled;

    private String provider;

    private String activationToken;

    private LocalDateTime expiryDate;


    @OneToOne(mappedBy = "user", orphanRemoval = true, cascade=CascadeType.ALL)
    @JsonIgnore
    private Profile profile;

    public User(String username, String password, List<String> authorities, String provider,Boolean enabled){
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.provider = provider;
    }

    /* Return a collection of authorities (roles) granted to the user.
    */
    /* The authorities field is now a List<String>, and the @ElementCollection annotation stores it as a JSON-like collection in the database.
        In getAuthorities(), the List<String> is converted into a collection of GrantedAuthority objects using Java Streams.
    */
  @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }


    /*
    The various is___Expired() methods return a boolean to indicate whether
    or not the user’s account is enabled or expired.
    */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return enabled;
    }
}