package org.upgrad.upstac.testrequests.lab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.config.security.UserLoggedInService;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.TestRequestQueryService;
import org.upgrad.upstac.testrequests.TestRequestUpdateService;
import org.upgrad.upstac.testrequests.flow.TestRequestFlowService;
import org.upgrad.upstac.users.User;

import javax.validation.ConstraintViolationException;
import java.util.List;

import static org.upgrad.upstac.exception.UpgradResponseStatusException.asBadRequest;
import static org.upgrad.upstac.exception.UpgradResponseStatusException.asConstraintViolation;

@RestController
@RequestMapping("/api/labrequests")
public class LabRequestController {

    Logger log = LoggerFactory.getLogger(LabRequestController.class);

    @Autowired
    private TestRequestUpdateService testRequestUpdateService;

    @Autowired
    private TestRequestQueryService testRequestQueryService;

    @Autowired
    private TestRequestFlowService testRequestFlowService;

    @Autowired
    private UserLoggedInService userLoggedInService;

    @GetMapping("/to-be-tested")
    @PreAuthorize("hasAnyRole('TESTER')")
    public List<TestRequest> getForTests() {

        // Retrieving and returning list of all test requests in 'Initiated' status
        return testRequestQueryService.findBy(RequestStatus.INITIATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TESTER')")
    public List<TestRequest> getForTester()  {

        // An object of User class to store the current logged in tester
        User tester = userLoggedInService.getLoggedInUser();

        // Retrieving and returning list of test requests assigned to the current tester
        return testRequestQueryService.findByTester(tester);
    }

    @PreAuthorize("hasAnyRole('TESTER')")
    @PutMapping("/assign/{id}")
    public TestRequest assignForLabTest(@PathVariable Long id) {

        // An object of User class to store the current logged in tester
        User tester = userLoggedInService.getLoggedInUser();

        // Assigning the test request to the current tester for lab test
        return testRequestUpdateService.assignForLabTest(id, tester);
    }

    @PreAuthorize("hasAnyRole('TESTER')")
    @PutMapping("/update/{id}")
    public TestRequest updateLabTest(@PathVariable Long id, @RequestBody CreateLabResult createLabResult) {

        try {
            // An object of User class to store the current logged in tester
            User tester = userLoggedInService.getLoggedInUser();

            // Update the test request with lab test results by the tester
            return testRequestUpdateService.updateLabTest(id, createLabResult, tester);

        } catch (ConstraintViolationException e) {
            throw asConstraintViolation(e);

        } catch (AppException e) {
            throw asBadRequest(e.getMessage());
        }
    }
}
