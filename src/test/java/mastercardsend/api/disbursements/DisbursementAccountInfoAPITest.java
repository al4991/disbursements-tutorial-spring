package mastercardsend.api.disbursements;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mastercard.api.disbursements.Disbursement;
import mastercardsend.api.disbursements.model.MastercardSendDisbursement;
import mastercardsend.api.disbursements.service.MastercardService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.io.IOException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.util.ResourceUtils.getFile;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@PropertySource("classpath:application.properties")
public class DisbursementAccountInfoAPITest {
    public static final String DISBURSEMENT_JSON_FILE = "./src/test/resources/MastercardSendDisbursement.json";
    public static final String DISBURSEMENT_JSON_FILE_FOR_FORM = "./src/test/resources/MastercardSendDisbursementForm.json";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Value("${partnerId}")
    private String partnerId;

    @Test
    public void testCreateDisbursementMissingPartnerId() throws Exception {
        MastercardSendDisbursement disbursement = getMastercardSendDisbursement(DISBURSEMENT_JSON_FILE);
        mvc.perform(post("/createDisbursement")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(disbursement)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateDisbursementIneligibleRecipientAccount() throws Exception {
        MastercardSendDisbursement disbursement = getMastercardSendDisbursement(DISBURSEMENT_JSON_FILE);
        disbursement.setPartnerId(partnerId);
        disbursement.setRecipientAccountUri("pan:5432123456789012;exp=2099-02;cvc=123");
        mvc.perform(post("/createDisbursement")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(disbursement)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateDisbursementSuccessWithForm() throws Exception {
        MastercardSendDisbursement disbursement = getMastercardSendDisbursement(DISBURSEMENT_JSON_FILE_FOR_FORM);
        disbursement.setPartnerId(partnerId);
        mvc.perform(post("/submitForm").flashAttr("disbursement", disbursement)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(MockMvcResultMatchers.flash().attribute("success", "Disbursement for Jane Smith was successfully created!"))
                .andExpect(redirectedUrl("/"));
    }

    @Test
    public void testCreateDisbursementSuccess() throws Exception {
        MastercardSendDisbursement disbursement = getMastercardSendDisbursement(DISBURSEMENT_JSON_FILE);
        disbursement.setPartnerId(partnerId);
        mvc.perform(post("/createDisbursement")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(mapper.writeValueAsString(disbursement)))
                .andExpect(status().isOk());
    }

    @Test
    public void testReadByIdSuccess() throws Exception {
        MastercardSendDisbursement disbursement = getMastercardSendDisbursement(DISBURSEMENT_JSON_FILE);
        disbursement.setPartnerId(partnerId);
        Disbursement response = MastercardService.create(disbursement);
        mvc.perform(get("/response/{id}", response.get("disbursement.id").toString()))
                .andExpect(status().isOk());
    }

    private MastercardSendDisbursement getMastercardSendDisbursement(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(getFile(filePath), MastercardSendDisbursement.class);
    }

}