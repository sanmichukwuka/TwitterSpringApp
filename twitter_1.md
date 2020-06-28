# Twitter Project
## Part One

Useful Resources

* [Install Lombok for Eclipse or Spring Tools Suite](https://projectlombok.org/setup/eclipse)
* [Font Awesome CDN](https://fontawesome.com/start)
* [Bootstrap CDN](https://getbootstrap.com/docs/3.3/getting-started/)

1. Intro & Outline
2. Project setup
3. Authentication back end
4. Authentication front end
5. Tweet back end
6. Tweet front end
7. Validation

---

### Intro & Outline
You now have the coding skills to create your own version of one of the most popular sites on the Internet...TWITTER!

Twitter itself was originally built using Ruby on Rails, another popular framework for creating web applications. We're going to create our own version of Twitter using Spring Boot! It may seem like a daunting task to create an app that mimics one of the top social media sites, but we'll take it step-by-step.

Today's Checklist:
1. Create the project
2. Add all necessary dependencies
3. Add Bootstrap imports
4. Set up User authentication
5. Set up Tweet posting
6. Create validation for Users and Tweets
7. Create a navigation bar

---

### Project Setup

We're going to knock out all the project setup at once. There are some pieces that we won't use for a while, but it will be nice to have everything in place for when when we do need it.

First, install Project Lombok for Eclipse, if you choose to do so, it is optional. Using Lombok annotations in the project, is claimed to eliminate the need to write much of the boilerplate code (constructors, getters, setters, toString methods, etc). Instructions can be found [here](https://projectlombok.org/setup/eclipse). Be sure to restart Eclipse after installing.

(Dislcaimer: Using Lombok may or may not be realiable, the Lombok annotations will be shown once, but the author doesn't use Lombok so the annotations won't be found elsewhere and are not active in the build, personal preference, not TTS.)

Create a new Spring Boot Project: (You may name it as you wish, just be consistent)

| Field       | Value                          |
| ----------- |------------------------------- |
| Name        | TechTalentTwitter              |
| Group       | com.tts                        |
| Artifact    | TechTalentTwitter              |
| Version     | 0.0.1-SNAPSHOT                 |
| Description | Spring boot verison of Twitter!|
| Package     | com.tts.TechTalentTwitter      |

Let's add some Spring Starter dependencies:
* Spring Boot DevTools
* H2 Database
* Spring Data JPA
* Lombok  (optional)
* Spring Security
* Thymeleaf
* Validation
* Spring Web

And add a two more dependencies to the pom.xml, `thymeleaf-extras-springsecurity4` gives access to advanced Thymeleaf functionality for authorization, and `org.ocpsoft.prettytime` provides formating for dates in a special way, it's used later on in the project. Add these inside of the `<dependencies>` tags. (Look for latest stable versions on maven which may be different than those shown here)
```xml
<dependencies>
...
  <dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity4</artifactId>
    <version>3.0.4.RELEASE</version>
  </dependency>
  <dependency>
    <groupId>org.ocpsoft.prettytime</groupId>
    <artifactId>prettytime</artifactId>
    <version>4.0.5.Final</version>
  </dependency>
</dependencies>
```

Set up the package structure. Inside the main package `com.tts.TechTalentTwitter`, create 5 child packages:
1. `com.tts.TechTalentTwitter.configuration`
2. `com.tts.TechTalentTwitter.controller`
3. `com.tts.TechTalentTwitter.model`
4. `com.tts.TechTalentTwitter.repository`
5. `com.tts.TechTalentTwitter.service`

Add settings to the `application.properties` file. The queries at the bottom will be needed with authentication.
```
# configure data source
# the db will be stored in a file that persist across app restarts
spring.h2.console.enabled = true
spring.h2.console.path = /console
spring.datasource.url = jdbc:h2:file:~/twitter
spring.datasource.username = sa
spring.datasource.password =
spring.datasource.driver-class-name = org.h2.Driver
spring.datasource.platform=h2

# show sql queries in console
spring.jpa.show-sql = true

# update ddl when app starts
spring.jpa.hibernate.ddl-auto = update

# set up queries for loading users and roles
spring.queries.users-query = select username, password, active from user where username=?
spring.queries.roles-query = select u.username, r.role from user u inner join user_role ur on(u.user_id=ur.user_id) inner join role r on(ur.role_id=r.role_id) where u.username=?
```

Add to the resources folder:
1. Inside of `src/main/resources`, create a file called `data.sql`
2. Inside of `src/main/resources/static`, create a file called `custom.css`
3. Inside of `src/main/resources/static`, create a file called `custom.js`
4. Inside of `src/main/resources/templates`, create a folder called `fragments`

---

### Authentication back end

Configure authentication so that users can login to the app. The user account information is stored in the h2 database.

Create a model for user inside of the `model` package: `User.java` (The Lombok annotations are shown above @Entity)
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User {
...
```
The first four annotations are to use Lombok and reportedly will automatically generate  getters, setters, constructors, and toString method at runtime. An advantage would be to keep the classes leaner, however one disadvantage is that it is a 'black box' where you don't see some of the processes, another is that it has not always been reliable (my opinion, not TTS.)

Add the instance variables, fields, or attributes (all are descriptive terms used) for the User class, which will automatically map to corresponding columns in the user database table.

```java
@Id
@GeneratedValue(strategy = GenerationType.AUTO)
@Column(name = "user_id")
private Long id;

private String email;
private String username;
private String password;
private String firstName;
private String lastName;
private int active;

@CreationTimestamp 
private Date createdAt;

@ManyToMany(cascade = CascadeType.ALL)
@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
private Set<Role> roles;
```

In order to use Spring Security, we have to define roles for each user. To keep things simple, every user in our application will have the same role - "USER". In our `User` class, we have set up the Many to Many relationship for users and roles. A user can have multiple roles and the same role can be assigned to multiple users. This information will be stored in a join table called `user_role`, which will contain two columns - `user_id` and `role_id`.

Additionally, we have created a variable called `active`, which will indicate whether or not the user is active. In our application, every user will have a value of 1 for `active`, indicating that their account is enabled. Again, this is for Spring Security, we won't use it in this build. A use for this would be with user account confirmation where the user receives an email with a link to confirm the account, etc.

Create the model for role inside the `model` package, called `Role.java`
```java
@Entity
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "role_id")
  private Long id;
  private String role;
}
```
That's all there is to it, we won't touch the `Role` class again.

Next, create repositories to interact with the user and role tables. Inside the `repository` package, create a new interface called `UserRepository`. Have the repository extend CrudRepository to inherit the methods defined in CRUD. Add one additional method to find a user based on their username.

```java
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
  public User findByUsername(String username);
}
```

Set up the RoleRepository the same way with one method to find by the role name.
```java
@Repository
public interface RoleRepository extends CrudRepository<Role, Long> {
  public Role findByRole(String role);
}
```

Create a `UserService` class in the `service` package to add some additional functionality on top of those inherited by the repositories. Inside the `service` package, create `UserService`.

First, annotate the class with `@Service` so that it gets picked up by the Spring framework as a service layer. 

Create a constructor with three autowired dependencies. Our service will use the two repositories we just created, as well as a password encoder class from BCrypt.

```java
@Service
public class UserService {

  private UserRepository userRepository;
  private RoleRepository roleRepository;
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired
  public UserService(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
      this.userRepository = userRepository;
      this.roleRepository = roleRepository;
      this.bCryptPasswordEncoder = bCryptPasswordEncoder;
  }
...
```

Add service methods to call the repository methods:
```java
public User findByUsername(String username) {
  return userRepository.findByUsername(username);
}
    
public List<User> findAll(){
  return (List<User>) userRepository.findAll();
}
    
public void save(User user) {
  userRepository.save(user);
}
```

Add a method to create a new `User`. This method encodes the password before saving it to the database, (it is bad practice to save the user passwords as raw strings in an application, it invites data hacking vulnerabilities). This method also sets the user to active and gives them the role USER, to satisfy Spring Security.
```java
public User saveNewUser(User user) {
  user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
  user.setActive(1);
  Role userRole = roleRepository.findByRole("USER");
  user.setRoles(new HashSet<Role>(Arrays.asList(userRole)));
  return userRepository.save(user);
}
```

Add one final method to get the currently logged in `User`. This isn't needed for authentication, necessarily, but will be a very useful utility method.
```java
public User getLoggedInUser() {
  String loggedInUsername = SecurityContextHolder.getContext().getAuthentication().getName();
  return findByUsername(loggedInUsername);
}
```
The User service is done!

There are a couple things referenced in the `UserService` that haven't been set up. First, is the creation of our USER role. When the app starts up for the first time, all of the tables in the database will be empty. We want to create the role USER in the role table at startup so that we can access it and associate it with users. To do this, we will add one line to our database seed file (`data.sql`), which runs right after Spring sets up the database schema.

In `data.sql`, which we previously created inside of `src/main/resources`, add the following line:
```sql
MERGE INTO `role` VALUES (1,'USER');
```
Now, this role will be created automatically at startup.

The `UserService` uses a password encoder, which needs to be set up as well. For this, create a Bean, which can be injected into the constructor of our `UserService`. 

In the `configuration` package, create a new class called `WebMvcConfiguration`. It needs to look like this:
```java
@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {
  @Bean
  public BCryptPasswordEncoder passwordEncoder() {
    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    return bCryptPasswordEncoder;
  }
}
```

Speaking of creating beans (remember the @Bean annotation), let's create one more to allow access to security information in Thymeleaf templates. In the `configuration` package, create a new class called `ThymeleafConfiguration` and add the following:
```java
@Configuration
public class ThymeleafConfiguration {
    @Bean
    public SpringSecurityDialect springSecurityDialect(){
        return new SpringSecurityDialect();
    }
}
```

Remember to resolve all the necessary imports along the way.

As the last piece of authorization, add security configuration. This configuration will let Spring know how to access users and roles, as well as how to protect parts of the app. In the `configuration` package, create a class called `SecurityConfiguration`.

There is a lot going on in the class, and many of the details are more advanced than this lesson, but the most important piece at this time is the method to configure HTTP security. It sets up the security filter which allows certain pages/resources to be accessed without logging in (the login screen, signup screen, h2 console, stylesheets, etc), while restricting access to all other pages and redirecting the user to the login screen if they try to access them directly.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private BCryptPasswordEncoder bCryptPasswordEncoder;

  @Autowired
  private DataSource dataSource;

  @Value("${spring.queries.users-query}")
  private String usersQuery;

  @Value("${spring.queries.roles-query}")
  private String rolesQuery;

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.jdbcAuthentication()
        .usersByUsernameQuery(usersQuery)
        .authoritiesByUsernameQuery(rolesQuery)
        .dataSource(dataSource)
        .passwordEncoder(bCryptPasswordEncoder);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.authorizeRequests()
        .antMatchers("/console/**").permitAll()
        .antMatchers("/login").permitAll()
        .antMatchers("/signup").permitAll()
        .antMatchers("/custom.js").permitAll()
        .antMatchers("/custom.css").permitAll()
        .antMatchers().hasAuthority("USER").anyRequest()
        .authenticated().and().csrf().disable().formLogin()
        .loginPage("/login").failureUrl("/login?error=true")
        .defaultSuccessUrl("/tweets")
        .usernameParameter("username")
        .passwordParameter("password")
        .and().logout()
        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
        .logoutSuccessUrl("/login").and().exceptionHandling();
      
    http.headers().frameOptions().disable();
  }

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring()
       .antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
  }

}
```

As the final piece to the back end security configuration, create a controller to handle all requests that pertain to authorization (logging in and signing up).

Inside the `controller` package, create a new class called `AuthorizationController`. Give it the `@Controller` annotation.

Autowire the `UserService` and create a method to serve up the login page.
```java
@Autowired
private UserService userService;

@GetMapping(value="/login")
public String login(){
    return "login";
}
```

Add methods to serve up the signup page (GET), as well as handle form submissions when a user hits submit on the signup page (POST).
```java
@GetMapping(value="/signup")
public String signup(Model model){
  User user = new User();
  model.addAttribute("user", user);
  return "signup";
}

@PostMapping(value = "/signup")
public String createNewUser(@Valid User user, BindingResult bindingResult, Model model) {
  User userExists = userService.findByUsername(user.getUsername());
  if (userExists != null) {
    bindingResult.rejectValue("username", "error.user", "Username is already taken");
  }
  if (!bindingResult.hasErrors()) {
    userService.saveNewUser(user);
    model.addAttribute("success", "Sign up successful!");
    model.addAttribute("user", new User());
  }
  return "signup";
}
```

When a user tries to sign up, make sure that their username hasn't already been taken by another user. Validations will be added to the `User` class later. If there are no validation errors, create the user by passing the object into the `UserService`, where it handles password encoding, setting roles, and inserting the user object into the database.

---

### Authentication front end
Everything is set up for authentication in terms of controllers, models, services, repositories, and configuration. Now, it's time to create some Thymeleaf template pages so that we can actually see something in the browser!

In `src/main/resources/templates`, create `login.html`. At the top of our document, in the standard html tag reference in Thymeleaf functionality, including the extras for security. Also import bootstrap, fontawesome, and the empty custom.css.
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity4">

<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
  <title>Insert title here</title>
  <link rel="stylesheet" href="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/css/bootstrap.min.css" integrity="sha384-9aIt2nRpC12Uk9gS9baDl411NQApFmC26EwAOH8WgZl5MYYxFfc+NcPb1dKGj7Sk" crossorigin="anonymous">
  <link href="https://stackpath.bootstrapcdn.com/font-awesome/4.7.0/css/font-awesome.min.css" rel="stylesheet" integrity="sha384-wvfXpqpZZVQGK6TAh5PVlGOfQNHSoD2xbE+QkPxCAFlNEevoEH3Sl0sibVcOQVnN" crossorigin="anonymous">
  <link rel="stylesheet" type="text/css" href="/custom.css">
</head>
```

Just above the body closing tag add the script tags for jQuery, Popper.js, Bootstrap, and the empty custom.js file.
```html
<script src="https://code.jquery.com/jquery-3.5.1.slim.min.js"
  integrity="sha384-DfXdz2htPH0lsSSs5nCTpuj/zy4C+OGpamoFVy38MVBnE+IbbVYUew+OrCXaRkfj"
  crossorigin="anonymous"></script>
<script
  src="https://cdn.jsdelivr.net/npm/popper.js@1.16.0/dist/umd/popper.min.js"
  integrity="sha384-Q6E9RHvbIyZFJoft+2mJbHaEWldlvI9IOYy5n3zV9zzTtmI3UksdQRVvoxMfooAo"
  crossorigin="anonymous"></script>
<script
  src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.0/js/bootstrap.min.js"
  integrity="sha384-OgVRvuATP1z7JjHLkuOU7Xw704+h835Lr+6QL9UvYjZE3Ipu6Tp75j7Bh/kR0JKI"
  crossorigin="anonymous"></script>
<script type="text/javascript" src="/custom.js"></script>
```

Inside the body tag and container div, build a form that takes in a username and password, then makes a POST request to `/login`, which is handled in the `AuthorizationController`. Add code to display an error message in case the user types in bad credentials.
```html
<div class="container-fluid">
  <h2>Welcome!</h2> 
  <form th:action="@{/login}" method="post"><br/>
    <input type="text" id="username" name="username" th:placeholder="Username" class="form-control"/><br/>
    <input type="password" th:placeholder="Password" id="password" name="password" class="form-control"/><br/>
    <div align="center" th:if="${param.error}">
      <h4 style="color: red">Login unsuccessful.</h4>
    </div>
    <button class="btn btn-md btn-success btn-block" name="Submit" value="Login" type="Submit">Log in</button> 
  </form>      
</div>
```

Create the signup page, `signup.html`. Use the same header as on the login page. Inside the body and container div, build a form that receives the information for all of the fields needed to create a `User`. User the label tag for each field, as a place to show validation error messages for when that gets implemented later. For example, if a user leaves a field blank and hits sumbit, an error message will appear above that field.
```html
<div class="container-fluid">
<h2>Sign up</h2>
<form th:action="@{/signup}" th:object="${user}" method="post" autocomplete="off">
  <div class="form-group">
    <label th:if="${#fields.hasErrors('firstName')}" th:errors="*{firstName}" class="validation-message"></label>
    <input type="text" th:field="*{firstName}" placeholder="First Name" class="form-control" />
    <label th:if="${#fields.hasErrors('lastName')}" th:errors="*{lastName}" class="validation-message"></label>
    <input type="text" th:field="*{lastName}" placeholder="Last Name" class="form-control" />
    <label th:if="${#fields.hasErrors('username')}" th:errors="*{username}" class="validation-message"></label>
    <input type="text" th:field="*{username}" placeholder="Username" class="form-control" />
    <label th:if="${#fields.hasErrors('email')}" th:errors="*{email}" class="validation-message"></label>
    <input type="text" th:field="*{email}" placeholder="Email" class="form-control" />
    <label th:if="${#fields.hasErrors('password')}" th:errors="*{password}" class="validation-message"></label>
    <input type="password" th:field="*{password}" placeholder="Password" class="form-control" />
  </div>
  <button class="btn btn-sm btn-success btn-block" name="Submit" value="submit" type="submit" th:text="Signup"></button>
</form>
</div>
```
And just like that, we have two usable template pages! Users can visit the site and sign up for an account, as well as log in. Once they log in, though, there is nowhere for them to go yet.

---

### Tweet back end
Authentication is out of the way now, moving onto stuff that is more fun - Tweets! 
Create the full set of classes/interfaces:
1. `Tweet.java` in the `model` package
2. `TweetRepository.java` in the `repository` package (this one is an interface!)
3. `TweetService.java` in the `service` package
4. `TweetController.java` in the `controller` package

First, the `Tweet` class.
```java
@Entity
public class Tweet {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "tweet_id")
  private Long id;
    
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;
    
  private String message;
    
  @CreationTimestamp 
  private Date createdAt;
```
A tweet belongs to one user, and a user can have many tweets, so that's why the Many-To-One relationship is used. Each row in the tweet table will have a foreign key that points to a particular user. A tweet also has a message and a creation timestamp, which is automatically set. Use the source generators for the constructors, getters and setters, plus the toString method if not using Lombok.

Next, `TweetRepository` interface.
```java
@Repository
public interface TweetRepository extends CrudRepository<Tweet, Long> {
  public List<Tweet> findAllByOrderByCreatedAtDesc();
  public List<Tweet> findAllByUserOrderByCreatedAtDesc(User user);
  public List<Tweet> findAllByUserInOrderByCreatedAtDesc(List<User> users);
}
```
Three interesting method signatures here. One to find all tweets, one to find all tweets belonging to a particular user, and one to find all tweets belonging to anyone in a group of users. In each case, the tweet list returned is sorted based on the creation date in descending order (newest first).

Create `TweetService` class. Initially, keep it pretty simple and basically just use it as a passthrough from the controller to the repository. Later, it will need to be more complex with business logic methods regarding tweets. Notice that the `TweetRepository` is injected with the `@Autowired` annotation, which is what allows the service to call methods in the repository. `TweetService` will be the only class that directly interacts with `TweetRepository`, kind of like a doorman.
```java
@Service
public class TweetService {
  @Autowired
  private TweetRepository tweetRepository;

  public List<Tweet> findAll() {
    List<Tweet> tweets = tweetRepository.findAllByOrderByCreatedAtDesc();
    return tweets;
  }

  public List<Tweet> findAllByUser(User user) {
    List<Tweet> tweets = tweetRepository.findAllByUserOrderByCreatedAtDesc(user);
    return tweets;
  }

  public List<Tweet> findAllByUsers(List<User> users){
    List<Tweet> tweets = tweetRepository.findAllByUserInOrderByCreatedAtDesc(users);
    return tweets;
  }

  public void save(Tweet tweet) {
    tweetRepository.save(tweet);
  }
}
```

Create the `TweetController` class. It needs both the `UserService` and `TweetService` to be injected (`@Autowired`).

```java
@Controller
public class TweetController {
  @Autowired
  private UserService userService;

  @Autowired
  private TweetService tweetService;

```

Start out with three methods. The first will get all tweets, and will accept a GET request to either `/` or `/tweets`. This will direct the user to `feed.html`, which will be the home page for app and is the page where the user automatically gets sent after successfully logging in.
```java
@GetMapping(value= {"/tweets", "/"})
public String getFeed(Model model){
  List<Tweet> tweets = tweetService.findAll();
  model.addAttribute("tweetList", tweets);
  return "feed";
}
```

The second method will serve up the 'new tweet' page, `newTweet.html`, which will contain a simple form to get input for a new tweet. Notice that it passes an empty tweet object into the Model, which is then filled in by the form.
```java
@GetMapping(value = "/tweets/new")
public String getTweetForm(Model model) {
  model.addAttribute("tweet", new Tweet());
  return "newTweet";
}
```

The third method will handle the form submission from the 'new tweet' page. When the tweet is received by the form, the only field that is filled in by user input is the message. So, this method gets the logged in user to associate them with the tweet. There is also a check for validation errors (`@Valid` annotation); validations will be added to the object classes later. If there are no errors, the tweet gets saved to the database through the `TweetService`. The user is routed back to the same `newTweet.html` page with a flash message indicating a successful tweet creation, and giving the user the option to create another tweet.
```java
@PostMapping(value = "/tweets/new")
public String submitTweetForm(@Valid Tweet tweet, BindingResult bindingResult, Model model) {
  User user = userService.getLoggedInUser();
  if (!bindingResult.hasErrors()) {
      tweet.setUser(user);
      tweetService.save(tweet);
      model.addAttribute("successMessage", "Tweet successfully created!");
      model.addAttribute("tweet", new Tweet());
  }
  return "newTweet";
}
```

---

### Tweet front end

In the `TweetController`, the user is routed to two html pages. Create those now.

First is the 'new tweet' form page. In `src/main/resources/templates`, create `newTweet.html`. Use the same head content as with the other pages. Inside the container div, create a form that will POST a tweet object to `/tweets/new`. Use some bootstrap styling as well, so that it looks nice from the start.

This form will contain just two elements. One that is a text area where the tweet message is entered, and a submit button. In the text area, set placeholder text to mimic the actual functionality of Twitter, and prompt the user. Also include the ability to display validation messages similar to the user sign up form. Notice that the form uses a **textarea** tag instead of a normal text field so that there is more room to compose the tweet.

Lastly, after the form, display a success message only if a tweet was successfully created. This message is added to the Model in the controller. The contents of the div are only displayed if the Model contains a value for `${successMessage}`. This allows the page to give instant feedback to the user when they submit a `Tweet`.

```html
<div class="jumbotron col-md-8 col-md-offset-2">
  <h2>New Tweet</h2>
  <form th:action="@{/tweets}" th:object="${tweet}" method="post" autocomplete="off" >
    <div class="form-group">
      <label th:if="${#fields.hasErrors('message')}" th:errors="*{message}" class="validation-message"></label> 
      <textarea rows="5" th:field="*{message}" placeholder="What's happening?" class="form-control"></textarea>
    </div>
    <div class="form-group">
      <button class="btn btn-md btn-success btn-block" name="Submit" value="Submit" type="Submit">Tweet it!</button>
    </div>
  </form>
  <div th:if="${successMessage}">
    <h3><span class="text-success" th:utext="${successMessage}"></span></h3>
  </div> 
</div>
```

The next page we need to create is the Twitter feed, `feed.html`. Initially, this page will just show all of the tweets that exist. More functionality will be added later. In the controller, a list of tweets was send to the Model for use in the Thymeleaf template. Generate html for to iterate through each tweet in the list with the Thymeleaf version of an enhanced for-loop, `th:each="tweet:${tweetList}"`. Everything inside the div will appear on the page once for each tweet.
```html
<body>
  <div class="container-fluid">
    <h2>Tweets</h2>
    <div th:each="tweet:${tweetList}">
      <h5>@<span th:text="${tweet.user.username}"></span><small><span th:text="${#dates.format(tweet.createdAt, 'M/d/yy')}"></span></small></h5>
      <p th:utext="${tweet.message}"></p>
    </div>
  </div>
</body>
```
For each tweet, display the username, message, and creation date. Use built-in Thymeleaf functionality to format the date.

To see this page, navigate to `/tweets/new` (manually) in the browser and create a few tweets, go to `/tweets` to see all the Tweets!

---

### Validation

In some of the code written so far, the foundation for using validation has been set up. Essentially, anytime a user submits a form, we want to make sure that what they have entered is acceptable before actually going through with the submission and storing it in the database. Add validation annotations to the models. 

`User`:
```java
@Email(message = "Please provide a valid email")
@NotEmpty(message = "Please provide an email")
private String email;
    
@Length(min = 3, message = "A username must have at least 3 characters")
@Length(max = 15, message = "A username cannot have more than 15 characters")
@Pattern(regexp="[^\\s]", message="A username cannot contain spaces")
private String username;
    
@Length(min = 5, message = "A password must have at least 5 characters")
private String password;
    
@NotEmpty(message = "Please provide your first name")
private String firstName;
    
@NotEmpty(message = "Please provide your last name")
private String lastName;
```

For `Tweet`, add validations on one field.
```java
@NotEmpty(message = "A Tweet cannot be empty")
@Length(max = 280, message = "A Tweet cannot have more than 280 characters")
private String message;
```

Now, if you try to sign up or post a tweet and any of the validation conditions are not met, the forms will not submit and the validation messages will be displayed.