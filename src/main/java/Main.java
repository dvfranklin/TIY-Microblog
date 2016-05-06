import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Scanner;

import static spark.Spark.halt;

public class Main {

    static HashMap<String, User> users = new HashMap<>();

    public static void main(String[] args){

        Gson gson = new GsonBuilder().create();
        File jsonFile = new File("users.json");


        //users.put("franks", new User("franks", "password"));

        Spark.get(
                "/",
                (request,response) -> {
                    HashMap m = new HashMap();
                    User user = new User();

                    if(!request.session().attributes().contains("user")){
                        // if not logged in, or gave bad login info
                        m.put("missingInfo", request.queryParams("missingInfo"));
                        m.put("badInfo", request.queryParams("badInfo"));


                        return new ModelAndView(m, "login.mustache");
                    } else {


                        // new user in session
                        user = request.session().attribute("user");
                        m.put("user", user);

                        // show the messages page
                        return new ModelAndView(m, "messages.mustache");
                    }
                },
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/login",
                (request, response) -> {
                    // get username & password from login form
                    String userName = request.queryParams("userName");
                    String password = request.queryParams("password");

                    // if either field is null, return an error
                    if(userName.equals("") || password.equals("")){
                        response.redirect("/?missingInfo=true");
                        halt();
                    }


                    // import User objects from json, convert to users Hashmap
                    Scanner scanner = new Scanner(jsonFile);
                    scanner.useDelimiter("\\Z");
                    if(scanner.hasNext()){
                        String contents = scanner.next();
                        users = gson.fromJson(contents, new TypeToken<HashMap<String, User>>(){}.getType());
                    }

                    // get the user from hashmap
                    User user = users.get(userName);

                    // if user doesn't exist (not in hashmap), or password is incorrect, return error
                    if((user == null) || !user.getPassword().equals(password)){
                        response.redirect("/?badInfo=true");
                        halt();
                    }

                    // add user to the session and redirect to webroot
                    request.session().attribute("user", user);
                    response.redirect("/");
                    halt();

                    return "";
                }
        );

        Spark.get(
                "/signup",
                (request, response) -> {
                    // displays the signup page
                    return new ModelAndView(null, "signup.mustache");
                },
                new MustacheTemplateEngine()
        );

        Spark.post(
                "/create-user",
                (request, response) -> {

                    // create new user with user & password provided
                    User user = new User(request.queryParams("userName"), request.queryParams("password"));

                    // add new user to the Hashmap
                    users.put(user.getUsername(), user);

                    // write the updated Hashmap to json
                    String jsonUsers = gson.toJson(users);
                    FileWriter fw = new FileWriter(jsonFile);
                    fw.write(jsonUsers);
                    fw.close();

                    // add user to session and redirect to webroot
                    request.session().attribute("user", user);
                    response.redirect("/");
                    halt();
                    return "";
                }
        );

        Spark.post(
                "/create-message",
                (request, response) -> {

                    // message as String, inserted in ArrayList for user
                    String text = request.queryParams("message");
                    User user = request.session().attribute("user");
                    Message message = new Message(text);
                    user.messages.add(message);

                    // save Users Hashmap in json file
                    String jsonUsers = gson.toJson(users);
                    FileWriter fw = new FileWriter(jsonFile);
                    fw.write(jsonUsers);
                    fw.close();

                    // redirect to webroot
                    response.redirect("/");
                    halt();


                    return "";
                }
        );

        Spark.post(
                "/delete",
                (request, response) -> {

                    // get the requested message from User's ArrayList of messages (-1 to revert to zero-based index)
                    // delete the requested message
                    User user = request.session().attribute("user");
                    int deleteId = Integer.valueOf(request.queryParams("messageId")) - 1;
                    user.messages.remove(deleteId);

                    // redirect to webroot
                    response.redirect("/");
                    halt();

                    return "";
                }
        );

        Spark.post(
                "/edit",
                (request, response) -> {

                    // get the requested message from User's ArrayList of messages (-1 to revert to zero-based index)
                    // deletes the requested message and replaces it with the new messageText from form
                    User user = request.session().attribute("user");
                    int editId = Integer.valueOf(request.queryParams("messageId")) - 1;
                    Message editMessage = new Message(request.queryParams("messageText"));

                    user.messages.remove(editId);
                    user.messages.add(editId, editMessage);

                    // redirect to webroot
                    response.redirect("/");
                    halt();

                    return "";
                }
        );

        Spark.get(
                "/logout",
                (request, response) -> {
                    // kill session, return to root for login
                    request.session().invalidate();
                    response.redirect("/");
                    halt();
                    return null;
                },
                new MustacheTemplateEngine()
        );


    }
}
