package homesick.resources;

import org.springframework.data.jpa.repository.JpaRepository;

import homesick.models.User;

public interface UserRepository extends JpaRepository<User, Long> {

}
