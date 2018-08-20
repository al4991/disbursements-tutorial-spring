package mastercardsend.api.disbursements.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import mastercardsend.api.disbursements.service.MastercardService;

import java.util.LinkedList;
import java.util.List;

/**
 * Request Spring model that contains required information for making Disbursements API calls.
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MastercardSendDisbursement {
    private String partnerId;
    private String disbursementReference;
    private String paymentType = MastercardService.BUSINESS_DISBURSEMENT; // as defined during onboarding process
    private String amount;
    private String currency;
    private String uriScheme;
    private String uriIdentifier;
    private String uriExpYear;
    private String uriExpMonth;
    private String uriCvc;
    private String recipientAccountUri;
    private String firstName;
    private String lastName;
    private String line1;
    private String city;
    private String postalCode;
    private String nameOnAccount;

    /**
     * Get all the currently valid recipient URI schemes.
     * @return the recipient URI schemes
     */
    public static List<String> getAllURISchemes() {
        List<String> uriSchemes = new LinkedList<String>();
        uriSchemes.add("PAN");
        uriSchemes.add("Account Token");
        uriSchemes.add("IBAN");
        return uriSchemes;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getDisbursementReference() {
        return disbursementReference;
    }

    public void setDisbursementReference(String disbursementReference) {
        this.disbursementReference = disbursementReference;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getUriScheme() {
        return uriScheme;
    }

    public void setUriScheme(String uriScheme) {
        this.uriScheme = uriScheme;
    }

    public String getUriIdentifier() {
        return uriIdentifier;
    }

    public void setUriIdentifier(String uriIdentifier) {
        this.uriIdentifier = uriIdentifier;
    }

    public String getUriExpYear() {
        return uriExpYear;
    }

    public void setUriExpYear(String uriExpYear) {
        this.uriExpYear = uriExpYear;
    }

    public String getUriExpMonth() {
        return uriExpMonth;
    }

    public void setUriExpMonth(String uriExpMonth) {
        this.uriExpMonth = uriExpMonth;
    }

    public String getUriCvc() {
        return uriCvc;
    }

    public void setUriCvc(String uriCvc) {
        this.uriCvc = uriCvc;
    }

    public String getRecipientAccountUri() {
        return recipientAccountUri;
    }

    /**
     * Sets the recipient account URI in the correct format for the API call.
     */
    public void setRecipientAccountUri() {
        String scheme = getFormattedUriScheme();
        if (scheme == null) {
            System.err.println("Recipient account URI could not be set because of invalid URI scheme.");
            return;
        } else if (scheme.equals(MastercardService.PAN)) {
            recipientAccountUri = scheme + ":" + uriIdentifier + ";exp=" + uriExpYear + "-" + uriExpMonth;
            // add CVC info if given
            if (uriCvc != null) {
                recipientAccountUri = recipientAccountUri + ";cvc=" + uriCvc;
            }
        } else {
            recipientAccountUri = scheme + ":" + uriIdentifier;
        }
    }

    public void setRecipientAccountUri(String recipientAccountUri) {
        this.recipientAccountUri = recipientAccountUri;
    }

    /**
     * Returns the correct corresponding value of the URI scheme for making the Disbursement API call.
     * @return The corresponding value to include in the request
     */
    private String getFormattedUriScheme() {
        switch (uriScheme.toLowerCase()) {
            case "pan":
                return MastercardService.PAN;
            case "account token":
                return MastercardService.ACCOUNT_TOKEN;
            case "iban":
                return MastercardService.IBAN;
            default:
                break;
        }
        System.err.println("Invalid URI Scheme.");
        return null;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getNameOnAccount() {
        return nameOnAccount;
    }

    public void setNameOnAccount(String nameOnAccount) {
        this.nameOnAccount = nameOnAccount;
    }
}
