package mastercardsend.api.disbursements.service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercard.api.core.ApiConfig;
import com.mastercard.api.core.exception.ApiException;
import com.mastercard.api.core.model.RequestMap;
import com.mastercard.api.core.security.oauth.OAuthAuthentication;
import com.mastercard.api.disbursements.AccountInfo;
import com.mastercard.api.disbursements.Disbursement;
import mastercardsend.api.disbursements.model.MastercardSendDisbursement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.UUID;

/**
 * Service communicating with Mastercard Send Disbursements API
 */
@Service
public class MastercardService {
    /** Correct String input for the recipient account URI scheme. For PAN accounts. **/
    public static final String PAN = "pan";
    /** Correct String input for the recipient account URI scheme. For temporary account tokens. **/
    public static final String ACCOUNT_TOKEN = "acct-token";
    /** Correct String input for the recipient account URI scheme. For IBAN. **/
    public static final String IBAN = "iban";
    /** Correct String input for the payment type. For business disbursements. **/
    public static final String BUSINESS_DISBURSEMENT = "BDB";
    /** Correct String input for the payment type. For government disbursements. **/
    public static final String GOVERNMENT_DISBURSEMENT = "GDB";
    /** Correct String input for the payment type. For acquirer  merchant settlements. **/
    public static final String ACQUIRER_MERCHANT_SETTLEMENT = "AMS";
    /** Correct String input for the payment type. For credit card bill payments. **/
    public static final String CREDIT_CARD_BILLPAYMENT = "CBP";
    // Most recent error message
    private static String error;
    // Most recent request
    private static String request;

    /**
     * Initiate SDK authentication.
     * @param env Environment from which property details are obtained
     * @throws IOException
     */
    @Autowired
    public MastercardService(Environment env) throws IOException {
        String consumerKey = env.getProperty("consumerKey");
        String keyAlias = env.getProperty("keyAlias");
        String keyPassword = env.getProperty("keyPassword");
        InputStream is = new FileInputStream(env.getProperty("p12PrivateKey"));

        ApiConfig.setAuthentication(new OAuthAuthentication(consumerKey, is, keyAlias, keyPassword));
        ApiConfig.setEnvironment(com.mastercard.api.core.model.Environment.SANDBOX_STATIC);
        ApiConfig.setDebug(true);

    }

    /**
     * Initiate the Disbursements API call to get the recipient account information.
     * Specifically checks whether the recipient is able to receive funds.
     * @param disbursement Disbursement Spring model containing the disbursement details
     * @return True if the the recipient can receive funds, false if not
     */
    public boolean isEligible(MastercardSendDisbursement disbursement) {
        try {
            RequestMap map = new RequestMap();
            map.set("partnerId", disbursement.getPartnerId());
            map.set("account_info.account_uri", disbursement.getRecipientAccountUri());
            map.set("account_info.payment_type", disbursement.getPaymentType());
            map.set("account_info.amount", disbursement.getAmount());
            map.set("account_info.currency", disbursement.getCurrency());

            AccountInfo accountInfo = new AccountInfo(map).read();
            boolean eligible = Boolean.parseBoolean((String) accountInfo.get("account_info.receiving_eligibility.eligible"));
            if (!eligible) {
                error = (String) accountInfo.get("account_info.receiving_eligibility.reason_description");
            }
            return eligible;
        } catch (ApiException e) {
            error = "HttpStatus: " + e.getHttpStatus() +
                    "\nMessage: " + e.getMessage() +
                    "\nReason Code: " + e.getReasonCode() +
                    "\nSource: " + e.getSource() ;
            printErrors(e);
            return false;
        }
    }

    /**
     * Initiate the Disbursements API call to push a disbursement.
     * @param disbursement Disbursement Spring model containing the disbursement details
     * @return Response containing all the disbursement details if the disbursement was successfully pushed, null if failed
     */
    public static Disbursement create(MastercardSendDisbursement disbursement) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            RequestMap map = new RequestMap();
            map.set("partnerId", disbursement.getPartnerId());
            map.set("payment_disbursement.disbursement_reference", UUID.randomUUID().toString());
            map.set("payment_disbursement.payment_type", disbursement.getPaymentType());
            map.set("payment_disbursement.amount", disbursement.getAmount());
            map.set("payment_disbursement.currency", disbursement.getCurrency());
            map.set("payment_disbursement.recipient_account_uri", disbursement.getRecipientAccountUri());
            map.set("payment_disbursement.recipient.first_name", disbursement.getFirstName());
            map.set("payment_disbursement.recipient.last_name", disbursement.getLastName());
            map.set("payment_disbursement.recipient.address.line1", disbursement.getLine1());
            map.set("payment_disbursement.recipient.address.city", disbursement.getCity());
            map.set("payment_disbursement.recipient.address.postal_code", disbursement.getPostalCode());
            if (disbursement.getNameOnAccount() != null) { // for when funds are disbursed into an account and not a card
                map.set("payment_disbursement.recipient.name_on_account", disbursement.getNameOnAccount());
            }

            request = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(map); // to display on page

            Disbursement response = Disbursement.create(map); // API call

            String responseString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);

            System.out.println("Request:\n" + request);
            System.out.println("\nResponse:\n" + responseString);
            System.out.println("Disbursement success!");

            return response;
        } catch (JsonProcessingException e) {
            System.err.println("Could not convert request to JSON.");
            return null;
        }   catch (ApiException e) {
            error = "HttpStatus: " + e.getHttpStatus() +
                    "\nMessage: " + e.getMessage() +
                    "\nReason Code : " + e.getReasonCode() +
                    "\nSource: " + e.getSource() ;
            printErrors(e);
            return null;
        }
    }

    /**
     * Retrieve the transaction details of a disbursement with its ID.
     * Not currently used in this reference application.
     * @param partnerId the partner ID associated with the disbursement
     * @param id the ID of the disbursement to retrieve
     * @return
     */
    public Disbursement readId(String partnerId, String id) {
        try {
            RequestMap map = new RequestMap();
            map.set("partnerId", partnerId);
            map.set("disbursementId", id);
            return Disbursement.readByID(id, map);
        } catch (ApiException e) {
            error = e.getMessage();
            printErrors(e);
        }
        return null;
    }

    /**
     * Print errors to the console.
     * @param e
     */
    private static void printErrors(ApiException e) {
        System.err.println("HttpStatus: " + e.getHttpStatus());
        System.err.println("Message: " + e.getMessage());
        System.err.println("ReasonCode: " + e.getReasonCode());
        System.err.println("Source: " + e.getSource());
        System.err.println("Request:\n" + request);
    }

    /**
     * Return the last received error message.
     * @return The last received error message
     */
    public String getError() {
        return error;
    }

    /**
     * Return the last request submitted.
     * @return The last pushed request submitted
     */
    public String getRequest() {
        return request;
    }
}
