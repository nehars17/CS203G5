package csd.cuemaster.client;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestTemplateClient {
    private final RestTemplate template;

    /**
    //  * Add authentication information for the RestTemplate
    //  */
    public RestTemplateClient(RestTemplateBuilder restTemplateBuilder) {
        this.template = restTemplateBuilder
                .basicAuthentication("admin", "goodpassword")
                .build();
    }

    // /**
    //  * Returns a sorted list of players with given id.
    //  * @param URI
    //  * @param id
    //  * @return
    //  */
    // public List<Profile> getLeaderboard(final String URI, final Long id) {
    //     ResponseEntity<List<Profile>> response = template.exchange(
    //     URI + "/" + id,
    //     HttpMethod.GET,
    //     null,
    //     new ParameterizedTypeReference<List<Profile>>() {}
    //     );
    //     List<Profile> profiles = response.getBody();
    //     return profiles;
    // }

    // /**
    //  * Returns a list of players after a points reset with given id.
    //  * @param URI
    //  * @param id
    //  * @return
    //  */
    // public List<Profile> resetPoints(final String URI, final Long id) {
    //     ResponseEntity<List<Profile>> response = template.exchange(
    //     URI + "/" + id,
    //     HttpMethod.PUT,
    //     null,
    //     new ParameterizedTypeReference<List<Profile>>() {}
    //     );
    //     List<Profile> profiles = response.getBody();
    //     return profiles;
    // }
}