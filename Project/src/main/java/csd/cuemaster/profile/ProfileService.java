@Repository
public interface ProfileRepository extends JpaRepository <Profile, Long> {
    Optional<Profile> findByUser(User user); 
}
