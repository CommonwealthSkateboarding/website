package controllers;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import models.skatepark.Membership;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import java.util.List;

/**
 * Created by cdelargy on 2/7/15.
 *
 * Service for returning autocomplete data in JSON format
 */
@Restrict({@Group("ADMIN")})
@Security.Authenticated(Secured.class)
public class Autocomplete extends Controller {

    public static Result searchMembersByName(String partial) {
        List<Membership> results = Membership.find.where().like("name", "%" + partial + "%")
                .orderBy(Admin.RECENT_VISIT_ORDER).setMaxRows(10).findList();
        return ok(Json.toJson(results));
    }
}
