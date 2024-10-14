package csd.cuemaster.user;

import org.springframework.security.core.GrantedAuthority;

public class SimpleGrantedAuthorityImpl implements GrantedAuthority {
    private String authority;

    public SimpleGrantedAuthorityImpl(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return authority;
    }
}

