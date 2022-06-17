package homesick.controllers.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.mediatype.problem.Problem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import homesick.models.User;
import homesick.resources.UserRepository;

@RestController
@RequestMapping(path = "/api/users")
public class UserController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserModelAssembler assembler;

	@GetMapping("deprecated")
	public List<User> allOriginal() {
		return userRepository.findAll();
	}

	@GetMapping("")
	public CollectionModel<EntityModel<User>> all() {
		/*
		 * List<EntityModel<User>> employees = userRepository.findAll().stream() //
		 * .map(assembler::toModel) // .collect(Collectors.toList());
		 * 
		 * return CollectionModel.of(employees,
		 * linkTo(methodOn(UserController.class).all()).withSelfRel());
		 */
		/*
		 * List<EntityModel<User>> users = userRepository.findAll().stream() .map(user
		 * -> EntityModel.of(user,
		 * linkTo(methodOn(UserController.class).one(user.getId())).withSelfRel(),
		 * linkTo(methodOn(UserController.class).all()).withRel("users")))
		 * .collect(Collectors.toList());
		 */
		List<EntityModel<User>> users = userRepository.findAll().stream().map(user -> assembler.toModel(user))
				.collect(Collectors.toList());

		return CollectionModel.of(users, linkTo(methodOn(UserController.class).all()).withSelfRel());
	}

	@GetMapping("{id}")
	public EntityModel<User> one(@PathVariable Long id) {
		// return userRepository.findById(id).orElseThrow();
		User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
		/*
		 * return EntityModel.of(user, //
		 * linkTo(methodOn(UserController.class).one(id)).withSelfRel(),
		 * linkTo(methodOn(UserController.class).all()).withRel("users"));
		 */
		return assembler.toModel(user);
	}

	@PostMapping("")
	public ResponseEntity<EntityModel<User>> create(@RequestBody User user) {

		user = userRepository.saveAndFlush(user);
		/*
		 * EntityModel<User> model = EntityModel.of(user,
		 * linkTo(methodOn(UserController.class).one(user.getId())).withSelfRel(),
		 * linkTo(methodOn(UserController.class).all()).withRel("users"));
		 */
		EntityModel<User> model = assembler.toModel(user);

		return ResponseEntity //
				.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(model);
	}

	@PutMapping("{id}")
	public ResponseEntity<?> update(@PathVariable Long id, @RequestBody User user) {

		User result = userRepository.findById(id).map(ouser -> {
			ouser.setName(user.getName());
			ouser.setUsername(user.getUsername());
			ouser.setPassword(user.getPassword());
			return userRepository.save(ouser);
		}).orElseGet(() -> {
			user.setId(id);
			return userRepository.save(user);
		});

		// return result;
		/*
		 * EntityModel<User> model = EntityModel.of(result,
		 * linkTo(methodOn(UserController.class).one(result.getId())).withSelfRel(),
		 * linkTo(methodOn(UserController.class).all()).withRel("users"));
		 */
		EntityModel<User> model = assembler.toModel(result);
		return ResponseEntity //
				.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
				.body(model);
	}

	@DeleteMapping("{id}")
	public ResponseEntity<User> delete(@PathVariable Long id) {
		userRepository.deleteById(id);
		return ResponseEntity.noContent().build();
	}

	// best practice for implement business rules ***
	@DeleteMapping("{id}/cancel")
	public ResponseEntity<?> cancel(@PathVariable Long id) {
		User user = userRepository.findById(id).orElseThrow();
		if (user.getLegacyId() == 1) {
			user.setLegacyId(0);
			userRepository.save(user);
			/*
			 * EntityModel<User> model = EntityModel.of(user,
			 * linkTo(methodOn(UserController.class).one(user.getId())).withSelfRel(),
			 * linkTo(methodOn(UserController.class).all()).withRel("users"));
			 */
			EntityModel<User> model = assembler.toModel(user);

			return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
					.body(model);

		}

		return ResponseEntity //
				.status(HttpStatus.METHOD_NOT_ALLOWED) //
				.header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
				.body(Problem.create() //
						.withTitle("Method not allowed") //
						.withDetail("You can't cancel an order that is in the " + Long.toString(user.getLegacyId())
								+ " status"));
	}

	@PutMapping("{id}/complete")
	public ResponseEntity<?> complete(@PathVariable Long id) {
		User user = userRepository.findById(id).orElseThrow();
		if (user.getLegacyId() == 1) {
			user.setLegacyId(0);
			userRepository.save(user);
			EntityModel<User> model = assembler.toModel(user);
			return ResponseEntity.created(model.getRequiredLink(IanaLinkRelations.SELF).toUri()) //
					.body(model);
		}

		return ResponseEntity //
				.status(HttpStatus.METHOD_NOT_ALLOWED) //
				.header(HttpHeaders.CONTENT_TYPE, MediaTypes.HTTP_PROBLEM_DETAILS_JSON_VALUE) //
				.body(Problem.create() //
						.withTitle("Method not allowed") //
						.withDetail("You can't complete an order that is in the " + Long.toString(user.getLegacyId())
								+ " status"));
	}

}
