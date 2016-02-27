package rest;

import main.AccountService;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * Created by e.shubin on 25.02.2016.
 */
@Singleton
@Path("/user")
public class Users {
    private AccountService accountService;

    public Users(AccountService accountService) {
        this.accountService = accountService;
    }
    // TODO: не нужен.
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUsers() {
        final Collection<UserProfile> allUsers = accountService.getAllUsers();
        return Response.status(Response.Status.OK).entity(allUsers.toArray(new UserProfile[allUsers.size()])).build();
    }

    // TODO: создание пользователя.
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(UserProfile user, @Context HttpHeaders headers){
        if(accountService.addUser(user.getLogin(), user)){
            final String result = "id:" + user.getId();
            final Id id = new Id(user.getId());
            return Response.status(Response.Status.OK).entity(id).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @GET
    @Path("{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByName(@PathParam("name") String name) {
        final UserProfile user = accountService.getUser(name);
        if(user == null){
            return Response.status(Response.Status.FORBIDDEN).build();
        }else {
            return Response.status(Response.Status.OK).entity(user).build();
        }
    }
}
