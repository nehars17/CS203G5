package csd.cuemaster.user;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import csd.cuemaster.models.TOTPToken;
import csd.cuemaster.profile.Profile;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
// Other imports...

@Entity
@Table(name = "[User]")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class User implements UserDetails {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Email should not be null")
    @Size(min = 5, max = 30, message = "Email address should be between 5 and 30 characters")
    private String username;

    @JsonProperty(access = Access.WRITE_ONLY)
    @NotNull(message = "Password should not be null")
    @Size(min = 8, message = "Password should be at least 8 characters")
    private String password;

    @JsonProperty(access = Access.WRITE_ONLY)
    private String authorities;

    private boolean enabled;


    private boolean unlocked;

    private String provider;

    @JsonIgnore
    private String activationToken;

    @JsonIgnore
    private Key secret;
    
    @JsonProperty(access = Access.WRITE_ONLY)
    private String recaptchaToken;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")

    @JsonIgnore
    private TOTPToken totpToken;

    @JsonIgnore
    private int failedLoginAttempts;

    @JsonIgnore
    private LocalDateTime expiryDate;

    @OneToOne(mappedBy = "user", orphanRemoval = true, cascade = CascadeType.ALL)
    @JsonIgnore
    private Profile profile;

    public User(String username, String password, String authorities, String provider, boolean enabled) {
        this.username = username;
        this.password = password;
        this.authorities = authorities;
        this.authorities = authorities;
        this.enabled = enabled;
        this.unlocked = true;
        this.failedLoginAttempts=0;
        this.provider = provider;
    }

    /*
     * Return a collection of authorities (roles) granted to the user.
     */
    /*
     * Return a collection of authorities (roles) granted to the user.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Arrays.asList(new SimpleGrantedAuthority(authorities));
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return unlocked;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}