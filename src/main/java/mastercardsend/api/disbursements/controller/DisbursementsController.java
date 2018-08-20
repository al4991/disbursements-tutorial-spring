package mastercardsend.api.disbursements.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercard.api.disbursements.Disbursement;
import mastercardsend.api.disbursements.model.MastercardSendDisbursement;
import mastercardsend.api.disbursements.service.MastercardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class DisbursementsController {
    @Autowired
    private MastercardService service;

    // Partner ID obtained and injected from application.properties
    @Value("${partnerId}")
    private String partnerId;

    /**
     * Index page displays a form to disburse funds.
     * Visit localhost:8080 to view.
     * @param model Spring model for adding attributes
     * @return index page
     */
    @GetMapping("/")
    public String index(Model model) {
        MastercardSendDisbursement disbursement = new MastercardSendDisbursement();
        model.addAttribute("disbursement", disbursement);
        model.addAttribute("uriSchemes", disbursement.getAllURISchemes());
        return "index";
    }

    /**
     * Submits the form to check for recipient receiving eligibility.
     * If eligible, then the funds will be disbursed.
     * @param disbursement Disbursement model bound to the form
     * @param redirectAttrs for flash notifications when redirecting
     * @return
     */
    @PostMapping("/submitForm")
    public String submitForm(@ModelAttribute("disbursement") MastercardSendDisbursement disbursement,
                             RedirectAttributes redirectAttrs) {
        disbursement.setPartnerId(partnerId);
        disbursement.setRecipientAccountUri();

        Disbursement response = service.create(disbursement);

        try {
            if (response != null) {
                redirectAttrs.addFlashAttribute("request", service.getRequest());
                redirectAttrs.addFlashAttribute("response", new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(response));
                redirectAttrs.addFlashAttribute("success", "Disbursement for " + disbursement.getFirstName() + " " + disbursement.getLastName() + " was successfully created!");
                return "redirect:/";
            } else {
                redirectAttrs.addFlashAttribute("error", "Failed to create disbursement. " + service.getError());
                return "redirect:/";
            }
        } catch (JsonProcessingException e) {
            System.err.println("Unable to convert response to JSON String.");
            return null;
        }
    }

    /**
     * Disburse funds. Used for testing.
     * @param disbursementRequest Disbursement model containing the disbursement details
     * @return 200 status code if the disbursement was successful, 400 status code if unsuccessful
     */
    @PostMapping(value = "/createDisbursement")
    public ResponseEntity createDisbursement(@RequestBody MastercardSendDisbursement disbursementRequest) {
        if (service.isEligible(disbursementRequest)) {
            Disbursement response = service.create(disbursementRequest);
            if (response != null) {
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.badRequest().body(null);
        }
        return ResponseEntity.badRequest().body(null);
    }

    /**
     * Retrieves the transaction details of a disbursement by its ID.
     * Not currently used in this reference application.
     * @param id the disbursement ID
     * @return Details of transaction
     */
    @GetMapping("/response/{id}")
    public ResponseEntity getResponse(@PathVariable String id) {
        return ResponseEntity.ok(service.readId(partnerId, id));
    }
}
