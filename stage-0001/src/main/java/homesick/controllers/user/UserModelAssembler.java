package homesick.controllers.user;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import homesick.models.User;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {

  @Override
  public EntityModel<User> toModel(User user) {

    EntityModel<User> model = EntityModel.of(user, //
        linkTo(methodOn(UserController.class).one(user.getId())).withSelfRel(),
        linkTo(methodOn(UserController.class).all()).withRel("users"));
    
    // Conditional links based on state of the order

    if (user.getLegacyId() == 1L) {
      model.add(linkTo(methodOn(UserController.class).cancel(user.getId())).withRel("cancel"));
      model.add(linkTo(methodOn(UserController.class).complete(user.getId())).withRel("complete"));
    }

    return model;
  }

}