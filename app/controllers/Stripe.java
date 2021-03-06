package controllers;

import com.stripe.exception.*;
import com.stripe.model.Charge;
import play.Logger;
import play.Play;
import play.mvc.Controller;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cdelargy on 1/19/15.
 */
public class Stripe extends Controller {

    private static String STRIPE_API_KEY = Play.application().configuration().getString("stripe.apikey");
    public static String STRIPE_PUBLIC_KEY = Play.application().configuration().getString("stripe.publickey");

    public static Charge chargeStripe(Double amount, String stripeToken, String description) throws CardException, APIException, AuthenticationException, InvalidRequestException, APIConnectionException {

        com.stripe.Stripe.apiKey = STRIPE_API_KEY;

        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", (new Double(amount * 100)).intValue());
        chargeParams.put("currency", "usd");
        chargeParams.put("card", stripeToken);
        chargeParams.put("description", description);

        Charge charge = null;

        charge = Charge.create(chargeParams);
        return charge;
    }
}
