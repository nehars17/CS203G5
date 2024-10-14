package csd.cuemaster.user;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import csd.cuemaster.profile.Profile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
    // We define three roles/authorities: ROLE_PLAYER or ROLE_ADMIN or ROLE_ORGANISER
    @ElementCollection(fetch = FetchType.EAGER)
    private List<SimpleGrantedAuthorityImpl> authorities;

    private boolean enabled;

    private String provider;

    private String activationToken;

    private LocalDateTime expiryDate;


    @OneToOne(mappedBy = "user", orphanRemoval = true, cascade=CascadeType.ALL)
    @JsonIgnore
    private Profile profile;

    public User(String username, String password, List<SimpleGrantedAuthorityImpl> authorities, String provider, Boolean enabled){
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.provider = provider;
        this.enabled = enabled;
    }

    /* Return a collection of authorities (roles) granted to the user.
    */
    // @Override
    // public Collection<? extends GrantedAuthority> getAuthorities() {
    //     return Arrays.asList(new SimpleGrantedAuthority(authorities));
    // }

    // @JsonCreator
    // public User(@JsonProperty("username") String username,
    //             @JsonProperty("password") String password,
    //             @JsonProperty("authorities") List<GrantedAuthority> authorities) {
    //     this.username = username;
    //     this.password = password;
    //     this.authorities = authorities;

    // }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities; // Convert List<String> to List<GrantedAuthority>
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

