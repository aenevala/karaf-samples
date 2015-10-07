package com.github.aenevala.karaf.restds;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.apache.shiro.subject.Subject;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;

/**
 * Created by nevalaa on 29.9.2015.
 */
@Path("/")
public class PersonService {


    @GET
    @RequiresRoles("admin")
    public String sayHello() {
        return "Hello World";
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/read/{username}")
    @RequiresPermissions("test:read")
    public Person read(@PathParam("username") String username) {
        Subject subject = SecurityUtils.getSubject();
        return new Person(username);
    }
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Path("/write")
    @RequiresPermissions("test:write")
    public Response write(Person person) {
        return Response.accepted().entity(person).build();
    }


}
