package example;

public class UserBuilder implements UserBuilderMethods<User, UserBuilder> {
    private final User user = new User();

    @Override
    public User getEntity() {
        return user;
    }
}
